package com.gracecode.tracker.dao;

import com.jjoe64.graphview.series.DataPointInterface;

import java.io.Serializable;

/**
 * Created by dengt on 16-1-22.
 */
public class DataTime implements DataPointInterface, Serializable {
    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }
}
