package com.example.sharehelmet.frag3_QR;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.example.sharehelmet.R;
import com.example.sharehelmet.model.Storage;
import com.example.sharehelmet.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BarcodeEndFragment3 extends Fragment {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private DecoratedBarcodeView barcodeView;
    private String firebaseId;
    private DatabaseReference db;
    private User user;
    private String storageId;
    private LocalDateTime rentalStartTime;
    String helmetId;
    Button back_button;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barcode_end3, container, false);
        db = FirebaseDatabase.getInstance().getReference();
        barcodeView = view.findViewById(R.id.barcode_scanner);
        back_button=view.findViewById(R.id.back);
        Bundle bundle = getArguments();
        if (bundle != null) {
            firebaseId=bundle.getString("firebaseId");
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            barcodeView.resume();
        }
        db.child("users").child(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setNow_qr(1);
                db.child("users").child(firebaseId).setValue(user);

                //프래그먼트 이동
                BorrowingFragment3 borrowingFragment3=new BorrowingFragment3();
                Bundle bundle = new Bundle();
                bundle.putString("firebaseId",firebaseId);
                borrowingFragment3.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, borrowingFragment3)
                        .commit();
            }
        });
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(Collections.singletonList(BarcodeFormat.QR_CODE)));
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                handleResult(result);
            }
            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
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
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        rentalStartTime= LocalDateTime.parse(user.getRental_info().get(1), formatter);
                        helmetId = user.getRental_info().get(0);
                        barcodeView.pause();
                        ReturnHelmet1();
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
    private void ReturnHelmet1() {
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
                            ArrayList<String> storedHelmetID = storage.getStoredHelmetID();
                            if (storedHelmetID != null && helmetIndex >= 0 && helmetIndex < storedHelmetID.size()) {
                                if ("-".equals(storedHelmetID.get(helmetIndex))) {

                                    //토스트 메세지
                                    Toast.makeText(getContext(), "No."+helmetId+" 헬멧 반납", Toast.LENGTH_SHORT).show();

                                    //places 파베 수정
                                    storedHelmetID.set(helmetIndex, helmetId);
                                    storage.setStoredHelmetID(storedHelmetID);
                                    storage.setStock(storage.getStock()+1);
                                    db.child("places").child(placeKey).setValue(storage);

                                    //users 파베 수정
                                    ArrayList<String> return_info=new ArrayList<>();
                                    return_info.add(helmetId);
                                    return_info.add(updateElapsedTime());
                                    return_info.add(String.valueOf(calculateMoney()));
                                    return_info.add(storageId);
                                    user.setNow_qr(3);
                                    user.setReturn_info(return_info);
                                    db.child("users").child(firebaseId).setValue(user);

                                    //프래그먼트 이동
                                    ResultFragment3 resultFragment3=new ResultFragment3();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("firebaseId",firebaseId);
                                    resultFragment3.setArguments(bundle);
                                    getActivity().getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, resultFragment3)
                                            .commit();
                                } else {
                                    Toast.makeText(getContext(), "빈 보관함이 아닙니다.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "유효하지 않은 보관소 번호입니다", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "보관함 데이터를 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
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
    private String updateElapsedTime() {
        if (rentalStartTime != null) {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(rentalStartTime, now);
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        }
        return null;
    }
    private int calculateMoney(){
        String[] parts = updateElapsedTime().split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int totalMinute=hours*60+minutes;
        return 300+totalMinute*50;
    }
}