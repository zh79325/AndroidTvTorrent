package com.github.se_bastiaan.torrentstreamer.sample;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.tvcommon.db.model.TorrentTaskFile;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.TorrentStream;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by zh_zhou on 2017/3/11.
 */

public class TorrentAdaptor extends ArrayAdapter<TorrentTaskFile> implements TorrentListener {

    private static final String TORRENT = "Torrent";

    private TorrentStream torrentStream;
    TorrentAdaptor self;
    Context context;
    public TorrentAdaptor(Context context, int resource, List<TorrentTaskFile> objects) {
        super(context, resource, objects);
        self=this;
        this.context=context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final TorrentTaskFile user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.torrent_file, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.videoName);
        Button tvHome = (Button) convertView.findViewById(R.id.playBtn);
        // Populate the data into the template view using the data object
        tvName.setText(user.getFileName());
        tvHome.setText("play");
        return convertView;

    }

    @Override
    public void onStreamPrepared(Torrent torrent) {
        Log.d(TORRENT, "OnStreamPrepared");
        torrent.startDownload();
    }

    @Override
    public void onStreamStarted(Torrent torrent) {
        Log.d(TORRENT, "onStreamStarted");
    }

    @Override
    public void onStreamError(Torrent torrent, Exception e) {
        Log.e(TORRENT, "onStreamError", e);
    }

    @Override
    public void onStreamReady(Torrent torrent) {
        Log.d(TORRENT, "onStreamReady: " + torrent.getVideoFile());
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(torrent.getVideoFile().toString()));
//        intent.setDataAndType(Uri.parse(torrent.getVideoFile().toString()), "video/mp4");
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    @Override
    public void onStreamProgress(Torrent torrent, StreamStatus status) {
        Log.d(TORRENT, "Progress: " + status.progress+" speed:"+readableFileSize((long) status.downloadSpeed)+"/s");
        Log.d(TORRENT, "Buff Progress: " + status.bufferProgress+" seeds:"+status.seeds  );
        TextView tvName = (TextView) ((Activity) context).findViewById(R.id.status);
        tvName.setText(status.progress+"% "+readableFileSize((long) status.downloadSpeed)+"/s");
        if(status.progress>=100){
            torrentStream.stopStream();
        }
    }

    @Override
    public void onStreamStopped() {
        Log.d(TORRENT, "onStreamStopped");
    }

    @Override
    public void fireAlert(Alert<?> alert) {

    }

}
