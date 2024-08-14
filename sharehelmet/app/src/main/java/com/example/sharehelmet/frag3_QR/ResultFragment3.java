package com.example.sharehelmet.frag3_QR;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.sharehelmet.R;
import com.example.sharehelmet.frag2_payment.TotalPaymentActivity;
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
import java.util.HashMap;
import java.util.List;
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result3, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            firebaseId = bundle.getString("firebaseId");
        }
        db = FirebaseDatabase.getInstance().getReference();

        t21 = view.findViewById(R.id.overhelmetId);
        t22 = view.findViewById(R.id.overtime);
        t23 = view.findViewById(R.id.overcharge);
        overButton = view.findViewById(R.id.returntostart);
        TextView askButton = view.findViewById(R.id.ask_button);
        Button returnToChargeButton = view.findViewById(R.id.returntocharge);  // 추가된 버튼

        // 고객센터 문의하기 버튼 클릭 리스너 설정
        askButton.setOnClickListener(askButtonClickListener);

        // Firebase에서 사용자 정보를 가져옴
        db.child("users").child(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user != null) {
                    // 만약 user.getMoney()가 음수이면 overButton을 비활성화하고 returnToChargeButton을 visible로 설정
                    if (user.getMoney() < 0) {

                        overButton.setEnabled(false);  // 버튼 비활성화
                        overButton.setBackground(getResources().getDrawable(R.drawable.custom_ripple_effect3));

                        returnToChargeButton.setVisibility(View.VISIBLE);  // 충전하기 버튼 표시
                    } else {
                        overButton.setEnabled(true);  // 버튼 활성화
                        returnToChargeButton.setVisibility(View.GONE);  // 충전하기 버튼 숨김
                    }

                    helmetId = user.getReturn_info().get(0);
                    storageId = user.getReturn_info().get(3);
                    ReturnHelmet2();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        overButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.setNow_qr(0);
                db.child("users").child(firebaseId).setValue(user);

                // 프래그먼트 이동
                BarcodeStartFragment3 barcodeStartFragment3 = new BarcodeStartFragment3();
                Bundle bundle = new Bundle();
                bundle.putString("firebaseId", firebaseId);
                barcodeStartFragment3.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, barcodeStartFragment3)
                        .commit();
            }
        });

        // 충전하기 버튼 클릭 리스너 설정
        returnToChargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TotalPaymentActivity.class);
                startActivity(intent);
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


            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // 클래스 내부에 클릭 리스너를 정의
    private View.OnClickListener askButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 여기에 고객센터 문의하기 동작을 추가하세요.
            // 예를 들어, 고객센터로 전화 연결 또는 다른 액티비티로 이동 등을 추가할 수 있습니다.

            String url = "http://pf.kakao.com/_KxnEyG/chat";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    };

}