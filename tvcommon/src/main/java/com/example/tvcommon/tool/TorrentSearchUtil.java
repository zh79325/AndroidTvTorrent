package com.example.tvcommon.tool;

import com.example.tvcommon.common.SearchFinishListener;
import com.example.tvcommon.common.TorrentDownloadLink;
import com.example.tvcommon.common.TorrentLink;
import com.example.tvcommon.common.TorrentPage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zh_zhou on 2017/3/13.
 */

public class TorrentSearchUtil {
    private static OkHttpClient client = null;


    synchronized static OkHttpClient getClient(){
        if(client==null){
            client=new OkHttpClient();
        }
        return client;
    }

    public static TorrentPage search(String keyWord,SearchFinishListener listener) throws IOException {
        return getPage(keyWord,null,listener);
    }
    public static TorrentPage search(TorrentPage page,boolean prev,SearchFinishListener listener) throws IOException {
        String url=prev?page.getPrev():page.getNext();
        return getPage(null,url,listener);
    }

    static final String server="http://www.btbtt.co/";

    static TorrentPage getPage(String key,String url,SearchFinishListener listener) throws IOException {


        if(!StringUtils.isEmpty(key)){
            key = URLEncoder.encode(key, "utf8");
            url= String.format("%ssearch-index-keyword-%s.htm",server, key);
        }else{
            url=server+url;
        }


        Document doc = getDocument(url);

        TorrentPage page = new TorrentPage();

        Elements tds = doc.select("a.subject_link");
        List<TorrentLink> links=new ArrayList<>();
        page.setLinks(links);
        for (int i = 0; i < tds.size(); i++) {
            Element a= tds.get(i);
            String name= a.attr("title");
            if(!StringUtils.isEmpty(name)){
                name=name.replaceAll("<span class=red>","");
                name=name.replaceAll("</span>","");
            }
            String link=a.attr("href");
            if(StringUtils.isEmpty(name)||StringUtils.isEmpty(link)){
                continue;
            }
            TorrentLink torrentLink=new TorrentLink();
            torrentLink.setName(name);
            torrentLink.setUrl(link);
            parseLinkDownload(torrentLink,listener);
            links.add(torrentLink);
        }
        Elements next=doc.select("div.page");
        if(next!=null&&next.size()>0){
            Elements nextLinks= next.get(0).select("a");
            if(nextLinks!=null&&nextLinks.size()>0){

                for (int i = 0; i < nextLinks.size(); i++) {
                    Element a= nextLinks.get(i);
                    String nextUrl=a.attr("href");
                    String txt=a.text();
                    if("下一页".equalsIgnoreCase(txt)){
                        page.setNext(nextUrl);
                    }
                    if("上一页".equalsIgnoreCase(txt)){
                        page.setPrev(nextUrl);
                    }
                }

            }
        }

        return page;
    }

    private static Document getDocument(String url) throws IOException {
        OkHttpClient client = getClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        String content = response.body().string();
        return Jsoup.parse(content);
    }


    private static void parseLinkDownload(final TorrentLink link,final SearchFinishListener listener) throws IOException {
        String url=server+link.getUrl();
        OkHttpClient client = getClient();

        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                link.setLoading(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String content = response.body().string();
                Document doc = Jsoup.parse(content);
                Elements lists=  doc.select("div.attachlist table a");
                Set<TorrentDownloadLink> downloads=new HashSet<TorrentDownloadLink>();
                ExecutorService executorService= Executors.newFixedThreadPool(2);
                for (int i = 0; i < lists.size(); i++) {
                    Element e=lists.get(i);
                    Elements img=e.select("img");
                    if(img.size()>0){
                        TorrentDownloadLink downloadLink=new TorrentDownloadLink();
                        String url=server+e.attr("href");
                        String name=e.text();
                        downloadLink.setName(name);
                        downloadLink.setUrl(url);
                        downloadLink.setResourceName(link.getName());
                        link.setId(i);
                        parseRealLink(executorService,link,downloadLink,listener);
                        downloads.add(downloadLink);
                    }
                }
                if(downloads.size()==0){
                    link.setLoading(false);
                }
                link.setDownloads(downloads);
                if(listener!=null){
                    listener.parsed(link);
                }
            }
        });

    }

    private static void parseRealLink(final ExecutorService executorService, final TorrentLink link, final TorrentDownloadLink downloadLink, final SearchFinishListener listener) throws IOException {

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    String url=downloadLink.getUrl();
                    OkHttpClient client = getClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response =  client.newCall(request).execute();
                    String content = response.body().string();
                    downloadLink.setProcessing(false);
                    Document doc = Jsoup.parse(content);
                    Elements elements=  doc.select("dd a");
                    for (int i = 0; i < elements.size(); i++) {
                        Element element=  elements.get(i);
                        String href=element.attr("href");
                        String target=element.attr("target");
                        if(!StringUtils.isEmpty(href)&&!StringUtils.isEmpty(target)){
                            downloadLink.setUrl(href);
                            if(!href.contains("attach-download")){
                                parseRealLink(executorService, link,downloadLink,listener);
                            }
                        }
                    }
                    checkFinish(link,listener);
                }catch (Exception ex){
                    int time= downloadLink.getParseTime();
                    if(time>1){
                        if(listener!=null){
                            listener.parsed(link);
                        }
                        downloadLink.setProcessing(false);
                        checkFinish(link,listener);
                    }else{
                        time++;
                        downloadLink.setParseTime(time);
                        try {
                            parseRealLink(executorService, link,downloadLink,listener);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });



    }

    private static void checkFinish(TorrentLink link, SearchFinishListener listener) {
        if(!link.hasDownload()){
            link.setLoading(false);
            if(listener!=null){
                listener.parsed(link);
            }
        }
        boolean finish=true;
        Set<TorrentDownloadLink> downloadLinks= link.getDownloads();
        if(downloadLinks==null){
            finish=false;
        }else{
            int parseSuccess=0;
            for (TorrentDownloadLink downloadLink : link.getDownloads()) {
                if(downloadLink.isProcessing()){
                    finish=false;
                }else{
                    parseSuccess++;
                }
            }
            link.setParseSuccess(parseSuccess);
            if(listener!=null){
                listener.parsed(link);
            }
        }

        if(finish){
            link.setLoading(false);
            if(listener!=null){
                listener.parsed(link);
            }
        }
    }
}
