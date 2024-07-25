package com.example.sharehelmet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    Button loginBtn, signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        setViews();
    }



    protected void setViews(){
        loginBtn = findViewById(R.id.loginbtn);
        signupBtn = findViewById(R.id.signupbtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO : 로그인 기능 구현하기
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        signupBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //TODO : 회원가입 액티비티 만들어서 연결짓기
                Toast.makeText(LoginActivity.this, "회원가입 액티비티 작업 예정", Toast.LENGTH_SHORT).show();
            }
        });
    }


}