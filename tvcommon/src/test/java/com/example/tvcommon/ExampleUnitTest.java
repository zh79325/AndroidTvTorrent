package com.example.tvcommon;

import com.frostwire.jlibtorrent.LibTorrent;
import com.frostwire.jlibtorrent.StatsMetric;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.SessionStatsAlert;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.TorrentOptions;
import com.github.se_bastiaan.torrentstream.TorrentStream;
import com.github.se_bastiaan.torrentstream.TorrentStream2;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;

import org.junit.Test;

import java.io.File;

import static com.frostwire.jlibtorrent.alerts.AlertType.SESSION_STATS;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {

        System.out.println(new File("").getAbsolutePath());
        File torrent=new File("d:\\Users\\zh_zhou\\Desktop\\2.torrent");

        TorrentInfo torrentInfo = TorrentInfoUtil.getTorrentInfo(torrent.getAbsolutePath());

        TorrentOptions torrentOptions = new TorrentOptions.Builder()
                .saveLocation(new File(torrent.getParentFile(),torrentInfo.name()))
                .removeFilesAfterStop(false)
                .build();
        TorrentStream2 stream=TorrentStream.init2(torrentOptions);
        TorrentListener downloadTorrentListener=new TorrentListener(){

            @Override
            public void onStreamPrepared(Torrent torrent, int index) {
                System.out.println("onStreamPrepared");
            }

            @Override
            public void onStreamStarted(Torrent torrent, int index) {
                System.out.println("onStreamStarted");
            }

            @Override
            public void onStreamError(Torrent torrent, Exception e) {
                System.out.println("onStreamError");
            }

            @Override
            public void onStreamReady(Torrent torrent, int index) {
                System.out.println("onStreamReady");
            }

            @Override
            public void onStreamProgress(Torrent torrent, StreamStatus status, int i) {
                System.out.println("onStreamProgress");
            }

            @Override
            public void onStreamStopped() {
                System.out.println("onStreamStopped");
            }

            @Override
            public void fireAlert(Alert<?> alert) {

               if(SESSION_STATS==alert.type()){
                   SessionStatsAlert a=(SessionStatsAlert)alert;
                   for (StatsMetric metric : LibTorrent.sessionStatsMetrics()) {
                       String name= metric.name;
                       int i= metric.valueIndex;

                       long  v = a.value(i);
                       if(v>0){
                           System.out.println(name+"=>"+v);
                       }
                   }
               }
                System.out.println("\n\n\n\n\n\n");
            }

            @Override
            public void onDownloadFinish(Torrent torrentExtend, int index) {
                System.out.println("onDownloadFinish");
            }
        };

        stream.addListener(downloadTorrentListener);
        stream.startStream(torrentInfo, 0);
        Thread.sleep(2000);
        for (int i = 0; i < torrentInfo.files().numFiles(); i++) {
            stream.startStream(torrentInfo, i);
        }
        Thread.sleep(1000*1000000);
    }

}