/*
 *
 *  * This file is part of TorrentStreamer-Android.
 *  *
 *  * TorrentStreamer-Android is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Lesser General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * TorrentStreamer-Android is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with TorrentStreamer-Android. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.se_bastiaan.torrentstreamer.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.tvcommon.db.model.TorrentTaskFile;
import com.example.tvcommon.TorrentInfoUtil;
import com.example.tvcommon.tool.FileDialog;
import com.frostwire.jlibtorrent.FileStorage;
import com.frostwire.jlibtorrent.TorrentInfo;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity  {


    private Button button;
    private ProgressBar progressBar;



    private HandlerThread torrentThread;
    private Handler torrentHandler;
    private static final  String APP_TORRENT_THREAD="APP_LIBTORRENT_THREAD_NAME";
    MainActivity current;

    private String streamUrl = "magnet:?xt=urn:btih:88594aaacbde40ef3e2510c47374ec0aa396c08e&dn=bbb%5Fsunflower%5F1080p%5F30fps%5Fnormal.mp4&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=udp%3A%2F%2Ftracker.publicbt.com%3A80%2Fannounce&ws=http%3A%2F%2Fdistribution.bbb3d.renderfarming.net%2Fvideo%2Fmp4%2Fbbb%5Fsunflower%5F1080p%5F30fps%5Fnormal.mp4";

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button:
                    File mPath = Environment.getExternalStorageDirectory();

                    FileDialog fileDialog = new FileDialog(current, mPath, ".torrent");
                    fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                        public void fileSelected(final File file) {
                            Thread th=new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        final TorrentInfo torrentInfo = TorrentInfoUtil.getTorrentInfo(file.getAbsolutePath());
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                FileDialog fileDialog1=new FileDialog(current,Environment.getExternalStorageDirectory());
                                                fileDialog1.setSelectDirectoryOption(true);
                                                fileDialog1.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
                                                    @Override
                                                    public void directorySelected(File directory) {

                                                        if(torrentInfo==null){
                                                        }else {
                                                            List<TorrentTaskFile> infoList=new ArrayList<TorrentTaskFile>();
                                                            FileStorage fs=  torrentInfo.origFiles();
                                                            for (int i = 0; i < fs.numFiles(); i++) {
                                                                String name=fs.fileName(i);
                                                                TorrentTaskFile info=new TorrentTaskFile();
                                                                info.setFileName(name);
                                                                info.setFileIndex(i);
                                                                infoList.add(info);
                                                            }
                                                            TorrentAdaptor adaptor=new TorrentAdaptor(current,R.layout.torrent_file,infoList);
                                                            ListView listView = (ListView) findViewById(R.id.list_view1);
                                                            listView.setAdapter(adaptor);
                                                        }
                                                    }
                                                });
                                                fileDialog1.showDialog();

                                            }
                                        });
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            th.start();
                        }
                    });
                    fileDialog.showDialog();
                    break;

            }


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        current=this;
        torrentThread = new HandlerThread(APP_TORRENT_THREAD);
        torrentThread.start();
        torrentHandler=new Handler(torrentThread.getLooper());


        String action = getIntent().getAction();
        Uri data = getIntent().getData();
        if (action != null && action.equals(Intent.ACTION_VIEW) && data != null) {
            try {
                streamUrl = URLDecoder.decode(data.toString(), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }




        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(onClickListener);
        findViewById(R.id.button2).setOnClickListener(onClickListener);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        progressBar.setMax(100);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
    }




}
