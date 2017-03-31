package com.example.tvcommon.db.model;

import com.example.tvcommon.db.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by zh_zhou on 2017/3/11.
 */
@Table(database = AppDatabase.class)
public class TorrentTaskFile extends BaseModel {
    @PrimaryKey(autoincrement = true)
    long id; // package-private recommended, not required

    @Column
    long torrentId;
    @Column
    String fileName;
    @Column
    String storeFolder;
    @Column
    private int fileIndex;
    @Column
    float percent;
    @Column
    float speed;
    @Column
    boolean finished;
    @Column
    boolean streamReady;
    @Column
    int downloading;

    public int getDownloading() {
        return downloading;
    }

    public void setDownloading(int downloading) {
        this.downloading = downloading;
    }

    public TorrentTaskFile() {
        percent=-1;
        speed=-1;
        finished=false;
        streamReady=false;
        fileIndex=-1;
        downloading=-1;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStoreFolder() {
        return storeFolder;
    }

    public void setStoreFolder(String storeFolder) {
        this.storeFolder = storeFolder;
    }

    public int getFileIndex() {
        return fileIndex;
    }

    public void setFileIndex(int fileIndex) {
        this.fileIndex = fileIndex;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isStreamReady() {
        return streamReady;
    }

    public void setStreamReady(boolean streamReady) {
        this.streamReady = streamReady;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTorrentId() {
        return torrentId;
    }

    public void setTorrentId(long torrentId) {
        this.torrentId = torrentId;
    }

}
