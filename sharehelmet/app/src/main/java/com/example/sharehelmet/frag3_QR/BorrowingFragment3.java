package com.example.sharehelmet.frag3_QR;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.annotation.SuppressLint;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharehelmet.R;
import com.example.sharehelmet.frag1_home.HomeFrag1;
import com.example.sharehelmet.model.Helmet;
import com.example.sharehelmet.model.User;
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
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BorrowingFragment3 extends Fragment implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
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
        return view;
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