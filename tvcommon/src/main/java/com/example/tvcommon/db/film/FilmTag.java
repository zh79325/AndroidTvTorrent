package com.example.tvcommon.db.film;

import com.example.tvcommon.db.AppDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

/**
 * Created by zh_zhou on 2017/4/1.
 */
@Table(database = AppDatabase.class)
public class FilmTag {
    @PrimaryKey(autoincrement = true)
    long id;
    @Column
    long filmId;
    @Column
    String tag;

    public long getFilmId() {
        return filmId;
    }

    public void setFilmId(long filmId) {
        this.filmId = filmId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
