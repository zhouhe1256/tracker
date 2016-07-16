
package com.gracecode.tracker.ui.activity.maps;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.widget.SeekBar;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.ui.activity.Records;
import com.gracecode.tracker.util.Helper;
import com.gracecode.tracker.util.ScreenUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class GaoDeNewMap extends Activity implements SeekBar.OnSeekBarChangeListener {
    private Archiver archiver;
    private MapView mapView;
    private String archiveFileName;
    private AMap aMap;
    private SeekBar mSeeker;
    private SimpleDateFormat dateFormat;
    protected double topBoundary;
    protected double leftBoundary;
    protected double rightBoundary;
    protected double bottomBoundary;
    private Polyline polyline;
    protected Helper helper;

    protected AMapLocation locationTopLeft;
    protected AMapLocation locationBottomRight;
    private double longgitude = 0.0;
    private double latitude = 0.0;
    private double elonggitude = 0.0;
    private double elatitude = 0.0;

    protected float maxDistance;
    protected ArrayList<AMapLocation> locations;
    private static final int DRAW=1;
    private Handler handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case DRAW:
                    getBoundary();
                    //  line();
                    mapLine();
                    //aMap.invalidate();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gaode_new_map);
        mapView = (MapView) findViewById(R.id.gmapsView);
        mapView.onCreate(savedInstanceState);
        if (aMap == null){
            aMap = mapView.getMap();
        }
        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        archiver = new Archiver(getApplicationContext(), archiveFileName);
//        locations = archiver.fetchAll();
//      getBoundary();
//        //  line();
//        mapLine();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                locations = archiver.fetchAll();

                Message msg=new Message();
                msg.what=DRAW;
                handler.sendMessage(msg);

            }
        },1000);

    }

    private void line() {
        for (int i=0;i<locations.size();i++){
            if(longgitude==0.0&&latitude==0.0){
                longgitude = locations.get(i).getLongitude();
                latitude = locations.get(i).getLatitude();
            }else{
                elonggitude = locations.get(i).getLongitude();
                elatitude = locations.get(i).getLatitude();
                setUpline();

            }
        }
    }


    private void mapLine() {
        // TODO Auto-generated method stub
        BitmapDescriptor iconStart = BitmapDescriptorFactory
                .fromResource(R.drawable.point_start);
        BitmapDescriptor iconEnd = BitmapDescriptorFactory
                .fromResource(R.drawable.point_end);
        if (locations != null && locations.size() >= 2) {
            LatLng latLngLocationStart = new LatLng(
                    locations.get(0).getLatitude(), locations
                    .get(0).getLongitude());
            LatLng latLngLocationEnd = new LatLng(
                    locations.get(locations.size() - 1).getLatitude(),
                    locations.get(locations.size() - 1).getLongitude());
            aMap.addMarker(new MarkerOptions()
                    .icon(iconStart)
                    .position(
                            latLngLocationStart));
            aMap.addMarker(new MarkerOptions()
                    .icon(iconEnd).position(
                            latLngLocationEnd));
            double max = locations.get(0).getSpeed();
            double min = locations.get(0).getSpeed();
            // 划线 只需要 集合少一个 速度 2个点 画一条先
            for (int i = 1; i < locations.size() - 1; i++) {
                // 附近的2个点 同时有效 才可以
                // if (locations.get(i).getIsSport() == 1
                //&& locations.get(i + 1).getIsSport() == 1) {
                if (max < locations.get(i).getSpeed()) { // 求最大额
                    // 并复制
                    max = locations.get(i).getSpeed(); // 最大值
                }
                if (min > locations.get(i).getSpeed()) { // 求最小额
                    // 并复制
                    min = locations.get(i).getSpeed(); // 最小值
                }

                // }
            }
            // 最大值 速度 最小值 速度 已经知道了 找到 均值 速度 作为比较
            // 去花颜色
            double avgV = (min + max) / 2; // 均值速度
            int a = (int) (256 / (max - min));
             double cha = max - min;
            //double cha=8.0-0.1;
            LatLngBounds.Builder buider = new LatLngBounds.Builder();
            // 最后一点的速度不要了
            for (int i = 0; i < locations.size() - 1; i++) {
//                if (list.get(i).getIsSport() == 1
//                        && list.get(i + 1).getIsSport() == 1) {
                // 红色
                int red = (int) (locations.get(i).getSpeed() * 255 / cha);
                int green = 255 - red;
                int red2 = (int) (locations.get(i+1).getSpeed() * 255 / cha);
                int green2 = 255 - red2;
                LatLng latlng1 = new LatLng(
                        locations.get(i).getLatitude(),
                        locations.get(i).getLongitude());
                LatLng latlng2 = new LatLng(
                        locations.get(i + 1).getLatitude(),
                        locations.get(i + 1).getLongitude());
                buider.include(latlng1);
                List<Integer> colors=new ArrayList<>();
                colors.add(Color.rgb(red,green, 00));
                colors.add(Color.rgb(red2,green2, 00));
                aMap.addPolyline(
                        (new PolylineOptions()).useGradient(true)
                                .add(latlng1,
                                        latlng2)
                                .width(dp2px(2))
                                        .colorValues(colors))
                        .setGeodesic(true);
                // }

            }
            int widthScreen = ScreenUtils
                    .getScreenWidth(this);
            buider.include(latLngLocationStart)
                    .include(latLngLocationEnd);
            aMap.moveCamera(CameraUpdateFactory
                    .newLatLngBounds(
                            buider.build(),
                            widthScreen,
                            (int) dp2px(200),
                            50));

        }

    }
    private float dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * 绘制轨迹
     */
    private void setUpline(){
        //aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
        polyline = aMap.addPolyline((new PolylineOptions()).add(
                new LatLng(latitude, longgitude), new LatLng(elatitude, elonggitude)).color(
                Color.RED));
       // aMap.invalidate();//刷新地图
        longgitude = elonggitude;
        latitude = elatitude;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    protected void getBoundary() {
        topBoundary=  leftBoundary = locations.get(0).getLatitude();
        rightBoundary=  bottomBoundary = locations.get(0).getLongitude();

        //topBoundary = locations.get(locations.size()-1).getLatitude();
        //rightBoundary = locations.get(locations.size()-1).getLongitude();
        for(int i=0;i<locations.size();i++){
            if(leftBoundary>locations.get(i).getLatitude()){
                leftBoundary=locations.get(i).getLatitude();
            }
            if(bottomBoundary>locations.get(i).getLongitude()){
                bottomBoundary=locations.get(i).getLongitude();
            }
            if(topBoundary<locations.get(i).getLatitude()){
                topBoundary=locations.get(i).getLatitude();
            }
            if(rightBoundary<locations.get(i).getLongitude()){
                rightBoundary=locations.get(i).getLongitude();
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
