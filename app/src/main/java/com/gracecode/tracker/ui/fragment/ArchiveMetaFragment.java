package com.gracecode.tracker.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;
import com.gracecode.tracker.R;
import com.gracecode.tracker.TrackerApplication;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.ui.activity.base.PaceActivity;
import com.gracecode.tracker.util.Helper;
import com.gracecode.tracker.util.PreferencesUtils;


import java.util.ArrayList;
import java.util.Date;


@SuppressLint("ValidFragment")
public class ArchiveMetaFragment extends Fragment {
    public ArchiveMeta meta;
    private Context context;
    private View layoutView;
    private TextView mDistance;
    private TextView mAvgSpeed;
    private TextView mMaxSpeed;
    private TextView mRecords;
    private TextView mCalorie;
    private TextView mClimbing;
    private TextView mPace;
    private TextView mTenPace;
    private LinearLayout linearLayout;
    private String formatter;
    private ArrayList<AMapLocation> tenlocations=new ArrayList<AMapLocation>();

    public ArchiveMetaFragment(Context context, ArchiveMeta meta) {
        this.meta = meta;
        this.context = context;
        this.formatter = context.getString(R.string.records_formatter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutView = inflater.inflate(R.layout.archive_meta_items, container, false);
        mDistance = (TextView) layoutView.findViewById(R.id.item_distance);
        mAvgSpeed = (TextView) layoutView.findViewById(R.id.item_avg_speed);
        mMaxSpeed = (TextView) layoutView.findViewById(R.id.item_max_speed);
        mRecords = (TextView) layoutView.findViewById(R.id.item_records);
        mCalorie = (TextView) layoutView.findViewById(R.id.item_calorie);
        mClimbing = (TextView) layoutView.findViewById(R.id.item_climbing);
        mPace=(TextView) layoutView.findViewById(R.id.item_pace);
        mTenPace =(TextView) layoutView.findViewById(R.id.item_title_pace);
        linearLayout=(LinearLayout)layoutView.findViewById(R.id.ten_pace);
       // getTenLocation();
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TrackerApplication trackerApplication=(TrackerApplication)getActivity().getApplicationContext();
               // trackerApplication.setTenlocations(tenlocations);
                trackerApplication.setMeta(meta);
                Intent intent=new Intent(getActivity(), PaceActivity.class);
              //  intent.putExtra("data",meta);
                startActivity(intent);
            }
        });
        return layoutView;
    }

    @Override
    public void onStart() {
        super.onStart();
       // update();
    }

    public void update() {
        try {
          // mDistance.setText(String.valueOf(meta.getDistance() / ArchiveMeta.TO_KILOMETRE));
            mDistance.setText(String.format(formatter, meta.getDistance() / ArchiveMeta.TO_KILOMETRE));
            mMaxSpeed.setText(String.format(formatter, meta.getMaxSpeed() * ArchiveMeta.KM_PER_HOUR_CNT));
            mAvgSpeed.setText(String.format(formatter, meta.getAverageSpeed() * ArchiveMeta.KM_PER_HOUR_CNT));
            mRecords.setText(String.valueOf(meta.getCount()));
          //  meta.setCalorie(String.valueOf(meta.getDistance() * Float.parseFloat(meta.getWeight())*ArchiveMeta.K));
            meta.setCalorie(String.format(formatter,(meta.getDistance() / 1000) * PreferencesUtils.getFloat(getActivity(), "weight") * ArchiveMeta.K));
           mCalorie.setText(String.format(formatter,(meta.getDistance() / 1000) * PreferencesUtils.getFloat(getActivity(), "weight") * ArchiveMeta.K));
          //  mCalorie.setText(String.format(formatter, meta.getDistance() * 80 * ArchiveMeta.K));
            mClimbing.setText(meta.getClimbingValue());
//            if(meta.getDistance()!=0){
//           //     mPace.setText(getPace((long)(getTime(meta.getStartTime(),meta.getEndTime())/(meta.getDistance()/1000))));
//           if(meta.getEndTime()==null){
//               mPace.setText(getPace((long)(Float.valueOf(meta.getCostTimeStringByNow())/(meta.getDistance()/1000))));
//           }else{
//               mPace.setText(getPace((long)(getTime(meta.getStartTime(),meta.getEndTime())/(meta.getDistance()/1000))));
//           }
//            }else{
//                mPace.setText("0'0\"");
//            }
            if(meta.getSpeedByNow()!=null){

                mPace.setText(getPace((long)(3600 /(Float.valueOf(meta.getSpeedByNow()) * ArchiveMeta.KM_PER_HOUR_CNT)*1000)));
            }else{
                mPace.setText("0'0\"");
            }


        } catch (Exception e) {
            Helper.Logger.e(e.getMessage());
        }
    }

    public void update(ArchiveMeta meta) {
        this.meta = meta;
        this.update(meta);
    }
    public long getTime(Date start,Date end){
        long startTimeStamp = start.getTime();
        long endTimeStamp = end.getTime();
        long between = endTimeStamp - startTimeStamp;
        return between;
    }
    public String getPace(long between){

        long day = between / (24 * 60 * 60 * 1000);
        long hour = (between / (60 * 60 * 1000) - day * 24);
        long minute = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long second = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);
        return minute+"'"+second+"\"";
    }
    public ArrayList<AMapLocation> getTenLocation(){
        ArrayList<AMapLocation> locations = meta.getArchive().fetchAll();
       // Log.i("sss",locations.size()+"");
        AMapLocation lastComputedLocation = null;
        int flag=1;
        float distance = 0;
        for (int i = 0; i < locations.size(); i++) {
            AMapLocation location = locations.get(i);
            if (lastComputedLocation != null) {
                LatLng startLatlng = new LatLng(lastComputedLocation.getLatitude(), lastComputedLocation.getLongitude());
                LatLng endLatlng = new LatLng(location.getLatitude(), location.getLongitude());
// 计算量坐标点距离
                distance += AMapUtils.calculateLineDistance(startLatlng, endLatlng);
                 //AMapUtils.calculateLineDistance(lastComputedLocation.getLongitude(), lastComputedLocation.getLatitude());.calculateLineDistance(location);
                if(distance>10*flag){
                    Log.i("location",location.getTime()+"");
                    tenlocations.add(location);
                    flag++;
                }

            }

            lastComputedLocation = location;
        }

        return tenlocations;
    }

}
