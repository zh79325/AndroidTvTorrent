package com.example.tvcommon;

import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.TorrentOptions;
import com.github.se_bastiaan.torrentstream.TorrentStream;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;

import org.junit.Test;

import java.io.File;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {

        File torrent=new File("d:\\Users\\zh_zhou\\Desktop\\temp\\Bones.S12E01.HDTV.x264-FLEET[btbtt.co].torrent");
        TorrentOptions torrentOptions = new TorrentOptions.Builder()
                .saveLocation(torrent.getParentFile())
                .removeFilesAfterStop(false)
                .build();
        TorrentStream stream=TorrentStream.init(torrentOptions);
        TorrentListener downloadTorrentListener=new TorrentListener(){

            @Override
            public void onStreamPrepared(Torrent torrent) {

            }

            @Override
            public void onStreamStarted(Torrent torrent) {

            }

            @Override
            public void onStreamError(Torrent torrent, Exception e) {

            }

            @Override
            public void onStreamReady(Torrent torrent) {

            }

            @Override
            public void onStreamProgress(Torrent torrent, StreamStatus status) {

            }

            @Override
            public void onStreamStopped() {

            }

            @Override
            public void fireAlert(Alert<?> alert) {

            }
        };
        TorrentInfo torrentInfo = TorrentInfoUtil.getTorrentInfo(torrent.getAbsolutePath());
        stream.addListener(downloadTorrentListener);
        stream.startStream(torrentInfo,0);
    }

}