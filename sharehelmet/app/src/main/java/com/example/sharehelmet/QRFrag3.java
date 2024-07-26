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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.sharehelmet.model.Helmet;
import com.example.sharehelmet.model.Storage;
import com.example.sharehelmet.model.User;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class QRFrag3 extends Fragment {
    private User user;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private DecoratedBarcodeView barcodeView;
    private DatabaseReference db;
    private ConstraintLayout using_layout;
    private ConstraintLayout over_layout;
    private TextView t11;
    private TextView t12;
    private TextView t13;
    private TextView t14;
    private TextView t21;
    private TextView t22;
    private TextView t23;
    private LocalDateTime rentalStartTime; // 전역 변수로 대여 시작 시간 저장
    private Button returnButton;
    private Button overButton;// 반납 버튼
    private boolean isover=false;
    private boolean c1=false;
    HashMap<String, String> Record = new HashMap<>();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag3_qr, container, false);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable("user");
        }
        Record=user.getRecord();
        barcodeView = view.findViewById(R.id.barcode_scanner);
        using_layout = view.findViewById(R.id.using);
        over_layout = view.findViewById(R.id.over);
        t11 = view.findViewById(R.id.helmetId);
        t12 = view.findViewById(R.id.battery);
        t13 = view.findViewById(R.id.start_time);
        t14 = view.findViewById(R.id.using_time);
        t21 = view.findViewById(R.id.overhelmetId);
        t22 = view.findViewById(R.id.overtime);
        t23 = view.findViewById(R.id.overcharge);
        returnButton = view.findViewById(R.id.return_button); // 반납 버튼 초기화
        overButton = view.findViewById(R.id.returntostart);
        db = FirebaseDatabase.getInstance().getReference();

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

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startScanning();
        }

        // 반납 버튼 클릭 이벤트 설정
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isover=true;
                c1=false;
                handleReturn();
            }
        });
        overButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isover=false;
                barcodeView.setVisibility(View.VISIBLE);
                barcodeView.resume();
                using_layout.setVisibility(View.GONE);
                over_layout.setVisibility(View.GONE);
            }
        });
        /*saveHelmetData("001","001-001");
        saveHelmetData("002","001-002");
        saveHelmetData("003","001-003");
        saveHelmetData("004","001-004");
        saveHelmetData("005","001-005");
        saveHelmetData("006","001-006");
        saveHelmetData("007","001-007");
        saveHelmetData("008","001-008");
        saveHelmetData("009","001-009");
        saveHelmetData("010","001-010");
        saveHelmetData("011","001-011");
        saveHelmetData("012","001-012");
        saveHelmetData("013","001-013");
        saveHelmetData("014","001-014");
        saveHelmetData("015","001-015");
        saveHelmetData("016","001-016");
        saveHelmetData("017","001-017");
        saveHelmetData("018","001-018");
        saveHelmetData("019","001-019");
        saveHelmetData("020","001-020");*/

        return view;
    }



    private void startScanning() {
        barcodeView.resume();
    }

    private void handleResult(BarcodeResult result) {
        String resultText = result.getText();

        if (resultText != null && resultText.startsWith("SAVE-THE-TTUK ")) {
            String storageId = resultText.substring("SAVE-THE-TTUK ".length()).trim();
            if(isover){
                returnHelmet(storageId);
            }
            else {
                findHelmetData(storageId);
            }
        } else {
            Toast.makeText(getContext(), "Invalid QR code.", Toast.LENGTH_SHORT).show();
        }
    }
    private void returnHelmet(String storageId) {
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
                                if ("-".equals(storedHelmetID.get(helmetIndex))) {
                                    String helmetId = t11.getText().toString().substring(3); // 헬멧 ID 추출
                                    Toast.makeText(getContext(), "Returning Helmet ID: " + helmetId, Toast.LENGTH_SHORT).show();
                                    updateHelmetAndStockOnReturn(placeKey, helmetIndex, storage.getStock(), helmetId,storageId);
                                } else {
                                    Toast.makeText(getContext(), "Slot already occupied by another helmet.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "Invalid storage index.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "No storage data found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "No place found with the specified location ID.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to retrieve data from Firebase.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateHelmetAndStockOnReturn(String placeKey, int helmetIndex, int currentStock, String helmetId,String storageId) {
        db.child("places").child(placeKey).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Storage storage = mutableData.getValue(Storage.class);
                if (storage == null) {
                    return Transaction.success(mutableData);
                }

                int newStock = currentStock + 1;
                storage.setStock(newStock);

                List<String> storedHelmetID = storage.getStoredHelmetID();
                if (storedHelmetID != null && helmetIndex >= 0 && helmetIndex < storedHelmetID.size()) {
                    storedHelmetID.set(helmetIndex, helmetId);
                }

                mutableData.setValue(storage);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Toast.makeText(getContext(), "Failed to update place data", Toast.LENGTH_SHORT).show();
                } else {
                    updateHelmetDataOnReturn(helmetId,storageId);
                }
            }
        });
    }

    private void updateHelmetDataOnReturn(String helmetId,String storageId) {
        db.child("helmets").child(helmetId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Helmet helmet = mutableData.getValue(Helmet.class);
                if (helmet == null) {
                    return Transaction.success(mutableData);
                }
                LocalDateTime rentalEndTime = LocalDateTime.now(); // 대여 시작 시간 저장
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formattedEndTime = rentalEndTime.format(formatter);
                Record.put(t13.getText().toString(),formattedEndTime);

                helmet.setBorrow(false);
                helmet.setStorageId(storageId);
                helmet.setUserId("-");
                t21.setText(helmetId);
                t22.setText(t14.getText());
                t23.setText("0000원");
                mutableData.setValue(helmet);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Toast.makeText(getContext(), "Failed to update helmet data", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Helmet returned successfully", Toast.LENGTH_SHORT).show();
                    // 반납 완료 후 UI 업데이트
                    isover=false;
                    barcodeView.setVisibility(View.GONE);
                    barcodeView.pause();
                    using_layout.setVisibility(View.GONE);
                    over_layout.setVisibility(View.VISIBLE);
                    isover = false;
                }
            }
        });
    }


    private void findHelmetData(String storageId) {
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
                                    Toast.makeText(getContext(), "Helmet ID: " + helmetId, Toast.LENGTH_SHORT).show();
                                    updateHelmetAndStock(placeKey, helmetIndex, storage.getStock(), helmetId);
                                } else {
                                    Toast.makeText(getContext(), "No helmet found at the specified index.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "No helmet found at the specified index.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "No storage data found.", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "No place found with the specified location ID.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to retrieve data from Firebase.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateHelmetAndStock(String placeKey, int helmetIndex, int currentStock, String helmetId) {
        db.child("places").child(placeKey).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Storage storage = mutableData.getValue(Storage.class);
                if (storage == null) {
                    return Transaction.success(mutableData);
                }

                int newStock = currentStock - 1;
                storage.setStock(newStock);

                List<String> storedHelmetID = storage.getStoredHelmetID();
                if (storedHelmetID != null && helmetIndex >= 0 && helmetIndex < storedHelmetID.size()) {
                    storedHelmetID.set(helmetIndex, "-");
                }

                mutableData.setValue(storage);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Toast.makeText(getContext(), "Failed to update place data", Toast.LENGTH_SHORT).show();
                } else {
                    updateHelmetData(helmetId);
                }
            }
        });
    }

    private void updateHelmetData(String helmetId) {
        db.child("helmets").child(helmetId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Helmet helmet = mutableData.getValue(Helmet.class);
                if (helmet == null) {
                    return Transaction.success(mutableData);
                }

                helmet.setBorrow(true);
                helmet.setStorageId("-");
                helmet.setUserId("0000");

                rentalStartTime = LocalDateTime.now(); // 대여 시작 시간 저장
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formattedStartTime = rentalStartTime.format(formatter);
                t11.setText("No."+helmet.getHelmetId());
                t12.setText(helmet.getBatteryState()+"%");
                t13.setText(formattedStartTime); // 대여 시작 시간 텍스트 뷰 업데이트

                mutableData.setValue(helmet);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Toast.makeText(getContext(), "Failed to update helmet data", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Helmet data updated successfully", Toast.LENGTH_SHORT).show();
                    // 헬멧 데이터 업데이트 성공 후 레이아웃 전환
                    barcodeView.setVisibility(View.GONE);
                    barcodeView.pause();
                    using_layout.setVisibility(View.VISIBLE);
                    c1=true;
                    // 대여 시간 경과 업데이트
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            updateElapsedTime();
                            handler.postDelayed(this, 1000); // 1초마다 업데이트
                        }
                    }, 1000);
                }
            }
        });
    }

    private void updateElapsedTime() {
        if (rentalStartTime != null&&c1==true) {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(rentalStartTime, now);
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;
            String elapsedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
            t14.setText(elapsedTime); // 경과 시간 텍스트 뷰 업데이트
        }
    }

    private void handleReturn() {
        // QR 코드 스캔 화면으로 돌아가기 위한 설정
        using_layout.setVisibility(View.GONE);
        barcodeView.setVisibility(View.VISIBLE);
        barcodeView.resume();
        startScanning(); // QR 코드 스캔 시작
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
        barcodeView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();
    }
    private void saveHelmetData(String id, String storageID) {
        Helmet helmet = new Helmet();
        helmet.setHelmetId(id);
        helmet.setBatteryState(100);
        helmet.setBorrow(false);
        helmet.setStorageId(storageID);
        helmet.setUserId("-");

        db.child("helmets").child(id).setValue(helmet)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Helmet added successfully", Toast.LENGTH_SHORT).show();
                    updatePlaceData(storageID, id);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add helmet", Toast.LENGTH_SHORT).show());
    }

    private void updatePlaceData(String storageID, String helmetId) {
        String locationID = storageID.substring(0, 3);
        int helmetIndex = Integer.parseInt(storageID.substring(4, 7)) - 1;

        db.child("places").orderByChild("locationID").equalTo(locationID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                        String placeKey = placeSnapshot.getKey();
                        db.child("places").child(placeKey).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                Storage storage = mutableData.getValue(Storage.class);
                                if (storage == null) {
                                    return Transaction.success(mutableData);
                                }
                                int newStock = storage.getStock() + 1;
                                storage.setStock(newStock);
                                ArrayList<String> storedHelmetID = storage.getStoredHelmetID();
                                if (storedHelmetID != null && helmetIndex >= 0 && helmetIndex < storedHelmetID.size()) {
                                    storedHelmetID.set(helmetIndex, helmetId);
                                }
                                mutableData.setValue(storage);
                                return Transaction.success(mutableData);
                            }
                            @Override
                            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                                if (databaseError != null) {
                                    Toast.makeText(getContext(), "Failed to update place data", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Place data updated successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to update place data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

