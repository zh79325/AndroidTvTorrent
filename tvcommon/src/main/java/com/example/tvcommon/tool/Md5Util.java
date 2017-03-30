package com.example.tvcommon.tool;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by zh_zhou on 2017/3/25.
 */

public class Md5Util {
    //获取单个文件MD5
    public static String getFileMD5(File file)
    {
        if(!file.isFile())
        {
            return null;
        }
        MessageDigest digest=null;
        FileInputStream in=null;
        byte buffer[]=new byte[1024];
        int len;
        try
        {
            digest= MessageDigest.getInstance("MD5");
            in=new FileInputStream(file);
            while((len=in.read(buffer, 0, 1024))!=-1)
            {
                digest.update(buffer, 0, len);
            }
            in.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }

        BigInteger bigint=new BigInteger(1,digest.digest());
        return bigint.toString(16);
    }
}
