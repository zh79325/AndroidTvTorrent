package com.example.yuntv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.tvcommon.common.TorrentDownloadLink;
import com.example.tvcommon.db.model.TorrentTask;
import com.example.tvcommon.db.model.TorrentTask_Table;
import com.example.yuntv.ui.HistoryAdaptor;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.List;

public class LocalHistoryActivity extends Activity {

    private ListView listView;
    HistoryAdaptor adapter=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_history);

        listView=(ListView)findViewById(R.id.local_history_list);

        SQLite.select()
                .from(TorrentTask.class)
                .orderBy(TorrentTask_Table.id,false)
                .async().queryListResultCallback(new QueryTransaction.QueryResultListCallback<TorrentTask>() {
            @Override
            public void onListQueryResult(QueryTransaction transaction, @NonNull List<TorrentTask> tResult) {
                adapter=new HistoryAdaptor(LocalHistoryActivity.this,R.layout.history_item,tResult);
                listView.setAdapter(adapter);
            }
        }).execute();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TorrentTask t=adapter.getItem(position);
                TorrentDownloadLink choosen=new TorrentDownloadLink();
                choosen.setLocalPath(t.getLocalPath());
                choosen.setName(t.getFileName());
                Intent intent = new Intent(LocalHistoryActivity.this, TorrentDetailActivity.class);
                intent.putExtra(getResources().getString(R.string.torrent), choosen);
                startActivity(intent);
            }
        });
    }
}
