package com.example.sharehelmet.frag5_profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sharehelmet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileUpdateActivity extends AppCompatActivity {
    FirebaseUser firebaseuser = FirebaseAuth.getInstance().getCurrentUser();
    Button changeAccount;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);
        //TODO : 파이어베이스 연동해서 데이터 수정하기
        loadDataFromDatabase();
        updateUI();
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(0, R.anim.horizontal_exit);
    }

    protected void loadDataFromDatabase(){

    }
    protected void updateUI(){
        changeAccount = findViewById(R.id.update_btn);
        backButton = findViewById(R.id.back_button);

        changeAccount.setOnClickListener(v -> {
            showCustomToast("프로필 수정 기능 구현 전입니다");
        });
        backButton.setOnClickListener(v->onBackPressed());
    }
    protected void showCustomToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        TextView text = layout.findViewById(R.id.toast_text);
        text.setText(message);

        //ImageView image = layout.findViewById(R.id.toast_images);
        //image.setImageResource(R.drawable.logo01); // 원하는 아이콘 리소스 설정

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
}
