package com.example.sharehelmet.frag2_payment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sharehelmet.CuponAdapter;
import com.example.sharehelmet.R;
import com.example.sharehelmet.model.Cupon;
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
import java.util.Map;


public class PaymentFrag2 extends Fragment  {
    String firebaseId;
    DatabaseReference mDatabaseRef;
    private User user;
    private int money;

    ImageView paymentIcons;
    EditText promotionCodeEdit;
    TextView promotionButton;
    TextView money_textview;
    ListView cuponList;

    Map<String, Object> userCupon = new HashMap<>();
    Map<String, Object> totalCupon = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag2_payment, container, false);
        money_textview=view.findViewById(R.id.beam_credits_amount);
        if (getArguments() != null) {
            firebaseId = getArguments().getString("firebaseId");
            loadDataFromDatabase();
        }

        paymentIcons = view.findViewById(R.id.payment_icons);
        promotionCodeEdit = view.findViewById(R.id.promotion_code_edit);
        promotionButton = view.findViewById(R.id.promotion_button);
        cuponList = view.findViewById(R.id.cupon_list);

        setViews();//클릭리스너 설정

        return view;
    }

    protected void setViews(){
        paymentIcons.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TotalPaymentActivity.class);
            intent.putExtra("firebaseId",firebaseId);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.horizontal_enter,R.anim.none);
        });

        //쿠폰 등록
        promotionButton.setOnClickListener(v->{
            String codeText;
            codeText = promotionCodeEdit.getText().toString().trim();
            if(codeText.isEmpty()){
                showCustomToast("쿠폰 코드를 입력해주세요");
                return;
            }
            if(userCupon.containsKey(codeText)){
                showCustomToast("이미 보유한 쿠폰입니다");
                return;
            }
            else if (!totalCupon.containsKey(codeText)){
                showCustomToast("쿠폰이 존재하지 않습니다");
                return;
            }
            userCupon.put(codeText, totalCupon.get(codeText));
            user.setCupons(userCupon);
            mDatabaseRef.child("users").child(firebaseId).setValue(user)
                    .addOnSuccessListener(aVoid -> showCustomToast("쿠폰이 등록되었습니다"))
                    .addOnFailureListener(e -> Log.e("PaymentFrag2", "Failed to update user data.", e));
        });

    }


    protected void loadDataFromDatabase(){
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        //유저와 유저 쿠폰 리스트 가져오기
        mDatabaseRef.child("users").child(firebaseId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue(User.class) != null){
                    user = snapshot.getValue(User.class);
                    userCupon = user.getCupons();
                    money_textview.setText(String.valueOf(user.getMoney()));
                    updateUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PaymentFrag2", "Failed to read user data.", error.toException());
            }
        });

        //전체 쿠폰 리스트 가져오기
        mDatabaseRef.child("cuponList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue() != null){
                    totalCupon = (Map<String, Object>)snapshot.getValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PaymentFrag2", "Failed to read data.", error.toException());
            }
        });
    }


    protected void updateUI() {
        if(userCupon == null || userCupon.isEmpty()) return;

        List<Cupon> cuponListData = new ArrayList<>();

        for(String key : userCupon.keySet()){
            Map<String, Object> cuponData = (Map<String, Object>) userCupon.get(key);
            Cupon tempCupon = new Cupon(
                    (String) cuponData.get("code"),
                    (String) cuponData.get("name"),
                    ((Long) cuponData.get("value")).intValue()
            );
            cuponListData.add(tempCupon);
        }
        Context context = getContext();
        if (context == null) {
            Log.e("PaymentFrag2", "Context is null");
            return;
        }
        CuponAdapter adapter = new CuponAdapter(context, cuponListData);
        cuponList.setAdapter(adapter);
    }


    protected void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(getContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }


}


/*
//쿠폰을 임시등록,
  //mDatabaseRef = FirebaseDatabase.getInstance().getReference();
            String tempCodeText;
            tempCodeText = promotionCodeEdit.getText().toString();
            Cupon cupon = new Cupon(tempCodeText,"SAMPLE2", 2000);
            mDatabaseRef.child("cuponList").child(tempCodeText).setValue(cupon);
 */