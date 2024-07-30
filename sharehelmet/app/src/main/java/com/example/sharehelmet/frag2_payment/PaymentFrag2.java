package com.example.sharehelmet.frag2_payment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sharehelmet.R;
import com.example.sharehelmet.TotalPaymentActivity;


public class PaymentFrag2 extends Fragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag2_payment, container, false);

        RelativeLayout testView = view.findViewById(R.id.test);
        testView.setOnClickListener(this);

        ImageView paymentIcons = view.findViewById(R.id.payment_icons);
        paymentIcons.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.test || id == R.id.payment_icons) {
            Intent intent = new Intent(getActivity(), TotalPaymentActivity.class);
            startActivity(intent);
        }
    }
}
