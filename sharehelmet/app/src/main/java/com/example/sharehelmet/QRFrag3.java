package com.example.sharehelmet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QRFrag3 extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private DecoratedBarcodeView barcodeView;
    private TextView resultTextView;
    private DatabaseReference db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag3_qr, container, false);

        barcodeView = view.findViewById(R.id.barcode_scanner);
        resultTextView = view.findViewById(R.id.result_text_view);

        // Initialize Firebase Database
        db = FirebaseDatabase.getInstance().getReference("helmet");

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

        // Example data initialization (can be removed if not needed)
        // Uncomment below lines if you need to save example data
        /*
        saveHelmetData("00000000","반납","00000000","00000000","0000:00:00:00:00:00");
        saveHelmetData("00000001","반납","00000000","00000000","0000:00:00:00:00:00");
        saveHelmetData("00000010","반납","00000000","00000000","0000:00:00:00:00:00");
        saveHelmetData("00000011","반납","00000000","00000000","0000:00:00:00:00:00");
        saveHelmetData("00000100","반납","00000000","00000000","0000:00:00:00:00:00");
        saveHelmetData("00000101","반납","00000000","00000000","0000:00:00:00:00:00");
        saveHelmetData("00000110","반납","00000000","00000000","0000:00:00:00:00:00");
        saveHelmetData("00000111","반납","00000000","00000000","0000:00:00:00:00:00");
        */

        return view;
    }

    private void startScanning() {
        barcodeView.resume();
    }

    private void handleResult(BarcodeResult result) {
        // QR 코드 스캔 결과를 처리합니다.
        String resultText = result.getText();

        if (resultText != null && resultText.startsWith("SAVE-THE-TTUK")) {
            // "SAVE-THE-TTUK"을 제외한 나머지 문자열 추출
            String helmetId = resultText.substring("SAVE-THE-TTUK".length()).trim();

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
                    String rentalStartTime = (String) helmetData.get("rentalStartTime");

                    // 결과를 텍스트뷰에 표시
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

                // QR 코드 스캔을 중지하고 텍스트뷰를 표시합니다.
                barcodeView.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 데이터베이스 요청이 취소된 경우
                Toast.makeText(getContext(), "Failed to fetch helmet data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveHelmetData(String id, String currentStatus, String returnStation, String userId, String rentalStartTime) {
        Map<String, Object> helmetData = new HashMap<>();
        helmetData.put("id", id);
        helmetData.put("currentStatus", currentStatus);
        helmetData.put("returnStation", returnStation);
        helmetData.put("userId", userId);
        helmetData.put("rentalStartTime", rentalStartTime);

        db.child(id).setValue(helmetData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Helmet added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add helmet", Toast.LENGTH_SHORT).show());
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



