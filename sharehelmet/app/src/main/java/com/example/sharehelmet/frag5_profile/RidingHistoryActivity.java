package com.example.sharehelmet.frag5_profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sharehelmet.R;
import com.example.sharehelmet.RidingHistoryDataProcessor;
import com.example.sharehelmet.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class RidingHistoryActivity extends AppCompatActivity {
    ImageView backButton;
    private User user;
    String firebaseId;
    private DatabaseReference db;
    Map<String, String> hashMap = new HashMap<>();
    ListView Riding_History;

    private void loadDataFromDatabase() {
        db = FirebaseDatabase.getInstance().getReference();
        db.child("users").child(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user != null) {
                    hashMap = user.getRecord();
                    updateListView();
                } else {
                    Log.e("QRFrag3", "User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("QRFrag3", "Failed to read user data", error.toException());
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riding_history);
        setBackButton();
        Intent intent = getIntent();
        if (intent != null) {
            firebaseId = intent.getStringExtra("firebaseId");
            if (firebaseId != null && isValidFirebaseId(firebaseId)) {
                loadDataFromDatabase();
            } else {
                Log.e("MyActivity", "Invalid Firebase ID");
            }
        }
        Riding_History = findViewById(R.id.riding_history_listview);

        // 리스트뷰 아이템 클릭 리스너 설정
        Riding_History.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 빈 액티비티로 이동
                Intent detailIntent = new Intent(RidingHistoryActivity.this, RidingDetailActivity.class);
                startActivity(detailIntent);
            }
        });
    }

    private boolean isValidFirebaseId(String firebaseId) {
        return !(firebaseId.contains(".") || firebaseId.contains("#") ||
                firebaseId.contains("$") || firebaseId.contains("[") || firebaseId.contains("]"));
    }

    protected void setBackButton() {
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.horizontal_exit);
    }

    private void updateListView() {
        RidingHistoryDataProcessor ridingHistoryDataProcessor = new RidingHistoryDataProcessor(this, hashMap);
        Riding_History.setAdapter(ridingHistoryDataProcessor);
    }
}
