package com.gracecode.tracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gracecode.tracker.R;

import java.util.List;

/**
 * Created by zhouh on 16-3-10.
 */
public class FriendsAdapter extends BaseAdapter{
    private Context context;
    private LayoutInflater inflater;
    private List<String> users;
    public FriendsAdapter(Context context,List<String> users) {
        this.context = context;
        this.users = users;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView==null){
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.friends_list_item,null);
            viewHolder.userNameTextView = (TextView) convertView.findViewById(R.id.user_name);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.userNameTextView.setText(users.get(position));
        return convertView;
    }
    public class ViewHolder{
        TextView userNameTextView;
    }
}
