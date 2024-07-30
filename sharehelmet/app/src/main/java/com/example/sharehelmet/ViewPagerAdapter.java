package com.example.sharehelmet;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.sharehelmet.frag5_profile.GuideFrag1;
import com.example.sharehelmet.frag5_profile.GuideFrag2;
import com.example.sharehelmet.frag5_profile.GuideFrag3;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new GuideFrag1();
            case 1:
                return new GuideFrag2();
            case 2:
                return new GuideFrag3();
            default:
                return new GuideFrag1();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
