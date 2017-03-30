package com.example.tvcommon;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.TorrentOptions;
import com.github.se_bastiaan.torrentstream.TorrentStream;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();


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
