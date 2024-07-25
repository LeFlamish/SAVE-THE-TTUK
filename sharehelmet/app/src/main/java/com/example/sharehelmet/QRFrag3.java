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
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Collections;
import java.util.List;

public class QRFrag3 extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private DecoratedBarcodeView barcodeView;
    private TextView resultTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag3_qr, container, false);

        barcodeView = view.findViewById(R.id.barcode_scanner);
        resultTextView = view.findViewById(R.id.result_text_view);

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

        return view;
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

    private void startScanning() {
        barcodeView.resume();
    }

    private void handleResult(BarcodeResult result) {
        // QR 코드 스캔 결과를 처리합니다.
        String resultText = result.getText();

        if (resultText != null && resultText.startsWith("SAVE-THE-TTUK")) {
            // "SAVE-THE-TTUK"을 제외한 나머지 문자열 추출
            String displayText = resultText.substring("SAVE-THE-TTUK".length()).trim();

            // QR 코드 스캔을 중지하고 텍스트뷰를 표시합니다.
            barcodeView.setVisibility(View.GONE);
            resultTextView.setVisibility(View.VISIBLE);
            resultTextView.setText(displayText);
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


