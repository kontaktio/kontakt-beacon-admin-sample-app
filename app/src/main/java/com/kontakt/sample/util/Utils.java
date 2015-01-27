package com.kontakt.sample.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.Surface;
import android.widget.Toast;

import java.util.List;

public final class Utils {

    public static void cancelNotifications(final Context context, final List<Integer> notificationIdList) {
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        for(final int notificationId : notificationIdList) {
            notificationManager.cancel(notificationId);
        }
    }

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

    public static void showToast(final Context context, final String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private Utils() { }
}