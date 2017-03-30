package com.example.tvcommon.common;

import java.io.File;
import java.io.Serializable;

/**
 * Created by zh_zhou on 2017/3/14.
 */

public class TorrentDownloadLink implements Serializable {
    String resourceName;
    String name;
    String url;
    String localPath;
    int parseTime;

    public int getParseTime() {
        return parseTime;
    }

    public void setParseTime(int parseTime) {
        this.parseTime = parseTime;
    }

    boolean processing=true;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
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

    public File storePath(){
        return new File(localPath,name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TorrentDownloadLink that = (TorrentDownloadLink) o;

        if (processing != that.processing) return false;
        if (resourceName != null ? !resourceName.equals(that.resourceName) : that.resourceName != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        return localPath != null ? localPath.equals(that.localPath) : that.localPath == null;

    }

    @Override
    public int hashCode() {
        int result = resourceName != null ? resourceName.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (localPath != null ? localPath.hashCode() : 0);
        result = 31 * result + (processing ? 1 : 0);
        return result;
    }
}
