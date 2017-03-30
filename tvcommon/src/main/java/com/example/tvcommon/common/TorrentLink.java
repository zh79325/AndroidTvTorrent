package com.example.tvcommon.common;

import java.util.Set;

/**
 * Created by zh_zhou on 2017/3/13.
 */

public class TorrentLink {
    String name;
    String url;
    boolean loading;
    Set<TorrentDownloadLink> downloads;
    int id;
    int parseSuccess;

    public int getParseSuccess() {
        return parseSuccess;
    }

    public void setParseSuccess(int parseSuccess) {
        this.parseSuccess = parseSuccess;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TorrentLink( ) {
        loading=true;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public Set<TorrentDownloadLink> getDownloads() {
        return downloads;
    }

    public boolean hasDownload(){
        return downloads!=null&&downloads.size()>0;
    }
    public void setDownloads(Set<TorrentDownloadLink> downloads) {
        this.downloads = downloads;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
