package com.example.sharehelmet;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sharehelmet.frag1_home.PlaceAdapter;
import com.example.sharehelmet.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        Riding_History=(ListView)findViewById(R.id.riding_history_listview);
    }
    private boolean isValidFirebaseId(String firebaseId) {
        return !(firebaseId.contains(".") || firebaseId.contains("#") ||
                firebaseId.contains("$") || firebaseId.contains("[") || firebaseId.contains("]"));
    }
    protected void setBackButton(){
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v->{
            onBackPressed();
        });
    }
    @Override
    public void onBackPressed(){
        overridePendingTransition(0, R.anim.horizontal_exit);
        super.onBackPressed();
    }
    private void updateListView() {
        RidingHistoryDataProcessor ridingHistoryDataProcessor = new RidingHistoryDataProcessor(this, hashMap);
        Riding_History.setAdapter(ridingHistoryDataProcessor);
    }
}
