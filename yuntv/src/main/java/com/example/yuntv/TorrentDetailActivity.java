/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.yuntv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tvcommon.TorrentInfoUtil;
import com.example.tvcommon.common.TorrentDownloadLink;
import com.example.tvcommon.db.model.TorrentTask;
import com.example.tvcommon.db.model.TorrentTaskFile;
import com.example.tvcommon.db.model.TorrentTaskFile_Table;
import com.example.tvcommon.db.model.TorrentTask_Table;
import com.example.tvcommon.tool.FolderUtil;
import com.example.tvcommon.tool.Md5Util;
import com.example.yuntv.ui.TorrentAdaptor;
import com.example.yuntv.ui.TorrentClickListener;
import com.frostwire.jlibtorrent.FileStorage;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.github.se_bastiaan.torrentstream.TorrentOptions;
import com.github.se_bastiaan.torrentstream.TorrentStream;
import com.github.se_bastiaan.torrentstream.utils.ThreadUtils;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*
 * MainActivity class that loads MainFragment
 */
public class TorrentDetailActivity extends Activity {
    private static final java.lang.String APP_TORRENT_THREAD = "Torrent_detail_thread";
    /**
     * Called when the activity is first created.
     */

    private HandlerThread torrentThread;
    private Handler torrentHandler;

    private TorrentDownloadLink link;
    TorrentAdaptor adaptor;
    TextView torrentName,torrentStatus;
    ProgressBar torrentProgress;
    Button deleteBtn;

    TorrentStream  []torrentStreams;;
    TorrentInfo torrentInfo;
    TorrentTask task;
    Context context;

    Runnable downloadThread=new Runnable() {
        @Override
        public void run() {
            updateStatus(false,"正在下载种子文件");

            File f=link.storePath();

            downloadTorrent(f);
            if(!f.exists()){
                downloadTorrent(f);
            }
            if(f.exists()){
                torrentHandler.post(torrentParseThread);
            }

        }
    };

    private void downloadTorrent(File f) {
        OkHttpClient client=new OkHttpClient();
        Request request = new Request.Builder()
                .url(link.getUrl())
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            byte [] bytes = response.body().bytes();
            if(f.exists()){
                f.delete();
            }
            FileOutputStream writer = null;
            try
            {
                writer = new FileOutputStream ( f);
                writer.write( bytes);
            }
            catch ( IOException e)
            {
            }
            finally
            {
                try
                {
                    if ( writer != null)
                        writer.close( );
                }
                catch ( IOException e)
                {
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void updateStatus(final boolean finish, final String text){
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(finish){
                    adaptor.notifyDataSetChanged();
                    torrentProgress.setVisibility(View.GONE);
                }else{
                    torrentProgress.setVisibility(View.VISIBLE);
                }
                torrentStatus.setText(text);
            }
        });
    }
    Runnable torrentParseThread=new Runnable() {
        @Override
        public void run() {
            updateStatus(false,"正在解析种子文件");
            File file=link.storePath();

            task=new TorrentTask();
            task.setFileName(file.getName());
            task.setLocalPath(file.getParentFile().getAbsolutePath());
            String hash= Md5Util.getFileMD5(file);
            task.setHash(hash);
            TorrentTask storedTask= SQLite.select()
                    .from(TorrentTask.class)
                    .where(TorrentTask_Table.hash.eq(task.getHash())).querySingle();
            if(storedTask==null){
                long id=  task.insert();
                task.setId(id);
            }else{
                File storedTorrent=new File(storedTask.getLocalPath(),storedTask.getFileName());
                if(storedTorrent.exists()){
                    task= storedTask;
                }else{
                    SQLite.update(TorrentTask.class)
                            .set(
                                    TorrentTask_Table.fileName.eq(task.getFileName()),
                                    TorrentTask_Table.hash.eq(task.getHash()),
                                    TorrentTask_Table.localPath.eq(task.getLocalPath())
                            )
                            .where(TorrentTask_Table.id.eq(storedTask.getId()))
                            .execute();
                    task=SQLite.select()
                            .from(TorrentTask.class)
                            .where(TorrentTask_Table.id.eq(storedTask.getId())).querySingle();
                }
            }

            List<TorrentTaskFile> files=SQLite.select()
                    .from(TorrentTaskFile.class)
                    .where(TorrentTaskFile_Table.torrentId.eq(task.getId())).queryList();
            task.setFileList(files);
            task.update();
            if(files==null||files.size()==0){
                try {
                    torrentInfo = TorrentInfoUtil.getTorrentInfo(file.getAbsolutePath());
                    files=new ArrayList<TorrentTaskFile>();
                    FileStorage fs=  torrentInfo.origFiles();
                    String nameFolder= torrentInfo.name();//.substring(0,task.getFileName().lastIndexOf("."));
                    File storeFolder= FolderUtil.getDownloadFolder(nameFolder);
                    task.setFileStoreFolder(storeFolder.getAbsolutePath());


                    SQLite.update(TorrentTask.class)
                            .set(
                                    TorrentTask_Table.fileStoreFolder.eq(task.getFileStoreFolder())
                            )
                            .where(TorrentTask_Table.id.eq(task.getId()))
                            .execute();

                    for (int i = 0; i < fs.numFiles(); i++) {
                        String name=fs.fileName(i);
                        TorrentTaskFile info=new TorrentTaskFile();
                        info.setFileName(name);
                        info.setStoreFolder(storeFolder.getAbsolutePath());
                        info.setFileIndex(i);
                        info.setTorrentId(task.getId());
                        long id=info.insert();
                        info.setId(id);
                        files.add(info);
                    }
                    files=SQLite.select()
                            .from(TorrentTaskFile.class)
                            .where(TorrentTaskFile_Table.torrentId.eq(task.getId())).queryList();
                    task.setFileList(files);
                    updateStatus(true,"解析完成");
                } catch (Exception e) {
                    e.printStackTrace();
                    updateStatus(true,"解析失败");
                    file.delete();
                }
            }else{
                updateStatus(true,"解析完成");
            }
            final List<TorrentTaskFile> adaptorFiles=task.getFileList();
            torrentStreams=new TorrentStream[adaptorFiles.size()];
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adaptor.clear();
                    adaptor.addAll(adaptorFiles);
                    deleteBtn.setVisibility(View.VISIBLE);
                }
            });


        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torrent_detail);
        context=this;
        torrentThread = new HandlerThread(APP_TORRENT_THREAD);
        torrentThread.start();
        torrentHandler=new Handler(torrentThread.getLooper());


        torrentName=(TextView)findViewById(R.id.torrent_name);
        torrentStatus=(TextView)findViewById(R.id.torrent_status);
        torrentProgress=(ProgressBar)findViewById(R.id.torrent_process_progress);
        deleteBtn=(Button)findViewById(R.id.delete_torrent);
        deleteBtn.setVisibility(View.GONE);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                torrentHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        new AlertDialog.Builder(TorrentDetailActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("删除种子及文件")
                                .setMessage("是否确认删除种子及全部文件?")
                                .setPositiveButton("是", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        for (TorrentStream stream : torrentStreams) {
                                            if(stream!=null){
                                                stream.stopStream();
                                            }
                                        }
                                        if(task.getFileStoreFolder()!=null){
                                            File f=new File(task.getFileStoreFolder());
                                            if(f.exists()){
                                                f.delete();
                                            }
                                        }

                                    }

                                })
                                .setNegativeButton("否", null)
                                .show();

                    }
                });
            }
        });
        adaptor=new TorrentAdaptor(this,R.layout.torrent_file,new ArrayList<TorrentTaskFile>());

        adaptor.setListener(new TorrentClickListener() {
            @Override
            public void torrentClicked(EventType type, TorrentTaskFile fileInfo) {
                fileInfo.setSpeed(10000);
                adaptor.notifyDataSetChanged();
            }
        });
        ListView listView = (ListView) findViewById(R.id.torrent_file_list);
        listView.setAdapter(adaptor);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final TorrentTaskFile fileInfo=  adaptor.getItem(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(fileInfo.getFileName());
                int downloading=fileInfo.getDownloading();
                List<String>  ops=new ArrayList<String>();
                final String DOWNLOAD="下载";
                final String PAUSE="暂停";
                final String CONTINUE="继续";
                final String PLAY="播放";
                if(downloading==-1){
                    ops.add(DOWNLOAD);
                }else if(downloading==0){
                    ops.add(CONTINUE);
                }else if(downloading==1){
                    ops.add(PAUSE);
                }
                if(fileInfo.isStreamReady()){
                    ops.add(PLAY);
                }

                final CharSequence [] sequences=new CharSequence[ops.size()];
                for (int i = 0; i < ops.size(); i++) {
                    sequences[i]=ops.get(i);
                }
                builder.setItems(sequences,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String text=  sequences[which].toString();


                                switch (text){
                                    case DOWNLOAD:
                                        fileInfo.setDownloading(1);
                                        startDownload(fileInfo);
                                        break;
                                    case PAUSE:
                                        fileInfo.setDownloading(0);
                                        pauseDownload(fileInfo);
                                        break;
                                    case CONTINUE:
                                        fileInfo.setDownloading(1);
                                        startDownload(fileInfo);
                                        break;
                                    case PLAY:
                                        playDownload(fileInfo);
                                        break;
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adaptor.notifyDataSetChanged();
                                    }
                                });
                            }
                        });
                builder.create().show();

            }});

        Bundle  bundle  = getIntent().getExtras();
        if(bundle!=null){
            link= (TorrentDownloadLink) bundle.get(getResources().getString(R.string.torrent));
        }
        if(link!=null){
            File f=link.storePath();
            torrentName.setText(f.getName());
            if(!f.exists()){
                torrentHandler.post(downloadThread);
            }else{
                torrentHandler.post(torrentParseThread);
            }
        }else{
            torrentName.setVisibility(View.GONE);
            torrentStatus.setVisibility(View.GONE);
            torrentProgress.setVisibility(View.GONE);

        }
    }

    private void playDownload(TorrentTaskFile fileInfo) {
        Toast.makeText(context,"start play",Toast.LENGTH_LONG).show();
    }

    private void pauseDownload(TorrentTaskFile fileInfo) {
        TorrentStream stream=getStream(fileInfo);
        stream.stopStream();
    }

    private void startDownload(TorrentTaskFile fileInfo) {
        TorrentStream stream=getStream(fileInfo);
        if(stream!=null){
            stream.startStream(torrentInfo,fileInfo.getFileIndex());
        }
    }
    TorrentStream getStream(final TorrentTaskFile fileInfo){
        final int i=fileInfo.getFileIndex();
        if(torrentStreams[i]==null){
            torrentHandler.post(new Runnable() {
                @Override
                public void run() {
                    TorrentOptions torrentOptions = new TorrentOptions.Builder()
                            .saveLocation(fileInfo.getStoreFolder())
                            .removeFilesAfterStop(false)
                            .build();
                    DownloadTorrentListener downloadTorrentListener=new DownloadTorrentListener(adaptor,fileInfo);
                    torrentStreams[i] = TorrentStream.init(torrentOptions);
                    torrentStreams[i].addListener(downloadTorrentListener);
                    torrentStreams[i].startStream(torrentInfo,fileInfo.getFileIndex());
                }
            });

        }
        return torrentStreams[i];
    }

}
