package com.gracecode.tracker.ui.activity.base;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.gracecode.tracker.R;
import com.gracecode.tracker.TrackerApplication;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.model.FriendAddress;
import com.gracecode.tracker.model.UserModel;
import com.gracecode.tracker.ui.activity.FriendsActivity;
import com.gracecode.tracker.ui.activity.Records;
import com.gracecode.tracker.ui.activity.Tracker;
import com.gracecode.tracker.ui.activity.XiaoMi;
import com.gracecode.tracker.util.PreferencesUtils;

import cn.bmob.v3.listener.SaveListener;

public class EditeDataActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText editHeight;
    private EditText editWeight;
    private EditText edittime;
    private EditText editdistance;
    private int height;
    private int weight;
    private ArchiveMeta archiveMeta;
    int time = 0;
    int distance = 0;
    private boolean btime = true;
    private boolean bdistance = true;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edite_data);
//        Archiver archiver=new Archiver(EditeDataActivity.this,ArchiveMeta.TABLE_NAME);
//        archiveMeta=new ArchiveMeta(archiver);
        initView();
       // initData();

    }

    private void initData() {
        if(editHeight.getText().toString()==null||editHeight.getText().toString().isEmpty()){
            Toast.makeText(this,"数据不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else if(editWeight.getText()==null||editWeight.getText().toString().isEmpty()){
            Toast.makeText(this,"数据不能为空",Toast.LENGTH_SHORT).show();
            return;
        }else{
            height=Integer.valueOf(editHeight.getText().toString());
            weight=Integer.valueOf(editWeight.getText().toString());
        }


        if(edittime.getText().toString()==null||edittime.getText().toString().equals("")){
            btime = false;
            time = 0;
        }else{
            String timeS = edittime.getText().toString();
            time = Integer.parseInt(timeS);
            btime = true;
        }
        if(editdistance.getText().toString()==null||editdistance.getText().toString().equals("")){
            bdistance = false;
            distance = 0;
        }else{
            String distanceS = editdistance.getText().toString();
            distance = Integer.parseInt(distanceS);
            bdistance = true;
        }

        if((btime&&time<2)||time>180||time<0){
            Toast.makeText(this,"倒计时时间不能小于2或者大于180",Toast.LENGTH_SHORT).show();
            return;
        }
        if(bdistance&&distance<1||distance>42||distance<0){
            Toast.makeText(this,"选择距离不能小于1或者大于42",Toast.LENGTH_SHORT).show();
            return;
        }
        TrackerApplication.TIME = time;
        TrackerApplication.DISTANCE = distance;
        PreferencesUtils.putFloat(EditeDataActivity.this, "height", height);
        PreferencesUtils.putFloat(EditeDataActivity.this,"weight",weight);
        Intent intent=new Intent(EditeDataActivity.this, Records.class);
        startActivity(intent);
    }

    private void initView() {
        editHeight=(EditText)findViewById(R.id.height);
        editWeight=(EditText)findViewById(R.id.weight);
        edittime=(EditText)findViewById(R.id.time);
        editdistance=(EditText)findViewById(R.id.distance);
    }
   /* private void userName(){
        UserModel userModel = new UserModel();
        userModel.setUserName("张三");
        userModel.save(this, new SaveListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditeDataActivity.this,"保存姓名成功",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int i, String s) {
                Toast.makeText(EditeDataActivity.this,i+","+s,Toast.LENGTH_LONG).show();
            }
        });
    }*/
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button:
                //userName();
                initData();
                break;
            case R.id.imageView:
                finish();
                break;
            case R.id.xm_login:
                Intent intent=new Intent(EditeDataActivity.this, XiaoMi.class);
                startActivity(intent);
                break;
            case R.id.button1:
                Intent intent1=new Intent(EditeDataActivity.this, FriendsActivity.class);
                startActivity(intent1);
                break;
        }
    }
}
