package com.example.sharehelmet.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.sharehelmet.MainActivity;
import com.example.sharehelmet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StartActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;         //파이어 베이스 인증
    //private DatabaseReference mDatabaseRef;     //실시간 데이터 베이스
    private FirebaseUser firebaseuser;
    private TextView subtitle, title1, title2;
    private Button signupBtn, loginBtn;
    int isLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start);

        Intent intent = getIntent();
        isLogout = intent.getIntExtra("isLogout", 0);

        mFirebaseAuth = FirebaseAuth.getInstance();
        //mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        autoLogin();
        setViews();

    }
    private void autoLogin(){
        firebaseuser = mFirebaseAuth.getCurrentUser();
        Intent login = getIntent();
        int isLogout = login.getIntExtra("isLogout",-1);
        if(isLogout == -1) firebaseuser = null;
        else if(isLogout == 1 || isLogout == 0){
            FirebaseAuth.getInstance().signOut();
            firebaseuser = null;
        }

        if(firebaseuser != null){
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            intent.putExtra("userid",firebaseuser.getUid());
            intent.putExtra("usermail",firebaseuser.getEmail());
            startActivity(intent);
            //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }



    protected void setViews(){
        subtitle = findViewById(R.id.subTitle);
        title1 = findViewById(R.id.title);
        title2 = findViewById(R.id.title2);
        signupBtn = findViewById(R.id.signup_btn);
        loginBtn = findViewById(R.id.login_btn);

        int red_orange = ContextCompat.getColor(this,R.color.red_orange);

        String subTitle = "공유 킥보드를 위한\n헬멧 대여 서비스";
        String Title1 = "SAVETHE";
        String Title2 = "TTUK";
        SpannableString spannableString = new SpannableString(subTitle);
        spannableString.setSpan(new ForegroundColorSpan(red_orange), 3, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(red_orange), 11, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        subtitle.setText(spannableString);

        SpannableString spannableString2 = new SpannableString(Title2);
        spannableString2.setSpan(new ForegroundColorSpan(red_orange), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        title2.setText(spannableString2);

        signupBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginBtn.setOnClickListener(v->{
            Intent intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }


}