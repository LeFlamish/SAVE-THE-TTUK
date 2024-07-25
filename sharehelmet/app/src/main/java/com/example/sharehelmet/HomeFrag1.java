package com.example.sharehelmet;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFrag1 extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private MapView mapView;
    private NaverMap naverMap;
    private FusedLocationSource locationSource;
    private Button btnModeToggle;
    private int locationTrackingMode = 1; // Start with Follow mode
    private DatabaseReference db;
    private List<Marker> markerList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag1_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = view.findViewById(R.id.map);
        btnModeToggle = view.findViewById(R.id.btn_mode_toggle);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        btnModeToggle.setOnClickListener(v -> {
            if (naverMap != null) {
                toggleLocationTrackingMode();
            }
        });

        db = FirebaseDatabase.getInstance().getReference();

        /*savePlace("00000000", "IT 1호관", 35.88748984889353, 128.61274302005768);
        savePlace("00000001", "본관", 35.89044078812516, 128.61201882362366);
        savePlace("00000010", "첨성관", 35.89136211922988, 128.61490488052368);
        savePlace("00000011", "글로벌플라자", 35.89185754756124, 128.6112678050995);
        savePlace("00000100", "경상대학",35.88934560701709,128.6159348487854);
        savePlace("00000101", "대운동장",35.88812872135325,128.60642910003662);
        savePlace("00000110", "정문",35.88522550431494,128.61462593078613);
        savePlace("00000111", "북문",35.89235499478949,128.6094578539692);
        savePlace("00001000", "도서관",35.89166632997614,128.61208319664001);
        savePlace("00001001", "일청담",35.8886304944912,128.61211539394523);*/
    }

    private void savePlace(String id, String name, double latitude, double longitude) {
        Map<String, Map<String, Object>> subplaces = new HashMap<>();
        subplaces.put("001", createSubplaceData(false, "0"));
        subplaces.put("002", createSubplaceData(false, "0"));
        subplaces.put("003", createSubplaceData(false, "0"));
        subplaces.put("004", createSubplaceData(false, "0"));
        subplaces.put("005", createSubplaceData(false, "0"));
        subplaces.put("006", createSubplaceData(false, "0"));
        subplaces.put("007", createSubplaceData(false, "0"));
        subplaces.put("008", createSubplaceData(false, "0"));
        subplaces.put("009", createSubplaceData(false, "0"));
        subplaces.put("010", createSubplaceData(false, "0"));
        Map<String, Object> place = new HashMap<>();
        place.put("name", name);
        place.put("latitude", latitude);
        place.put("longitude", longitude);
        place.put("stock",0);
        place.put("subplaces", subplaces);

        db.child("places").child(id).setValue(place)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Place added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add place", Toast.LENGTH_SHORT).show());
    }
    private Map<String, Object> createSubplaceData(boolean hasHelmet, String helmetId) {
        Map<String, Object> subplaceData = new HashMap<>();
        subplaceData.put("hasHelmet", hasHelmet);
        subplaceData.put("helmetId", helmetId);
        return subplaceData;
    }
    private void toggleLocationTrackingMode() {
        locationTrackingMode = (locationTrackingMode + 1) % 2;
        setTrackingMode();
    }

    private void setTrackingMode() {
        if (locationTrackingMode == 0) {
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        } else {
            naverMap.setLocationTrackingMode(LocationTrackingMode.Face);
        }
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        naverMap.setLocationSource(locationSource);

        if (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
        }

        naverMap.addOnLocationChangeListener(location -> {
            if (naverMap.getLocationTrackingMode() != getCurrentTrackingMode()) {
                setTrackingMode();
            }
            displayNearbyPlaces(location);
        });
    }

    private LocationTrackingMode getCurrentTrackingMode() {
        return locationTrackingMode == 0 ? LocationTrackingMode.Follow : LocationTrackingMode.Face;
    }

    private void displayNearbyPlaces(Location currentLocation) {
        if (currentLocation == null) {
            return;
        }

        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        db.child("places").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                clearMarkers();

                List<Place> places = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double latitude = snapshot.child("latitude").getValue(Double.class);
                    double longitude = snapshot.child("longitude").getValue(Double.class);
                    String name = snapshot.child("name").getValue(String.class);
                    int stock = snapshot.child("stock").getValue(Integer.class); // 재고 정보 가져오기

                    LatLng placeLatLng = new LatLng(latitude, longitude);
                    double distance = calculateDistance(currentLatLng, placeLatLng);

                    if (distance <= 1.5) { // 1.5km 이내의 장소만 표시
                        places.add(new Place(name, placeLatLng, distance, stock));
                    }
                }

                if (!places.isEmpty()) {
                    places.sort((p1, p2) -> Double.compare(p1.distance, p2.distance));

                    // 재고가 있는 장소 중 가장 가까운 장소를 찾기
                    Place nearestPlaceWithStock = null;for (Place place : places) {
                        if (place.stock > 0) {
                            nearestPlaceWithStock = place;
                            break;
                        }
                    }

                    for (Place place : places) {
                        Marker marker = new Marker();
                        marker.setPosition(place.latLng);
                        marker.setCaptionText(place.name + "\n" + "(" + String.format("%.2f", place.distance) + "km)");

                        if (place == nearestPlaceWithStock) {
                            marker.setCaptionColor(Color.GREEN); // 재고가 있는 가장 가까운 장소는 초록색
                        } else if (place.stock == 0) {
                            marker.setIconTintColor(Color.RED); // 재고가 없는 장소는 빨간색
                        }

                        marker.setMap(naverMap);
                        markerList.add(marker);

                        // 마커 클릭 리스너 설정
                        marker.setOnClickListener(overlay -> {
                            Toast.makeText(getContext(), place.name + " 재고: " + place.stock, Toast.LENGTH_SHORT).show();
                            return true;
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (naverMap != null) {
                    naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
                }
            } else {
                Toast.makeText(getContext(), "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private static class Place {
        String name;
        LatLng latLng;
        double distance;
        int stock;

        Place(String name, LatLng latLng, double distance, int stock) {
            this.name = name;
            this.latLng = latLng;
            this.distance = distance;
            this.stock = stock;
        }
    }
}


