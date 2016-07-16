package com.gracecode.tracker.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.amap.api.location.AMapLocation;
import com.gracecode.tracker.util.Helper;

import java.io.File;
import java.util.ArrayList;

public class Archiver {
    public static final int MODE_READ_ONLY = 0x001;
    public static final int MODE_READ_WRITE = 0x002;
    public static final String TABLE_NAME = "step_records";
    private static final String NEVER_USED_LOCATION_PROVIDER = "";


    public final static class DATABASE_COLUMN {
        static final String ID = "id";
        static final String LATITUDE = "latitude";
        static final String LONGITUDE = "longitude";
        static final String SPEED = "speed";
        static final String BEARING = "bearing";
        static final String ALTITUDE = "altitude";
        static final String ACCURACY = "accuracy";
        static final String TIME = "time";
        static final String COUNT = "count";
        static final String META_NAME = "meta";
        static final String META_VALUE = "value";
        static final String STEP = "step";
        static final String STOP = "stop";
    }

    protected class ArchiveDatabaseHelper extends SQLiteOpenHelper {

        protected static final String SQL_CREATE_LOCATION_TABLE =
            "create table " + TABLE_NAME + " ("
                + "id integer primary key autoincrement, "
                + "latitude double not null, "
                + "longitude double not null,"
                + "speed double not null, "
                + "bearing float not null,"
                + "altitude double not null,"
                + "accuracy float not null,"
                + "time long not null,"
                + "stop INTEGER not null,"
                + "step INTEGER"
                + ");";

        protected static final String SQL_CREATE_META_TABLE =
            "create table " + ArchiveMeta.TABLE_NAME + " ("
                + "id integer primary key autoincrement, "
                + "meta string not null unique,"
                + "value string default null"
                + ");";

        private static final int DB_VERSION = 4;

        public ArchiveDatabaseHelper(Context context, String name) {
            super(context, name, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            try {
                database.execSQL(SQL_CREATE_META_TABLE);
                database.execSQL(SQL_CREATE_LOCATION_TABLE);
            } catch (SQLException e) {
                Helper.Logger.e(e.getMessage());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
           // Log.d("db", "DatabaseHelper onUpgrade");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + ArchiveMeta.TABLE_NAME);
            onCreate(db);
        }
    }

    protected String name;
    private ArrayList<AMapLocation> locations;
    private ArrayList<Integer> stops;
    private ArrayList<LocationModel> locationModels;

    private ArchiveMeta meta;
    protected ArchiveDatabaseHelper databaseHelper = null;
    protected SQLiteDatabase database;
    protected Context context;
    protected int mode = MODE_READ_ONLY;

    public Archiver(Context context) {
        this.context = context;
        this.locations = new ArrayList<AMapLocation>();
        this.stops = new ArrayList<Integer>();
        this.locationModels = new ArrayList<LocationModel>();
    }

    public Archiver(Context context, String name) {
        this.context = context;
        this.locations = new ArrayList<AMapLocation>();
        this.stops = new ArrayList<Integer>();
        this.locationModels = new ArrayList<LocationModel>();
        this.open(name, MODE_READ_ONLY);
    }

    public Archiver(Context context, String name, int mode) {
        this.context = context;
        this.locations = new ArrayList<AMapLocation>();
        this.locationModels = new ArrayList<LocationModel>();
        this.stops = new ArrayList<Integer>();
        this.mode = mode;
        this.open(name, this.mode);
    }

    public String getName() {
        return name;
    }

    public void open(String name, int mode) {
        // 防止重复打开数据库
        if (databaseHelper != null) {
            close();
        }
        this.name = name;
        this.mode = mode;
        this.databaseHelper = new ArchiveDatabaseHelper(context, name);

        this.reopen(this.mode);
        this.meta = new ArchiveMeta(this);
    }

    public void open(String name) {
        open(name, MODE_READ_ONLY);
    }

    public SQLiteDatabase reopen(int mode) {
        switch (mode) {
            case MODE_READ_ONLY:
                database = databaseHelper.getReadableDatabase();
                break;
            case MODE_READ_WRITE:
                database = databaseHelper.getWritableDatabase();
                break;
        }

        return database;
    }

    public boolean delete() {
        if (databaseHelper != null) {
            close();
        }
        File file = new File(name);
        return (file == null) ? false : file.delete();
    }

    public boolean exists() {
        File file = new File(name);
        return (file == null) ? false : file.exists();
    }

    public ArchiveMeta getMeta() {
        return meta;
    }

    public boolean add(AMapLocation point, long timeMillis,int step,int stop) {
        ContentValues values = new ContentValues();

        values.put(DATABASE_COLUMN.LATITUDE, point.getLatitude());
        values.put(DATABASE_COLUMN.LONGITUDE, point.getLongitude());
        values.put(DATABASE_COLUMN.SPEED, point.getSpeed());
        values.put(DATABASE_COLUMN.BEARING, point.getBearing());
        values.put(DATABASE_COLUMN.ALTITUDE, point.getAltitude());
        values.put(DATABASE_COLUMN.ACCURACY, point.getAccuracy());
        values.put(DATABASE_COLUMN.TIME, timeMillis);
        values.put(DATABASE_COLUMN.STEP,step);
        values.put(DATABASE_COLUMN.STOP,stop);

        try {
            return database.insert(TABLE_NAME, null, values) > 0 ? true : false;
        } catch (SQLException e) {
            Helper.Logger.e(e.getMessage());
        }

        return false;
    }

    public boolean add(AMapLocation point,int step,int stop) {
        return add(point, System.currentTimeMillis(),step,stop);
    }

    /**
     * 获取最后个已记录的位置
     *
     * @return
     */
    public AMapLocation getLastRecord() {
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME
                + " ORDER BY time DESC LIMIT 1", null);
            cursor.moveToFirst();

            if (cursor.getCount() > 0) {
                return getLocationFromCursor(cursor);
            }
            cursor.close();
        } catch (SQLiteException e) {
            Helper.Logger.e(e.getMessage());
        }

        return null;
    }

    public AMapLocation getFirstRecord() {
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME
                + " ORDER BY time ASC LIMIT 1", null);
            cursor.moveToFirst();

            if (cursor.getCount() > 0) {
                return getLocationFromCursor(cursor);
            }
            cursor.close();
        } catch (SQLiteException e) {
            Helper.Logger.e(e.getMessage());
        }

        return null;
    }
    private AMapLocation getLocationFromCursor(Cursor cursor) {
        AMapLocation location = new AMapLocation(NEVER_USED_LOCATION_PROVIDER);

        location.setLatitude(cursor.getDouble(cursor.getColumnIndex(DATABASE_COLUMN.LATITUDE)));
        location.setLongitude(cursor.getDouble(cursor.getColumnIndex(DATABASE_COLUMN.LONGITUDE)));
        location.setBearing(cursor.getFloat(cursor.getColumnIndex(DATABASE_COLUMN.BEARING)));
        location.setAltitude(cursor.getDouble(cursor.getColumnIndex(DATABASE_COLUMN.ALTITUDE)));
        location.setAccuracy(cursor.getFloat(cursor.getColumnIndex(DATABASE_COLUMN.ACCURACY)));
        location.setSpeed(cursor.getFloat(cursor.getColumnIndex(DATABASE_COLUMN.SPEED)));
        location.setTime(cursor.getLong(cursor.getColumnIndex(DATABASE_COLUMN.TIME)));

        return location;
    }
    private LocationModel getStepFromCursor(Cursor cursor) {
        LocationModel location = new LocationModel(NEVER_USED_LOCATION_PROVIDER);

        location.setLatitude(cursor.getDouble(cursor.getColumnIndex(DATABASE_COLUMN.LATITUDE)));
        location.setLongitude(cursor.getDouble(cursor.getColumnIndex(DATABASE_COLUMN.LONGITUDE)));
        location.setBearing(cursor.getFloat(cursor.getColumnIndex(DATABASE_COLUMN.BEARING)));
        location.setAltitude(cursor.getDouble(cursor.getColumnIndex(DATABASE_COLUMN.ALTITUDE)));
        location.setAccuracy(cursor.getFloat(cursor.getColumnIndex(DATABASE_COLUMN.ACCURACY)));
        location.setSpeed(cursor.getFloat(cursor.getColumnIndex(DATABASE_COLUMN.SPEED)));
        location.setTime(cursor.getLong(cursor.getColumnIndex(DATABASE_COLUMN.TIME)));
        location.setStep(cursor.getInt(cursor.getColumnIndex(DATABASE_COLUMN.STEP)));

        return location;
    }
    public ArrayList<LocationModel> fetchStepAll() {
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME
                    +" WHERE "
                    + DATABASE_COLUMN.LATITUDE+" > 0.0"
                    +" AND "+DATABASE_COLUMN.ALTITUDE+" > 0.0 "
                    +" AND "+DATABASE_COLUMN.SPEED+" > 0.0"
                    + " ORDER BY time ASC", null);

            locationModels.clear();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                locationModels.add(getStepFromCursor(cursor));
            }

            cursor.close();
        } catch (SQLiteException e) {
            Helper.Logger.e(e.getMessage());
        } catch (IllegalStateException e) {
            Helper.Logger.e(e.getMessage());
        }

        return locationModels;
    }
    public ArrayList<AMapLocation> fetchAll() {
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME
                    +" WHERE "
                    + DATABASE_COLUMN.LATITUDE+" > 0.0"
                    +" AND "+DATABASE_COLUMN.ALTITUDE+" > 0.0 "
                    +" AND "+DATABASE_COLUMN.SPEED+" > 0.0"
                    + " ORDER BY time ASC", null);

            locations.clear();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                locations.add(getLocationFromCursor(cursor));
            }

            cursor.close();
        } catch (SQLiteException e) {
            Helper.Logger.e(e.getMessage());
        } catch (IllegalStateException e) {
            Helper.Logger.e(e.getMessage());
        }

        return locations;
    }
    public ArrayList<Integer> stopAll() {
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME
                    +" WHERE "
                    + DATABASE_COLUMN.STOP+" < 3"
                    , null);

            stops.clear();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                stops.add(stop(cursor));
            }

            cursor.close();
        } catch (SQLiteException e) {
            Helper.Logger.e(e.getMessage());
        } catch (IllegalStateException e) {
            Helper.Logger.e(e.getMessage());
        }

        return stops;
    }
    private int stop(Cursor cursor) {
        int stop = cursor.getInt(cursor.getColumnIndex(DATABASE_COLUMN.STOP));
        return stop;
    }
    public void close() {
        if (databaseHelper != null) {
            databaseHelper.close();
            databaseHelper = null;
        }
    }
}
