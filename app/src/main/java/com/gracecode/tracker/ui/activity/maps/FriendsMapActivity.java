package com.gracecode.tracker.ui.activity.maps;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.gracecode.tracker.R;
import com.gracecode.tracker.model.FriendAddress;
import com.gracecode.tracker.util.ScreenUtils;
import com.gracecode.tracker.util.ViewUtils;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobDate;
import cn.bmob.v3.listener.FindListener;

public class FriendsMapActivity extends Activity {
    private MapView mapView;
    private AMap aMap;
    private List<BmobQuery<FriendAddress>> and;
    private long fristTime;
    private TimerTask timerTask;
    private Timer timer;
    private static final int DRAW=1;
    private List<FriendAddress> friendAddresses;
    private int friendAddressCount = 0;//记录每次数据的大小
    protected double topBoundary;
    protected double leftBoundary;
    protected double rightBoundary;
    protected double bottomBoundary;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case DRAW:
                    mapLine();
                    getBoundary();
                    break;
            }
            super.handleMessage(msg);

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_map);
        initView();
        mapView.onCreate(savedInstanceState);
        setData();

    }

    private void setData() {
        and = new ArrayList<BmobQuery<FriendAddress>>();
        BmobQuery<FriendAddress> q1 = new BmobQuery<FriendAddress>();
        Date date  = new Date(new Date().getTime()-(60000*10));
        q1.addWhereGreaterThanOrEqualTo("createdAt",new BmobDate(date));
        q1.setLimit(1000*1000);
        and.add(q1);
        and.get(0).findObjects(this, new FindListener<FriendAddress>() {
            @Override
            public void onSuccess(List<FriendAddress> list) {
                fristTime =
                                list.get(0).getFristTime()>
                                list.get(list.size()-1).getFristTime()?
                                list.get(0).getFristTime():
                                list.get(list.size()-1).getFristTime();
                timerDelayed();
            }

            @Override
            public void onError(int i, String s) {
                fristTime = 0;
            }
        });
    }

    private void queryData() {
        if(fristTime!=0){
        BmobQuery<FriendAddress> eq1 = new BmobQuery<FriendAddress>();
            eq1.addWhereEqualTo("userName","李四");
            eq1.addWhereEqualTo("fristTime",fristTime);
            eq1.setLimit(1000*1000);
            eq1.findObjects(this, new FindListener<FriendAddress>() {
            @Override
            public void onSuccess(List<FriendAddress> list) {
                friendAddresses = list;
                Message msg = handler.obtainMessage();
                msg.what = DRAW;
                handler.sendMessage(msg);
            }

            @Override
            public void onError(int i, String s) {

            }
        });
        }
    }

    private void initView() {
        mapView = (MapView) findViewById(R.id.friends_gmapsView);
        if (aMap == null){
            aMap = mapView.getMap();
        }
        friendAddresses = new ArrayList<FriendAddress>();
        timer = new Timer();
    }

    private void timerDelayed(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                queryData();
            }
        };
        timer.schedule(timerTask,2000,2000);
    }

    private void mapLine() {
        BitmapDescriptor iconStart = BitmapDescriptorFactory
                .fromResource(R.drawable.point_start);

        if (friendAddresses != null && friendAddresses.size() >= 2) {
        LatLng latLngLocationStart = new LatLng(
                friendAddresses.get(0).getLatitude(),
                friendAddresses.get(0).getLongitude());
        aMap.addMarker(new MarkerOptions()
                .icon(iconStart)
                .position(
                        latLngLocationStart));
        LatLngBounds.Builder buider = new LatLngBounds.Builder();
        for (int i = friendAddressCount; i < friendAddresses.size()-1; i++) {
            LatLng latlng1 = new LatLng(
                    friendAddresses.get(i).getLatitude(),
                    friendAddresses.get(i).getLongitude());
            LatLng latlng2 = new LatLng(
                    friendAddresses.get(i + 1).getLatitude(),
                    friendAddresses.get(i + 1).getLongitude());
            buider.include(latlng1);
            aMap.addPolyline(
                            (new PolylineOptions())
                            .useGradient(true)
                            .add(latlng1, latlng2)
                            .width(ViewUtils.dp2px(this,2))
                            .color(Color.GREEN))
                    .setGeodesic(true);
        }
        int widthScreen = ScreenUtils
                .getScreenWidth(this);
        LatLng latLngLocationEnd = new LatLng(
                friendAddresses.get(friendAddresses.size() - 1).getLatitude(),
                friendAddresses.get(friendAddresses.size() - 1).getLongitude());
        buider.include(latLngLocationStart)
                .include(latLngLocationEnd);
        aMap.moveCamera(CameraUpdateFactory
                .newLatLngBounds(
                        buider.build(),
                        widthScreen,
                        (int) ViewUtils.dp2px(this, 200),
                        50)
        );
            friendAddressCount = friendAddresses.size()-1;

        }
    }

    protected void getBoundary() {
        topBoundary=  leftBoundary = friendAddresses.get(0).getLatitude();
        rightBoundary=  bottomBoundary = friendAddresses.get(0).getLongitude();

        //topBoundary = locations.get(locations.size()-1).getLatitude();
        //rightBoundary = locations.get(locations.size()-1).getLongitude();
        for(int i=0;i<friendAddresses.size();i++){
            if(leftBoundary>friendAddresses.get(i).getLatitude()){
                leftBoundary=friendAddresses.get(i).getLatitude();
            }
            if(bottomBoundary>friendAddresses.get(i).getLongitude()){
                bottomBoundary=friendAddresses.get(i).getLongitude();
            }
            if(topBoundary<friendAddresses.get(i).getLatitude()){
                topBoundary=friendAddresses.get(i).getLatitude();
            }
            if(rightBoundary<friendAddresses.get(i).getLongitude()){
                rightBoundary=friendAddresses.get(i).getLongitude();
            }
        }

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(new LatLng(leftBoundary, bottomBoundary + 0.003))
                .include(new LatLng(topBoundary + 0.003, bottomBoundary))
                .include(new LatLng(leftBoundary, rightBoundary + 0.003))
                .include(new LatLng(topBoundary + 0.003, rightBoundary))
                .build();
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));


    }

}
