package com.example.sharehelmet.frag2_payment;

import static java.lang.Integer.parseInt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sharehelmet.R;
import com.example.sharehelmet.model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skydoves.expandablelayout.ExpandableLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.bootpay.android.Bootpay;
import kr.co.bootpay.android.events.BootpayEventListener;
import kr.co.bootpay.android.models.BootExtra;
import kr.co.bootpay.android.models.BootItem;
import kr.co.bootpay.android.models.BootUser;
import kr.co.bootpay.android.models.Payload;

public class TotalPaymentActivity extends AppCompatActivity {
//    BootpayWebView bootpayWebView;
    String firebaseId;
    DatabaseReference mDatabaseRef;
    private User user;
    TextInputEditText moneySelect;
    Button startPayment;
    ImageView backButton;
    int userMoney;
    int chargeMoney;
    ExpandableLayout expandable;

    private TextInputEditText virtualMoneyEdit;
    private Button virtualStartPaymentButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_payment);

        Intent intent = getIntent();
        if (intent != null) {
            firebaseId = intent.getStringExtra("firebaseId");
            if (firebaseId != null && isValidFirebaseId(firebaseId)) {
                loadDataFromDatabase();
            } else {
                Log.e("paymentActivity", "Invalid Firebase ID");
            }
        }

        moneySelect = findViewById(R.id.money_select_edit);
        startPayment = findViewById(R.id.start_payment);
        backButton = findViewById(R.id.back_button);
        expandable=findViewById(R.id.expandable1);

        expandable.getParentLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandable.toggleLayout();
            }
        });





        // layout_second1을 inflate하고 findViewById를 사용
        LayoutInflater inflater = LayoutInflater.from(this);
//        View secondLayoutView = inflater.inflate(R.layout.layout_second1, null);
        View secondLayoutView = expandable.getSecondLayout();

        virtualMoneyEdit = secondLayoutView.findViewById(R.id.virtual_money_select_edit);
        virtualStartPaymentButton = secondLayoutView.findViewById(R.id.virtual_start_payment);

        virtualStartPaymentButton.setOnClickListener(v -> processVirtualPayment());
        Log.d("ch","gharg2");


        setViews();
    }

    private void processVirtualPayment() {
        Log.d("ch","gharg");
        try {
            // 입력된 금액 가져오기
            String inputMoneyStr = virtualMoneyEdit.getText().toString();
            int inputMoney = Integer.parseInt(inputMoneyStr);

            // 사용자의 기존 금액에 입력된 금액 더하기
            int updatedMoney = user.getMoney() + inputMoney;

            // Firebase에 업데이트
            mDatabaseRef.child("users").child(firebaseId).child("money").setValue(updatedMoney)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(TotalPaymentActivity.this, "금액이 성공적으로 추가되었습니다.", Toast.LENGTH_SHORT).show();
                            // 사용자 객체의 money 필드 업데이트
                            user.setMoney(updatedMoney);
                        } else {
                            Toast.makeText(TotalPaymentActivity.this, "금액 추가에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(TotalPaymentActivity.this, "유효한 숫자를 입력하세요.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(0, R.anim.horizontal_exit);
    }


    private boolean isValidFirebaseId(String firebaseId) {
        return !(firebaseId.contains(".") || firebaseId.contains("#") ||
                firebaseId.contains("$") || firebaseId.contains("[") || firebaseId.contains("]"));
    }


    protected void setViews(){
        startPayment.setOnClickListener(v->{
            try{
                chargeMoney = parseInt(moneySelect.getText().toString());
            }
            catch(Exception e){
                showCustomToast("유효한 숫자를 입력해주세요");
            }
            PaymentTest(v);
        });
        backButton.setOnClickListener(v -> onBackPressed());

    }

    protected void loadDataFromDatabase(){
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef.child("users").child(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user != null) {
                    userMoney = user.getMoney();
                } else {
                    Log.e("paymentActivity", "User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("paymentActivity", "Failed to read user data", error.toException());
            }
        });
    }


    public void PaymentTest(View v) {
        BootUser user = new BootUser().setPhone("010-1234-5678"); // 구매자 정보

        BootExtra extra = new BootExtra()
                .setCardQuota("0,2,3"); // 일시불, 2개월, 3개월 할부 허용, 할부는 최대 12개월까지 사용됨 (5만원 이상 구매시 할부허용 범위)


        List<BootItem> items = new ArrayList<>();
        BootItem item1 = new BootItem().setName("포인트 결제").setId("ITEM_CODE_MOUSE").setQty(1).setPrice((double)chargeMoney);
        items.add(item1);

        Payload payload = new Payload();
        payload.setApplicationId("5b8f6a4d396fa665fdc2b5e8")
                .setOrderName("부트페이 결제테스트")
                .setOrderId("1234")
                .setPrice((double)chargeMoney)
                .setUser(user)
                .setExtra(extra)
                .setItems(items);

        Map<String, Object> map = new HashMap<>();
        map.put("1", "abcdef");
        map.put("2", "abcdef55");
        map.put("3", 1234);
        payload.setMetadata(map);
//        payload.setMetadata(new Gson().toJson(map));

        Bootpay.init(getSupportFragmentManager(), getApplicationContext())
                .setPayload(payload)
                .setEventListener(new BootpayEventListener() {
                    @Override
                    public void onCancel(String data) {
                        Log.d("bootpay", "cancel: " + data);
                    }

                    @Override
                    public void onError(String data) {
                        Log.d("bootpay", "error: " + data);
                    }

                    @Override
                    public void onClose() {
                        Bootpay.removePaymentWindow();
                    }

                    @Override
                    public void onIssued(String data) {
                        Log.d("bootpay", "issued: " +data);
                    }

                    @Override
                    public boolean onConfirm(String data) {
                        Log.d("bootpay", "confirm: " + data);
//                        Bootpay.transactionConfirm(data); //재고가 있어서 결제를 진행하려 할때 true (방법 1)
                        return true; //재고가 있어서 결제를 진행하려 할때 true (방법 2)
//                        return false; //결제를 진행하지 않을때 false
                    }

                    @Override
                    public void onDone(String data) {
                        Log.d("done", data);
                    }


                }).requestPayment();
    }

    protected void showCustomToast(String message) {

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

    }
}