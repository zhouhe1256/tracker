package com.gracecode.tracker.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.gracecode.tracker.ui.activity.AltitudeiCharts;
import com.gracecode.tracker.ui.activity.ChartsTotal;
import com.gracecode.tracker.ui.activity.Records;
import com.gracecode.tracker.ui.activity.base.PaceActivity;
import com.gracecode.tracker.util.Helper;
import com.gracecode.tracker.util.PreferencesUtils;

import java.util.ArrayList;
import java.util.Date;


@SuppressLint("ValidFragment")
public class ArchiveNewMetaFragment extends Fragment {
    private String archiveFileName;
    public ArchiveMeta meta;
    private Context context;
    private View layoutView;
    private TextView mDistance;
    private TextView mbupin;
    private TextView mbufu;
    private TextView mStepCount;
    private TextView mAvgSpeed;
    private TextView mMaxSpeed;
    private TextView mRecords;
    private TextView mCalorie;
    private TextView mClimbing;
   // private TextView mPace;
   // private TextView mTenPace;
    private LinearLayout linearLayout;
    private LinearLayout paceLayout;
    private String formatter;
    private ArrayList<AMapLocation> tenlocations=new ArrayList<AMapLocation>();

    public ArchiveNewMetaFragment(Context context, ArchiveMeta meta, String archiveFileName) {
        this.meta = meta;
        this.context = context;
        this.formatter = context.getString(R.string.records_formatter);
        this.archiveFileName=archiveFileName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutView = inflater.inflate(R.layout.archive_new_meta_items, container, false);
        //step_count
        mbupin = (TextView) layoutView.findViewById(R.id.bupin);
        mbufu = (TextView) layoutView.findViewById(R.id.bufu);
        mStepCount = (TextView) layoutView.findViewById(R.id.step_count);
        mDistance = (TextView) layoutView.findViewById(R.id.item_distance);
        mAvgSpeed = (TextView) layoutView.findViewById(R.id.item_avg_speed);
        mMaxSpeed = (TextView) layoutView.findViewById(R.id.item_max_speed);
        mRecords = (TextView) layoutView.findViewById(R.id.item_records);
        mCalorie = (TextView) layoutView.findViewById(R.id.item_calorie);
        mClimbing = (TextView) layoutView.findViewById(R.id.item_climbing);
      //  mPace=(TextView) layoutView.findViewById(R.id.item_pace);
      //  mTenPace =(TextView) layoutView.findViewById(R.id.item_title_pace);
        linearLayout=(LinearLayout)layoutView.findViewById(R.id.ten_pace);
        paceLayout=(LinearLayout)layoutView.findViewById(R.id.pace_layout);
       // getTenLocation();
        paceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TrackerApplication.getInstance().setMeta(meta);
                Intent intent = new Intent(context, PaceActivity.class);
                intent.putExtra("avg",mAvgSpeed.getText().toString());
                startActivity(intent);
            }
        });
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                TrackerApplication trackerApplication=(TrackerApplication)getActivity().getApplicationContext();
//                trackerApplication.setTenlocations(tenlocations);
//                Intent intent=new Intent(getActivity(), PaceActivity.class);
//              //  intent.putExtra("data",meta);
//                startActivity(intent);

                Intent intent = new Intent(context, ChartsTotal.class);
                intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
                startActivity(intent);
            }
        });
        //update();
        return layoutView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        update();
    }

    @Override
    public void onStart() {
        super.onStart();
       // update();
    }

    public void update() {
        try {
            String step=meta.getStep();

            float distance=meta.getDistance();
            float maxspeed=meta.getMaxSpeed();
            long bwn=getTime(meta.getStartTime(), meta.getEndTime());
           mStepCount.setText(step);
            mbupin.setText(String.valueOf(Integer.valueOf(step) / ((bwn)/(1000*60))));
            if(step.equals("0")){
                mbufu.setText("0");
            }else{
                mbufu.setText(String.format(formatter,(distance * 100) / Integer.valueOf(step)));
            }

            mDistance.setText(String.format(formatter, distance / ArchiveMeta.TO_KILOMETRE));
            mMaxSpeed.setText(String.format(formatter, maxspeed * ArchiveMeta.KM_PER_HOUR_CNT));
           // mAvgSpeed.setText(String.format(getPace((long)(3600 /(Float.valueOf(meta.getAverageSpeed()) * ArchiveMeta.KM_PER_HOUR_CNT)*1000))));
            mAvgSpeed.setText(String.format(getPace((long)((bwn)/(distance / ArchiveMeta.TO_KILOMETRE)))));
            mRecords.setText(String.valueOf(meta.getCount()));
          //  meta.setCalorie(String.valueOf(meta.getDistance() * Float.parseFloat(meta.getWeight())*ArchiveMeta.K));
            meta.setCalorie(String.format(formatter,(distance / 1000) * PreferencesUtils.getFloat(getActivity(), "weight") * ArchiveMeta.K));
           mCalorie.setText(String.format(formatter,(distance / 1000) * PreferencesUtils.getFloat(getActivity(), "weight") * ArchiveMeta.K));
          //  mCalorie.setText(String.format(formatter, meta.getDistance() * 80 * ArchiveMeta.K));
           mClimbing.setText(meta.getClimbingValue());
          //  mClimbing.setText(meta.getClimbing()+"");
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
//            if(meta.getSpeed()!=null){
//
//                mPace.setText(getPace((long)(1000 / Float.parseFloat(meta.getSpeed()))));
//            }else{
//                mPace.setText("0'0\"");
//            }



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

    @Override
    public void onDestroy() {
        super.onDestroy();
      //  meta.getArchive().close();
    }
}
