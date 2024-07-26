package com.example.sharehelmet;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.sharehelmet.login.LoginActivity;

public class ProfileFrag5 extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag5_profile, container, false);

        RelativeLayout profileSection = view.findViewById(R.id.profile_section);
        profileSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileUpdateActivity.class);
                startActivity(intent);
            }
        });

        RelativeLayout ridingGuideSection = view.findViewById(R.id.riding_guide_section);
        ridingGuideSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RidingGuideActivity.class);
                startActivity(intent);
            }
        });


        RelativeLayout signOutSection = view.findViewById(R.id.sign_out_section);
        signOutSection.setOnClickListener( v -> {//람다 표현식 사용
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }
}
