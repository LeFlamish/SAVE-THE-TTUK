package com.example.sharehelmet.frag3_QR;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.sharehelmet.R;
import com.example.sharehelmet.model.Storage;
import com.example.sharehelmet.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BarcodeStartFragment3 extends Fragment {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private DecoratedBarcodeView barcodeView;
    private String firebaseId;
    private DatabaseReference db;
    private User user;
    private String storageId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barcode_start3, container, false);
        db = FirebaseDatabase.getInstance().getReference();
        barcodeView = view.findViewById(R.id.barcode_scanner);
        Bundle bundle = getArguments();
        if (bundle != null) {
            firebaseId=bundle.getString("firebaseId");
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            barcodeView.resume();
        }
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(Collections.singletonList(BarcodeFormat.QR_CODE)));
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                handleResult(result);
            }
            @Override
            public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {}
        });
        return view;
    }
    private void handleResult(BarcodeResult result) {
        String resultText = result.getText();
        if (resultText != null && resultText.startsWith("SAVE-THE-TTUK ")) {
            storageId = resultText.substring("SAVE-THE-TTUK ".length()).trim();
            db.child("users").child(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    user = snapshot.getValue(User.class);
                    if (user != null) {
                        barcodeView.pause();
                        BorrowHelmet1();
                        barcodeView.resume();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        } else {
            Toast.makeText(getContext(), "유효하지 않은 QR코드입니다", Toast.LENGTH_SHORT).show();
        }
    }
    private void BorrowHelmet1() {
        String locationID = storageId.substring(0, 3);
        int helmetIndex = Integer.parseInt(storageId.substring(4, 7)) - 1;
        db.child("places").orderByChild("locationID").equalTo(locationID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                        String placeKey = placeSnapshot.getKey();
                        Storage storage = placeSnapshot.getValue(Storage.class);
                        if (storage != null) {
                            List<String> storedHelmetID = storage.getStoredHelmetID();
                            if (storedHelmetID != null && helmetIndex >= 0 && helmetIndex < storedHelmetID.size()) {
                                String helmetId = storedHelmetID.get(helmetIndex);
                                if (!"-".equals(helmetId)) {

                                    //토스트 메세지
                                    Toast.makeText(getContext(), "No."+helmetId+" 헬멧 대여", Toast.LENGTH_SHORT).show();

                                    //places 파베 수정
                                    storedHelmetID.set(helmetIndex, "-");
                                    storage.setStock(storage.getStock()-1);
                                    db.child("places").child(placeKey).setValue(storage);

                                    //Users 파베 수정
                                    LocalDateTime rentalStartTime = LocalDateTime.now(); // 대여 시작 시간 저장
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                    String formattedStartTime = rentalStartTime.format(formatter);
                                    ArrayList<String> rental_info=new ArrayList<>();
                                    rental_info.add(helmetId);
                                    rental_info.add(formattedStartTime);
                                    user.setNow_qr(1);
                                    user.setRental_info(rental_info);
                                    db.child("users").child(firebaseId).setValue(user);

                                    //프래그먼트 이동
                                    BorrowingFragment3 borrowingFragment3=new BorrowingFragment3();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("firebaseId",firebaseId);
                                    borrowingFragment3.setArguments(bundle);
                                    getActivity().getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, borrowingFragment3)
                                            .addToBackStack(null)
                                            .commit();
                                }
                                else {
                                    Toast.makeText(getContext(), "보관함이 비어있습니다.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "보관함이 비어있습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "보관함 데이터를 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                } else {
                    Toast.makeText(getContext(), "보관함이 존재하지 않습니다", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "정보를 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }
}