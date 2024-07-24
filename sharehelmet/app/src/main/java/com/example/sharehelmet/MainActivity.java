package com.example.sharehelmet;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private HomeFrag1 homeFrag1;
    private PaymentFrag2 paymentFrag2;
    private QRFrag3 QRFrag3;
    private QnAFrag4 qnAFrag4;
    private ProfileFrag5 settingsFragment;
    private BottomNavigationView bottomNavigationView;
    private boolean isUserInteraction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeFrag1 = new HomeFrag1();
        paymentFrag2 = new PaymentFrag2();
        QRFrag3 = new QRFrag3();
        qnAFrag4 = new QnAFrag4();
        settingsFragment = new ProfileFrag5();

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

        // Set default selection
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void setFrag(int n) {
        Fragment selectedFragment = null;
        switch (n) {
            case 0:
                selectedFragment = homeFrag1;
                break;
            case 1:
                selectedFragment = paymentFrag2;
                break;
            case 2:
                selectedFragment = QRFrag3;
                break;
            case 3:
                selectedFragment = qnAFrag4;
                break;
            case 4:
                selectedFragment = settingsFragment;
                break;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }
    }
}
