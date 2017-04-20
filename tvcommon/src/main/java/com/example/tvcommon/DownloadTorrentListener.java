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
import com.frostwire.jlibtorrent.alerts.Alert;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.TorrentFileInfo;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

import static com.example.tvcommon.service.TorrentDownloadService.DOWNLOAD_TASK_FILE_ID;
import static com.example.tvcommon.service.TorrentDownloadService.DOWNLOAD_TASK_ID;


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

    String getCacheString(TorrentTask task,TorrentTaskFile fileInfo){
        String str="task_%d_(p:%.3f,sp:%.2f,f:%b) file_%d_(p%.3f,f%b,sr:%b)";
        return String.format(str,
                task.getId(),
                task.getPercent(),
                task.getSpeed(),
                task.isFinish(),
                fileInfo.getId(),
                fileInfo.getPercent(),
                fileInfo.isFinished(),
                fileInfo.isStreamReady()
                );
    }

    String prevStatus="";

    void notifyAdaptor(Torrent torrent, TorrentTaskFile fileInfo){
        TorrentFileInfo info=torrent.getFileInfo(fileInfo.getFileIndex());
        fileInfo.setPercent(info.progress());
        updateDataBase(fileInfo);

        String status=getCacheString(task,fileInfo);
        if(prevStatus.equalsIgnoreCase(status)){
            Log.d("aaaaaaa","dddddd");
           return;
        }
        prevStatus=status;
        Intent intent =
                new Intent(TorrentDownloadService.BROADCAST_TORRENT_FILE_STATUS);
        intent.putExtra(DOWNLOAD_TASK_ID, fileInfo.getTorrentId());
        intent.putExtra(DOWNLOAD_TASK_FILE_ID, fileInfo.getId());
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
        List<SQLCondition> taskFileCondition=new ArrayList<>();
        if(fileInfo.isFinished()){
            taskFileCondition.add(TorrentTaskFile_Table.finished.eq(fileInfo.isFinished()));
        }
        if(fileInfo.isStreamReady()){
            taskFileCondition.add(TorrentTaskFile_Table.streamReady.eq(fileInfo.isStreamReady()));
        }
        taskFileCondition.add(TorrentTaskFile_Table.percent.eq(fileInfo.getPercent()));
        taskFileCondition.add(TorrentTaskFile_Table.downloading.eq(1));
        SQLCondition[] conditions= taskFileCondition.toArray(new SQLCondition[]{});
        SQLite.update(TorrentTaskFile.class)
                .set(
                        conditions
                )
                .where(TorrentTaskFile_Table.id.eq(fileInfo.getId()))
                .async()
                .execute();
        List<SQLCondition> taskCondition=new ArrayList<>();
        taskCondition.add(TorrentTask_Table.percent.eq(task.getPercent()));
        taskCondition.add(TorrentTask_Table.speed.eq(task.getSpeed()));
        if(task.isFinish()){
            taskCondition.add(TorrentTask_Table.finish.eq(task.isFinish()));
        }
        SQLCondition[] conditions2= taskCondition.toArray(new SQLCondition[]{});
        SQLite.update(TorrentTask.class)
                .set(
                        conditions2
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
        if(torrent.finished()){
            task.setFinish(true);
        }
        notifyAdaptor(torrent, fileInfo);
    }

    @Override
    public void fireAlert(Alert<?> alert) {
//        switch (alert.type()){
//            case PEER_CONNECT:
//                peerConnected((PeerConnectAlert)alert);
//                break;
//            case PEER_DISCONNECTED:
//                peerDisConnected((PeerDisconnectedAlert)alert);
//                break;
//            case SESSION_STATS:
//                sessionStatus((SessionStatsAlert)alert);
//                break;
//        }
//        String txt= String.format("%s => what:%s msg:%s",alert.type(),alert.what(),alert.message());
//        Log.d("TorrentAlert", txt);
    }




}
