package com.example.sharehelmet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;         //파이어 베이스 인증
    DatabaseReference mDatabaseRef;     //실시간 데이터 베이스
    EditText editMAIL, editPW;
    Button loginBtn, signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        setViews();
    }



    protected void setViews(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        editMAIL = findViewById(R.id.editMAIL);
        editPW = findViewById(R.id.editPW);
        loginBtn = findViewById(R.id.loginbtn);
        signupBtn = findViewById(R.id.signupbtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String message = "일단 입력 안 하고도 메인이랑 연결되게 해둠";
                //showCustomToast(message);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

/*
                String userMAIL = editMAIL.getText().toString().trim();
                String userPW = editPW.getText().toString().trim();
                if(userMAIL.isEmpty()){
                    editMAIL.setError("이메일을 입력해주세요");
                    editMAIL.requestFocus();
                    return;
                }
                if(userPW.isEmpty()){
                    editPW.setError("PW를 입력해주세요");
                    editPW.requestFocus();
                    return;
                }
                mFirebaseAuth.signInWithEmailAndPassword(userMAIL, userPW).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            finish();
                        } else {
                            //커스텀 토스트 메시지 제작 가능
                            String message = "로그인에 실패했어요\n아이디나 비밀번호를 확인해주세요";
                            //showCustomToast(message);
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }//onComplete
                });//onCompleteListener

 */
            }//onClick
        });//onClickListener

        signupBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String message = "회원가입 xml만 만들고 자바 안 짰어요";
                //showCustomToast(message);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }//onClick
        });//onClickListener
    }


}