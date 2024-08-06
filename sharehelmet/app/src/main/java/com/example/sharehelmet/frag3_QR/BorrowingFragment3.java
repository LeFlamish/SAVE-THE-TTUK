package com.example.sharehelmet.frag3_QR;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharehelmet.BluetoothService;
import com.example.sharehelmet.R;
import com.example.sharehelmet.model.Helmet;
import com.example.sharehelmet.model.Storage;
import com.example.sharehelmet.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BorrowingFragment3 extends Fragment {
    private String storageId;
    private String firebaseId;
    private String helmetId;
    private User user;
    private Storage storage;
    private Helmet helmet;
    private TextView t11;
    private TextView t12;
    private TextView t13;
    private TextView t14;
    private DatabaseReference db;
    private LocalDateTime rentalStartTime;
    private Button returnButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_borrowing3, container, false);
        db = FirebaseDatabase.getInstance().getReference();
        t11 = view.findViewById(R.id.helmetId);
        t12 = view.findViewById(R.id.battery);
        t13 = view.findViewById(R.id.start_time);
        t14 = view.findViewById(R.id.using_time);
        Bundle bundle = getArguments();
        if (bundle != null) {
            firebaseId=bundle.getString("firebaseId");
        }


        db.child("users").child(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user != null) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    rentalStartTime= LocalDateTime.parse(user.getRental_info().get(1), formatter);
                    helmetId=user.getRental_info().get(0);
                    BorrowHelmet2();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });



        returnButton = view.findViewById(R.id.return_button);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setNow_qr(2);
                db.child("users").child(firebaseId).setValue(user);


                BarcodeEndFragment3 barcodeEndFragment3=new BarcodeEndFragment3();
                Bundle bundle = new Bundle();
                bundle.putString("firebaseId",firebaseId);
                barcodeEndFragment3.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, barcodeEndFragment3)
                        .addToBackStack(null)
                        .commit();
            }
        });
        return view;
    }


    private void BorrowHelmet2() {
        db.child("helmets").child(helmetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                helmet=snapshot.getValue(Helmet.class);
                helmet.setBorrow(true);
                helmet.setStorageId("-");
                helmet.setUserId(firebaseId);

                t11.setText(helmetId);


                t12.setText("100%");//나중에 수정


                t13.setText(user.getRental_info().get(1));


                db.child("helmets").child(helmetId).setValue(helmet);


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
}