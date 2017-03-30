package com.example.tvcommon;

import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by zh_zhou on 2017/3/6.
 */

public class TorrentInfoUtil {

    public static TorrentInfo getTorrentInfo(String torrentUrl) throws IOException, InterruptedException {
        TorrentInfo info=null;
        if(torrentUrl.startsWith(("magnet"))){
            info=readFromMagnet(torrentUrl);
        }else if(torrentUrl.startsWith("http") || torrentUrl.startsWith("https")){
            URL url = new URL(torrentUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            if (connection.getResponseCode() == 200) {
                info = getTorrentFromInputStream(inputStream);
            }
            inputStream.close();
            connection.disconnect();
        }else {
            File file = new File(torrentUrl);
            info  = new TorrentInfo(file);
        }
        return info;
    }

    private static TorrentInfo readFromMagnet(String torrentUrl) throws InterruptedException, IOException {
        final SessionManager s = new SessionManager();
        s.start();

        final CountDownLatch signal = new CountDownLatch(1);

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long nodes = s.stats().dhtNodes();
                // wait for at least 10 nodes in the DHT.
                if (nodes >= 10) {
                    signal.countDown();
                    timer.cancel();
                }
            }
        }, 0, 1000);

        boolean r = signal.await(10, TimeUnit.SECONDS);
        if (!r) {
            s.stop();
            timer.cancel();
            throw new IOException(torrentUrl+" not found");
        }

        byte[] data = s.fetchMagnet(torrentUrl,10000);
        timer.cancel();
        s.stop();
        return TorrentInfo.bdecode(data);
    }


    private static TorrentInfo getTorrentFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return TorrentInfo.bdecode( byteBuffer.toByteArray());
    }
}
