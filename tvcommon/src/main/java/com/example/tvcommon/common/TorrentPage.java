package com.example.tvcommon.common;

import java.util.List;

/**
 * Created by zh_zhou on 2017/3/13.
 */

public class TorrentPage {
    String next;
    String prev;
    List<TorrentLink> links;

    public String getPrev() {
        return prev;
    }

    public void setPrev(String prev) {
        this.prev = prev;
    }

    public String getNext() {
        return next;
    }


    public void setNext(String next) {
        this.next = next;
    }

    public List<TorrentLink> getLinks() {
        return links;
    }

    public void setLinks(List<TorrentLink> links) {
        this.links = links;
    }
}
