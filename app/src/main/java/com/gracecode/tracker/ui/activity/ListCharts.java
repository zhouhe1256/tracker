package com.gracecode.tracker.ui.activity;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.ui.activity.base.Activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by dengt on 16-1-22.
 */
public class ListCharts extends Activity {
    private String archiveFileName;
    private Archiver archiver;
    private String description;
    private ArchiveMeta archiveMeta;
    private SimpleDateFormat dateFormat;
    private ListView listView;
    private ArrayList<String> archiveFileNames;
    private ArrayList<Archiver> archives;
    private ArrayList<AMapLocation> locations;
    private LocationAdapter archivesAdapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.total_list);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        archiver = new Archiver(context, archiveFileName, Archiver.MODE_READ_ONLY);
        archiveMeta = archiver.getMeta();
        this.dateFormat = new SimpleDateFormat(context.getString(R.string.time_format), Locale.CHINA);
        description = archiver.getMeta().getDescription();
        locations=archiver.fetchAll();
        if (description.length() <= 0) {
            description = getString(R.string.no_description);
        }
        this.listView = (ListView) findViewById(R.id.records_list);
        this.archives = new ArrayList<Archiver>();
        this.archivesAdapter = new LocationAdapter(locations);
        this.listView.setAdapter(archivesAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        actionBar.removeAllActions();
        actionBar.setTitle("列表");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        archiver.close();
    }

    /**
     * ListView Adapter
     */
    public class LocationAdapter extends ArrayAdapter<AMapLocation> {

        public LocationAdapter(ArrayList<AMapLocation> locations) {
            super(context, R.layout.records_row, locations);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Location archive = locations.get(position);
          //  ArchiveMeta archiveMeta = archive.getMeta();

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.records_row, parent, false);

            TextView mDescription = (TextView) rowView.findViewById(R.id.description);
            TextView mCostTime = (TextView) rowView.findViewById(R.id.cost_time);
            TextView mDistance = (TextView) rowView.findViewById(R.id.distance);

            mDistance.setText(String.format(getString(R.string.records_formatter),
                    archive.getSpeed())+"小时/");

            String costTime =dateFormat.format(archive.getTime()) ;
            mCostTime.setText(costTime.length() > 0 ? costTime : getString(R.string.not_available));

//            String description = archiveMeta.getDescription();
//            if (description.length() <= 0) {
//                description = getString(R.string.no_description);
//                mDescription.setTextColor(getResources().getColor(R.color.gray));
//            }
           // mDescription.setText(description);

            return rowView;
        }
    }
}
