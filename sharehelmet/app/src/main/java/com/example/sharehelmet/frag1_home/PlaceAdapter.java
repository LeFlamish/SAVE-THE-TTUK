package com.example.sharehelmet.frag1_home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.sharehelmet.R;

import java.util.List;

public class PlaceAdapter extends BaseAdapter {
    private Context context;
    private List<HomeFrag1.Place> places;

    public PlaceAdapter(Context context, List<HomeFrag1.Place> places) {
        this.context = context;
        this.places = places;
    }

    @Override
    public int getCount() {
        return places.size();
    }

    @Override
    public Object getItem(int position) {
        return places.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.place_list_item, parent, false);
        }

        HomeFrag1.Place place = places.get(position);

        TextView placeName = convertView.findViewById(R.id.place_name);
        TextView placeDistance = convertView.findViewById(R.id.place_distance);
        TextView placeStock = convertView.findViewById(R.id.place_stock);

        placeName.setText(place.name);
        placeDistance.setText(String.format("%.2f km", place.distance));
        placeStock.setText("재고: " + place.stock);

        return convertView;
    }
}

