package com.example.sharehelmet.frag3_QR;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.sharehelmet.R;
import com.example.sharehelmet.model.Helmet;
import com.example.sharehelmet.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

public class ResultFragment3 extends Fragment {
    private String storageId;
    private String firebaseId;
    private String helmetId;
    private User user;
    private Helmet helmet;
    private TextView t21;
    private TextView t22;
    private TextView t23;
    private DatabaseReference db;
    private Button overButton;
    Map<String, List<String>> hashMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result3, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            firebaseId=bundle.getString("firebaseId");
        }
        db = FirebaseDatabase.getInstance().getReference();
        db.child("users").child(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user != null) {
                    hashMap = user.getRecord();
                    helmetId=user.getReturn_info().get(0);
                    storageId=user.getReturn_info().get(3);
                    ReturnHelmet2();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
        t21 = view.findViewById(R.id.overhelmetId);
        t22 = view.findViewById(R.id.overtime);
        t23 = view.findViewById(R.id.overcharge);
        overButton = view.findViewById(R.id.returntostart);
        overButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setNow_qr(0);
                db.child("users").child(firebaseId).setValue(user);

                //프래그먼트 이동
                BarcodeStartFragment3 barcodeStartFragment3=new BarcodeStartFragment3();
                Bundle bundle = new Bundle();
                bundle.putString("firebaseId",firebaseId);
                barcodeStartFragment3.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, barcodeStartFragment3)
                        .commit();
            }
        });
        return view;
    }
    private void ReturnHelmet2() {
        db.child("helmets").child(helmetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //helmets 파베 수정
                helmet=snapshot.getValue(Helmet.class);
                helmet.setBorrow(false);
                helmet.setStorageId(storageId);
                helmet.setUserId("-");
                db.child("helmets").child(helmetId).setValue(helmet);

                //텍스트뷰 표시
                t21.setText(user.getReturn_info().get(0));
                t22.setText(user.getReturn_info().get(1));
                t23.setText(user.getReturn_info().get(2));

                //users 파베 수정
                LocalDateTime rentalEndTime = LocalDateTime.now(); // 대여 시작 시간 저장
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault());
                String formattedEndTime = rentalEndTime.format(formatter);

                ArrayList<String> result=new ArrayList<>();
                result.add(user.getRental_info().get(1).split(" ")[1]);
                result.add(formattedEndTime);
                result.add(user.getReturn_info().get(0));
                result.add(user.getReturn_info().get(1));
                result.add(user.getReturn_info().get(2));

                hashMap.put(user.getRental_info().get(1).split(" ")[0],result);
                user.setRecord(hashMap);
                db.child("users").child(firebaseId).setValue(user);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}