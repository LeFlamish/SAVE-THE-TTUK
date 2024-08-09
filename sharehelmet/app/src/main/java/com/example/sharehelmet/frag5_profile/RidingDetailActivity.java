package com.example.sharehelmet.frag5_profile;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sharehelmet.R;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.widget.LocationButtonView;

public class RidingDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private NaverMap naverMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riding_detail);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        // NaverMap 객체를 비동기적으로 받아오기
        mapView.getMapAsync(this);
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
