
package com.gracecode.tracker.ui.activity.maps;

import android.app.Activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.Projection;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.GroundOverlay;
import com.amap.api.maps2d.model.GroundOverlayOptions;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.ui.activity.Records;
import com.gracecode.tracker.ui.view.MyMap;
import com.gracecode.tracker.util.Helper;
import com.gracecode.tracker.util.ScreenUtils;

import java.util.ArrayList;

public class GaoDeMap extends Activity implements SeekBar.OnSeekBarChangeListener {
    private Archiver archiver;
    private MapView mapView;
    private String archiveFileName;
    private AMap aMap;
    protected double topBoundary;
    protected double leftBoundary;
    protected double rightBoundary;
    protected double bottomBoundary;
    private Polyline polyline;
    protected Helper helper;

    private double longgitude = 0.0;
    private double latitude = 0.0;
    private double elonggitude = 0.0;
    private double elatitude = 0.0;
    LatLngBounds bounds;
    protected ArrayList<AMapLocation> locations;
    protected ArrayList<Integer> stops;
    private MyMap l;
    private GroundOverlay groundoverlay;
    private ArrayList<Point> tracksList = new ArrayList<Point>();
    private boolean b = false;
    private LatLng latl;
    private LatLng latr;
    private LatLng labl;
    private LatLng labr;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                Bitmap bitmap = createViewBitmap(l);
                // 设置图片的显示区域。
//                LatLngBounds bounds = new LatLngBounds.Builder()
//                        .include(
//                                new LatLng(locations.get(locations.size() - 1).getLatitude(),
//                                        locations.get(locations.size() - 1).getLongitude()))
//                        .include(new LatLng(locations.get(0).getLatitude(),
//                                locations.get(0).getLongitude()))
//                        .build();
                int rw = ScreenUtils.getScreenWidth(GaoDeMap.this);
                int rh = ScreenUtils.getScreenHeight(GaoDeMap.this);
                Point tl = new Point(0,0);
                Point tr = new Point(rw,0);
                Point bl = new Point(0,rh);
                Point br = new Point(rw,rh);
                Projection jectionProjection = aMap.getProjection();
                latl = jectionProjection.fromScreenLocation(tl);
                latr = jectionProjection.fromScreenLocation(tr);
                labl = jectionProjection.fromScreenLocation(bl);
                labr = jectionProjection.fromScreenLocation(br);
                LatLngBounds bounds1 = new LatLngBounds.Builder()
                        .include(labl)
                        .include(labr)
                        .include(latl)
                        .include(latr)
                        .build();
                groundoverlay = aMap.addGroundOverlay(new GroundOverlayOptions()
                        .transparency(0.5f)
                        .image(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .positionFromBounds(bounds1));

            }
        }
    };
    public Bitmap createViewBitmap(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gaode_map);
        mapView = (MapView) findViewById(R.id.gmapsView);
        mapView.setVisibility(View.INVISIBLE);
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        l = (MyMap) findViewById(R.id.mymap);
       // l.setVisibility(View.INVISIBLE);
        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        archiver = new Archiver(getApplicationContext(), archiveFileName);
        locations = archiver.fetchAll();
        stops = archiver.stopAll();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getBoundary();
                mapLine();
               // aMap.invalidate();

            }
        }, 1000);

    }

    private void line() {
        for (int i = 0; i < locations.size(); i++) {
            if (longgitude == 0.0 && latitude == 0.0) {
                longgitude = locations.get(i).getLongitude();
                latitude = locations.get(i).getLatitude();
            } else {
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
            for (int i = 0; i < locations.size() - 1; i++) {
                LatLng latLng = new LatLng(locations.get(i).getLatitude(),
                        locations.get(i).getLongitude());
                Projection jectionProjection = aMap.getProjection();
                Point point = jectionProjection.toScreenLocation(latLng);
                tracksList.add(point);
                if (i == locations.size() - 2) { // 最后一个点
                    LatLng latLng1 = new LatLng(locations.get(i + 1).getLatitude(),
                            locations.get(i + 1).getLongitude());
                    Projection jectionProjection1 = aMap.getProjection();
                    Point point1 = jectionProjection1
                            .toScreenLocation(latLng1);
                    tracksList.add(point1);
                }

            }
        }
        l.setDate(this, locations, tracksList, handler,stops);
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
    private void setUpline() {
        // aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
        polyline = aMap.addPolyline((new PolylineOptions()).add(
                new LatLng(latitude, longgitude), new LatLng(elatitude, elonggitude)).color(
                        Color.RED));
        aMap.invalidate();// 刷新地图
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
        topBoundary = leftBoundary = locations.get(0).getLatitude();
        rightBoundary = bottomBoundary = locations.get(0).getLongitude();

        // topBoundary = locations.get(locations.size()-1).getLatitude();
        // rightBoundary = locations.get(locations.size()-1).getLongitude();
        for (int i = 0; i < locations.size(); i++) {
            if (leftBoundary > locations.get(i).getLatitude()) {
                leftBoundary = locations.get(i).getLatitude();
            }
            if (bottomBoundary > locations.get(i).getLongitude()) {
                bottomBoundary = locations.get(i).getLongitude();
            }
            if (topBoundary < locations.get(i).getLatitude()) {
                topBoundary = locations.get(i).getLatitude();
            }
            if (rightBoundary < locations.get(i).getLongitude()) {
                rightBoundary = locations.get(i).getLongitude();
            }
        }

         bounds = new LatLngBounds.Builder()
                .include(new LatLng(leftBoundary, bottomBoundary + 0.003))
                .include(new LatLng(topBoundary + 0.003, bottomBoundary))
                .include(new LatLng(leftBoundary, rightBoundary + 0.003))
                .include(new LatLng(topBoundary + 0.003, rightBoundary))
                .build();
        aMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));

    }

    /**
     * save view as a bitmap
     */
    private Bitmap saveViewBitmap(View view) {
        // get current view bitmap
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);
        Bitmap bitmap = view.getDrawingCache(true);

        Bitmap bmp = duplicateBitmap(bitmap);
        if (bitmap != null && !bitmap.isRecycled()) { bitmap.recycle(); bitmap = null; }
        // clear the cache
        view.setDrawingCacheEnabled(true);
        return bmp;
    }


    public static Bitmap duplicateBitmap(Bitmap bmpSrc)
    {
        if (null == bmpSrc)
        { return null; }

        int bmpSrcWidth = bmpSrc.getWidth();
        int bmpSrcHeight = bmpSrc.getHeight();

        Bitmap bmpDest = Bitmap.createBitmap(bmpSrcWidth, bmpSrcHeight, Bitmap.Config.ARGB_8888); if (null != bmpDest) { Canvas canvas = new Canvas(bmpDest); final Rect rect = new Rect(0, 0, bmpSrcWidth, bmpSrcHeight);

        canvas.drawBitmap(bmpSrc, rect, rect, null); }

        return bmpDest;
    }
}
