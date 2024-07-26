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

        /*savePlace("001", "IT 1호관", 35.88748984889353, 128.61274302005768,20);
        savePlace("002", "본관", 35.89044078812516, 128.61201882362366,20);
        savePlace("003", "첨성관", 35.89136211922988, 128.61490488052368,20);
        savePlace("004", "글로벌플라자", 35.89185754756124, 128.6112678050995,20);
        savePlace("005", "경상대학",35.88934560701709,128.6159348487854,20);
        savePlace("006", "대운동장",35.88812872135325,128.60642910003662,20);
        savePlace("007", "정문",35.88522550431494,128.61462593078613,20);
        savePlace("008", "북문",35.89235499478949,128.6094578539692,20);
        savePlace("009", "도서관",35.89166632997614,128.61208319664001,20);
        savePlace("010", "일청담",35.8886304944912,128.61211539394523,20);*/
    }

    private void savePlace(String id, String name, double latitude, double longitude,int room_num) {
        Storage storage=new Storage();
        storage.setLocationID(id);
        storage.setLatitude(latitude);
        storage.setLongitude(longitude);
        storage.setStock(0);
        ArrayList<String> storedHelmetID=new ArrayList<>();
        for(int i=0;i<room_num;i++){
            storedHelmetID.add("-");
        }
        storage.setStoredHelmetID(storedHelmetID);
        db.child("places").child(name).setValue(storage)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Place added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add place", Toast.LENGTH_SHORT).show());
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
                    int stock = snapshot.child("stock").getValue(Integer.class); // 재고 정보 가져오기

                    LatLng placeLatLng = new LatLng(latitude, longitude);
                    double distance = calculateDistance(currentLatLng, placeLatLng);

                    if (distance <= 1.5) { // 1.5km 이내의 장소만 표시
                        places.add(new Place(placeLatLng, distance, stock));
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
                        marker.setCaptionText("(" + String.format("%.2f", place.distance) + "km)");

                        if (place == nearestPlaceWithStock) {
                            marker.setCaptionColor(Color.GREEN); // 재고가 있는 가장 가까운 장소는 초록색
                        } else if (place.stock == 0) {
                            marker.setIconTintColor(Color.RED); // 재고가 없는 장소는 빨간색
                        }

                        marker.setMap(naverMap);
                        markerList.add(marker);

                        // 마커 클릭 리스너 설정
                        marker.setOnClickListener(overlay -> {
                            Toast.makeText(getContext(), " 재고: " + place.stock, Toast.LENGTH_SHORT).show();
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
        LatLng latLng;
        double distance;
        int stock;

        Place(LatLng latLng, double distance, int stock) {
            this.latLng = latLng;
            this.distance = distance;
            this.stock = stock;
        }
    }
}


