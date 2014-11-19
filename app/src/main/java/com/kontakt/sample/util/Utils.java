package com.kontakt.sample.util;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.Surface;

public final class Utils {

    public static void setOrientationChangeEnabled(final boolean state, final Activity activity) {
        if(!state) {
            final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

            switch (rotation) {
                case Surface.ROTATION_180:
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    break;
                case Surface.ROTATION_270:
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    break;
                case Surface.ROTATION_0:
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                case Surface.ROTATION_90:
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
            }
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    private Utils() { }
}