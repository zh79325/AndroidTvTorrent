package com.example.tvcommon.db.model;

import com.example.tvcommon.db.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

/**
 * Created by zh_zhou on 2017/3/25.
 */
@Table(database = AppDatabase.class)
public class TorrentTask extends BaseModel {
    @PrimaryKey(autoincrement = true)
    long id; // package-private recommended, not required
    @Column
    String fileName;
    @Column
    String localPath;
    @Column
    String hash;

    @Column
    String fileStoreFolder;

    List<TorrentTaskFile> fileList;



    public List<TorrentTaskFile> getFileList() {
        return fileList;
    }

    public void setFileList(List<TorrentTaskFile> fileList) {
        this.fileList = fileList;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setFileStoreFolder(String fileStoreFolder) {
        this.fileStoreFolder = fileStoreFolder;
    }

    public String getFileStoreFolder() {
        return fileStoreFolder;
    }
}
