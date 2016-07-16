package com.gracecode.tracker.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.location.AMapLocation;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.util.DateAxisLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * <p/>
 * User: mingcheng
 * Date: 12-9-20
 */
@SuppressLint("ValidFragment")
public class AltitudeChartsFragment extends Fragment {
    private Context context;
    private Archiver archiver;
    private ArrayList<AMapLocation> locations;
    private GraphView graphView;
    private static int HORIZONTAL_LABELS_ITEM_SIZE = 9;
    private LineGraphSeries<DataPoint> speedSeries;
    private SimpleDateFormat dateFormatter;


    public AltitudeChartsFragment(Context context, Archiver archiver) {
        this.context = context;
        this.archiver = archiver;

        this.locations = archiver.fetchAll();
        speedSeries = new LineGraphSeries<DataPoint>();
        dateFormatter = new SimpleDateFormat(context.getString(R.string.sort_time_format), Locale.getDefault());

       // graphView = GraphView(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.speedchart_fragment, null, false);
         graphView = (GraphView) view.findViewById(R.id.graph);
        setGraphDataAndStyle();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    private void setGraphDataAndStyle() {
        // label
     //   graphView..set.setHorizontalLabels(getHorizontalLabels());

        //style
       // graphView.setBackgroundColor(Color.BLACK);

        // data
       graphView.addSeries(new LineGraphSeries(getSeriesData()));
        graphView.setTitle("海拔");
        graphView.getGridLabelRenderer().setLabelFormatter(new DateAxisLabelFormatter(getActivity()));
        graphView.getGridLabelRenderer().setNumVerticalLabels(locations.size());
        graphView.getViewport().setMinX(locations.get(0).getTime());
        graphView.getViewport().setMaxX(locations.get(locations.size()-1).getTime());
        graphView.getViewport().setXAxisBoundsManual(true);

    }


    private String[] getHorizontalLabels() {
        int size = locations.size();
        ArrayList<String> labels = new ArrayList<String>();

        for (int i = 0; i < size; i += (size / HORIZONTAL_LABELS_ITEM_SIZE)) {
            Location location = locations.get(i);
            if (location != null) {
                labels.add(dateFormatter.format(location.getTime()));
            }
        }

        return labels.toArray(new String[labels.size()]);
    }


    private DataPoint[] getSeriesData() {
       // speedSeries..clear();
      List<DataPoint>  a=new ArrayList<>();
        Iterator<AMapLocation> locationIterator = locations.iterator();
        while (locationIterator.hasNext()) {
            Location location = locationIterator.next();
            DataPoint graphViewData = new DataPoint(location.getTime(),
                location.getAltitude());
           a.add(graphViewData);
           // speedSeries.add(graphViewData);
        }

        return  (DataPoint[])a.toArray(new DataPoint[a.size()]);
      //  return speedSeries.toArray(new GraphView.GraphViewData[speedSeries.size()]);
    }
}
