
package com.gracecode.tracker.ui.activity;

import android.app.LocalActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import com.gracecode.tracker.R;
import com.gracecode.tracker.dao.ArchiveMeta;
import com.gracecode.tracker.dao.Archiver;
import com.gracecode.tracker.ui.activity.base.Activity;
import com.gracecode.tracker.ui.activity.maps.GaoDeMap;
import com.gracecode.tracker.ui.activity.maps.GaoDeNewMap;
import com.gracecode.tracker.ui.fragment.ArchiveMetaFragment;
import com.gracecode.tracker.ui.fragment.ArchiveMetaTimeFragment;
import com.gracecode.tracker.ui.fragment.ArchiveNewMetaFragment;
import com.gracecode.tracker.ui.fragment.ArchiveNewMetaTimeFragment;
import com.markupartist.android.widget.ActionBar;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Detail extends Activity implements View.OnTouchListener, View.OnClickListener {
    private String archiveFileName;

    private Archiver archiver;
    private ArchiveMeta archiveMeta;

    private ArchiveNewMetaFragment archiveMetaFragment;
    private ArchiveNewMetaTimeFragment archiveMetaTimeFragment;

    private TextView mDescription;
    private LocalActivityManager localActivityManager;
   // private TabHost mTabHost;
    private View mMapMask;
    public static final String INSIDE_TABHOST = "inside_tabhost";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        localActivityManager = new LocalActivityManager(this, false);
        localActivityManager.dispatchCreate(savedInstanceState);

        archiveFileName = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
        archiver = new Archiver(context, archiveFileName, Archiver.MODE_READ_WRITE);
        archiveMeta = archiver.getMeta();

        mMapMask = findViewById(R.id.map_mask);
        mDescription = (TextView) findViewById(R.id.item_description);
       // mTabHost = (TabHost) findViewById(R.id.tabhost);

        archiveMetaFragment = new ArchiveNewMetaFragment(context, archiveMeta,archiveFileName);
        archiveMetaTimeFragment = new ArchiveNewMetaTimeFragment(context, archiveMeta);

        addArchiveMetaTimeFragment();
        addArchiveMetaFragment();

        mDescription.setOnClickListener(this);
        mMapMask.setOnTouchListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        String description = archiveMeta.getDescription().trim();
        if (description.length() > 0) {
            mDescription.setTextColor(getResources().getColor(R.color.snowhite));
            mDescription.setText(description);
        } else {
            mDescription.setTextColor(getResources().getColor(R.color.gray));
            mDescription.setText(getString(R.string.no_description));
        }

        actionBar.setTitle(getString(R.string.title_detail));
        actionBar.removeAllActions();

        // Speed charts
        actionBar.addAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_view;
            }

            @Override
            public void performAction(View view) {
                Intent intent = new Intent(context, SpeedCharts.class);
                intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
                startActivity(intent);
            }
        });

//        // Share to Sina Weibo
//        actionBar.addAction(new ActionBar.Action() {
//            @Override
//            public int getDrawable() {
//                return R.drawable.ic_menu_share;
//            }
//
//            @Override
//            public void performAction(View view) {
//                shareToSina();
//            }
//        });
        actionBar.addAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_manage;
            }

            @Override
            public void performAction(View view) {
                Intent intent = new Intent(context, AltitudeiCharts.class);
                intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
                startActivity(intent);
            }
        });
//        actionBar.addAction(new ActionBar.Action() {
//            @Override
//            public int getDrawable() {
//                return R.drawable.ic_menu_sort_by_size;
//            }
//
//            @Override
//            public void performAction(View view) {
//                Intent intent = new Intent(context, ListCharts.class);
//                intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
//                startActivity(intent);
//            }
//        });
        actionBar.addAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_mapmode;
            }

            @Override
            public void performAction(View view) {
                Intent intent = new Intent(context, GaoDeMap.class);
                intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        actionBar.addAction(new ActionBar.Action() {
            @Override
            public int getDrawable() {
                return R.drawable.ic_menu_mapmode;
            }

            @Override
            public void performAction(View view) {
                Intent intent = new Intent(context, GaoDeNewMap.class);
                intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    public void shareToSina() {
        byte[] bitmap = helper.convertBitmapToByteArray(getRouteBitmap());
        String recordsFormatter = getString(R.string.records_formatter);
        SimpleDateFormat dateFormatter = new SimpleDateFormat(getString(R.string.time_format),
                Locale.getDefault());

        // Build string for share by microblog etc.
        String message = String.format(
                getString(R.string.share_report_formatter),
                archiveMeta.getDescription().length() > 0 ? "(" + archiveMeta.getDescription()
                        + ")" : "",
                String.format(recordsFormatter, archiveMeta.getDistance()
                        / ArchiveMeta.TO_KILOMETRE),
                dateFormatter.format(archiveMeta.getStartTime()),
                dateFormatter.format(archiveMeta.getEndTime()),
                archiveMeta.getRawCostTimeString(),
                String.format(recordsFormatter, archiveMeta.getMaxSpeed()
                        * ArchiveMeta.KM_PER_HOUR_CNT),
                String.format(recordsFormatter, archiveMeta.getAverageSpeed()
                        * ArchiveMeta.KM_PER_HOUR_CNT)
                );

        helper.shareToSina(context, message, bitmap);
    }

    private void confirmDelete() {
        helper.showConfirmDialog(
                getString(R.string.delete),
                String.format(getString(R.string.sure_to_del), archiveMeta.getName()),
                new Runnable() {
                    @Override
                    public void run() {
                        if (archiver.delete()) {
                            finish();
                        }
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        // ...
                    }
                }
                );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share:
                shareToSina();
                break;

            case R.id.menu_delete:
                confirmDelete();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /**
     * Take screenshot from tabhost for sharing
     *
     * @return
     */
    private Bitmap getRouteBitmap() {
        View view = findViewById(R.id.detail_layout);
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        view.destroyDrawingCache();
        view.setDrawingCacheQuality(100);
        return Bitmap.createBitmap(view.getDrawingCache());
    }

    @Override
    public void onResume() {
        super.onResume();

//        Intent mapIntent = new Intent(this, GaoDeMap.class);
//        String name = getIntent().getStringExtra(Records.INTENT_ARCHIVE_FILE_NAME);
//        mapIntent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, name);
//        mapIntent.putExtra(INSIDE_TABHOST, true);
//
//        mTabHost.setup(localActivityManager);
//        mTabHost.clearAllTabs();
//
//        TabHost.TabSpec tabSpec =
//                mTabHost.newTabSpec("").setIndicator("").setContent(mapIntent);
//        mTabHost.addTab(tabSpec);
//
//        localActivityManager.dispatchResume();
        if (!archiver.exists()) {
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //mTabHost.clearAllTabs();
//        localActivityManager.removeAllActivities();
//        localActivityManager.dispatchPause(isFinishing());
    }

    private void addArchiveMetaTimeFragment() {
        addFragment(R.id.archive_meta_time_layout, archiveMetaTimeFragment);
    }

    private void addArchiveMetaFragment() {
        addFragment(R.id.archive_meta_layout, archiveMetaFragment);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
//            Intent intent = new Intent(this, GaoDeMap.class);
//            intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, Modify.class);
        intent.putExtra(Records.INTENT_ARCHIVE_FILE_NAME, archiveFileName);
        startActivity(intent);
    }
}
