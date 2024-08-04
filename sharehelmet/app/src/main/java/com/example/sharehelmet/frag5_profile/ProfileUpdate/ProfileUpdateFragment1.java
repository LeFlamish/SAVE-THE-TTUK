package com.example.sharehelmet.frag5_profile.ProfileUpdate;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharehelmet.R;
import com.example.sharehelmet.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileUpdateFragment1 extends Fragment {
    FirebaseAuth mFirebaseAuth;         //파이어 베이스 인증
    DatabaseReference mDatabaseRef;     //실시간 데이터 베이스
    FirebaseUser firebaseuser;
    String firebaseId, mail, pw;
    User user;
    EditText etEmail, etPassword;
    Button updateButton;

    public ProfileUpdateFragment1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_update1, container, false);

        etEmail = view.findViewById(R.id.edit_MAIL);
        etPassword = view.findViewById(R.id.edit_Current_PW);
        updateButton = view.findViewById(R.id.update_btn);

        if (getArguments() != null) {
            firebaseId = getArguments().getString("firebaseId");
        }
        loadDataFromDatabase();

        return view;
    }

    protected void loadDataFromDatabase(){
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef.child("users").child(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                etEmail.setText(user.getEmail());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        firebaseuser = mFirebaseAuth.getCurrentUser();
        setViews();
    }

    protected void setViews(){

        updateButton.setOnClickListener(v -> {
            mail = etEmail.getText().toString().trim();
            pw = etPassword.getText().toString().trim();
            if(mail.isEmpty()){
                etEmail.setError("이메일을 입력해주세요");
                etEmail.requestFocus();
                return;
            }
            if(pw.isEmpty()){
                etPassword.setError("비밀번호를 입력해주세요");
                etPassword.requestFocus();
                return;
            }
            reauthenticateUser(mail,pw);
        });
    }

    protected void reauthenticateUser(String email, String password){
        if(firebaseuser == null) {
            showCustomToast("사용자가 로그인되지 않았습니다");
            return;
        }
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        firebaseuser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 인증 성공
                Bundle bundle = new Bundle();
                bundle.putString("firebaseId",firebaseId);
                Fragment profileUpdate2 = new ProfileUpdateFragment2();
                profileUpdate2.setArguments(bundle);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, profileUpdate2)
                        .addToBackStack(null)
                        .commit();
            } else {
                // 인증 실패
                showCustomToast("비밀번호가 틀렸습니다");
            }
        });

    }

    private void showCustomToast(String message) {
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