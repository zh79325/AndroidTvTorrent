package com.example.tvcommon.db.film;

import com.example.tvcommon.db.AppDatabase;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

/**
 * Created by zh_zhou on 2017/4/1.
 */
@Table(database = AppDatabase.class)
public class FilmDataVersion {
    @PrimaryKey(autoincrement = true)
    long id; // package-private recommended, not required

}
