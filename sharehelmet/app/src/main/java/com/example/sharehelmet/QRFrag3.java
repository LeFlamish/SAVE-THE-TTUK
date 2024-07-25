package com.example.sharehelmet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class QRFrag3 extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private DecoratedBarcodeView barcodeView;
    private TextView resultTextView, rentalDurationTextView;
    private EditText returnStationEditText;
    private Button returnButton;
    private DatabaseReference db;
    private String helmetId;
    private Handler handler;
    private Runnable updateRunnable;
    private String rentalStartTime;
    private boolean isHelmetRented = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag3_qr, container, false);

        barcodeView = view.findViewById(R.id.barcode_scanner);
        resultTextView = view.findViewById(R.id.result_text_view);
        rentalDurationTextView = view.findViewById(R.id.rental_duration_text_view);
        returnStationEditText = view.findViewById(R.id.return_station_edit_text);
        returnButton = view.findViewById(R.id.return_button);

        // Initialize Firebase Database
        db = FirebaseDatabase.getInstance().getReference("helmets");

        // QR 코드 스캔을 위한 설정
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(Collections.singletonList(BarcodeFormat.QR_CODE)));
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                handleResult(result);
            }

            @Override
            public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
                // Optional: handle possible result points
            }
        });

        // 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startScanning();
        }



        returnButton.setOnClickListener(v -> {
            if (isHelmetRented) {
                String returnStationId = returnStationEditText.getText().toString().trim();
                if (!returnStationId.isEmpty()) {
                    processReturn(helmetId, returnStationId);
                } else {
                    Toast.makeText(getContext(), "Please enter a return station ID.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "No helmet to return.", Toast.LENGTH_SHORT).show();
            }
        });

        handler = new Handler(Looper.getMainLooper());
        saveHelmetData("00000000",100,"반납","00000000","001","0","0");
        saveHelmetData("00000001",100,"반납","00000000","002","0","0");
        saveHelmetData("00000010",100,"반납","00000000","003","0","0");
        saveHelmetData("00000011",100,"반납","00000000","004","0","0");
        saveHelmetData("00000100",100,"반납","00000000","005","0","0");
        saveHelmetData("00000101",100,"반납","00000000","006","0","0");
        saveHelmetData("00000110",100,"반납","00000000","007","0","0");
        saveHelmetData("00000111",100,"반납","00000000","008","0","0");
        saveHelmetData("00001000",100,"반납","00000000","009","0","0");
        saveHelmetData("00001001",100,"반납","00000000","010","0","0");
        return view;
    }
    private void saveHelmetData(String id, int battery,String currentStatus, String returnStation,String returnSubStation, String userId, String rentalStartTime) {
        Map<String, Object> helmetData = new HashMap<>();
        helmetData.put("battery",battery);
        helmetData.put("currentStatus", currentStatus);
        helmetData.put("returnStation", returnStation);
        helmetData.put("returnSubStation", returnSubStation);
        helmetData.put("userId", userId);
        helmetData.put("rentalStartTime", rentalStartTime);

        db.child(id).setValue(helmetData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Helmet added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add helmet", Toast.LENGTH_SHORT).show());
    }
    private void startScanning() {
        barcodeView.resume();
    }

    private void handleResult(BarcodeResult result) {
        // QR 코드 스캔 결과를 처리합니다.
        String resultText = result.getText();

        if (resultText != null && resultText.startsWith("SAVE-THE-TTUK")) {
            // "SAVE-THE-TTUK"을 제외한 나머지 문자열 추출
            helmetId = resultText.substring("SAVE-THE-TTUK".length()).trim();

            // Firebase에서 헬멧 정보를 가져옴
            fetchHelmetData(helmetId);
        } else {
            // 오류 토스트 메시지 표시
            Toast.makeText(getContext(), "Invalid QR code.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchHelmetData(String helmetId) {
        db.child(helmetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // 데이터가 존재하는 경우
                    Map<String, Object> helmetData = (Map<String, Object>) dataSnapshot.getValue();
                    String id = (String) helmetData.get("id");
                    String currentStatus = (String) helmetData.get("currentStatus");
                    String returnStation = (String) helmetData.get("returnStation");
                    String userId = (String) helmetData.get("userId");
                    rentalStartTime = (String) helmetData.get("rentalStartTime");

                    if ("반납".equals(currentStatus)) {
                        updateHelmetData(id, returnStation);
                    } else {
                        Toast.makeText(getContext(), "Helmet is already rented.", Toast.LENGTH_SHORT).show();
                    }

                    resultTextView.setVisibility(View.VISIBLE);
                    resultTextView.setText(
                            "Helmet ID: " + id + "\n" +
                                    "Status: " + currentStatus + "\n" +
                                    "Return Station: " + returnStation + "\n" +
                                    "User ID: " + userId + "\n" +
                                    "Rental Start Time: " + rentalStartTime
                    );
                } else {
                    // 데이터가 없는 경우
                    Toast.makeText(getContext(), "Helmet data not found.", Toast.LENGTH_SHORT).show();
                }

                barcodeView.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 요청이 취소된 경우
                Toast.makeText(getContext(), "Failed to fetch helmet data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateHelmetData(String helmetId, String returnStationId) {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> updates = new HashMap<>();
        updates.put("currentStatus", "대여");
        updates.put("rentalStartTime", currentTime);
        updates.put("returnStation", "0");

        db.child(helmetId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Helmet rented successfully.", Toast.LENGTH_SHORT).show();
                    updateReturnStationStock(returnStationId);
                    rentalStartTime = currentTime;
                    isHelmetRented = true;
                    startRentalDurationUpdate();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to rent helmet.", Toast.LENGTH_SHORT).show());
    }

    private void updateReturnStationStock(String returnStationId) {
        DatabaseReference placeRef = FirebaseDatabase.getInstance().getReference("places").child(returnStationId);

        placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer currentStock = dataSnapshot.child("stock").getValue(Integer.class);
                    if (currentStock != null && currentStock > 0) {
                        placeRef.child("stock").setValue(currentStock - 1)
                                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Stock updated successfully.", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update stock.", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(getContext(), "No stock available to update.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Place data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch place data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processReturn(String helmetId, String returnStationId) {
        DatabaseReference placeRef = FirebaseDatabase.getInstance().getReference("places").child(returnStationId);

        placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("currentStatus", "반납");
                    updates.put("returnStation", returnStationId);
                    updates.put("rentalStartTime", "0");

                    db.child(helmetId).updateChildren(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Helmet returned successfully.", Toast.LENGTH_SHORT).show();
                                isHelmetRented = false;
                                stopRentalDurationUpdate();
                                updateReturnStationStockOnReturn(returnStationId);
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to return helmet.", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(getContext(), "Return station not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch return station data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateReturnStationStockOnReturn(String returnStationId) {
        DatabaseReference placeRef = FirebaseDatabase.getInstance().getReference("places").child(returnStationId);

        placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer currentStock = dataSnapshot.child("stock").getValue(Integer.class);
                    if (currentStock != null) {
                        placeRef.child("stock").setValue(currentStock + 1)
                                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Stock updated successfully.", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update stock.", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(getContext(), "Invalid stock value.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Place data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to fetch place data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startRentalDurationUpdate() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isHelmetRented) {
                    updateRentalDuration();
                    handler.postDelayed(this, 1000); // 1초마다 업데이트
                }
            }
        };
        handler.post(updateRunnable);
    }

    private void updateRentalDuration() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date startDate = sdf.parse(rentalStartTime);
            Date currentDate = new Date();

            if (startDate != null) {
                long duration = currentDate.getTime() - startDate.getTime();

                long seconds = (duration / 1000) % 60;
                long minutes = (duration / (1000 * 60)) % 60;
                long hours = (duration / (1000 * 60 * 60)) % 24;

                rentalDurationTextView.setText(String.format(Locale.getDefault(), "Rental Duration: %02d:%02d:%02d", hours, minutes, seconds));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRentalDurationUpdate() {
        handler.removeCallbacks(updateRunnable);
        rentalDurationTextView.setText("");
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(getContext(), "Camera permission is required to scan QR codes.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeView != null) {
            barcodeView.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeView != null) {
            barcodeView.resume();
        }
    }
}




