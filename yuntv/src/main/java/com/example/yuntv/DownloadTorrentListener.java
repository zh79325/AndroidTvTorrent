package com.example.yuntv;

import android.util.Log;

import com.example.tvcommon.db.model.TorrentTaskFile;
import com.example.tvcommon.db.model.TorrentTaskFile_Table;
import com.example.yuntv.ui.TorrentAdaptor;
import com.frostwire.jlibtorrent.StatsMetric;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.PeerConnectAlert;
import com.frostwire.jlibtorrent.alerts.PeerDisconnectedAlert;
import com.frostwire.jlibtorrent.alerts.SessionStatsAlert;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.TorrentFileInfo;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import com.github.se_bastiaan.torrentstream.utils.ThreadUtils;
import com.raizlabs.android.dbflow.sql.language.SQLite;


/**
 * Created by zh_zhou on 2017/3/18.
 */

public class DownloadTorrentListener implements TorrentListener {

    TorrentAdaptor adaptor;
    TorrentTaskFile task;


    public DownloadTorrentListener(TorrentAdaptor adaptor, TorrentTaskFile task) {
        this.adaptor = adaptor;
        this.task=task;
    }

    @Override
    public void onStreamPrepared(Torrent torrent, int index) {
        notifyAdaptor(torrent, index);
        torrent.startDownload();
    }

    void notifyAdaptor(Torrent torrent, int index){
        TorrentFileInfo info=torrent.getFileInfo(index);
        TorrentTaskFile fileInfo= adaptor.getItem(index);
        fileInfo.setPercent(info.progress());
        updateDataBase(fileInfo);
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adaptor.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStreamStarted(Torrent torrent, int index) {
        notifyAdaptor(torrent,index);
    }

    @Override
    public void onStreamError(Torrent torrent, Exception e) {
    }

    @Override
    public void onStreamReady(Torrent torrent,int index) {
        TorrentTaskFile fileInfo= adaptor.getItem(index);
        fileInfo.setStreamReady(true);
        notifyAdaptor(torrent, index);
    }

    @Override
    public void onStreamProgress(Torrent torrent, StreamStatus status,int index) {
        task.setPercent(torrent.progress());
        task.setSpeed(status.downloadSpeed);
        adaptor.updateTaskUI(task);
        notifyAdaptor(torrent, index);
    }

    private void updateDataBase(TorrentTaskFile fileInfo) {
        SQLite.update(TorrentTaskFile.class)
                .set(
                        TorrentTaskFile_Table.percent.eq(fileInfo.getPercent()),
                        TorrentTaskFile_Table.streamReady.eq(fileInfo.isStreamReady()),
                        TorrentTaskFile_Table.finished.eq(fileInfo.isFinished())
                )
                .where(TorrentTaskFile_Table.id.eq(fileInfo.getId()))
                .async()
                .execute();
    }

    @Override
    public void onStreamStopped() {

    }

    @Override
    public void onDownloadFinish(Torrent torrent, int index) {
        TorrentTaskFile fileInfo= adaptor.getItem(index);
        fileInfo.setFinished(true);
        notifyAdaptor(torrent, index);
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
        String txt= String.format("%s => what:%s msg:%s",alert.type(),alert.what(),alert.message());
        Log.d("TorrentAlert", txt);
    }

    private void sessionStatus(SessionStatsAlert alert) {
        long  dhtNodes = alert.value(StatsMetric.DHT_NODES_GAUGE_INDEX);
        long receiverNum=alert.value(StatsMetric.NET_RECV_IP_OVERHEAD_BYTES_COUNTER_INDEX);
        Log.d("TorrentAlert", "NodesNum=>"+dhtNodes+"");
    }

    private void peerDisConnected(PeerDisconnectedAlert alert) {

    }

    private void peerConnected(PeerConnectAlert alert) {

    }
}
