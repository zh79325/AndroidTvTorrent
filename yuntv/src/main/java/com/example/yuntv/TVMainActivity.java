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
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tvcommon.PermissionUtil;
import com.example.tvcommon.common.SearchFinishListener;
import com.example.tvcommon.common.TorrentDownloadLink;
import com.example.tvcommon.common.TorrentLink;
import com.example.tvcommon.common.TorrentPage;
import com.example.tvcommon.tool.FileDialog;
import com.example.tvcommon.tool.StringUtils;
import com.example.tvcommon.tool.TorrentSearchUtil;
import com.example.yuntv.ui.SearchResultAdaptor;
import com.github.se_bastiaan.torrentstream.utils.ThreadUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * MainActivity class that loads MainFragment
 */
public class TVMainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    TorrentPage curPage;
    SearchResultAdaptor adaptor;

    Button searchButton,localButton,localHistory,nextButton,prevButton;
    ProgressBar searchIngPb;

    TVMainActivity mainActivity;


    private HandlerThread handlerThread;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvmain);

        handlerThread = new HandlerThread("MAIN_ACTIVITY_THREAD");
        handlerThread.start();
        handler=new Handler(handlerThread.getLooper());


        mainActivity=this;
        searchIngPb=(ProgressBar)findViewById(R.id.searchIngPb);
        searchIngPb.setVisibility(View.GONE);

        nextButton=(Button) findViewById(R.id.next_btn);
        nextButton.setVisibility(View.GONE);
        nextButton.setOnClickListener(mainClickListener);

        prevButton=(Button) findViewById(R.id.prev_btn);
        prevButton.setVisibility(View.GONE);
        prevButton.setOnClickListener(mainClickListener);


        searchButton= (Button) findViewById(R.id.search_btn);
        searchButton.setOnClickListener(mainClickListener);

        localButton= (Button) findViewById(R.id.load_local);
        localButton.setOnClickListener(mainClickListener);


        localHistory=(Button) findViewById(R.id.local_history);
        localHistory.setOnClickListener(mainClickListener);

        adaptor=new SearchResultAdaptor(this,R.layout.search_torrent_result,new ArrayList<TorrentLink>());
        ListView listView = (ListView) findViewById(R.id.result_list_view);
        listView.setAdapter(adaptor);
        listView.setOnItemClickListener(itemClickListener);
        listView.setItemsCanFocus(true);
        PermissionUtil.requestAppPermission(this);
    }
    AdapterView.OnItemClickListener itemClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            TorrentLink link= adaptor.getItem(position);
            if(link.isLoading()&&link.getParseSuccess()==0){
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.wait_for_loading), Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            if(!link.hasDownload()){
                Toast.makeText(getApplicationContext(), getResources().getText(R.string.no_downloads), Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            final List<TorrentDownloadLink> list=new ArrayList<>(link.getDownloads());
            if(list.size()==1){
                selectTorrentLink(list.get(0));
            }else{

                List<String> titles=new ArrayList<>();
                final List<Integer> titleIds=new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    TorrentDownloadLink l=list.get(i);
                    if(!l.isProcessing()){
                        titles.add(l.getName());
                        titleIds.add(i);
                    }
                }
                String []strings=titles.toArray(new String[]{});
                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                builder.setTitle(getResources().getText(R.string.select_torrent));
                builder.setItems(strings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        TorrentDownloadLink choosen = list.get(titleIds.get(which));
                        selectTorrentLink(choosen);
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });

                builder.show();
            }

        }
    };

    private void selectTorrentLink(final TorrentDownloadLink choosen) {
        FileDialog fileDialog1=new FileDialog(this, Environment.getExternalStorageDirectory());
        fileDialog1.setSelectDirectoryOption(true);
        fileDialog1.setName(getResources().getText(R.string.select_save_folder).toString());
        fileDialog1.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
            @Override
            public void directorySelected(final File directory) {
                choosen.setLocalPath(directory.getAbsolutePath());
                Intent intent = new Intent(mainActivity, TorrentDetailActivity.class);
                intent.putExtra(getResources().getString(R.string.torrent), choosen);
                startActivity(intent);
            }
        });
        fileDialog1.showDialog();
    }

    View.OnClickListener mainClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.search_btn:
                    v.requestFocus();
                    searchTorrent();
                    break;
                case  R.id.next_btn:
                    searchIngPb.setVisibility(View.VISIBLE);
                    searchButton.setVisibility(View.GONE);
                    nextButton.setVisibility(View.GONE);
                    prevButton.setVisibility(View.GONE);
                    startSearch(null,false);
                    break;
                case  R.id.prev_btn:
                    searchIngPb.setVisibility(View.VISIBLE);
                    searchButton.setVisibility(View.GONE);
                    nextButton.setVisibility(View.GONE);
                    prevButton.setVisibility(View.GONE);
                    startSearch(null,true);
                    break;
                case R.id.load_local:
                    loadLocalTorrent();
                    break;
                case R.id.local_history:
                    Intent intent = new Intent(mainActivity, LocalHistoryActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    private void loadLocalTorrent() {
        FileDialog fileDialog1=new FileDialog(this, Environment.getExternalStorageDirectory(),".torrent");
        fileDialog1.setName(getResources().getText(R.string.select_from_local).toString());
        fileDialog1.addFileListener(new FileDialog.FileSelectedListener() {
            @Override
            public void fileSelected(File file) {
                TorrentDownloadLink choosen=new TorrentDownloadLink();
                choosen.setName(file.getName());
                choosen.setLocalPath(file.getParentFile().getAbsolutePath());
                Intent intent = new Intent(mainActivity, TorrentDetailActivity.class);
                intent.putExtra(getResources().getString(R.string.torrent), choosen);
                startActivity(intent);
            }
        });

        fileDialog1.showDialog();
    }

    private void searchTorrent() {
        EditText editText=(EditText)findViewById(R.id.search_text);
        String txt=editText.getText().toString();
        if(txt!=null){
            txt=txt.trim().toLowerCase();
        }
        if(StringUtils.isEmpty(txt)){
            Toast.makeText(getApplicationContext(), getResources().getText(R.string.input_key_word), Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        searchIngPb.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.GONE);
        startSearch(txt,false);
    }
    SearchFinishListener listener=new SearchFinishListener() {
        @Override
        public void parsed(TorrentLink link) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adaptor.notifyDataSetChanged();
                }
            });
        }
    };

    private void startSearch(final String keyWork, final boolean prev) {
        Thread th=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!StringUtils.isEmpty(keyWork)){
                        curPage=  TorrentSearchUtil.search(keyWork, listener);
                    }else {
                        curPage=  TorrentSearchUtil.search(curPage,prev, listener);
                    }
                    if(!StringUtils.isEmpty(curPage.getNext())){
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                nextButton.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    if(!StringUtils.isEmpty(curPage.getPrev())){
                        ThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                prevButton.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            searchIngPb.setVisibility(View.GONE);
                            searchButton.setVisibility(View.VISIBLE);
                            adaptor.clear();
                            adaptor.addAll(curPage.getLinks());
                            adaptor.notifyDataSetChanged();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            searchIngPb.setVisibility(View.GONE);
                            searchButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
        handler.post(th);
    }
}
