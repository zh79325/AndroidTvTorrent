package com.github.se_bastiaan.torrentstream;

import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.TorrentAddedAlert;
import com.github.se_bastiaan.torrentstream.exceptions.DirectoryModifyException;
import com.github.se_bastiaan.torrentstream.exceptions.TorrentInfoException;
import com.github.se_bastiaan.torrentstream.listeners.TorrentAddedAlertListener;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import com.github.se_bastiaan.torrentstream.utils.ThreadUtils;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zh_zhou on 2017/3/30.
 */

public class TorrentStream2 extends TorrentStream {
    TorrentStream2(TorrentOptions options) {
        super(options);
    }

    @Override
    protected void initialise() {
        if (libTorrentThread != null && torrentSession != null) {
            resumeSession();
        } else {
            if ((initialising || initialised) && libTorrentThread != null) {
                libTorrentThread.interrupt();
            }

            initialising = true;
            initialised = false;
            initialisingLatch = new CountDownLatch(1);

            torrentSession = new SessionManager();
            setOptions(torrentOptions);

            torrentSession.addListener(dhtStatsAlertListener);
            torrentSession.startDht();

            initialising = false;
            initialised = true;
            initialisingLatch.countDown();
        }
    }

    protected class InternalTorrentListener2 implements TorrentListener {

        public void onStreamStarted(final Torrent torrent, final int index) {
            for (final TorrentListener listener : listeners) {
                listener.onStreamStarted(torrent, index);
            }
        }

        public void onStreamError(final Torrent torrent, final Exception e) {
            for (final TorrentListener listener : listeners) {
                listener.onStreamError(torrent, e);
            }
        }

        public void onStreamReady(final Torrent torrent, final int index) {
            for (final TorrentListener listener : listeners) {
                listener.onStreamReady(torrent, index);
            }
        }

        public void onStreamProgress(final Torrent torrent, final StreamStatus status, final int i) {
            for (final TorrentListener listener : listeners) {
                listener.onStreamProgress(torrent, status, i);
            }
        }

        @Override
        public void onStreamStopped() {
            // Not used
        }

        @Override
        public void fireAlert(final Alert<?> alert) {
            for (final TorrentListener listener : listeners) {
                listener.fireAlert(alert);
            }
        }

        @Override
        public void onDownloadFinish(final Torrent torrent, final int index) {
            for (final TorrentListener listener : listeners) {
                listener.onDownloadFinish(torrent, index);
            }
        }

        @Override
        public void onStreamPrepared(final Torrent torrent, final int index) {
            for (final TorrentListener listener : listeners) {
                listener.onStreamPrepared(torrent, index);
            }
        }
    }


    @Override
    public void startStream(final TorrentInfo torrentInfo, final int fileIndex) {
        if (!initialising && !initialised)
            initialise();

        if(currentTorrent!=null){
            currentTorrent.addDownloadFile(fileIndex);
            return;
        }
        if ( isStreaming) return;

        isCanceled = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                {
                    isStreaming = true;

                    if (initialisingLatch != null) {
                        try {
                            initialisingLatch.await();
                            initialisingLatch = null;
                        } catch (InterruptedException e) {
                            isStreaming = false;
                            return;
                        }
                    }


                    File saveDirectory = new File(torrentOptions.saveLocation);
                    if (!saveDirectory.isDirectory() && !saveDirectory.mkdirs()) {
                        for (final TorrentListener listener : listeners) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onStreamError(null, new DirectoryModifyException());
                                }
                            });
                        }
                        isStreaming = false;
                        return;
                    }
                    if(torrentAddedAlertListener==null){
                        torrentAddedAlertListener=  new TorrentAddedAlertListener() {
                            @Override
                            public void torrentAdded(TorrentAddedAlert alert) {
                                InternalTorrentListener2 listener = new InternalTorrentListener2();
                                TorrentHandle th = torrentSession.find(alert.handle().infoHash());
                                currentTorrent = new Torrent(fileIndex,th, listener, torrentOptions.prepareSize);

                                torrentSession.addListener(currentTorrent);
                            }
                        };
                    }
//                torrentSession.removeListener(torrentAddedAlertListener);
                    torrentSession.addListener(torrentAddedAlertListener);

                    if (torrentInfo == null) {
                        for (final TorrentListener listener : listeners) {
                            ThreadUtils.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onStreamError(null, new TorrentInfoException());
                                }
                            });
                        }
                        isStreaming = false;
                        return;
                    }

                    Priority[] priorities = new Priority[torrentInfo.numFiles()];
                    for (int i = 0; i < priorities.length; i++) {
                        priorities[i] = Priority.SEVEN;
                    }

                    if (isCanceled) {
                        return;
                    }

                    torrentSession.download(torrentInfo, saveDirectory, null, priorities, null);
                }
            }
        }).start();


    }
}
