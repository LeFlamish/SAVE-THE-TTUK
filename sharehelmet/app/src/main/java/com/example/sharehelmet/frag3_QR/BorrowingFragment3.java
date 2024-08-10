package com.example.sharehelmet.frag3_QR;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import android.Manifest;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharehelmet.R;
import com.example.sharehelmet.model.Helmet;
import com.example.sharehelmet.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BorrowingFragment3 extends Fragment implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final int REQUEST_BLUETOOTH_SCAN = 2;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "Bluetooth";
    private MapView mapView;
    private NaverMap naverMap;
    private FusedLocationSource locationSource;
    private String firebaseId;
    private String helmetId;
    private User user;
    private Helmet helmet;
    private TextView helmetName, price, battery, usingTime, startTime;
    private DatabaseReference db;
    private LocalDateTime rentalStartTime;
    private Button returnButton;
    private List<Marker> markerList = new ArrayList<>();
    private List<Place> places = new ArrayList<>();
    private List<Place> allPlaces = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null && device.getName().equals(helmetId)) {
                    Log.d(TAG, "Device found: " + device.getName());
                    bluetoothAdapter.cancelDiscovery();
                    try {
                        connectToDevice(device);
                    } catch (IOException e) {
                        Log.e(TAG, "Error connecting to device", e);
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Bluetooth device discovery finished");
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bluetooth Broadcast Receiver 등록
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(receiver, filter);

        // Firebase 데이터베이스 초기화
        db = FirebaseDatabase.getInstance().getReference();

        // Firebase에서 helmetId를 가져오기 위해 데이터베이스 리스너 설정
        if (firebaseId != null) {
            db.child("users").child(firebaseId).child("rental_info").child("0").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    helmetId = snapshot.getValue(String.class);
                    if (helmetId != null) {
                        // helmetId를 성공적으로 가져온 후 블루투스 장치에 연결 시도
                        initializeBluetooth();
                    } else {
                        Log.e(TAG, "헬멧 ID를 가져오는 데 실패했습니다.");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Firebase 데이터베이스 오류: " + error.getMessage());
                }
            });
        } else {
            Log.e(TAG, "firebaseId가 null입니다.");
        }

        // Bluetooth Adapter 초기화
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported on this device");
        } else {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT);
            } else {
                initializeBluetooth();
            }
        }

        // 위치 클라이언트 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        // 위치 요청 설정
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 업데이트 간격 (밀리초)
        locationRequest.setFastestInterval(5000); // 가장 빠른 업데이트 간격 (밀리초)

        // 위치 콜백 설정
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                for (Location location : locationResult.getLocations()) {
                    saveLocationToFirebase(location.getLatitude(), location.getLongitude());
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_borrowing3, container, false);
        try{
            mapView = view.findViewById(R.id.map);
            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);
            locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
        }catch(Exception ignored){}
        Button btnModeLocation = view.findViewById(R.id.btn_current_location);
        btnModeLocation.setOnClickListener(v -> {
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        });
        db = FirebaseDatabase.getInstance().getReference();

        helmetName = view.findViewById(R.id.helmet_id);
        price = view.findViewById(R.id.price_num);
        battery = view.findViewById(R.id.battery_num);
        usingTime = view.findViewById(R.id.using_time_num);
        startTime = view.findViewById(R.id.start_time_num);

        View bottomSheet = view.findViewById(R.id.persistent_bottom_sheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // BottomSheet를 처음부터 확장된 상태로 설정
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        Bundle bundle = getArguments();
        if (bundle != null) {
            firebaseId=bundle.getString("firebaseId");
        }
        db.child("users").child(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    rentalStartTime= LocalDateTime.parse(user.getRental_info().get(1), formatter);
                    helmetId=user.getRental_info().get(0);
                    BorrowHelmet2();
                    connectToHelmetDevice(); // 장치 연결 시도
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        returnButton = view.findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setNow_qr(2);
                db.child("users").child(firebaseId).setValue(user);

                //프래그먼트 이동
                BarcodeEndFragment3 barcodeEndFragment3=new BarcodeEndFragment3();
                Bundle bundle = new Bundle();
                bundle.putString("firebaseId",firebaseId);
                barcodeEndFragment3.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, barcodeEndFragment3)
                        .commit();
            }
        });
        fetchCurrentLocationAndUpdateFirebase();
        startLocationUpdates();
        return view;
    }
    private void initializeBluetooth() {
        Log.d(TAG, "Initializing Bluetooth...");

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        // 장치 검색 및 연결 시도
        connectToHelmetDevice();
    }
    private void connectToHelmetDevice() {
        if (bluetoothAdapter != null) {
            Log.d(TAG, "Starting Bluetooth device discovery...");
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.startDiscovery();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_BLUETOOTH_SCAN);
            }
        }
    }

    private void connectToDevice(BluetoothDevice device) throws IOException {
        bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        try {
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
            Log.d(TAG, "Connected to " + device.getName());

            // 데이터 수신 시작
            listenForData();
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to device", e);
            // 소켓을 닫고 재연결 시도
            try {
                bluetoothSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Error closing Bluetooth socket", closeException);
            }
        }
    }

    private void listenForData() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    if (inputStream == null) {
                        Log.e(TAG, "InputStream is null. Exiting data listener.");
                        break;
                    }

                    bytes = inputStream.read(buffer);
                    if (bytes == -1) {
                        throw new IOException("InputStream read returned -1, socket might be closed");
                    }

                    String receivedData = new String(buffer, 0, bytes).trim();
                    Log.d(TAG, "Data received: " + receivedData);

                    // 데이터 파싱
                    String[] parts = receivedData.split(", ");
                    if (parts.length == 2) {
                        String lightPart = parts[0];
                        String shockPart = parts[1];

                        // Light와 Shock 값 추출
                        String lightValue = lightPart.split(": ")[1];
                        String shockValue = shockPart.split(": ")[1];

                        // Firebase에 저장
                        saveDataToFirebase(lightValue, shockValue);
                    } else {
                        Log.e(TAG, "Invalid data format received: " + receivedData);
                    }

                } catch (IOException e) {
                    Log.e(TAG, "Error reading data", e);
                    // 데이터 읽기 오류 발생 시 소켓을 닫고 재연결 시도
                    try {
                        if (bluetoothSocket != null) {
                            bluetoothSocket.close();
                        }
                    } catch (IOException closeException) {
                        Log.e(TAG, "Error closing Bluetooth socket", closeException);
                    }
                    // 재연결 시도
                    reconnectBluetooth();
                    break;
                }
            }
        }).start();
    }

    private void reconnectBluetooth() {
        if (bluetoothAdapter != null && helmetId != null) {
            // 블루투스 장치 검색 및 연결 시도
            connectToHelmetDevice();
        }
    }

    private void saveDataToFirebase(String lightValue, String shockValue) {
        if (helmetId == null) {
            Log.e(TAG, "Helmet ID is null. Cannot save data.");
            return;
        }

        DatabaseReference dataRef = db.child("helmets").child(helmetId).child("data");

        // Light와 Shock 값을 Firebase에 별도로 저장
        dataRef.child("Light").setValue(lightValue);
        dataRef.child("Shock").setValue(shockValue);
    }

    private void saveLocationToFirebase(double latitude, double longitude) {
        if (helmetId == null) {
            Log.e(TAG, "헬멧 ID가 null입니다. 위치를 저장할 수 없습니다.");
            return;
        }

        DatabaseReference locationRef = db.child("helmets").child(helmetId);
        locationRef.child("latitude").setValue(latitude);
        locationRef.child("longitude").setValue(longitude);
    }

    private void fetchCurrentLocationAndUpdateFirebase() {
        if (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), location -> {
                        if (location != null) {
                            saveLocationToFirebase(location.getLatitude(), location.getLongitude());
                        }
                    });
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void BorrowHelmet2() {
        db.child("helmets").child(helmetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //helmets 파베 수정
                helmet=snapshot.getValue(Helmet.class);
                helmet.setBorrow(true);
                helmet.setStorageId("-");
                helmet.setUserId(firebaseId);
                db.child("helmets").child(helmetId).setValue(helmet);

                //텍스트뷰 표시
                helmetName.setText("#" + helmetId);
                battery.setText("100%");//나중에 수정
                startTime.setText(user.getRental_info().get(1));
                updateElapsedTime();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateElapsedTime();
                        handler.postDelayed(this, 1000); // 1초마다 업데이트
                    }
                }, 1000);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    @SuppressLint("SetTextI18n")
    private void updateElapsedTime() {
        if (rentalStartTime != null) {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(rentalStartTime, now);
            long minutes = duration.toMinutes();
            long seconds = duration.getSeconds() % 60;
            String elapsedTime = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
            long moneyAmount = 300 + minutes * 50;
            NumberFormat numberFormat = NumberFormat.getInstance(Locale.getDefault());
            String formattedMoney = numberFormat.format(moneyAmount);

            usingTime.setText(elapsedTime);
            price.setText(formattedMoney + "원");
        }
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        try{
            this.naverMap = naverMap;
            naverMap.setLocationSource(locationSource);
            if (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
            naverMap.addOnLocationChangeListener(location -> {
                displayNearbyPlaces(location);
            });
            UiSettings uiSettings = naverMap.getUiSettings();
            uiSettings.setCompassEnabled(true);
            uiSettings.setZoomControlEnabled(false);
            uiSettings.setLogoMargin(0, 0, 0, 0);
            uiSettings.setScaleBarEnabled(true);

            // LocationButtonView 연결
            LocationButtonView locationButtonView = getView().findViewById(R.id.location);
            locationButtonView.setMap(naverMap);
            
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                naverMap.setMapType(NaverMap.MapType.Navi); // 다크 모드
                naverMap.setNightModeEnabled(true);
            } else {
                naverMap.setMapType(NaverMap.MapType.Basic); // 라이트 모드
                naverMap.setNightModeEnabled(false);
            }
        }catch(Exception ignored){}
    }
    private void displayNearbyPlaces(Location currentLocation) {
        try{
            if (currentLocation == null) {
                return;
            }
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            db.child("places").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    clearMarkers();
                    allPlaces.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String id = snapshot.getKey();
                        double latitude = snapshot.child("latitude").getValue(Double.class);
                        double longitude = snapshot.child("longitude").getValue(Double.class);
                        int stock = snapshot.child("stock").getValue(Integer.class);
                        String name = snapshot.getKey();
                        LatLng placeLatLng = new LatLng(latitude, longitude);
                        double distance = calculateDistance(currentLatLng, placeLatLng);
                        Place place = new Place(id, placeLatLng, distance, stock, name);
                        allPlaces.add(place);
                        if (distance <= 1.5&&stock!=20) {
                            Marker marker = new Marker();
                            marker.setPosition(placeLatLng);
                            marker.setCaptionText(name);
                            marker.setMap(naverMap);
                            markerList.add(marker);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception ignored){

        }
    }
    private double calculateDistance(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        return results[0] / 1000.0; // 미터를 킬로미터로 변환
    }

    private void clearMarkers() {
        for (Marker marker : markerList) {
            marker.setMap(null);
        }
        markerList.clear();
    }
    public static class Place {
        LatLng latLng;
        double distance;
        int stock;
        String name;
        String id;

        Place(String id, LatLng latLng, double distance, int stock, String name) {
            this.id = id;
            this.latLng = latLng;
            this.distance = distance;
            this.stock = stock;
            this.name = name;
        }
    }
}