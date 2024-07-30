package com.example.sharehelmet.frag4_QnA;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.sharehelmet.R;

public class QnAFrag4 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag4_qna, container, false);

        Button chatKakaoButton = view.findViewById(R.id.chat_kakao);
        chatKakaoButton.setOnClickListener(v -> openKakaoTalkChannel());

        return view;
    }

    private void openKakaoTalkChannel() {
        String url = "http://pf.kakao.com/_dLAxeK/chat";// 빔 채팅.
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
