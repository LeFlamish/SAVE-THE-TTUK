package com.example.sharehelmet.frag1_home;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.example.sharehelmet.R;
import com.example.sharehelmet.frag5_profile.RidingGuideActivity;
import com.example.sharehelmet.model.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.CompassView;
import com.naver.maps.map.widget.LocationButtonView;

import java.util.ArrayList;
import java.util.List;

public class HomeFrag1 extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String LOCATION_TRACKING_MODE_KEY = "location_tracking_mode";
    private static final String SORT_KEY = "which_to_sort";
    private MapView mapView;
    private NaverMap naverMap;
    private ImageView thumbnail;
    private FusedLocationSource locationSource;
    private Spinner spinnerSort;
    private int locationTrackingMode = 0;
    private DatabaseReference db;
    private List<Marker> markerList = new ArrayList<>();
    private ListView listView;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private int sortCriteria = 0; // 정렬 기준 플래그: 0 - 거리 가까운 순, 1 - 재고 적은 순, 2 - 재고 많은 순
    private int stockMin = Integer.MIN_VALUE, stockMax = Integer.MAX_VALUE;
    private List<Place> places = new ArrayList<>();
    private List<Place> allPlaces = new ArrayList<>(); // 모든 장소를 저장하는 리스트
    private int listViewIndex = -1;
    private int listViewTop = 0;
    private static final long HIDE_DELAY_MS = 4000; // 2초 지연 시간
    private Handler handler = new Handler();
    private Runnable hideRunnable;
    private Activity mActivity;
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        mContext = context;
        if (context instanceof Activity) {
            mActivity = (Activity)context;
        }
        super.onAttach(context);
    }
    @Override
    public void onDetach() {
        mActivity = null;
        mContext = null;
        super.onDetach();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag1_home, container, false);

        try{
            ImageButton helpButton = view.findViewById(R.id.help);
            helpButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), RidingGuideActivity.class);
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.horizontal_enter,R.anim.none);
            });


            View bottomSheet = view.findViewById(R.id.persistent_bottom_sheet);
            bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

            listView = view.findViewById(R.id.place_list_view);
            listView.setOnTouchListener((v,event)->{
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 리스트뷰 터치 시 바텀시트의 드래그 비활성화
                        bottomSheetBehavior.setDraggable(false);
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // 리스트뷰 터치 해제 시 바텀시트의 드래그 활성화
                        bottomSheetBehavior.setDraggable(true);
                        break;
                }
                return false; // 터치 이벤트를 리스트뷰에 전달
            });
        }catch (Exception ignored){

        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try{
            mapView = view.findViewById(R.id.map);

            mapView.onCreate(savedInstanceState);
            mapView.getMapAsync(this);

            locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
            db = FirebaseDatabase.getInstance().getReference();

            toggleLocationTrackingMode();

        }
        catch(Exception ignored){
        }

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

        try{
            spinnerSort = view.findViewById(R.id.spinner_sort);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.sort_options, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSort.setAdapter(adapter);

            spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    sortCriteria = position;
                    sortAndDisplayPlaces();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });

            // Initial sorting and displaying of places
            sortAndDisplayPlaces();

            listView.setOnItemClickListener((parent, view1, position, id) -> {
                Place selectedPlace = places.get(position);
                LatLng selectedLatLng = selectedPlace.latLng;
                naverMap.moveCamera(CameraUpdate.scrollTo(selectedLatLng)); // 선택한 장소로 지도 이동
            });
            if (savedInstanceState != null) {
                locationTrackingMode = savedInstanceState.getInt(LOCATION_TRACKING_MODE_KEY, 0);
                sortCriteria=savedInstanceState.getInt(SORT_KEY,0);
            }
        }catch (Exception ignored){

        }



    }

    private void savePlace(String id, String name, double latitude, double longitude, int room_num) {
        Storage storage = new Storage();
        storage.setLocationID(id);
        storage.setLatitude(latitude);
        storage.setLongitude(longitude);
        storage.setStock(0);
        ArrayList<String> storedHelmetID = new ArrayList<>();
        for (int i = 0; i < room_num; i++) {
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
        try{
            this.naverMap = naverMap;

            naverMap.setLocationSource(locationSource);

            if (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                naverMap.setLocationTrackingMode(getCurrentTrackingMode());
            }

            // 지도 클릭 이벤트 처리
            naverMap.setOnMapClickListener((point, coord) -> hidePlaceInfo());

            // 설정 코드 추가
            UiSettings uiSettings = naverMap.getUiSettings();
            uiSettings.setCompassEnabled(false);    //기본 사용x. 커스텀 나침반 o
            uiSettings.setZoomControlEnabled(false);
            uiSettings.setLogoMargin(0, 0, 0, 0);
            uiSettings.setScaleBarEnabled(true);


            // LocationButtonView 연결
            LocationButtonView locationButtonView = getView().findViewById(R.id.location);
            locationButtonView.setMap(naverMap);

            // 나침반 뷰를 NaverMap에 연결
            CompassView compassView = getView().findViewById(R.id.compass);
            compassView.setMap(naverMap);

            naverMap.addOnLocationChangeListener(location -> displayNearbyPlaces(location));

            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                naverMap.setMapType(NaverMap.MapType.Navi); // 다크 모드
                naverMap.setNightModeEnabled(true);
            } else {
                naverMap.setMapType(NaverMap.MapType.Basic); // 라이트 모드
                naverMap.setNightModeEnabled(false);
            }
        }catch(Exception ignored){

        }
    }

    private LocationTrackingMode getCurrentTrackingMode() {
        return locationTrackingMode == 0 ? LocationTrackingMode.Follow : LocationTrackingMode.Face;
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
                    Place closestPlaceWithStock = null;
                    double closestDistanceWithStock = Double.MAX_VALUE;

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
                        if (true || distance <= 1.5*1000000000) {

                            // 재고가 있는 마커 중에서 가장 가까운 마커를 찾기
                            if (stock > 0 && distance < closestDistanceWithStock) {
                                closestPlaceWithStock = place;
                                closestDistanceWithStock = distance;
                            }
                            Marker marker = new Marker();
                            marker.setPosition(placeLatLng);
                            marker.setCaptionText(name);
                            marker.setOnClickListener(new Overlay.OnClickListener() {
                                @Override
                                public boolean onClick(@NonNull Overlay overlay) {

                                    showPlaceInfo(name, stock, distance);
                                    return true;
                                }
                            });
                            if (stock == 0) {
                                marker.setIconTintColor(Color.RED); // 재고가 0인 경우 마커를 빨간색으로 표시
                            }
                            marker.setMap(naverMap);
                            markerList.add(marker);
                        }
                    }

                    // 가장 가까운 재고 있는 마커의 캡션 색상을 초록색으로 변경
                    if (closestPlaceWithStock != null) {
                        for (Marker marker : markerList) {
                            if (marker.getPosition().equals(closestPlaceWithStock.latLng)) {
                                marker.setCaptionColor(Color.GREEN);
                                break;
                            }
                        }
                    }
                    sortAndDisplayPlaces();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "데이터를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception ignored){

        }
    }

    public void showPlaceInfo(String name, int stock, double length) {
        try {

            View placeInfoLayout = getView().findViewById(R.id.place_info_layout);
            if (placeInfoLayout != null) {
                // 먼저 뷰의 가시성을 VISIBLE로 설정하고 투명도를 1로 설정하여 보이게 함
                placeInfoLayout.setVisibility(View.VISIBLE);
                placeInfoLayout.setAlpha(0f); // 초기 투명도 설정

                // 애니메이션을 사용하여 투명도를 1로 변경
                placeInfoLayout.animate()
                        .alpha(1f)
                        .setDuration(500) // 애니메이션 지속 시간 (예: 500ms)
                        .start();
            }
            else return;

            // 텍스트 뷰와 이미지 뷰를 찾아서 값을 설정
            TextView title = placeInfoLayout.findViewById(R.id.title);
            TextView rentalStatus = placeInfoLayout.findViewById(R.id.rental_status);
            TextView stockTextView = placeInfoLayout.findViewById(R.id.stock);
            TextView distance = placeInfoLayout.findViewById(R.id.distance);
            thumbnail = placeInfoLayout.findViewById(R.id.thumbnail);

            // Firebase Storage에서 이미지 가져오기
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference().child("images/"+name+".jpg"); // 이미지 경로 예시
            storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    title.setText(name);
                    rentalStatus.setText(" 대여함");
                    stockTextView.setText(stock + "개");
                    distance.setText(String.format("%.2fkm", length));
                    Activity activity = getActivity();

                    if (activity == null || activity.isFinishing()) {
                        return;
                    }

                    // Glide를 사용하여 이미지 로드
                    Glide.with(activity)
                            .load(uri)
                            .placeholder(R.drawable.camera_24) // 로딩 중일 때 표시할 이미지
                            .error(R.drawable.camera_24) // 로딩 실패 시 표시할 이미지
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                                    startHideDelay();
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, com.bumptech.glide.request.target.Target<Drawable> target, @NonNull com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                    startHideDelay();
                                    return false;
                                }
                            })
                            .into(thumbnail);

                     }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // 로딩 실패 시 기본 이미지를 유지
                        }
                     });

        }
        catch (Exception ignored){}
    }

    private void startHideDelay() {
        hideRunnable = new Runnable() {
            @Override
            public void run() {
                hidePlaceInfo();
            }
        };
        handler.postDelayed(hideRunnable, HIDE_DELAY_MS);
    }


    private void hidePlaceInfo() {

        try {
            View placeInfoLayout = getView().findViewById(R.id.place_info_layout); // RelativeLayout의 ID를 사용

            if (placeInfoLayout != null) {
                // 서서히 사라지게 하는 애니메이션
                placeInfoLayout.animate()
                        .alpha(0f) // 투명도 0으로 설정
                        .setDuration(300) // 애니메이션 시간 (500ms = 0.5초)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                // 애니메이션이 끝난 후 GONE으로 설정
                                placeInfoLayout.setVisibility(View.GONE);
                            }
                        });
            }
        }
        catch (Exception e){}
    }

    private void sortAndDisplayPlaces() {
        try {
            saveListViewScrollPosition();
            switch (sortCriteria) {
                case 0:
                    spinnerSort.setSelection(0);
                    break;
                case 1:
                    spinnerSort.setSelection(1);
                    break;
                case 2:
                    spinnerSort.setSelection(2);
                    break;
            }
            places.clear();
            for (Place place : allPlaces) {
                if (place.stock >= stockMin && place.stock <= stockMax) {
                    places.add(place);
                }
            }

            switch (sortCriteria) {
                case 0: // 거리 가까운 순 정렬
                    places.sort((p1, p2) -> Double.compare(p1.distance, p2.distance));
                    break;
                case 1: // 재고 적은 순 정렬
                    places.sort((p1, p2) -> Integer.compare(p1.stock, p2.stock));
                    break;
                case 2: // 재고 많은 순 정렬
                    places.sort((p1, p2) -> Integer.compare(p2.stock, p1.stock));
                    break;
            }

            PlaceAdapter placeAdapter = new PlaceAdapter(getContext(), places);
            listView.setAdapter(placeAdapter);
            restoreListViewScrollPosition();
        } catch (Exception ignored) {
        }
    }
    private void saveListViewScrollPosition() {
        listViewIndex = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(0);
        listViewTop = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());
    }

    private void restoreListViewScrollPosition() {
        if (listViewIndex != -1) {
            listView.setSelectionFromTop(listViewIndex, listViewTop);
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
    public void onStart() {
        super.onStart();
        mapView.onStart();
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
        try {
            // ImageView에 대한 Glide 요청을 취소하고 리소스 해제
            Glide.with(this).clear(thumbnail);
        }
        catch (Exception e){}
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // 상태 저장 메서드 추가
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LOCATION_TRACKING_MODE_KEY, locationTrackingMode);
        outState.putInt(SORT_KEY, sortCriteria);
    }

    @Override
    public void onViewStateRestored(@NonNull Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            locationTrackingMode = savedInstanceState.getInt(LOCATION_TRACKING_MODE_KEY, 0);
            sortCriteria=savedInstanceState.getInt(SORT_KEY,0);
            setTrackingMode();
            sortAndDisplayPlaces();
        }
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

    public void showDialog(String name, int stock, double length) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog, null);

        TextView t1 = dialogView.findViewById(R.id.station_name_text);
        TextView t2 = dialogView.findViewById(R.id.stock);
        TextView t3 = dialogView.findViewById(R.id.distance);
        ImageView stationImage = dialogView.findViewById(R.id.station_image); // ImageView 추가

        t1.setText(name);
        t2.setText(stock + "개");
        t3.setText(String.format("%.2fkm", length));

        // Firebase Storage에서 이미지 가져오기
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/"+name+".jpg"); // 이미지 경로 예시


        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Glide를 사용하여 이미지 로드
                Glide.with(getActivity())
                        .load(uri)
                        .placeholder(R.drawable.camera_24) // 로딩 중일 때 표시할 이미지
                        .error(R.drawable.camera_24) // 로딩 실패 시 표시할 이미지
                        .into(stationImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
