package com.example.yuntv.ui;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.tvcommon.db.model.TorrentTask;
import com.example.tvcommon.db.model.TorrentTaskFile;
import com.example.yuntv.R;
import com.example.yuntv.TorrentDetailActivity;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by zh_zhou on 2017/3/11.
 */

public class TorrentAdaptor extends ArrayAdapter<TorrentTaskFile>{



    private static final String TORRENT = "Torrent";


    TorrentDetailActivity context;
    private long taskId;

    public TorrentAdaptor(TorrentDetailActivity context, int resource, List<TorrentTaskFile> objects) {
        super(context, resource, objects);
        this.context=context;

    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TorrentTaskFile user = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.torrent_file, parent, false);
        }
        TextView name = (TextView) convertView.findViewById(R.id.torrent_file_name);
        TextView percent = (TextView) convertView.findViewById(R.id.torrent_file_percent);
        TextView status = (TextView) convertView.findViewById(R.id.torrent_file_status);


        float dPercent=user.getPercent();

        if(dPercent<0){
            percent.setVisibility(View.GONE);
        }else{
            percent.setVisibility(View.VISIBLE);
            String txt=readablePercent(dPercent);
            percent.setText(txt);
        }

        name.setText(user.getFileName());

        int downloading=user.getDownloading();

        String dText="",pText="正在加载";
        if(downloading==-1){
            dText="下载未开始";
        }else if(downloading==0){
            dText="下载已暂停";
        }else if(downloading==1){
            dText="正在下载";
        }

        if(user.isFinished()){
            dText="下载已完成";
        }

        if(user.isStreamReady()){
            pText="可以播放";
        }

        String txt=String.format("%s/%s(点击选择操作)",dText,pText);
        status.setText(txt);

        return convertView;

    }



    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String readablePercent(float percent) {
        String txt= new DecimalFormat("#,##0.#").format(percent*100)+"%";
        return txt;
    }


    public void onStreamProgress(Torrent torrent, StreamStatus status) {
        Log.d(TORRENT, "Progress: " + status.progress+" speed:"+readableFileSize((long) status.downloadSpeed)+"/s");
        Log.d(TORRENT, "Buff Progress: " + status.bufferProgress+" seeds:"+status.seeds  );
    }


    public void updateTaskUI(TorrentTask task) {
        context.updateTaskUI(task);
    }

    public void updateTaskFile(TorrentTaskFile taskFile) {
        for (int i = 0; i < this.getCount(); i++) {
            TorrentTaskFile file=getItem(i);
            if(file.getId()==taskFile.getId()){
                this.remove(file);
                this.insert(taskFile,i);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public long getTaskId() {
        return taskId;
    }



}
