package com.example.yuntv.ui;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tvcommon.db.model.TorrentTask;
import com.example.yuntv.R;

import java.util.List;

/**
 * Created by zh_zhou on 2017/4/14.
 */

public class HistoryAdaptor extends ArrayAdapter<TorrentTask> {
    public HistoryAdaptor(@NonNull Context context, @LayoutRes int resource, @NonNull List<TorrentTask> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.history_item, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.history_text);
        tvName.setText(getItem(position).getFileName());
        return convertView;
    }
}
