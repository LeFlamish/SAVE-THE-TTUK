package com.example.sharehelmet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.sharehelmet.R;
import com.example.sharehelmet.frag1_home.HomeFrag1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RidingHistoryDataProcessor extends BaseAdapter {
    private Context context;
    private List<Map.Entry<String, String>> sortedEntries;
    private LayoutInflater inflater;

    public RidingHistoryDataProcessor(Context context, Map<String, String> hashMap) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        sortedEntries = new ArrayList<>(hashMap.entrySet());
        Collections.sort(sortedEntries, (entry1, entry2) -> {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                Date date1 = dateFormat.parse(entry1.getKey());
                Date date2 = dateFormat.parse(entry2.getKey());
                return date2.compareTo(date1);
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }
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
            convertView = LayoutInflater.from(context).inflate(R.layout.riding_history_item, parent, false);
        }
        TextView date_text = convertView.findViewById(R.id.Date);
        TextView starttoend_text = convertView.findViewById(R.id.StarttoEnd);
        TextView time_text = convertView.findViewById(R.id.Time);

        Map.Entry<String, String> entry = (Map.Entry<String, String>) getItem(position);
        String startDateStr = entry.getKey();
        String endDateStr = entry.getValue();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

        try {
            Date startDate = dateFormat.parse(startDateStr);
            Date endDate = dateFormat.parse(endDateStr);

            if (startDate != null && endDate != null) {
                // 날짜 부분
                String date = new SimpleDateFormat("yy-MM-dd", Locale.getDefault()).format(startDate);
                date_text.setText(date);

                // 시간 부분
                String startTime = timeFormat.format(startDate);
                String endTime = timeFormat.format(endDate);
                starttoend_text.setText(String.format(Locale.getDefault(), "%s ~ %s", startTime, endTime));

                // 시간 차이 계산
                long duration = endDate.getTime() - startDate.getTime();
                String durationStr = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(duration),
                        TimeUnit.MILLISECONDS.toMinutes(duration) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(duration) % 60);
                time_text.setText(durationStr);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }



        return convertView;
    }
}