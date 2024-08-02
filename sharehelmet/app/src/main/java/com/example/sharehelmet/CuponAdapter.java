package com.example.sharehelmet;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sharehelmet.model.Cupon;

import java.util.List;

public class CuponAdapter extends ArrayAdapter<Cupon> {
    public CuponAdapter(Context context, List<Cupon> cupons) {
        super(context, 0, cupons);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Cupon cupon = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cupon_list_item, parent, false);
        }

        TextView titleTextView = convertView.findViewById(R.id.cupon_title);
        TextView dateTextView = convertView.findViewById(R.id.cupon_time);

        titleTextView.setText(cupon.getName());
        dateTextView.setText("2024-01-01:12:44");

        return convertView;
    }
}
