package com.example.sharehelmet.frag5_profile.ProfileUpdate;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharehelmet.R;
import com.example.sharehelmet.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileUpdateFragment2 extends Fragment {
    String firebaseId;
    FirebaseAuth mFirebaseAuth;         //파이어 베이스 인증
    DatabaseReference mDatabaseRef;     //실시간 데이터 베이스
    FirebaseUser firebaseuser;
    User user;
    EditText etEmail, etPassword, etPasswordConfirm, etPhone;
    TextView deleteButton;
    Button changeButton;
    String mail, password, passwordConfirm, phone;

    public ProfileUpdateFragment2() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_update2,container,false);

        etEmail = view.findViewById(R.id.edit_MAIL);
        etPhone = view.findViewById(R.id.edit_PHONE);
        etPassword = view.findViewById(R.id.edit_PW);
        etPasswordConfirm = view.findViewById(R.id.edit_PW_confilm);
        deleteButton = view.findViewById(R.id.delete_button);
        changeButton = view.findViewById(R.id.change_btn);


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
        deleteButton.setOnClickListener(v -> deleteUserInformation());
        changeButton.setOnClickListener(v -> changeUserInformation());
    }

    protected void deleteUserInformation(){
        showCustomToast("계정 삭제 기능 구현 전입니다");
    }

    protected void changeUserInformation(){
        if(firebaseuser == null) {
            showCustomToast("사용자가 로그인되지 않았습니다");
            return;
        }
        mail = etEmail.getText().toString().trim();
        password = etPassword.getText().toString().trim();
        passwordConfirm = etPasswordConfirm.getText().toString().trim();
        phone = etPhone.getText().toString().trim();
        if(mail.isEmpty()){
            etEmail.setError("이메일을 입력해주세요");
            etEmail.requestFocus();
            return;
        }
        if(password.length() < 6){
            etPassword.setError("비밀번호는 6자 이상이어야 합니다");
            etPassword.requestFocus();
            return;
        }
        if(password.isEmpty()){
            etPassword.setError("비밀번호를 입력해주세요");
            etPassword.requestFocus();
            return;
        }
        if(passwordConfirm.isEmpty()){
            etPasswordConfirm.setError("비밀번호를 다시 입력해주세요");
            etPasswordConfirm.requestFocus();
            return;
        }
        if(phone.isEmpty()){
            etPhone.setError("핸드폰 번호를 입력해주세요");
            etPhone.requestFocus();
            return;
        }
        if(!password.equals(passwordConfirm)){
            showCustomToast("비밀번호 확인이 맞지 않습니다");
            etPassword.requestFocus();
            return;
        }
        saveChanges();
    }

    protected void saveChanges(){
        // Email 업데이트
        firebaseuser.updateEmail(mail).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // 비밀번호 업데이트
                firebaseuser.updatePassword(password).addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        // Realtime Database에 업데이트
                        mDatabaseRef.child("users").child(firebaseId).child("email").setValue(mail);
                        mDatabaseRef.child("users").child(firebaseId).child("phone").setValue(phone);
                        showCustomToast("정보가 성공적으로 업데이트되었습니다");
                        getActivity().onBackPressed();

                    } else {
                        showCustomToast("비밀번호 업데이트 실패: " + task2.getException().getMessage());
                    }
                });
            } else {
                showCustomToast("이메일 업데이트 실패: " + task.getException().getMessage());
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