
package com.gracecode.tracker.dao;

import android.location.Location;

import com.amap.api.location.AMapLocation;

/**
 * Created by dengt on 16-1-28.
 */
public class LocationModel extends AMapLocation {
    private int step;

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public LocationModel(String s) {
        super(s);
    }

    public LocationModel(Location location) {
        super(location);
    }
}
