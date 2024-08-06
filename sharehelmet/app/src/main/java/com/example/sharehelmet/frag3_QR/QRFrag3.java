package com.example.sharehelmet.frag3_QR;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.sharehelmet.BluetoothService;
import com.example.sharehelmet.R;
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
import java.util.Map;

public class QRFrag3 extends Fragment {
    private User user;
    String firebaseId;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private DecoratedBarcodeView barcodeView;
    private DatabaseReference db;
    private LinearLayout using_layout;
    private LinearLayout over_layout;
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
    private boolean isover = false;
    Map<String, String> hashMap = new HashMap<>();
    private Helmet helmet;
    private Storage storage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag3_qr, container, false);
        if (getArguments() != null) {
            firebaseId = getArguments().getString("firebaseId");
            if (firebaseId != null && isValidFirebaseId(firebaseId)) {
                loadDataFromDatabase(view);
            } else {
                Log.e("QRFrag3", "Invalid Firebase ID");
            }
        }

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
            barcodeView.resume();
        }

        // 반납 버튼 클릭 이벤트 설정
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isover = true;
                user.setNow_qr(2);
                db.child("users").child(firebaseId).setValue(user);
                barcodeView.setVisibility(View.VISIBLE);
                using_layout.setVisibility(View.GONE);
                over_layout.setVisibility(View.GONE);
                barcodeView.resume();
            }
        });
        overButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isover = false;
                user.setNow_qr(0);
                db.child("users").child(firebaseId).setValue(user);
                barcodeView.setVisibility(View.VISIBLE);
                using_layout.setVisibility(View.GONE);
                over_layout.setVisibility(View.GONE);
                barcodeView.resume();
            }
        });

        // 헬멧 데이터 저장
        /*saveHelmetData("001", "001-001");
        saveHelmetData("002", "001-002");
        saveHelmetData("003", "001-003");
        saveHelmetData("004", "001-004");
        saveHelmetData("005", "001-005");
        saveHelmetData("006", "001-006");
        saveHelmetData("007", "001-007");
        saveHelmetData("008", "001-008");
        saveHelmetData("009", "001-009");
        saveHelmetData("010", "001-010");
        saveHelmetData("011", "001-011");
        saveHelmetData("012", "001-012");
        saveHelmetData("013", "001-013");
        saveHelmetData("014", "001-014");
        saveHelmetData("015", "001-015");
        saveHelmetData("016", "001-016");
        saveHelmetData("017", "001-017");
        saveHelmetData("018", "001-018");
        saveHelmetData("019", "001-019");
        saveHelmetData("020", "001-020");*/

        return view;
    }
    private void loadDataFromDatabase(View rootView) {
        db = FirebaseDatabase.getInstance().getReference();
        db.child("users").child(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user != null) {
                    hashMap = user.getRecord();
                    initializeUI();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void initializeUI() {
        if (user.getNow_qr() == 0) {
            Borrow_Barcode();
        }
        else if (user.getNow_qr() == 1) {
            Borrow_Layout();
            t11.setText(user.getRental_info().get(0));
            t12.setText(user.getRental_info().get(1));
            t13.setText(user.getRental_info().get(2));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            rentalStartTime= LocalDateTime.parse(user.getRental_info().get(2), formatter);
            updateElapsedTime();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateElapsedTime();
                    handler.postDelayed(this, 1000); // 1초마다 업데이트
                }
            }, 1000);
        }
        else if (user.getNow_qr() == 2) {
            Return_Barcode();
            t11.setText(user.getRental_info().get(0));
            t12.setText(user.getRental_info().get(1));
            t13.setText(user.getRental_info().get(2));
        }
        else if (user.getNow_qr() == 3) {
            Return_Layout();
            t21.setText(user.getReturn_info().get(0));
            t22.setText(user.getReturn_info().get(1));
            t23.setText(user.getReturn_info().get(2));
        }
    }

    private void handleResult(BarcodeResult result) {
        String resultText = result.getText();
        if (resultText != null && resultText.startsWith("SAVE-THE-TTUK ")) {
            String storageId = resultText.substring("SAVE-THE-TTUK ".length()).trim();
            if(!isover){
                BorrowHelmet1(storageId);
            }
            else {
                ReturnHelmet1(storageId);
            }
        } else {
            Toast.makeText(getContext(), "유효하지 않은 QR코드입니다", Toast.LENGTH_SHORT).show();
        }
    }
    private void BorrowHelmet1(String storageId) {
        String locationID = storageId.substring(0, 3);
        int helmetIndex = Integer.parseInt(storageId.substring(4, 7)) - 1;
        db.child("places").orderByChild("locationID").equalTo(locationID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                        String placeKey = placeSnapshot.getKey();
                        Storage storage1 = placeSnapshot.getValue(Storage.class);
                        if (storage1 != null) {
                            List<String> storedHelmetID = storage1.getStoredHelmetID();
                            if (storedHelmetID != null && helmetIndex >= 0 && helmetIndex < storedHelmetID.size()) {
                                String helmetId = storedHelmetID.get(helmetIndex);
                                if (!"-".equals(helmetId)) {
                                    Toast.makeText(getContext(), "No."+helmetId+" 헬멧 대여", Toast.LENGTH_SHORT).show();
                                    Borrow_Layout();
                                    BorrowHelmet2(placeKey, helmetIndex, helmetId);
                                    Intent serviceIntent = new Intent(getActivity(), BluetoothService.class);
                                    serviceIntent.putExtra("BLUETOOTH_ADDRESS", "00:11:22:33:AA:BB");
                                    serviceIntent.putExtra("HelmetID",helmetId);
                                    getActivity().startService(serviceIntent);
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

    private void BorrowHelmet2(String placeKey, int helmetIndex, String helmetId) {
        db.child("places").child(placeKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storage =snapshot.getValue(Storage.class);
                storage.setStock(storage.getStock()-1);
                List<String> storedHelmetID = storage.getStoredHelmetID();
                if (storedHelmetID != null && helmetIndex >= 0 && helmetIndex < storedHelmetID.size()) {
                    storedHelmetID.set(helmetIndex, "-");
                }
                db.child("places").child(placeKey).setValue(storage);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


        db.child("helmets").child(helmetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                helmet =snapshot.getValue(Helmet.class);
                helmet.setBorrow(true);
                helmet.setStorageId("-");
                helmet.setUserId(firebaseId);
                rentalStartTime = LocalDateTime.now(); // 대여 시작 시간 저장
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formattedStartTime = rentalStartTime.format(formatter);
                t11.setText("No."+ helmet.getHelmetId());
                t12.setText(helmet.getBatteryState()+"%");
                t13.setText(formattedStartTime);
                db.child("helmets").child(helmetId).setValue(helmet);
                ArrayList<String> rental_info=new ArrayList<>();
                rental_info.add(t11.getText().toString());
                rental_info.add(t12.getText().toString());
                rental_info.add(t13.getText().toString());
                user.setRental_info(rental_info);
                user.setNow_qr(1);
                db.child("users").child(firebaseId).setValue(user);

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateElapsedTime();
                        handler.postDelayed(this, 1000); // 1초마다 업데이트
                    }
                }, 1000);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    private void ReturnHelmet1(String storageId) {
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
                                    Toast.makeText(getContext(), "No."+helmetId+" 헬멧 반납", Toast.LENGTH_SHORT).show();
                                    Return_Layout();
                                    ReturnHelmet2(placeKey, helmetIndex, helmetId,storageId);
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
                    Toast.makeText(getContext(), "보관합이 존재하지 않습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "정보를 불러오지 못했습니다", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ReturnHelmet2(String placeKey, int helmetIndex, String helmetId,String storageId) {
        db.child("places").child(placeKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storage=snapshot.getValue(Storage.class);
                storage.setStock(storage.getStock()+1);
                List<String> storedHelmetID = storage.getStoredHelmetID();
                if (storedHelmetID != null && helmetIndex >= 0 && helmetIndex < storedHelmetID.size()) {
                    storedHelmetID.set(helmetIndex, helmetId);
                }
                db.child("places").child(placeKey).setValue(storage);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        db.child("helmets").child(helmetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                helmet=snapshot.getValue(Helmet.class);
                helmet.setBorrow(false);
                helmet.setStorageId(storageId);
                helmet.setUserId("-");
                db.child("helmets").child(helmetId).setValue(helmet);

                LocalDateTime rentalEndTime = LocalDateTime.now(); // 대여 시작 시간 저장
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String formattedEndTime = rentalEndTime.format(formatter);
                hashMap.put(t13.getText().toString(),formattedEndTime);
                user.setRecord(hashMap);
                t21.setText(helmetId);
                t22.setText(t14.getText());

                String[] parts = t14.getText().toString().split(":");
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int totalMinute=hours*60+minutes;
                int totalPrice=300+totalMinute*50;
                t23.setText(totalPrice+"원");

                user.setMoney(user.getMoney()-totalPrice);
                user.setNow_qr(3);
                ArrayList<String> return_info=new ArrayList<>();

                return_info.add(t21.getText().toString());
                return_info.add(t22.getText().toString());
                return_info.add(t23.getText().toString());
                user.setReturn_info(return_info);

                db.child("users").child(firebaseId).setValue(user);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }







    private void updateElapsedTime() {
        if (rentalStartTime != null) {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(rentalStartTime, now);
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;
            String elapsedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
            t14.setText(elapsedTime);
        }
    }
    public void Borrow_Barcode(){
        isover=false;
        barcodeView.setVisibility(View.VISIBLE);
        using_layout.setVisibility(View.GONE);
        over_layout.setVisibility(View.GONE);
        barcodeView.resume();
    }
    public void Borrow_Layout(){
        isover=false;
        barcodeView.setVisibility(View.GONE);
        using_layout.setVisibility(View.VISIBLE);
        over_layout.setVisibility(View.GONE);
        barcodeView.pause();
    }
    public void Return_Barcode(){
        isover=true;
        barcodeView.setVisibility(View.VISIBLE);
        using_layout.setVisibility(View.GONE);
        over_layout.setVisibility(View.GONE);
        barcodeView.resume();
    }
    public void Return_Layout(){
        isover=true;
        barcodeView.setVisibility(View.GONE);
        using_layout.setVisibility(View.GONE);
        over_layout.setVisibility(View.VISIBLE);
        barcodeView.pause();
    }
    private boolean isValidFirebaseId(String firebaseId) {
        return !(firebaseId.contains(".") || firebaseId.contains("#") ||
                firebaseId.contains("$") || firebaseId.contains("[") || firebaseId.contains("]"));
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {barcodeView.resume();}
            else {Toast.makeText(getContext(), "QR 스캔을 위해 카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();}
        }
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
    }
}


