package com.gracecode.tracker.ui.activity.base;



import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;
import com.gracecode.tracker.R;
import com.gracecode.tracker.TrackerApplication;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.ui.adapter.PaceAdapter;
import com.gracecode.tracker.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PaceActivity extends AppCompatActivity implements View.OnClickListener{
    private ListView listview;
    private ArrayList<AMapLocation> locations;
    private ArrayList<Map<String,String>> maps=new ArrayList<Map<String,String>>();
    private ArrayList<Double> distances=new ArrayList<Double>();
    private ArrayList<AMapLocation> tenlocations=new ArrayList<AMapLocation>();
    private TrackerApplication trackerApplication;
    private ArchiveMeta meta;
    private static final int SETDATA=1;
    private static final int GETDATA=2;
    private PaceAdapter paceAdapter;
    private TextView pace_a;
    private TextView pace_k;
    private TextView pace_s;
    private  long maxMap=0;
    private long minMap=0;
    private float ave;
    private long min;
    private long max;
    private double minDistance;
    private double maxDistance;
    //  private DataThread dataThread;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
//                case SETDATA:
//                   // getData();
//                    break;
                case GETDATA:
                    paceAdapter.setData(maps);
                    paceAdapter.notifyDataSetChanged();
                    pace_k.setText(String.valueOf(getPace(min)));
                    pace_s.setText(String.valueOf(getPace(max)));
                    pace_a.setText(aveSpeed);
                    break;
            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pace);

        //   locations=trackerApplication.getTenlocations();
        meta=TrackerApplication.getInstance().getMeta();
        initView();
        // initDatas();

        paceAdapter=new PaceAdapter(PaceActivity.this,maps);
        listview.setAdapter(paceAdapter);
        DataThread();

    }

    private void initDatas(){
        for(int i=0;i<10;i++){
            Map<String,String> map=new HashMap<String,String>();
            map.put("time",""+i);
            map.put("pace",""+i+"\"");
            maps.add(map);
        }
    }


    private void initView() {
        pace_a=(TextView)findViewById(R.id.pace_1);
        pace_k=(TextView)findViewById(R.id.pace_2);
        pace_s=(TextView)findViewById(R.id.pace_3);
        listview=(ListView)findViewById(R.id.list_pace);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imageView:
                finish();
                break;
        }
    }

    public void DataThread(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getTenLocation();
            }
        }, 1000);
    }
    public void getData(){
        for(int i=1;i<tenlocations.size();i++){
            double dis=distances.get(i - 1);
            AMapLocation location=tenlocations.get(i);
            AMapLocation location1=tenlocations.get(i-1);
            LatLng startLatlng = new LatLng(location1.getLatitude(), location1.getLongitude());
            LatLng endLatlng = new LatLng(location.getLatitude(), location.getLongitude());
            // 计算量坐标点距离
           long l=0;
            if(i==1){
                 l=(long)((location.getTime() - location1.getTime()) / dis);
            }else{
                 l=(long)((location.getTime() - location1.getTime()) / (dis-distances.get(i - 2)));
            }
            Map<String,String> map=new HashMap<String,String>();
            map.put("time",""+i);
            map.put("pace",String.valueOf(getPace(l)));
            maps.add(map);

            if(max==0&&min==0){
               max=l;
                min=l;

            }
            if (min > l){
                min = l;


            }

            if (max < l){
                max = l;

            }

        }

    }


    public String getPace(long between){

//        long day = between / (24 * 60 * 60 * 1000);
//        long hour = (between / (60 * 60 * 1000) - day * 24);
//        long minute = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
//        long second = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);
     long minute=between/60;
        long second=between-minute*60;
        return minute+"'"+second+"\"";
    }
    public String getPacelong(long between){

        long day = between / (24 * 60 * 60 * 1000);
        long hour = (between / (60 * 60 * 1000) - day * 24);
        long minute = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long second = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);
//        long minute=between/60;
//        long second=between-minute*60;
        return minute+"'"+second+"\"";
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            //    trackerApplication.clearDate();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
private String aveSpeed=null;
    public ArrayList<AMapLocation> getTenLocation(){
        float d=meta.getDistance();
        long bwn= TimeUtils.getTime(meta.getStartTime(), meta.getEndTime());
        if(d!=0){
            aveSpeed=String.format(TimeUtils.getPace((long) ((bwn) / (d / ArchiveMeta.TO_KILOMETRE))));
        }

        ave=meta.getAverageSpeed();
//        max=meta.getMaxSpeed();
//        min=meta.getMinSpeed();
        ArrayList<AMapLocation> locations = meta.getArchive().fetchAll();
        //  Log.i("SIZE",locations.size()+"");
        AMapLocation lastComputedLocation = null;
        int flag=1;
        double distance = 0;
        for (int i = 0; i < locations.size(); i++) {
            AMapLocation location = locations.get(i);
            if (lastComputedLocation != null) {
                LatLng startLatlng = new LatLng(lastComputedLocation.getLatitude(), lastComputedLocation.getLongitude());
                LatLng endLatlng = new LatLng(location.getLatitude(), location.getLongitude());
                // 计算量坐标点距离
                distance += AMapUtils.calculateLineDistance(startLatlng, endLatlng);
                //AMapUtils.calculateLineDistance(lastComputedLocation.getLongitude(), lastComputedLocation.getLatitude());.calculateLineDistance(location);
//if(i>145){
//    double a=distance;
//    Log.i("location", location.getTime() + "");
//}
                if(distance>=(double)(1000*flag)){
                    tenlocations.add(location);
                    distances.add(distance);
                    flag++;
                }

            }else{
                tenlocations.add(location);
            }
            lastComputedLocation = location;
        }
        if(tenlocations.size()>1||aveSpeed!=null){
            getData();
            Message msg=new Message();
            msg.what=GETDATA;
            handler.sendMessage(msg);
        }
        return tenlocations;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // meta.getArchive().close();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // meta.getArchive().close();
    }
}
