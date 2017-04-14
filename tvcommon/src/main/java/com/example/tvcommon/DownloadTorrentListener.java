package com.example.tvcommon;

import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.tvcommon.db.model.TorrentTask;
import com.example.tvcommon.db.model.TorrentTaskFile;
import com.example.tvcommon.db.model.TorrentTaskFile_Table;
import com.example.tvcommon.db.model.TorrentTask_Table;
import com.example.tvcommon.service.TorrentDownloadService;
import com.frostwire.jlibtorrent.LibTorrent;
import com.frostwire.jlibtorrent.StatsMetric;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.PeerConnectAlert;
import com.frostwire.jlibtorrent.alerts.PeerDisconnectedAlert;
import com.frostwire.jlibtorrent.alerts.SessionStatsAlert;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.TorrentFileInfo;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import static com.example.tvcommon.service.TorrentDownloadService.DOWNLOAD_TORRENT_FILE_INDEX;
import static com.example.tvcommon.service.TorrentDownloadService.DOWNLOAD_TORRENT_INDEX;


/**
 * Created by zh_zhou on 2017/3/18.
 */

public class DownloadTorrentListener implements TorrentListener {

    TorrentTask task;

    Service service;


    public DownloadTorrentListener( Service service, TorrentTask task) {
        this.task=task;
        this.service=service;
        List<TorrentTaskFile> files= SQLite.select()
                .from(TorrentTaskFile.class)
                .where(TorrentTaskFile_Table.torrentId.eq(task.getId())).queryList();
        task.setFileList(files);
    }

    @Override
    public void onStreamPrepared(Torrent torrent, int index) {
        notifyAdaptor(torrent, task.getTaskFile(index));
        torrent.startDownload();
    }

    void notifyAdaptor(Torrent torrent, TorrentTaskFile fileInfo){
        TorrentFileInfo info=torrent.getFileInfo(fileInfo.getFileIndex());
        fileInfo.setPercent(info.progress());
        updateDataBase(fileInfo);
        Intent intent =
                new Intent(TorrentDownloadService.BROADCAST_TORRENT_FILE_UPDATE);
        intent.putExtra(DOWNLOAD_TORRENT_INDEX, fileInfo.getTorrentId());
        intent.putExtra(DOWNLOAD_TORRENT_FILE_INDEX, fileInfo.getId());
        LocalBroadcastManager.getInstance(service).sendBroadcast(intent);
    }

    @Override
    public void onStreamStarted(Torrent torrent, int index) {
        notifyAdaptor(torrent,task.getTaskFile(index));
    }

    @Override
    public void onStreamError(Torrent torrent, Exception e) {
    }

    @Override
    public void onStreamReady(Torrent torrent,int index) {
        TorrentTaskFile fileInfo=task.getTaskFile(index);;
        fileInfo.setStreamReady(true);
        notifyAdaptor(torrent, fileInfo);
    }

    @Override
    public void onStreamProgress(Torrent torrent, StreamStatus status,int index) {
        TorrentTaskFile taskFile=task.getTaskFile(index);
        taskFile.setPercent(torrent.progress(index));
        task.setSpeed(status.downloadSpeed);
        task.setPercent(torrent.progress());
        notifyAdaptor(torrent, taskFile);
    }

    private void updateDataBase(TorrentTaskFile fileInfo) {
        SQLite.update(TorrentTaskFile.class)
                .set(
                        TorrentTaskFile_Table.percent.eq(fileInfo.getPercent()),
                        TorrentTaskFile_Table.streamReady.eq(fileInfo.isStreamReady()),
                        TorrentTaskFile_Table.downloading.eq(1),
                        TorrentTaskFile_Table.finished.eq(fileInfo.isFinished())
                )
                .where(TorrentTaskFile_Table.id.eq(fileInfo.getId()))
                .async()
                .execute();
        SQLite.update(TorrentTask.class)
                .set(
                        TorrentTask_Table.percent.eq(task.getPercent()),
                        TorrentTask_Table.speed.eq(task.getSpeed())
                )
                .where(TorrentTask_Table.id.eq(task.getId()))
                .async()
                .execute();
    }

    @Override
    public void onStreamStopped() {

    }

    @Override
    public void onDownloadFinish(Torrent torrent, int index) {
        TorrentTaskFile fileInfo= task.getTaskFile(index);;
        fileInfo.setFinished(true);
        notifyAdaptor(torrent, fileInfo);
    }

    @Override
    public void fireAlert(Alert<?> alert) {
        switch (alert.type()){
            case PEER_CONNECT:
                peerConnected((PeerConnectAlert)alert);
                break;
            case PEER_DISCONNECTED:
                peerDisConnected((PeerDisconnectedAlert)alert);
                break;
            case SESSION_STATS:
                sessionStatus((SessionStatsAlert)alert);
                break;
        }
//        String txt= String.format("%s => what:%s msg:%s",alert.type(),alert.what(),alert.message());
//        Log.d("TorrentAlert", txt);
    }

    private void sessionStatus(SessionStatsAlert alert) {
        StringBuilder sb=new StringBuilder();
        for (StatsMetric metric : LibTorrent.sessionStatsMetrics()) {
            String name= metric.name;
            int index=metric.valueIndex;
            long  value = alert.value(index);
            if(value!=0){
                sb.append(name+"=>"+value+"\n");
            }
        }
        Log.d("TorrentAlert", sb.toString());
    }

    private void peerDisConnected(PeerDisconnectedAlert alert) {

    }

    private void peerConnected(PeerConnectAlert alert) {

    }
}
