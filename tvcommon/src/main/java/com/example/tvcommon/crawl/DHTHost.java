package com.example.tvcommon.crawl;

/**
 * Created by zh_zhou on 2017/3/15.
 */

public class DHTHost {

    public static final DHTHost BOOTSTRAP_HOST=new DHTHost("router.utorrent.com",6881);

    String host;
    int port;



    public DHTHost(String host, int port)  {
        this.host = host;
        this.port = port;
    }

    public DHTHost() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
