package com.gracecode.tracker.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gracecode.tracker.R;
import com.gracecode.tracker.adapter.FriendsAdapter;
import com.gracecode.tracker.ui.activity.maps.FriendsMapActivity;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends Activity {
    private ListView friendListView;
    private List<String> users;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        initView();
        setData();
        setListener();

    }

    private void setListener() {
        friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(FriendsActivity.this, FriendsMapActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        friendListView = (ListView) findViewById(R.id.firend_list);
        users = new ArrayList<String>();
        users.add("李四");
    }
    private void setData(){
        friendListView.setAdapter(new FriendsAdapter(this,users));
    }
}
