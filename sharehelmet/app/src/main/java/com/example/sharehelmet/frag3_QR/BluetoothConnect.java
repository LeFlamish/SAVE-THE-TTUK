package com.example.sharehelmet.frag3_QR;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnect {
    private static final int REQUEST_BLUETOOTH_SCAN = 2;

    public static void initializeBluetooth(BorrowingFragment3 fragment, Context context, Activity activity) {
        Log.d("Bluetooth", "Initializing Bluetooth...");
        if (!fragment.getBluetoothAdapter().isEnabled()) {
            BluetoothAdapter bluetoothAdapter = fragment.getBluetoothAdapter();
            bluetoothAdapter.enable();
            fragment.setBluetoothAdapter(bluetoothAdapter);
        }
        connectToHelmetDevice(fragment, context, activity);
    }

    public static void connectToHelmetDevice(BorrowingFragment3 fragment, Context context, Activity activity) {
        if (fragment.getBluetoothAdapter() != null) {
            Log.d("Bluetooth", "Starting Bluetooth device discovery...");
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                BluetoothAdapter bluetoothAdapter = fragment.getBluetoothAdapter();
                bluetoothAdapter.startDiscovery();
                fragment.setBluetoothAdapter(bluetoothAdapter);
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_BLUETOOTH_SCAN);
            }
        }
    }

    public static void reconnectBluetooth(BorrowingFragment3 fragment, Context context, Activity activity) {
        if (fragment.getBluetoothAdapter() != null && fragment.getHelmetId() != null) {
            /*Log.d("Bluetooth", "Attempting to reconnect to Bluetooth device...");*/
            connectToHelmetDevice(fragment, context, activity);
            /*fragment.getBluetoothAdapter().startDiscovery();
            Handler reconnectionHandler = new Handler(Looper.getMainLooper());
            reconnectionHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!fragment.isBluetoothConnected()) {
                        Log.e("Bluetooth", "Failed to reconnect within 1 minute. Marking helmet as lost.");
                        markHelmetAsLost(fragment,context);
                    }
                }
            }, 60000);  // 1분 동안 재연결 시도*/

        }
    }
    /*public static void markHelmetAsLost(BorrowingFragment3 fragment, Context context) {
        // Firebase에서 헬멧 상태를 'lost'로 설정
        DatabaseReference db;
        db = FirebaseDatabase.getInstance().getReference();
        db.child("helmet").child(fragment.getHelmetId()).child("lost").setValue(true);
        Toast.makeText(context, "Failed to reconnect. Helmet marked as lost.", Toast.LENGTH_LONG).show();
    }*/
    public static void connectToDevice(BluetoothDevice device, BorrowingFragment3 fragment, Context context, Activity activity, UUID MY_UUID) throws IOException {
        fragment.setBluetoothSocket(device.createRfcommSocketToServiceRecord(MY_UUID));
        try {
            BluetoothSocket bluetoothSocket=fragment.getBluetoothSocket();
            bluetoothSocket.connect();
            fragment.setBluetoothSocket(bluetoothSocket);
            fragment.setInputStream(bluetoothSocket.getInputStream());
            fragment.setOutputStream(bluetoothSocket.getOutputStream());
            Log.d("Bluetooth", "Connected to " + device.getName());

            // 데이터 수신 시작
            listenForData(fragment,context,activity);
        } catch (IOException e) {
            Log.e("Bluetooth", "Error connecting to device", e);
            // 소켓을 닫고 재연결 시도
            try {
                BluetoothSocket bluetoothSocket=fragment.getBluetoothSocket();
                bluetoothSocket.close();
                fragment.setBluetoothSocket(bluetoothSocket);
            } catch (IOException closeException) {
                Log.e("Bluetooth", "Error closing Bluetooth socket", closeException);
            }
        }
    }
    public static void listenForData(BorrowingFragment3 fragment, Context context, Activity activity) {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    if (fragment.getInputStream() == null) {
                        Log.e("Bluetooth", "InputStream is null. Exiting data listener.");
                        break;
                    }

                    bytes = fragment.getInputStream().read(buffer);
                    if (bytes == -1) {
                        throw new IOException("InputStream read returned -1, socket might be closed");
                    }

                    String receivedData = new String(buffer, 0, bytes).trim();
                    Log.d("Bluetooth", "Data received: " + receivedData);

                    // 데이터 파싱
                    String[] parts = receivedData.split(", ");
                    if (parts.length == 2) {
                        String lightPart = parts[0];
                        String shockPart = parts[1];

                        // Light와 Shock 값 추출
                        String lightValue = lightPart.split(": ")[1];
                        String shockValue = shockPart.split(": ")[1];

                        // Firebase에 저장
                        saveDataToFirebase(fragment,lightValue, shockValue);
                    } else {
                        Log.e("Bluetooth", "Invalid data format received: " + receivedData);
                    }

                } catch (IOException e) {
                    Log.e("Bluetooth", "Error reading data", e);
                    // 데이터 읽기 오류 발생 시 소켓을 닫고 재연결 시도
                    try {
                        if (fragment.getBluetoothSocket() != null) {
                            BluetoothSocket bluetoothSocket=fragment.getBluetoothSocket();
                            bluetoothSocket.close();
                            fragment.setBluetoothSocket(bluetoothSocket);
                        }
                    } catch (IOException closeException) {
                        Log.e("Bluetooth", "Error closing Bluetooth socket", closeException);
                    }
                    // 재연결 시도
                    reconnectBluetooth(fragment,context,activity);
                    break;
                }
            }
        }).start();
    }
    public static void saveDataToFirebase(BorrowingFragment3 fragment,String lightValue, String shockValue) {
        if (fragment.getHelmetId() == null) {
            Log.e("Bluetooth", "Helmet ID is null. Cannot save data.");
            return;
        }
        DatabaseReference db;
        db = FirebaseDatabase.getInstance().getReference();
        DatabaseReference dataRef = db.child("helmets").child(fragment.getHelmetId()).child("data");

        // Light와 Shock 값을 Firebase에 별도로 저장
        dataRef.child("Light").setValue(lightValue);
        dataRef.child("Shock").setValue(shockValue);
    }
    public static void saveLocationToFirebase(BorrowingFragment3 fragment,double latitude, double longitude) {
        if (fragment.getHelmetId() == null) {
            Log.e("Bluetooth", "헬멧 ID가 null입니다. 위치를 저장할 수 없습니다.");
            return;
        }
        DatabaseReference db;
        db = FirebaseDatabase.getInstance().getReference();
        DatabaseReference locationRef = db.child("helmets").child(fragment.getHelmetId());
        locationRef.child("latitude").setValue(latitude);
        locationRef.child("longitude").setValue(longitude);
    }

}
