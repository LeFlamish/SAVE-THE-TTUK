package com.example.sharehelmet.frag5_profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sharehelmet.R;
import com.example.sharehelmet.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.widget.LocationButtonView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RidingDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private NaverMap naverMap;
    private ImageView backButton;
    private List<Map.Entry<String, List<String>>> sortedEntries;
    private int position;
    private User user;
    String firebaseId;
    private DatabaseReference db;
    Map<String, List<String>> hashMap = new HashMap<>();
    private boolean isValidFirebaseId(String firebaseId) {
        return !(firebaseId.contains(".") || firebaseId.contains("#") ||
                firebaseId.contains("$") || firebaseId.contains("[") || firebaseId.contains("]"));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riding_detail);
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        // NaverMap 객체를 비동기적으로 받아오기
        mapView.getMapAsync(this);
        Intent intent = getIntent();
        if (intent != null) {
            firebaseId = intent.getStringExtra("firebaseId");
            position=intent.getIntExtra("position",0);
            if (firebaseId != null && isValidFirebaseId(firebaseId)) {
                db = FirebaseDatabase.getInstance().getReference();
                db.child("users").child(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        if (user != null) {
                            hashMap = user.getRecord();
                            update();
                        } else {
                            Log.e("QRFrag3", "User not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("QRFrag3", "Failed to read user data", error.toException());
                    }
                });

            } else {
                Log.e("MyActivity", "Invalid Firebase ID");
            }
        }





    }
    private int calculateMoney(String time){
        String[] parts = time.split(":");
        int minutes = Integer.parseInt(parts[0]);
        return 300+minutes*50;
    }
    private void update(){
        sortedEntries = new ArrayList<>(hashMap.entrySet());
        sortedEntries.sort((entry2, entry1) -> {
            // 키를 먼저 비교
            int keyCompare = entry1.getKey().split(" ")[0].compareTo(entry2.getKey().split(" ")[0]);
            if (keyCompare != 0) {
                return keyCompare;
            }
            // 키가 같다면 리스트의 첫 번째 값을 비교
            List<String> list1 = entry1.getValue();
            List<String> list2 = entry2.getValue();
            if (list1 == null || list1.isEmpty()) {
                return -1;
            }
            if (list2 == null || list2.isEmpty()) {
                return 1;
            }
            return list1.get(0).compareTo(list2.get(0));
        });
        Map.Entry<String, List<String>> entry = sortedEntries.get(position);
        List<String> values = entry.getValue();

        TextView riding_date_text_view=findViewById(R.id.riding_date_text_view);
        TextView start_time_tv=findViewById(R.id.start_time_tv);
        TextView end_time_tv=findViewById(R.id.end_time_tv);
        TextView money1=findViewById(R.id.money1);
        TextView money2=findViewById(R.id.money2);
        TextView helmet=findViewById(R.id.helmet);
        TextView start_location_tv=findViewById(R.id.start_location_tv);
        TextView end_location_tv=findViewById(R.id.end_location_tv);

        SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()); // 24시간 형식
        SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm a", Locale.ENGLISH); // 12시간 형식 AM/PM

        String startTimeStr = values.get(0);
        String endTimeStr = values.get(1);

        Date startTime = null;
        try {
            startTime = timeFormat24.parse(startTimeStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Date endTime = null;
        try {
            endTime = timeFormat24.parse(endTimeStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        String startTimeFormatted = timeFormat12.format(startTime);
        String endTimeFormatted = timeFormat12.format(endTime);
        db.child("places").orderByChild("locationID").equalTo(values.get(5))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // snapshot.getKey()로 각 매칭되는 데이터의 키를 얻을 수 있음
                            String key = snapshot.getKey();
                            start_location_tv.setText(key);
                            double latitude = snapshot.child("latitude").getValue(Double.class);
                            double longitude = snapshot.child("longitude").getValue(Double.class);
                            startLatLng = new LatLng(latitude, longitude);
                            Marker marker = new Marker();
                            marker.setPosition(startLatLng);
                            marker.setCaptionText("출발");
                            marker.setMap(naverMap);
                        }
                        if (startLatLng != null && endLatLng != null) {
                            moveCameraToMidpoint(startLatLng, endLatLng);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("FirebaseError", "Error occurred: " + databaseError.getMessage());
                    }
                });
        db.child("places").orderByChild("locationID").equalTo(values.get(6))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            // snapshot.getKey()로 각 매칭되는 데이터의 키를 얻을 수 있음
                            String key = snapshot.getKey();
                            end_location_tv.setText(key);
                            double latitude = snapshot.child("latitude").getValue(Double.class);
                            double longitude = snapshot.child("longitude").getValue(Double.class);
                            endLatLng = new LatLng(latitude, longitude);
                            Marker marker = new Marker();
                            marker.setPosition(endLatLng);
                            marker.setCaptionText("도착");
                            marker.setMap(naverMap);
                        }
                        if (startLatLng != null && endLatLng != null) {
                            moveCameraToMidpoint(startLatLng, endLatLng);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("FirebaseError", "Error occurred: " + databaseError.getMessage());
                    }
                });

        riding_date_text_view.setText(entry.getKey().split(" ")[0]);
        start_time_tv.setText(startTimeFormatted);
        end_time_tv.setText(endTimeFormatted);
        money1.setText(String.valueOf(calculateMoney(values.get(3))));
        money2.setText(values.get(4));
        helmet.setText(values.get(2));



        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());
    }
    LatLng startLatLng = null;
    LatLng endLatLng = null;
    private void moveCameraToMidpoint(LatLng startLatLng, LatLng endLatLng) {
        double midLat = (startLatLng.latitude + endLatLng.latitude) / 2;
        double midLng = (startLatLng.longitude + endLatLng.longitude) / 2;
        LatLng midpoint = new LatLng(midLat, midLng);

        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(midpoint);
        naverMap.moveCamera(cameraUpdate);
    }
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        // 설정 코드 추가
        UiSettings uiSettings = naverMap.getUiSettings();
//        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlEnabled(false);
//        uiSettings.setLogoMargin(0, 0, 0, 0);
//        uiSettings.setScaleBarEnabled(true);
//
//        // LocationButtonView 연결
//        LocationButtonView locationButtonView = findViewById(R.id.location);
//        locationButtonView.setMap(naverMap);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
