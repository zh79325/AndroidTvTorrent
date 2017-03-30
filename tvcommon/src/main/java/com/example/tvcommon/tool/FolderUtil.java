package com.example.tvcommon.tool;

import android.os.Environment;

import java.io.File;

/**
 * Created by zh_zhou on 2017/3/25.
 */

public class FolderUtil {
    static final String APP_MAIN_FOLDER="yunTv";
    static final String CACHE="cache";
    static final String DOWNLOAD="download";


    static File getFolder(String path){
        File f=new File( Environment.getExternalStorageDirectory(),path);
        if(!f.exists()){
            f.mkdirs();
        }
        return f;
    }

    public   static File getCacheFile(String fileName){
        File folder=getFolder(APP_MAIN_FOLDER+"/"+CACHE);
        return new File(folder,fileName);
    }
    public   static File getDownloadFolder(String fileName){
        return getFolder(APP_MAIN_FOLDER+"/"+DOWNLOAD+"/"+fileName);
    }
}
