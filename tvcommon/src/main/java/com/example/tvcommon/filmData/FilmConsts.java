package com.example.tvcommon.filmData;

/**
 * Created by zh_zhou on 2017/4/1.
 */

public class FilmConsts {
    public static final String BASE_SERVER="https://zhou7758437.github.io/TvRecommend/";

    public static String getUrl(String server,String path){
        while (server.endsWith("/")){
            server=server.substring(0,server.length()-1);
        }
        while (path.startsWith("/")){
            path=path.substring(0);
        }
        return String.format("%s/%s",server,path);
    }

    public static String getBaseConfigUrl(){
        return getUrl(BASE_SERVER,"config.json");
    }

}
