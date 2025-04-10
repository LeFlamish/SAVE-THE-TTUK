package com.example.sharehelmet.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sharehelmet.R;
import com.example.sharehelmet.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth mFirebaseAuth;         //파이어 베이스 인증
    DatabaseReference mDatabaseRef;     //실시간 데이터 베이스
    EditText editNAME, editMAIL, editPW,checkPW;
    TextView nameCheck, mailCheck, pwCheck,pwnotcorrect;
    String name, mail, pw,pw2;
    Button signUp;
    ImageView backBtn;
    User USER;//TODO : 유저 클래스 수정했으면 181줄로
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(0, R.anim.horizontal_exit);
    }

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
        checkPW=findViewById(R.id.checkPW);

        nameCheck = findViewById(R.id.nameCheck);
        mailCheck = findViewById(R.id.mailCheck);
        pwCheck   = findViewById(R.id.pwCheck);
        pwnotcorrect=findViewById(R.id.pwnotcorrect);

        signUp = findViewById(R.id.signupbtn);
        backBtn = findViewById(R.id.back_button);

        loadDataFromDatabase();
        signupClicked();
        backBtnClicked();
    }

    protected void signupClicked(){
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameCheck.setVisibility(View.INVISIBLE);
                mailCheck.setVisibility(View.INVISIBLE);
                pwnotcorrect.setVisibility(View.INVISIBLE);
                name = editNAME.getText().toString().trim();
                mail = editMAIL.getText().toString().trim();
                pw   = editPW.getText().toString().trim();
                pw2 = checkPW.getText().toString().trim();
                //예외처리
                if(name.isEmpty()){
                    editNAME.setError("닉네임을 입력해주세요");
                    editNAME.requestFocus();
                    return;
                }
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
                if(pw2.isEmpty()){
                    checkPW.setError("비밀번호를 입력해주세요");
                    checkPW.requestFocus();
                    return;
                }
                if(!pw.equals(pw2)){
                    checkPW.setError("비밀번호가 일치하지 않습니다");
                    checkPW.requestFocus();
                    return;
                }

                //파이어베이스유저 생성, 데이터 전송
                mFirebaseAuth.createUserWithEmailAndPassword(mail,pw).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //TODO : 유저 클래스 수정 시 생성자 수정만 하면 됨
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            saveAdditionalUserInfo(user);
                            USER = new User(name, mail);

                            showCustomToast("회원가입이 완료되었습니다");
                            finish();
                            overridePendingTransition(0, R.anim.fade_out);

                        }else{
                            // Check the exception for specific error details
                            String errorMessage;
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                errorMessage = "이미 존재하는 이메일입니다";
                            } catch (FirebaseAuthWeakPasswordException e) {
                                errorMessage = "비밀번호를 6자리 이상 입력해주세요";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                errorMessage = "이메일 형식이 올바르지 않습니다";
                            } catch (Exception e) {
                                errorMessage = "회원가입에 실패했습니다. 관리자에게 문의해주세요";
                            }
                            showCustomToast(errorMessage);
                        }
                    }
                });
            }
        });

    }

    private void loadDataFromDatabase(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    }

    private void saveAdditionalUserInfo(FirebaseUser user) {
        // Firebase Realtime Database 참조
        DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        String userId = user.getUid();
        User userInfo = new User(name, mail);

        mDatabaseRef.child("users").child(userId).setValue(userInfo);
    }

    private void backBtnClicked(){
        backBtn.setOnClickListener(v->{
            onBackPressed();
        });
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