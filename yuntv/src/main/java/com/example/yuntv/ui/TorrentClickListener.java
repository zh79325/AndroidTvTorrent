package com.example.yuntv.ui;

import com.example.tvcommon.db.model.TorrentTaskFile;

/**
 * Created by zh_zhou on 2017/3/18.
 */

public interface TorrentClickListener {
    enum EventType{
        Play,
        Download,
        Pause
    }
    void torrentClicked(EventType type,TorrentTaskFile fileInfo);
}
