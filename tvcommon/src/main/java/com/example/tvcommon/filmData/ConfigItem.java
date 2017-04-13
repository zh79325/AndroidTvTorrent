package com.example.tvcommon.filmData;

import java.util.List;

/**
 * Created by zh_zhou on 2017/4/1.
 */

public class ConfigItem {
    String dataCenter;
    String imgCenter;
    List<ConfigFilmData> films;

    public String getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public String getImgCenter() {
        return imgCenter;
    }

    public void setImgCenter(String imgCenter) {
        this.imgCenter = imgCenter;
    }

    public List<ConfigFilmData> getFilms() {
        return films;
    }

    public void setFilms(List<ConfigFilmData> films) {
        this.films = films;
    }
}
