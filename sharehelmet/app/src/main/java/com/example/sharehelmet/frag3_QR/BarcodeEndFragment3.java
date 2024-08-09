package com.example.sharehelmet.frag3_QR;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.sharehelmet.R;
import com.example.sharehelmet.model.Storage;
import com.example.sharehelmet.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class BarcodeEndFragment3 extends Fragment {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private DecoratedBarcodeView barcodeView;
    private String firebaseId;
    private DatabaseReference db;
    private User user;
    private String storageId;
    private LocalDateTime rentalStartTime;
    private Context mContext;
    String helmetId;
    Button back_button;
    private ImageButton turnOnLight, writeCode;
    private boolean isFlashOn = false;
    private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{3}\\d{3}$");
    private FusedLocationProviderClient fusedLocationClient;
    private String insteadQrResult;
    Map<String, List<String>> hashMap = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barcode_end3, container, false);
        db = FirebaseDatabase.getInstance().getReference();
        barcodeView = view.findViewById(R.id.barcode_scanner);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
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
                handleResult(result.getText());
            }
            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        });

        turnOnLight = view.findViewById(R.id.turn_on_light);
        turnOnLight.setOnClickListener(v -> toggleFlashLight());

        writeCode = view.findViewById(R.id.write_code);
        writeCode.setOnClickListener(v -> showEditTextDialog());
        return view;
    }
    private void toggleFlashLight() {
        if (barcodeView != null) {
            if (isFlashOn) {
                barcodeView.setTorchOff();
                isFlashOn = false;
            } else {
                barcodeView.setTorchOn();
                isFlashOn = true;
            }
        }
    }
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return distance * 1000;
    }
    public void showEditTextDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_text, null);
        builder.setView(dialogView);
        barcodeView.pause();
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText editText = dialogView.findViewById(R.id.editText);
                String inputText = editText.getText().toString().trim();
                handleDialogInput(inputText);
                dialog.cancel();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                barcodeView.resume();
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void handleDialogInput(String input) {
        if (!CODE_PATTERN.matcher(input).matches()) {
            showCustomToast("코드 형식이 맞지 않습니다");
            return;
        }
        insteadQrResult = "SAVE-THE-TTUK " + input.substring(0,3)+"-"+input.substring(3,6);
        handleResult(insteadQrResult);
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    private void handleResult(String resultText) {
        if (resultText != null && resultText.startsWith("SAVE-THE-TTUK ")) {
            storageId = resultText.substring("SAVE-THE-TTUK ".length()).trim();
            db.child("users").child(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (isAdded()) {
                        user = snapshot.getValue(User.class);
                        if (user != null) {
                            barcodeView.pause();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            rentalStartTime= LocalDateTime.parse(user.getRental_info().get(1), formatter);
                            helmetId = user.getRental_info().get(0);
                            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        double myLatitude = location.getLatitude();
                                        double myLongitude = location.getLongitude();
                                        ReturnHelmet1(myLatitude, myLongitude);
                                    } else {
                                        showCustomToast("현재 위치를 가져올 수 없습니다");
                                    }
                                }
                            });
                            barcodeView.resume();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showCustomToast("정보를 불러오지 못했습니다");
                }
            });
        } else {
            showCustomToast("유효하지 않은 QR 코드입니다");
        }
    }
    private void ReturnHelmet1(double myLatitude, double myLongitude) {
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
                            if(haversine(myLatitude, myLongitude, storage.getLatitude(), storage.getLongitude())<=5*100000000){
                                ArrayList<String> storedHelmetID = storage.getStoredHelmetID();
                                if (storedHelmetID != null && helmetIndex >= 0 && helmetIndex < storedHelmetID.size()) {
                                    if ("-".equals(storedHelmetID.get(helmetIndex))) {

                                        //토스트 메세지
                                        showCustomToast("No."+helmetId+" 헬멧 반납");

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
                                        user.setMoney(user.getMoney()-Integer.parseInt(String.valueOf(calculateMoney())));

                                        db.child("users").child(firebaseId).setValue(user);

                                        LocalDateTime rentalEndTime = LocalDateTime.now(); // 대여 시작 시간 저장
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault());
                                        String formattedEndTime = rentalEndTime.format(formatter);

                                        ArrayList<String> result=new ArrayList<>();
                                        result.add(user.getRental_info().get(1).split(" ")[1]);
                                        result.add(formattedEndTime);
                                        result.add(user.getReturn_info().get(0));
                                        result.add(user.getReturn_info().get(1));
                                        result.add(user.getReturn_info().get(2));
                                        result.add(user.getRental_info().get(2));
                                        result.add(locationID);
                                        hashMap=user.getRecord();
                                        hashMap.put(user.getRental_info().get(1),result);
                                        user.setRecord(hashMap);
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
                                        showCustomToast("빈 보관함이 아닙니다");
                                    }
                                } else {
                                    showCustomToast("유효하지 않은 보관소 번호입니다");
                                }
                            }else{
                                showCustomToast("보관함이 너무 멀리 있습니다");
                            }
                        } else {
                            showCustomToast("보관함 데이터를 찾지 못했습니다");
                        }
                    }
                } else {
                    showCustomToast("보관함이 존재하지 않습니다");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                showCustomToast("정보를 불러오지 못했습니다");
            }
        });
    }
    private String updateElapsedTime() {
        if (rentalStartTime != null) {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(rentalStartTime, now);
            long minutes = duration.toMinutes();
            long seconds = duration.getSeconds() % 60;
            return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        }
        return null;
    }
    private int calculateMoney(){
        String[] parts = updateElapsedTime().split(":");
        int minutes = Integer.parseInt(parts[0]);
        return 300+minutes*50;
    }
    private void showCustomToast(String message) {
        Context context = getContext();
        if (context == null) {
            context = getActivity();
        }

        if (context != null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.custom_toast, null);

            TextView text = layout.findViewById(R.id.toast_text);
            text.setText(message);

            Toast toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
        } else {
            Log.e("MyFragment", "Context is null, cannot show Toast");
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}