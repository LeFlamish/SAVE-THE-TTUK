package com.example.sharehelmet.frag3_QR;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.sharehelmet.R;
import com.example.sharehelmet.model.Helmet;
import com.example.sharehelmet.model.Storage;
import com.example.sharehelmet.model.User;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import java.util.ArrayList;

public class QRFrag3 extends Fragment {
    private User user;
    String firebaseId;
    private DatabaseReference db;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag3_qr, container, false);
        if (getArguments() != null) {
            firebaseId = getArguments().getString("firebaseId");
            if (firebaseId != null && isValidFirebaseId(firebaseId)) {
                loadDataFromDatabase(view);
            }
        }

        /*saveHelmetData("001", "001-001");
        saveHelmetData("002", "001-002");
        saveHelmetData("003", "001-003");
        saveHelmetData("004", "001-004");
        saveHelmetData("005", "001-005");
        saveHelmetData("006", "001-006");
        saveHelmetData("007", "001-007");
        saveHelmetData("008", "001-008");
        saveHelmetData("009", "001-009");
        saveHelmetData("010", "001-010");
        saveHelmetData("011", "001-011");
        saveHelmetData("012", "001-012");
        saveHelmetData("013", "001-013");
        saveHelmetData("014", "001-014");
        saveHelmetData("015", "001-015");
        saveHelmetData("016", "001-016");
        saveHelmetData("017", "001-017");
        saveHelmetData("018", "001-018");
        saveHelmetData("019", "001-019");
        saveHelmetData("020", "001-020");*/

        return view;
    }
    private void loadDataFromDatabase(View rootView) {
        db = FirebaseDatabase.getInstance().getReference();
        db.child("users").child(firebaseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user != null) {
                    initializeUI();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void initializeUI() {
        Fragment fragment;
        Bundle bundle = new Bundle();
        bundle.putString("firebaseId",firebaseId);
        if (user.getNow_qr() == 0) {fragment=new BarcodeStartFragment3();}
        else if (user.getNow_qr() == 1) {fragment=new BorrowingFragment3();}
        else if (user.getNow_qr() == 2) {fragment=new BarcodeEndFragment3();}
        else{fragment=new ResultFragment3();}
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    private boolean isValidFirebaseId(String firebaseId) {
        return !(firebaseId.contains(".") || firebaseId.contains("#") ||
                firebaseId.contains("$") || firebaseId.contains("[") || firebaseId.contains("]"));
    }
    private void saveHelmetData(String id, String storageID) {
        Helmet helmet = new Helmet();
        helmet.setHelmetId(id);
        helmet.setBatteryState(100);
        helmet.setBorrow(false);
        helmet.setStorageId(storageID);
        helmet.setUserId("-");
        db.child("helmets").child(id).setValue(helmet)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Helmet added successfully", Toast.LENGTH_SHORT).show();
                    updatePlaceData(storageID, id);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add helmet", Toast.LENGTH_SHORT).show());
    }
    private void updatePlaceData(String storageID, String helmetId) {
        String locationID = storageID.substring(0, 3);
        int helmetIndex = Integer.parseInt(storageID.substring(4, 7)) - 1;
        db.child("places").orderByChild("locationID").equalTo(locationID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                        String placeKey = placeSnapshot.getKey();
                        db.child("places").child(placeKey).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                Storage storage = mutableData.getValue(Storage.class);
                                if (storage == null) {
                                    return Transaction.success(mutableData);
                                }
                                int newStock = storage.getStock() + 1;
                                storage.setStock(newStock);
                                ArrayList<String> storedHelmetID = storage.getStoredHelmetID();
                                if (storedHelmetID != null && helmetIndex >= 0 && helmetIndex < storedHelmetID.size()) {
                                    storedHelmetID.set(helmetIndex, helmetId);
                                }
                                mutableData.setValue(storage);
                                return Transaction.success(mutableData);
                            }
                            @Override
                            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                                if (databaseError != null) {
                                    Toast.makeText(getContext(), "Failed to update place data", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Place data updated successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to update place data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {}
            else {Toast.makeText(getContext(), "QR 스캔을 위해 카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();}
        }
    }
}


