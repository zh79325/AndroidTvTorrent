package com.example.tvcommon.filmData;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zh_zhou on 2017/4/1.
 */

public class FilmDataUtil {

    static OkHttpClient client=new OkHttpClient();


    public static ConfigItem getConfig() throws IOException {
        String url=FilmConsts.getBaseConfigUrl();
        String json=httpGet(url);
        ConfigItem item= JSON.parseObject(json,ConfigItem.class);
        return item;
    }

    public static String httpGet(String url) throws IOException {

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        String content = response.body().string();
        return content;
    }
}
