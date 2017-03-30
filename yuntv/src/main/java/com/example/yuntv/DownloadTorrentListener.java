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
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import com.github.se_bastiaan.torrentstream.utils.ThreadUtils;
import com.raizlabs.android.dbflow.sql.language.SQLite;


/**
 * Created by zh_zhou on 2017/3/18.
 */

public class DownloadTorrentListener implements TorrentListener {

    TorrentAdaptor adaptor;
    TorrentTaskFile fileInfo;

    public DownloadTorrentListener(TorrentAdaptor adaptor, TorrentTaskFile fileInfo) {
        this.adaptor = adaptor;
        this.fileInfo = fileInfo;
    }

    @Override
    public void onStreamPrepared(Torrent torrent) {
        notifyAdaptor();
        torrent.startDownload();
    }

    void notifyAdaptor(){
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adaptor.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStreamStarted(Torrent torrent) {
        fileInfo.setPercent(0);
        updateDataBase();
        notifyAdaptor();
    }

    @Override
    public void onStreamError(Torrent torrent, Exception e) {
        notifyAdaptor();
    }

    @Override
    public void onStreamReady(Torrent torrent) {
        fileInfo.setStreamReady(true);
        updateDataBase();
        notifyAdaptor();
    }

    @Override
    public void onStreamProgress(Torrent torrent, StreamStatus status) {
        fileInfo.setPercent(status.progress);
        fileInfo.setSpeed(status.downloadSpeed);
        updateDataBase();
        notifyAdaptor();
    }

    private void updateDataBase() {
        SQLite.update(TorrentTaskFile.class)
                .set(
                        TorrentTaskFile_Table.percent.eq(fileInfo.getPercent()),
                        TorrentTaskFile_Table.speed.eq(fileInfo.getSpeed()),
                        TorrentTaskFile_Table.streamReady.eq(fileInfo.isStreamReady()),
                        TorrentTaskFile_Table.finished.eq(fileInfo.isFinished())
                )
                .where(TorrentTaskFile_Table.id.eq(fileInfo.getId()))
                .execute();
    }

    @Override
    public void onStreamStopped() {
        fileInfo.setFinished(true);
        updateDataBase();
        notifyAdaptor();
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
