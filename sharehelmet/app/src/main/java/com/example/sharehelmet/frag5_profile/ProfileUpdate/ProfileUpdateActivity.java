package com.example.sharehelmet.frag5_profile.ProfileUpdate;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.sharehelmet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileUpdateActivity extends AppCompatActivity {
    String firebaseId;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        firebaseId = intent.getStringExtra("firebaseId");


        Bundle bundle = new Bundle();
        bundle.putString("firebaseId",firebaseId);
        Fragment profileUpdate = new ProfileUpdateFragment1();
        profileUpdate.setArguments(bundle);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, profileUpdate)
                    .commit();
        }
    }

    @Override
    public void onBackPressed(){
        finish();
        overridePendingTransition(0, R.anim.horizontal_exit);
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
