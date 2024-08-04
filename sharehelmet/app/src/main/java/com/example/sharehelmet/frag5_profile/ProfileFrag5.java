package com.example.sharehelmet.frag5_profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharehelmet.R;
import com.example.sharehelmet.login.StartActivity;
import com.example.sharehelmet.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFrag5 extends Fragment {
    private User user;
    TextView title, email;
    String firebaseId;
    DatabaseReference mDatabaseRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag5_profile, container, false);
        if (getArguments() != null) {
            firebaseId = getArguments().getString("firebaseId");
        }
        title = view.findViewById(R.id.title);
        email = view.findViewById(R.id.email);

        loadDataFromDatabase();

        ConstraintLayout profileSection = view.findViewById(R.id.profile_section);
        profileSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ProfileUpdateActivity.class);
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.horizontal_enter,R.anim.none);
            }
        });

        RelativeLayout ridingGuideSection = view.findViewById(R.id.riding_guide_section);
        ridingGuideSection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RidingGuideActivity.class);
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.horizontal_enter,R.anim.none);
            }
        });

        RelativeLayout ridingHistorySection = view.findViewById(R.id.riding_history_section);
        ridingHistorySection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RidingHistoryActivity.class);
                intent.putExtra("firebaseId", firebaseId);
                startActivity(intent);
                requireActivity().overridePendingTransition(R.anim.horizontal_enter,R.anim.none);
            }
        });

        RelativeLayout businessSection = view.findViewById(R.id.business_section);
        businessSection.setOnClickListener(v -> openKakaoTalkChannel());

        RelativeLayout signOutSection = view.findViewById(R.id.sign_out_section);
        signOutSection.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), StartActivity.class);
            intent.putExtra("isLogout", 1);
            startActivity(intent);
            getActivity().finish();
        });

        return view;
    }

    private void loadDataFromDatabase() {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef.child("users").child(firebaseId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showCustomToast("Failed to load data");
            }
        });
    }

    private void updateUI() {
        if (user != null) {
            title.setText(user.getNickname());
            email.setText(user.getEmail());
        }
    }

    private void openKakaoTalkChannel() {
        String url = "http://pf.kakao.com/_dLAxeK/chat";// 빔 채팅.
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
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
