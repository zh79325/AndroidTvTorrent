package com.example.tvcommon.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.example.tvcommon.DownloadTorrentListener;
import com.example.tvcommon.TorrentInfoUtil;
import com.example.tvcommon.db.model.TorrentTask;
import com.example.tvcommon.db.model.TorrentTaskFile;
import com.example.tvcommon.db.model.TorrentTaskFile_Table;
import com.example.tvcommon.db.model.TorrentTask_Table;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.github.se_bastiaan.torrentstream.TorrentOptions;
import com.github.se_bastiaan.torrentstream.TorrentStream;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.io.IOException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class TorrentDownloadService extends IntentService {
    private static final String ACTION_DOWNLOAD = "com.example.tvcommon.service.action.FOO";
    private static final String ACTION_PAUSE = "com.example.tvcommon.service.action.BAZ";

    public static final String DOWNLOAD_TASK_ID = "com.example.tvcommon.service.extra.PARAM1";
    public static final String DOWNLOAD_TASK_FILE_ID = "com.example.tvcommon.service.extra.PARAM2";

    public static final long ALL_FILES=-1;

    public static final String BROADCAST_TORRENT_UPDATE =
            "com.example.tvcommon.service.action.BROADCAST.TORRENT_UPDATE";
    public static final String BROADCAST_TORRENT_FILE_STATUS =
            "com.example.tvcommon.service.action.BROADCAST.TORRENT_FILE_UPDATE";
    public TorrentDownloadService() {
        super("TorrentDownloadService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startDownload(Context context, long taskId, long taskFileId) {
        Intent intent = new Intent(context, TorrentDownloadService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(DOWNLOAD_TASK_ID, taskId);
        intent.putExtra(DOWNLOAD_TASK_FILE_ID, taskFileId);
        context.startService(intent);
    }

    public static void pauseDownload(Context context, long taskId, long taskFileId) {
        Intent intent = new Intent(context, TorrentDownloadService.class);
        intent.setAction(ACTION_PAUSE);
        intent.putExtra(DOWNLOAD_TASK_ID, taskId);
        intent.putExtra(DOWNLOAD_TASK_FILE_ID, taskFileId);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
                final long taskId = intent.getLongExtra(DOWNLOAD_TASK_ID,ALL_FILES);
                final long taskFileId = intent.getLongExtra(DOWNLOAD_TASK_FILE_ID,ALL_FILES);
                handleDownload(taskId, taskFileId);
            } else if (ACTION_PAUSE.equals(action)) {
                final long taskId = intent.getLongExtra(DOWNLOAD_TASK_ID,ALL_FILES);
                final long taskFileId = intent.getLongExtra(DOWNLOAD_TASK_FILE_ID,ALL_FILES);
                handlePause(taskId, taskFileId);
            }
        }
    }

    static TorrentStream torrentStream;;
    static TorrentInfo torrentInfo;
    static  TorrentTask task;
    TorrentTask getTask(long taskId){
        return SQLite.select()
                .from(TorrentTask.class)
                .where(TorrentTask_Table.id.eq(taskId)).querySingle();
    }
    TorrentTaskFile getTaskFile(long fileId){
        return SQLite.select()
                .from(TorrentTaskFile.class)
                .where(TorrentTaskFile_Table.id.eq(fileId)).querySingle();
    }
    private void handlePause(long taskId, long taskFileId) {
        try {
            if(task==null||task.getId()!=taskId){
                return;
            }

            if(taskFileId==ALL_FILES){
                torrentStream.stopStream();
            }else{
                TorrentTaskFile file=getTaskFile(taskFileId);
                if(file==null){
                    return;
                }
                torrentStream.pauseDwonload(file.getFileIndex());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDownload(long taskId, long taskFileId) {
        try {
            if(task==null){
                initTaskAndStart(taskId,taskFileId);
                return;
            }else if(task.getId()==taskId){
                TorrentTaskFile taskFile=getTaskFile(taskFileId);
                if(taskFile!=null){
                    torrentStream.startStream(torrentInfo,taskFile.getFileIndex());
                }
            }else{
                torrentStream.stopStream();
                initTaskAndStart(taskId,taskFileId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTaskAndStart(long taskId, long taskFileId) throws IOException, InterruptedException {
        task=getTask(taskId);
        if(task==null){
            return;
        }
        torrentInfo=  TorrentInfoUtil.getTorrentInfo(task.getTorrentFile().getAbsolutePath());
        TorrentOptions torrentOptions = new TorrentOptions.Builder()
                .saveLocation(task.getFileStoreFolder())
                .removeFilesAfterStop(false)
                .build();

        DownloadTorrentListener downloadTorrentListener=new DownloadTorrentListener(this,task);
        torrentStream = TorrentStream.init(torrentOptions);
        torrentStream.addListener(downloadTorrentListener);
        if(taskFileId==ALL_FILES){
            torrentStream.startStream(torrentInfo,0);
        }else{
            TorrentTaskFile taskFile=getTaskFile(taskFileId);
            if(taskFile!=null){
                torrentStream.startStream(torrentInfo,taskFile.getFileIndex());
            }
        }


    }


}
