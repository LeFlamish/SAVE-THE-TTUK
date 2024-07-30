package com.example.sharehelmet.home;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.sharehelmet.frag1_home.HomeFrag1;
import com.example.sharehelmet.frag2_payment.PaymentFrag2;
import com.example.sharehelmet.frag5_profile.ProfileFrag5;
import com.example.sharehelmet.frag3_QR.QRFrag3;
import com.example.sharehelmet.frag4_QnA.QnAFrag4;
import com.example.sharehelmet.R;
import com.example.sharehelmet.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private User user;
    private String firebaseId, mail;
    DatabaseReference mDatabaseRef;
    private HomeFrag1 homeFrag1;
    private PaymentFrag2 paymentFrag2;
    private com.example.sharehelmet.frag3_QR.QRFrag3 QRFrag3;
    private QnAFrag4 qnAFrag4;
    private ProfileFrag5 settingsFragment;
    private BottomNavigationView bottomNavigationView;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private boolean isUserInteraction;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialzeUser();
        initializeFragments();
        setupBottomNavigationView();

        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // BottomSheetBehavior 설정
//        View bottomSheet = findViewById(R.id.persistent_bottom_sheet);
//        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void initialzeUser(){
        Intent intent = getIntent();
        firebaseId = intent.getStringExtra("userid");
        mail = intent.getStringExtra("usermail");

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef.child("users").child(firebaseId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                showCustomToast(user.getNickname());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void initializeFragments() {
        homeFrag1 = new HomeFrag1();
        paymentFrag2 = new PaymentFrag2();
        QRFrag3 = new QRFrag3();
        qnAFrag4 = new QnAFrag4();
        settingsFragment = new ProfileFrag5();
    }

    private void setupBottomNavigationView() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                isUserInteraction = true;

                int n = menuItem.getItemId();
                if (n == R.id.nav_home) {
                    setFrag(0);
                } else if (n == R.id.nav_search) {
                    setFrag(1);
                } else if (n == R.id.nav_notifications) {
                    setFrag(2);
                } else if (n == R.id.nav_profile) {
                    setFrag(3);
                } else if (n == R.id.nav_settings) {
                    setFrag(4);
                }

                return true;
            }
        });
    }

    private void setFrag(int n) {
        Fragment selectedFragment = null;
        switch (n) {
            case 0:
                selectedFragment = homeFrag1;
//                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            case 1:
                selectedFragment = paymentFrag2;
//                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                break;
            case 2:
                selectedFragment = QRFrag3;
//                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                break;
            case 3:
                selectedFragment = qnAFrag4;
//                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                break;
            case 4:
                selectedFragment = settingsFragment;
//                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                break;
        }
        Bundle bundle = new Bundle();
        bundle.putString("firebaseId",firebaseId);
        selectedFragment.setArguments(bundle);
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }
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
