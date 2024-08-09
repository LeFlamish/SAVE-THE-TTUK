package com.example.sharehelmet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RidingHistoryDataProcessor extends BaseAdapter {
    private Context context;
    private List<Map.Entry<String, List<String>>> sortedEntries;
    private LayoutInflater inflater;

    public RidingHistoryDataProcessor(Context context, Map<String, List<String>> hashMap) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);

        // 정렬된 맵을 리스트로 변환
        sortedEntries = new ArrayList<>(hashMap.entrySet());
        sortedEntries.sort((entry1, entry2) -> {
            // 키를 먼저 비교
            int keyCompare = entry1.getKey().compareTo(entry2.getKey());
            if (keyCompare != 0) {
                return keyCompare;
            }
            // 키가 같다면 리스트의 첫 번째 값을 비교
            List<String> list1 = entry1.getValue();
            List<String> list2 = entry2.getValue();
            if (list1 == null || list1.isEmpty()) {
                return -1;
            }
            if (list2 == null || list2.isEmpty()) {
                return 1;
            }
            return list1.get(0).compareTo(list2.get(0));
        });

    }

    @Override
    public int getCount() {
        return sortedEntries.size();
    }

    @Override
    public Object getItem(int position) {
        return sortedEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.riding_history_item, parent, false);
        }

        TextView dateText = convertView.findViewById(R.id.Date);
        TextView startToEndText = convertView.findViewById(R.id.StarttoEnd);
        TextView timeText = convertView.findViewById(R.id.Time);

        Map.Entry<String, List<String>> entry = sortedEntries.get(position);
        List<String> values = entry.getValue();



        try {
            dateText.setText(entry.getKey());

            SimpleDateFormat timeFormat24 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()); // 24시간 형식
            SimpleDateFormat timeFormat12 = new SimpleDateFormat("hh:mm a", Locale.ENGLISH); // 12시간 형식 AM/PM

            String startTimeStr = values.get(0);
            String endTimeStr = values.get(1);

            Date startTime = timeFormat24.parse(startTimeStr);
            Date endTime = timeFormat24.parse(endTimeStr);

            String startTimeFormatted = timeFormat12.format(startTime);
            String endTimeFormatted = timeFormat12.format(endTime);
            startToEndText.setText(startTimeFormatted + " ~ " + endTimeFormatted);

            // 주행 시간 설정
            timeText.setText(values.get(3));

        } catch (Exception e) {

        }

        return convertView;
    }
}

