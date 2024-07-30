package com.example.sharehelmet.frag5_profile;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sharehelmet.R;
import com.example.sharehelmet.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class RidingGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riding_guide);

        ViewPager2 viewPager = findViewById(R.id.viewPager_onBoarding);
        TabLayout tabLayout = findViewById(R.id.tab_onBoarding);
        Button nextButton = findViewById(R.id.button_next);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Customize tab indicators here if needed
        }).attach();

        nextButton.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                // 마지막 페이지
                finish();
            }
        });
    }
}
