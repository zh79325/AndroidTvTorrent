package com.example.tvcommon.db.model;

import com.example.tvcommon.db.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.io.File;

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
    boolean finished;
    @Column
    boolean streamReady;
    @Column
    int downloading;

    @Column
    long playPosition;

    public long getPlayPosition() {
        return playPosition;
    }

    public void setPlayPosition(long playPosition) {
        this.playPosition = playPosition;
    }

    public int getDownloading() {
        return downloading;
    }

    public void setDownloading(int downloading) {
        this.downloading = downloading;
    }

    public TorrentTaskFile() {
        percent=-1;
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

    public File getDownloadFile(){
        return new File(storeFolder,fileName);
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
