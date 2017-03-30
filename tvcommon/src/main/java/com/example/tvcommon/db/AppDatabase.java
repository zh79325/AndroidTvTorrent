package com.example.tvcommon.db;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by zh_zhou on 2017/3/25.
 */

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

    public static final String NAME = "yunTv"; // we will add the .db extension

    public static final int VERSION = 1;
}