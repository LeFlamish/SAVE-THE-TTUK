package com.example.sharehelmet.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sharehelmet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth mFirebaseAuth;         //파이어 베이스 인증
    DatabaseReference mDatabaseRef;     //실시간 데이터 베이스
    EditText editNAME, editMAIL, editPW;
    TextView nameCheck, mailCheck, pwCheck;
    String name, mail, pw;
    Button signUp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        setViews();
    }

    protected void setViews(){
        editNAME = findViewById(R.id.editNAME);
        editMAIL = findViewById(R.id.editMAIL);
        editPW   = findViewById(R.id.editPW);

        nameCheck = findViewById(R.id.nameCheck);
        mailCheck = findViewById(R.id.mailCheck);
        pwCheck   = findViewById(R.id.pwCheck);

        signUp = findViewById(R.id.signupbtn);
        signupClicked();
    }

    protected void signupClicked(){
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });

    }

}