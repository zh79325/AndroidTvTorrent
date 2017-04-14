package com.example.yuntv;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.tvcommon.db.model.TorrentTask;
import com.example.tvcommon.db.model.TorrentTaskFile;
import com.example.tvcommon.db.model.TorrentTaskFile_Table;
import com.example.tvcommon.db.model.TorrentTask_Table;
import com.example.tvcommon.service.TorrentDownloadService;
import com.example.tvcommon.video.AndroidMediaController;
import com.example.tvcommon.video.IjkVideoView;
import com.example.tvcommon.video.VideoPlayListener;
import com.github.se_bastiaan.torrentstream.utils.ThreadUtils;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.io.File;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayVideoActivity extends AppCompatActivity {

    private AndroidMediaController mMediaController;
    private IjkVideoView mVideoView;
    private TextView mToastTextView;
    private ProgressBar progressBar;
    private TableLayout mHudView;
    TorrentTask task;
    TorrentTaskFile videoFile;
    ActionBar actionBar;
    long current=0;

    private VideoPlayListener playListener = new VideoPlayListener() {
        @Override
        public void infoFresh(IjkMediaPlayer mp) {
            current=mp.getCurrentPosition();
            videoFile.setPlayPosition(current);
            SQLite.update(TorrentTaskFile.class)
                    .set(
                            TorrentTaskFile_Table.playPosition.eq(videoFile.getPlayPosition())
                    )
                    .where(TorrentTaskFile_Table.id.eq(videoFile.getId()))
                    .execute();
        }


    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        // init player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        actionBar = getSupportActionBar();

        mMediaController = new AndroidMediaController(this, false);
        mMediaController.setSupportActionBar(actionBar);

        mToastTextView = (TextView) findViewById(R.id.toast_text_view);
        mHudView = (TableLayout) findViewById(R.id.hud_view);
        progressBar=(ProgressBar) findViewById(R.id.video_loading_progress);


        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setHudView(mHudView,playListener);


        progressBar.setVisibility(View.VISIBLE);
        mVideoView.setVisibility(View.GONE);

        Bundle  bundle  = getIntent().getExtras();
        if(bundle!=null){
            final long taskId=bundle.getLong(TorrentDownloadService.DOWNLOAD_TASK_ID);
            final long fileId=bundle.getLong(TorrentDownloadService.DOWNLOAD_TASK_FILE_ID);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    task=SQLite.select()
                            .from(TorrentTask.class)
                            .where(TorrentTask_Table.id.eq(taskId)).querySingle();
                    videoFile=  SQLite.select()
                            .from(TorrentTaskFile.class)
                            .where(TorrentTaskFile_Table.id.eq(fileId)).querySingle();
                    final String mVideoPath=videoFile.getDownloadFile().getAbsolutePath() ;
                    actionBar.setTitle(videoFile.getFileName());
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mVideoView.setVideoPath(mVideoPath);
                            mVideoView.setStartPosition(current);
                            progressBar.setVisibility(View.GONE);
                            mVideoView.setVisibility(View.VISIBLE);
                            mVideoView.start();
                        }
                    });

                }
            }).start();
        }



    }
    void searchVideo(File folder, List<File> videos){
        for (File file : folder.listFiles()) {
            if(file.isDirectory()){
                searchVideo(file,videos);
            }else{
                if(file.getAbsolutePath().endsWith(".mp4")){
                    videos.add(file);
                }
            }
        }
    }
}
