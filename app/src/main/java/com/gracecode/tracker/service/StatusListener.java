package com.gracecode.tracker.service;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import com.gracecode.tracker.TrackerApplication;
import com.gracecode.tracker.util.Helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class StatusListener implements GpsStatus.Listener {
    private Context context;
    public StatusListener(Context context) {
        this.context = context;
    }

    /**
     * 卫星状态监听器
     */
    private List<GpsSatellite> numSatelliteList = new ArrayList<GpsSatellite>();
    @Override
    public void onGpsStatusChanged(int event) {

        LocationManager locationManager =
                (LocationManager) context.getSystemService(
                        Context.LOCATION_SERVICE);
        GpsStatus status = locationManager.getGpsStatus(null); //取当前状态
        int satelliteInfo = updateGpsStatus(event, status);
        if(satelliteInfo<=0){
            Toast.makeText(context, "没信号", Toast.LENGTH_LONG).show();
            // Recorder.isGpsEnable = false;
//            if(TrackerApplication.getInstance().getPauseStartDate()==null){
//                TrackerApplication.getInstance().setPauseStartDate(new Date());
//                TrackerApplication.getInstance().setPauseEndDate(null);
//              //  TrackerApplication.ff=false;
//            }else{
//                TrackerApplication.getInstance().setPauseStartDate(null);
//            }
        }else{
            // Recorder.isGpsEnable = true;
//            TrackerApplication.getInstance().setPauseEndDate(new Date());
        }

//        switch (event){
//            case GpsStatus.GPS_EVENT_FIRST_FIX:
//                Log.i("GPS","GPS_EVENT_FIRST_FIX");
//                break;
//            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
//                Log.i("GPS","GPS_EVENT_SATELLITE_STATUS");
//                break;
//            case GpsStatus.GPS_EVENT_STARTED:
//                Log.i("GPS","GPS_EVENT_STARTED");
//                break;
//            case GpsStatus.GPS_EVENT_STOPPED:
//                Log.i("GPS","GPS_EVENT_STOPPED");
//                break;
//        }
    }

    private int updateGpsStatus(int event, GpsStatus status) {
        StringBuilder sb2 = new StringBuilder("");
        int c = 0;
        if (status == null) {
            sb2.append("搜索到卫星个数：" +0);
            c = 0;
        } else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            int maxSatellites = status.getMaxSatellites();
            Iterator<GpsSatellite> it = status.getSatellites().iterator();
            numSatelliteList.clear();
            int count = 0;
            while (it.hasNext() && count <= maxSatellites) {
                GpsSatellite s = it.next();
                numSatelliteList.add(s);
                count++;
            }
            sb2.append("搜索到卫星个数：" + numSatelliteList.size());
            if(numSatelliteList.size() == 0){
                c = 0;
            }
            //c = numSatelliteList.size();
        }

        return c;
    }

}
