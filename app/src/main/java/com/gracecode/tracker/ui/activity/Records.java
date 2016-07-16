package com.gracecode.tracker.ui.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.*;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.service.ArchiveNameHelper;
import com.gracecode.tracker.ui.activity.base.Activity;
import com.gracecode.tracker.util.Helper;
import com.gracecode.tracker.util.TimeUtils;
import com.markupartist.android.widget.ActionBar;

import java.text.SimpleDateFormat;
import java.util.*;

public class Records extends Activity implements AdapterView.OnItemClickListener, DatePickerDialog.OnDateSetListener {
    private Context context;
    public static final String INTENT_ARCHIVE_FILE_NAME = "name";
    public static final String INTENT_SELECT_BY_MONTH = "month";
    private SimpleDateFormat dateFormat;
    private ListView listView;
    private ArrayList<String> archiveFileNames;
    private ArrayList<Archiver> archives;

    private ArchiveNameHelper archiveFileNameHelper;
    private ArchivesAdapter archivesAdapter;
    private long selectedTime;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1){
                archivesAdapter.notifyDataSetChanged();
            }
            super.handleMessage(msg);
        }
    };
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Archiver archiver = archives.get(i);
        Intent intent = new Intent(this, Detail.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(INTENT_ARCHIVE_FILE_NAME, archiver.getName());
        startActivity(intent);
    }

    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(year, month, day);

        Date selectDate = new Date(selectedTime);
        if (selectDate.getMonth() != month) {
            Intent intent = new Intent(context, Records.class);
            intent.putExtra(INTENT_SELECT_BY_MONTH, calendar.getTimeInMillis());
            startActivity(intent);
        }
    }

    /**
     * ListView Adapter
     */
    public class ArchivesAdapter extends ArrayAdapter<Archiver> {

        public ArchivesAdapter(ArrayList<Archiver> archives) {
            super(context, R.layout.records_row, archives);
            dateFormat = new SimpleDateFormat(context.getString(R.string.time_format), Locale.CHINA);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Archiver archive = archives.get(position);
            ArchiveMeta archiveMeta=null;
            try {
                archiveMeta = archive.getMeta();
            }catch (Exception e){
                Helper.Logger.e(e.getMessage());
            }

            float distance=archiveMeta.getDistance();


            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.records_row, parent, false);
            Date endTime = archiveMeta.getEndTime();
            TextView mTime = (TextView) rowView.findViewById(R.id.time);
            TextView mCostTime = (TextView) rowView.findViewById(R.id.cost_time);
            TextView mDistance = (TextView) rowView.findViewById(R.id.distance);
            TextView mSpeed = (TextView) rowView.findViewById(R.id.cost_s);
            mTime.setText(
                    endTime != null ?
                            dateFormat.format(endTime) : getString(R.string.not_available));

            if (archiveMeta.getEndTime()!=null){
                long bwn= TimeUtils.getTime(archiveMeta.getStartTime(), archiveMeta.getEndTime());
                try{
                    if(distance!=0){
                        mSpeed.setText(String.format(getPace((long) ((bwn) / (distance / ArchiveMeta.TO_KILOMETRE)))));
                    }else{
                        mSpeed.setText("0'0\"");
                    }
                }catch (Exception e) {
                    Helper.Logger.e(e.getMessage());
                }
            }else{
                mSpeed.setText(endTime != null ?
                        dateFormat.format(endTime) : getString(R.string.not_available));
            }
            mDistance.setText(String.format(getString(R.string.records_formatter),
                archiveMeta.getDistance() / ArchiveMeta.TO_KILOMETRE));

            String costTime = archiveMeta.getRawCostTimeString();
            mCostTime.setText(costTime.length() > 0 ? costTime : getString(R.string.not_available));

            String description = archiveMeta.getDescription();
            if (description.length() <= 0) {
                description = getString(R.string.no_description);
               // mDescription.setTextColor(getResources().getColor(R.color.gray));
            }
           // mDescription.setText(description);

            return rowView;
        }
        public String getPace(long between){

            long day = between / (24 * 60 * 60 * 1000);
            long hour = (between / (60 * 60 * 1000) - day * 24);
            long minute = ((between / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long second = (between / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);
            return minute+"'"+second+"\"";
        }
    }

    private RelativeLayout shouye;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.records);
        shouye = (RelativeLayout) findViewById(R.id.shouye);
        this.context = getApplicationContext();
        this.listView = (ListView) findViewById(R.id.records_list);
        this.archiveFileNameHelper = new ArchiveNameHelper(context);

        this.archives = new ArrayList<Archiver>();
        this.archivesAdapter = new ArchivesAdapter(archives);
        this.listView.setAdapter(archivesAdapter);
        shouye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Records.this,Tracker.class);
                startActivity(intent);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Records.this);
                builder.setMessage("确认删除me？");
                builder.setTitle("提示");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Archiver archive = archives.get(position);
                        archive.delete();
                        Toast.makeText(Records.this,archiveFileNames.get(position),Toast.LENGTH_LONG).show();
                        archives.remove(position);
                        archivesAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return true;
            }
        });
        listView.setOnItemClickListener(this);
        selectedTime = getIntent().getLongExtra(INTENT_SELECT_BY_MONTH, System.currentTimeMillis());
        SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.time_month_format));
        String selectedTitle = formatter.format(new Date(selectedTime));
        if (!selectedTitle.equals(formatter.format(new Date()))) {
            actionBar.setTitle(selectedTitle);
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getArchiveFilesByMonth(new Date(selectedTime));

            }
        },500);
    }

    @Override
    public void onStart() {
        super.onStart();
        actionBar.removeAllActions();
//        actionBar.addAction(
//            new ActionBar.Action() {
//                @Override
//                public int getDrawable() {
//                    return R.drawable.ic_menu_today;
//                }
//
//                @Override
//                public void performAction(View view) {
//                    showTimeSelectDialog();
//                }
//            }
//        );
        // setAction title as month string if there is not current month
        actionBar.setTitle(getString(R.string.title_records));

    }


    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        archivesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private DatePicker findDatePicker(ViewGroup group) {
        if (group != null) {
            for (int i = 0, j = group.getChildCount(); i < j; i++) {
                View child = group.getChildAt(i);
                if (child instanceof DatePicker) {
                    return (DatePicker) child;
                } else if (child instanceof ViewGroup) {
                    DatePicker result = findDatePicker((ViewGroup) child);
                    if (result != null)
                        return result;
                }
            }
        }
        return null;
    }

    private void showTimeSelectDialog() {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTime(new Date(selectedTime));

        DatePickerDialog datePicker = new DatePickerDialog(
            Records.this, Records.this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.show();
    }

    private void getArchiveFilesByMonth(Date date) {
        archiveFileNames = archiveFileNameHelper.getArchiveFilesNameByMonth(date);
        openArchivesFromFileNames();
        Message msg = handler.obtainMessage();
        msg.what = 1;
        handler.sendMessage(msg);
    }

    /**
     * 从指定目录读取所有已保存的列表
     */
    private void openArchivesFromFileNames() {
        Iterator<String> iterator = archiveFileNames.iterator();
        while (iterator.hasNext()) {
            String name = iterator.next();
            Archiver archive = new Archiver(context, name);

            if (archive.getMeta().getCount() > 0) {
                archives.add(archive);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.records, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_calendar:
                showTimeSelectDialog();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /**
     * 清除列表
     */
    private void closeArchives() {
        Iterator<Archiver> iterator = archives.iterator();
        while (iterator.hasNext()) {
            Archiver archive = (Archiver) iterator.next();
            if (archive != null) {
                archive.close();
            }
        }
        archives.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeArchives();
    }
}
