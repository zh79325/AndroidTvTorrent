package com.example.yuntv.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.tvcommon.db.model.TorrentTask;
import com.example.tvcommon.db.model.TorrentTaskFile;
import com.example.tvcommon.db.model.TorrentTaskFile_Table;
import com.example.tvcommon.db.model.TorrentTask_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import static com.example.tvcommon.service.TorrentDownloadService.ALL_FILES;
import static com.example.tvcommon.service.TorrentDownloadService.DOWNLOAD_TORRENT_FILE_INDEX;
import static com.example.tvcommon.service.TorrentDownloadService.DOWNLOAD_TORRENT_INDEX;

/**
 * Created by zh_zhou on 2017/4/14.
 */

public class DownloadStateReceiver extends BroadcastReceiver {
    TorrentAdaptor adaptor;
    public DownloadStateReceiver(TorrentAdaptor adaptor) {
        this.adaptor=adaptor;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId = intent.getLongExtra(DOWNLOAD_TORRENT_INDEX,ALL_FILES);
        long taskFileId = intent.getLongExtra(DOWNLOAD_TORRENT_FILE_INDEX,ALL_FILES);
        TorrentTask task= SQLite.select()
                .from(TorrentTask.class)
                .where(TorrentTask_Table.id.eq(taskId)).querySingle();

        TorrentTaskFile taskFile=
                SQLite.select()
                        .from(TorrentTaskFile.class)
                        .where(TorrentTaskFile_Table.id.eq(taskFileId)).querySingle();
        if(adaptor.getTaskId()!=task.getId()){
            return;
        }
        adaptor.updateTaskUI(task);
        adaptor.updateTaskFile(taskFile);


    }
}
