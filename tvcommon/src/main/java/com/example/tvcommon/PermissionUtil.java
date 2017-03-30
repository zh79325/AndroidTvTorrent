package com.example.tvcommon;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by zh_zhou on 2017/3/14.
 */

public class PermissionUtil {

    public static void  requestPermission(Activity activity, String ... permissions){
        for (String permission : permissions) {
            int readPermissionCheck = ContextCompat.checkSelfPermission(activity,
                    permission);
            if(readPermissionCheck!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(activity,permissions,1010);
            }
        }
    }

    public static void  requestAppPermission(Activity activity){
        requestPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
}
