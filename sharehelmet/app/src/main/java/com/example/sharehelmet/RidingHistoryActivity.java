package com.example.sharehelmet;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class RidingHistoryActivity extends AppCompatActivity {
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riding_history);
        setBackButton();
        // Riding history 관련 코드 작성
    }

    protected void setBackButton(){
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v->{
            onBackPressed();
        });
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        overridePendingTransition(R.anim.none, R.anim.horizontal_exit);
    }

}
