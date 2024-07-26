package com.example.sharehelmet.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sharehelmet.MainActivity;
import com.example.sharehelmet.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.annotation.NonNull;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;         //파이어 베이스 인증
    DatabaseReference mDatabaseRef;     //실시간 데이터 베이스
    FirebaseUser firebaseuser;
    EditText editMAIL, editPW;
    Button loginBtn, signupBtn;
    String mail,pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        autoLogin();
        setViews();
    }

    private void autoLogin(){
        firebaseuser = mFirebaseAuth.getCurrentUser();
        Intent login = getIntent();
        int isLogout = login.getIntExtra("isLogout",-1);
        if(isLogout == -1) firebaseuser = null;
        else if(isLogout == 1){
            FirebaseAuth.getInstance().signOut();
            firebaseuser = null;
        }


        if(firebaseuser != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("userid",firebaseuser.getUid());
            intent.putExtra("usermail",firebaseuser.getEmail());
            startActivity(intent);
            //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }



    protected void setViews(){
        editMAIL = findViewById(R.id.editMAIL);
        editPW = findViewById(R.id.editPW);
        loginBtn = findViewById(R.id.loginbtn);
        signupBtn = findViewById(R.id.signupbtn);

        loginClicked();

        signupBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    protected void loginClicked(){
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mail = editMAIL.getText().toString().trim();
                pw = editPW.getText().toString().trim();

                //예외처리
                if(mail.isEmpty()){
                    editMAIL.setError("이메일을 입력해주세요");
                    editMAIL.requestFocus();
                    return;
                }
                if(pw.isEmpty()){
                    editPW.setError("비밀번호를 입력해주세요");
                    editPW.requestFocus();
                    return;
                }
                loadDataFromDatabase();
            }
        });
    }

    private void loadDataFromDatabase(){

        mFirebaseAuth.signInWithEmailAndPassword(mail, pw).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    firebaseuser = mFirebaseAuth.getCurrentUser();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("userid",firebaseuser.getUid());
                    intent.putExtra("usermail",firebaseuser.getEmail());
                    startActivity(intent);
                    //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else {
                    String message = "로그인에 실패했어요\n아이디나 비밀번호를 확인해주세요";
                    showCustomToast(message);
                }
            }//onComplete
        });//onCompleteListener
    }


    private void showCustomToast(String message) {
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