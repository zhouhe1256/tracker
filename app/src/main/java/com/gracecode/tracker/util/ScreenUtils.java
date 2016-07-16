package com.gracecode.tracker.util;

import android.app.Activity;
import android.util.DisplayMetrics;

public class ScreenUtils {

	public static int getScreenWidth(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	public static int getScreenHeight(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}

	public static float getScreenDensity(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.density;
	}
	public  static float dp2px(Activity activity,float dp) {
		final float scale = activity.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

}
