/*
 * Copyright (C) 2015-2016 Sébastiaan (github.com/se-bastiaan)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.se_bastiaan.torrentstream;

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import com.frostwire.jlibtorrent.Priority;
import com.frostwire.jlibtorrent.SessionManager;
import com.frostwire.jlibtorrent.SessionParams;
import com.frostwire.jlibtorrent.SettingsPack;
import com.frostwire.jlibtorrent.TorrentHandle;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.frostwire.jlibtorrent.alerts.Alert;
import com.frostwire.jlibtorrent.alerts.TorrentAddedAlert;
import com.frostwire.jlibtorrent.swig.settings_pack;
import com.github.se_bastiaan.torrentstream.exceptions.DirectoryModifyException;
import com.github.se_bastiaan.torrentstream.exceptions.NotInitializedException;
import com.github.se_bastiaan.torrentstream.exceptions.TorrentInfoException;
import com.github.se_bastiaan.torrentstream.listeners.DHTStatsAlertListener;
import com.github.se_bastiaan.torrentstream.listeners.TorrentAddedAlertListener;
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener;
import com.github.se_bastiaan.torrentstream.utils.FileUtils;
import com.github.se_bastiaan.torrentstream.utils.ThreadUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public  class TorrentStream {

    static final String LIBTORRENT_THREAD_NAME = "TORRENTSTREAM_LIBTORRENT";
    private static final String STREAMING_THREAD_NAME = "TORRENTSTREAMER_STREAMING";
    private static TorrentStream sThis;

    CountDownLatch initialisingLatch;
    SessionManager torrentSession;
    Boolean initialising = false;
    Boolean initialised = false;
    Boolean isStreaming = false;
    Boolean isCanceled = false;
    TorrentOptions torrentOptions;

    Torrent currentTorrent;
    private String currentTorrentUrl;
    private Integer dhtNodes = 0;

    final List<TorrentListener> listeners = new ArrayList<>();

    HandlerThread libTorrentThread;
    HandlerThread streamingThread;
    Handler libTorrentHandler;
    private Handler streamingHandler;

    final DHTStatsAlertListener dhtStatsAlertListener = new DHTStatsAlertListener() {
        @Override
        public void stats(int totalDhtNodes) {
            dhtNodes = totalDhtNodes;
        }
    };

    TorrentAddedAlertListener torrentAddedAlertListener =null;

    protected TorrentStream(TorrentOptions options) {
        torrentOptions = options;
        initialise();
    }

    public static TorrentStream init(TorrentOptions options) {
        sThis = new TorrentStream(options);
        return sThis;
    }
    public static TorrentStream2 init2(TorrentOptions options) {
        TorrentStream2 sThis2 = new TorrentStream2(options);
        return sThis2;
    }

    public static TorrentStream getInstance() throws NotInitializedException {
        if (sThis == null)
            throw new NotInitializedException();

        return sThis;
    }

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

            libTorrentThread = new HandlerThread(LIBTORRENT_THREAD_NAME);
            libTorrentThread.start();
            libTorrentHandler = new Handler(libTorrentThread.getLooper());
            libTorrentHandler.post(new Runnable() {
                @Override
                public void run() {
                    torrentSession = new SessionManager();
                    setOptions(torrentOptions);

                    torrentSession.addListener(dhtStatsAlertListener);
                    torrentSession.startDht();

                    initialising = false;
                    initialised = true;
                    initialisingLatch.countDown();
                }
            });
        }
    }

    /**
     * Resume TorrentSession
     */
    public void resumeSession() {
        if (libTorrentThread != null && torrentSession != null) {
            libTorrentHandler.removeCallbacksAndMessages(null);

            //resume torrent session if needed
            if (torrentSession.isPaused()) {
                libTorrentHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        torrentSession.resume();
                    }
                });
            }

            if (!torrentSession.isDhtRunning()) {
                libTorrentHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        torrentSession.startDht();
                    }
                });
            }
        }
    }

    /**
     * Pause TorrentSession
     */
    public void pauseSession() {
        if (!isStreaming)
            libTorrentHandler.post(new Runnable() {
                @Override
                public void run() {
                    torrentSession.pause();
                }
            });
    }

    /**
     * Get torrent metadata, either by downloading the .torrent or fetching the magnet
     *
     * @param torrentUrl {@link String} URL to .torrent or magnet link
     * @return {@link TorrentInfo}
     */
    private TorrentInfo getTorrentInfo(String torrentUrl) throws TorrentInfoException {
        if (torrentUrl.startsWith("magnet")) {
            byte[] data = torrentSession.fetchMagnet(torrentUrl, 30000);
            if (data != null)
                try {
                    return TorrentInfo.bdecode(data);
                } catch (IllegalArgumentException e) {
                    throw new TorrentInfoException();
                }

        } else if (torrentUrl.startsWith("http") || torrentUrl.startsWith("https")) {
            try {
                URL url = new URL(torrentUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(true);
                connection.connect();

                InputStream inputStream = connection.getInputStream();

                byte[] responseByteArray = new byte[0];

                if (connection.getResponseCode() == 200) {
                    responseByteArray = getBytesFromInputStream(inputStream);
                }

                inputStream.close();
                connection.disconnect();

                if (responseByteArray.length > 0) {
                    return TorrentInfo.bdecode(responseByteArray);
                }
            } catch (IOException | IllegalArgumentException e) {
                throw new TorrentInfoException();
            }
        } else if (torrentUrl.startsWith("file")) {
            Uri path = Uri.parse(torrentUrl);
            File file = new File(path.getPath());

            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] responseByteArray = getBytesFromInputStream(fileInputStream);
                fileInputStream.close();

                if (responseByteArray.length > 0) {
                    return TorrentInfo.bdecode(responseByteArray);
                }
            } catch (IOException | IllegalArgumentException e) {
                throw new TorrentInfoException();
            }
        }

        return null;
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }

    /**
     * Start stream download for specified torrent
     *
     * @param torrentUrl {@link String} .torrent or magnet link
     */
    public void startStream(final String torrentUrl) {
        if (!initialising && !initialised)
            initialise();

        if (libTorrentHandler == null || isStreaming) return;

        isCanceled = false;

        streamingThread = new HandlerThread(STREAMING_THREAD_NAME);
        streamingThread.start();
        streamingHandler = new Handler(streamingThread.getLooper());

        streamingHandler.post(new Runnable() {
            @Override
            public void run() {
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

                currentTorrentUrl = torrentUrl;

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
                            InternalTorrentListener listener = new InternalTorrentListener();
                            TorrentHandle th = torrentSession.find(alert.handle().infoHash());
                            currentTorrent = new Torrent(th, listener, torrentOptions.prepareSize);

                            torrentSession.addListener(currentTorrent);
                        }
                    };
                }
                torrentSession.removeListener(torrentAddedAlertListener);
                TorrentInfo torrentInfo = null;
                try {
                    torrentInfo = getTorrentInfo(torrentUrl);
                } catch (final TorrentInfoException e) {
                    for (final TorrentListener listener : listeners) {
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onStreamError(null, e);
                            }
                        });
                    }
                }
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
                    priorities[i] = Priority.IGNORE;
                }

                if (!currentTorrentUrl.equals(torrentUrl) || isCanceled) {
                    return;
                }

                torrentSession.download(torrentInfo, saveDirectory, null, priorities, null);
            }
        });
    }

    /**
     * Stop current torrent stream
     */
    public void stopStream() {
        //remove all callbacks from handler
        if (libTorrentHandler != null)
            libTorrentHandler.removeCallbacksAndMessages(null);
        if (streamingHandler != null)
            streamingHandler.removeCallbacksAndMessages(null);

        isCanceled = true;
        isStreaming = false;
        if (currentTorrent != null) {
            final File saveLocation = currentTorrent.getSaveLocation();

            currentTorrent.pause();
            torrentSession.removeListener(currentTorrent);
            torrentSession.remove(currentTorrent.getTorrentHandle());
            currentTorrent = null;

            if (torrentOptions.removeFiles) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int tries = 0;
                        while (!FileUtils.recursiveDelete(saveLocation) && tries < 5) {
                            tries++;
                            try {
                                Thread.sleep(1000); // If deleted failed then something is still using the file, wait and then retry
                            } catch (InterruptedException e) {
                                for (final TorrentListener listener : listeners) {
                                    ThreadUtils.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            listener.onStreamError(currentTorrent, new DirectoryModifyException());
                                        }
                                    });
                                }
                            }
                        }
                    }
                }).start();
            }
        }

        if (streamingThread != null)
            streamingThread.interrupt();

        for (final TorrentListener listener : listeners) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onStreamStopped();
                }
            });
        }
    }

    public TorrentOptions getOptions() {
        return torrentOptions;
    }

    public void setOptions(TorrentOptions options) {
        torrentOptions = options;

        SettingsPack settingsPack = new SettingsPack()
            .anonymousMode(torrentOptions.anonymousMode)
            .connectionsLimit(torrentOptions.maxConnections)
            .downloadRateLimit(torrentOptions.maxDownloadSpeed)
            .uploadRateLimit(torrentOptions.maxUploadSpeed)
            .activeDhtLimit(torrentOptions.maxDht);

        if (torrentOptions.listeningPort != -1) {
            String ifStr = String.format(Locale.ENGLISH, "%s:%d", "0.0.0.0", torrentOptions.listeningPort);
            settingsPack.setString(settings_pack.string_types.listen_interfaces.swigValue(), ifStr);
        }

        if (torrentOptions.proxyHost != null) {
            settingsPack.setString(settings_pack.string_types.proxy_hostname.swigValue(), torrentOptions.proxyHost);
            if (torrentOptions.proxyUsername != null) {
                settingsPack.setString(settings_pack.string_types.proxy_username.swigValue(), torrentOptions.proxyUsername);
                if (torrentOptions.proxyPassword != null) {
                    settingsPack.setString(settings_pack.string_types.proxy_password.swigValue(), torrentOptions.proxyPassword);
                }
            }
        }

        if (torrentOptions.peerFingerprint != null) {
            settingsPack.setString(settings_pack.string_types.peer_fingerprint.swigValue(), torrentOptions.peerFingerprint);
        }

        if (!torrentSession.isRunning()) {
            SessionParams sessionParams = new SessionParams(settingsPack);
            torrentSession.start(sessionParams);
        } else {
            torrentSession.applySettings(settingsPack);
        }
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public String getCurrentTorrentUrl() {
        return currentTorrentUrl;
    }

    public Integer getTotalDhtNodes() {
        return dhtNodes;
    }

    public Torrent getCurrentTorrent() {
        return currentTorrent;
    }

    public void addListener(TorrentListener listener) {
        if (listener != null)
            listeners.add(listener);
    }

    public void removeListener(TorrentListener listener) {
        if (listener != null)
            listeners.remove(listener);
    }

    public void startStream(final TorrentInfo torrentInfo, final int fileIndex) {
        if (!initialising && !initialised)
            initialise();

        if(currentTorrent!=null){
            currentTorrent.addDownloadFile(fileIndex);
            return;
        }
        if (libTorrentHandler == null || isStreaming) return;

        isCanceled = false;

        streamingThread = new HandlerThread(STREAMING_THREAD_NAME);
        streamingThread.start();
        streamingHandler = new Handler(streamingThread.getLooper());

        streamingHandler.post(new Runnable() {
            @Override
            public void run() {
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
                            InternalTorrentListener listener = new InternalTorrentListener();
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
                    priorities[i] = Priority.IGNORE;
                }

                if (isCanceled) {
                    return;
                }

                torrentSession.download(torrentInfo, saveDirectory, null, priorities, null);
            }
        });
    }

    protected class InternalTorrentListener implements TorrentListener {

        public void onStreamStarted(final Torrent torrent, final int index) {
            for (final TorrentListener listener : listeners) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStreamStarted(torrent, index);
                    }
                });
            }
        }

        public void onStreamError(final Torrent torrent, final Exception e) {
            for (final TorrentListener listener : listeners) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStreamError(torrent, e);
                    }
                });
            }
        }

        public void onStreamReady(final Torrent torrent, final int index) {
            for (final TorrentListener listener : listeners) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStreamReady(torrent, index);
                    }
                });
            }
        }

        public void onStreamProgress(final Torrent torrent, final StreamStatus status, final int i) {
            for (final TorrentListener listener : listeners) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStreamProgress(torrent, status, i);
                    }
                });
            }
        }

        @Override
        public void onStreamStopped() {
            // Not used
        }

        @Override
        public void fireAlert(final Alert<?> alert) {
            for (final TorrentListener listener : listeners) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.fireAlert(alert);
                    }
                });
            }
        }

        @Override
        public void onDownloadFinish(final Torrent torrent, final int index) {
            for (final TorrentListener listener : listeners) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onDownloadFinish(torrent, index);
                    }
                });
            }
        }

        @Override
        public void onStreamPrepared(final Torrent torrent, final int index) {
            for (final TorrentListener listener : listeners) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onStreamPrepared(torrent, index);
                    }
                });
            }
        }
    }

}
