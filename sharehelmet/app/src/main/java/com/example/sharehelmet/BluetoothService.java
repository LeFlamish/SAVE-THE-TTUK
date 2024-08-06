package com.example.sharehelmet;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.example.sharehelmet.model.Helmet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;

public class BluetoothService extends Service {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private DatabaseReference db;
    private String HelmetID;
    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        db = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String address = intent.getStringExtra("BLUETOOTH_ADDRESS");
        HelmetID=intent.getStringExtra("HelmetID");
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);

        new Thread(new Runnable() {
            @Override
            public void run() {
                connectToDevice();
                listenForData();
            }
        }).start();

        return START_STICKY;
    }

    private void connectToDevice() {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenForData() {
        byte[] buffer = new byte[1024];
        int bytes;

        while (true) {
            try {
                bytes = inputStream.read(buffer);
                processData(buffer, bytes);
            } catch (IOException e) {
                break;
            }
        }
    }

    private void processData(byte[] buffer, int bytes) {
        double data1 = ByteBuffer.wrap(buffer, 0, 8).getDouble();
        double data2 = ByteBuffer.wrap(buffer, 8, 8).getDouble();
        int data3 = ByteBuffer.wrap(buffer, 16, 4).getInt();

        uploadDataToFirebase(data1, data2, data3);
    }
    private void uploadDataToFirebase(double data1, double data2, int data3) {
        db.child("helmets").child(HelmetID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Helmet helmet =snapshot.getValue(Helmet.class);
                helmet.setLatitude(data1);
                helmet.setLongitude(data2);
                helmet.setBatteryState(data3);
                db.child("helmets").child(HelmetID).setValue(helmet);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
