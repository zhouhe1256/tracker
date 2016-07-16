package com.gracecode.tracker.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gracecode.tracker.R;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by bjcathay on 16-1-25.
 */
public class PaceAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<Map<String,String>> maps;
    private LayoutInflater inflater;

    public PaceAdapter(Context context,ArrayList<Map<String,String>> maps){
        this.context=context;
        this.maps=maps;
        this.inflater=LayoutInflater.from(context);
    }
    public void setData(ArrayList<Map<String,String>> maps){
        this.maps=maps;
    }
    @Override
    public int getCount() {
        return maps.size();
    }

    @Override
    public Object getItem(int i) {
        return maps.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder=null;
        if(viewHolder==null){
            viewHolder=new ViewHolder();
            if(view==null){
                view=inflater.inflate(R.layout.layout,null);
            }
            viewHolder.s_text=(TextView)view.findViewById(R.id.distance);
            viewHolder.d_text=(TextView)view.findViewById(R.id.pace);
            view.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder)view.getTag();
        }
        viewHolder.s_text.setText(maps.get(i).get("time"));
        viewHolder.d_text.setText(maps.get(i).get("pace"));
//        if(view==null){
//            view=inflater.inflate(R.layout.layout,null);
//        }
//        TextView s_text=(TextView)view.findViewById(R.id.distance);
//        TextView d_text=(TextView)view.findViewById(R.id.pace);
//        s_text.setText(maps.get(i).get("time"));
//        d_text.setText(maps.get(i).get("pace"));
        return view;
    }
    class ViewHolder {
        TextView s_text;
        TextView d_text;
    }
}
