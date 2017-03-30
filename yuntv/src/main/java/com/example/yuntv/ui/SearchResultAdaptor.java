package com.example.yuntv.ui;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.tvcommon.common.TorrentDownloadLink;
import com.example.tvcommon.common.TorrentLink;
import com.example.yuntv.R;

import java.util.List;
import java.util.Set;

/**
 * Created by zh_zhou on 2017/3/13.
 */

public class SearchResultAdaptor  extends ArrayAdapter<TorrentLink> {
    public SearchResultAdaptor(@NonNull Context context, @LayoutRes int resource,@NonNull List<TorrentLink> objects) {
        super(context,resource,objects);
        self=this;
        this.context=context;
    }
    SearchResultAdaptor self;
    Context context;

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TorrentLink user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_torrent_result, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.linkName);

        TextView button = (TextView) convertView.findViewById(R.id.download);
        ProgressBar bar = (ProgressBar) convertView.findViewById(R.id.progressBar);

        // Populate the data into the template view using the data object
        tvName.setText(user.getName());

        if(user.isLoading()){
            bar.setVisibility(View.VISIBLE);
//
            Set<TorrentDownloadLink> downloads= user.getDownloads();
            if(downloads==null||downloads.size()==0){
                button.setVisibility(View.GONE);
            }else{
                button.setVisibility(View.VISIBLE);
                button.setText(user.getParseSuccess()+"/"+downloads.size());
            }
        }else {
            button.setVisibility(View.VISIBLE);
            Set<TorrentDownloadLink> downloads= user.getDownloads();
            if(downloads==null||downloads.size()==0){
                button.setText("暂无种子");
            }else{
                button.setText(downloads.size()+"种子");
            }
            bar.setVisibility(View.GONE);
        }
        return convertView;

    }
}
