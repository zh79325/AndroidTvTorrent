package com.example.yuntv;

import android.app.Application;
import android.os.Environment;
import android.test.ApplicationTestCase;

import java.io.File;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);

        File f= Environment.getExternalStorageDirectory();
        for (File file : f.listFiles()) {
            System.out.println(file.getName());
        }
    }
}