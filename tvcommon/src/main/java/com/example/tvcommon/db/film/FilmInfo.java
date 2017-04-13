package com.example.tvcommon.db.film;

import com.alibaba.fastjson.annotation.JSONField;
import com.example.tvcommon.db.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import java.util.List;

/**
 * Created by zh_zhou on 2017/4/1.
 */
@Table(database = AppDatabase.class)
public class FilmInfo {
    @PrimaryKey(autoincrement = true)
    long id;
    @JSONField(name = "n")
    String name;
    @Column
    int year;

    List<String> tags;
    @Column
    String description;
    @Column
    float socre;
    @Column
    String area;
    @Column
    String language;
    @Column
    String cover;
    List<String> imgs;
    @Column
    String torrentUrl;
    @Column
    String group;
    List<FilmVideos> videos;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getSocre() {
        return socre;
    }

    public void setSocre(float socre) {
        this.socre = socre;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public List<String> getImgs() {
        return imgs;
    }

    public void setImgs(List<String> imgs) {
        this.imgs = imgs;
    }

    public String getTorrentUrl() {
        return torrentUrl;
    }

    public void setTorrentUrl(String torrentUrl) {
        this.torrentUrl = torrentUrl;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<FilmVideos> getVideos() {
        return videos;
    }

    public void setVideos(List<FilmVideos> videos) {
        this.videos = videos;
    }
}
