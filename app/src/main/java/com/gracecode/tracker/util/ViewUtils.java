package com.gracecode.tracker.util;

import android.content.Context;

import com.gracecode.tracker.TrackerApplication;

/**
 * Created by zhouh on 16-3-11.
 */
public class ViewUtils {
    public static float dp2px(Context context,float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }
}
