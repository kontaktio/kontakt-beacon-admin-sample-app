package com.kontakt.sample.broadcast;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.activity.BackgroundScanActivity;
import com.kontakt.sdk.android.device.BeaconDevice;
import com.kontakt.sdk.android.device.Region;
import com.kontakt.sdk.core.Proximity;

public class NotificationBroadcastHandler extends AbstractBroadcastHandler {

    private final NotificationManager notificationManager;

    public NotificationBroadcastHandler(Context context) {
        super(context);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onBeaconAppeared(int info, BeaconDevice beaconDevice) {
        final Context context = getContext();
        final Intent redirectIntent = new Intent(context, BackgroundScanActivity.class);
        redirectIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final String deviceName = beaconDevice.getName();
        final String proximityUUID = beaconDevice.getProximityUUID().toString();
        final int major = beaconDevice.getMajor();
        final int minor = beaconDevice.getMinor();
        final double distance = beaconDevice.getAccuracy();
        final Proximity proximity = beaconDevice.getProximity();


        final Notification notification = new Notification.Builder(context)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setTicker(context.getString(R.string.beacon_appeared, deviceName))
                .setContentIntent(PendingIntent.getActivity(context,
                        0,
                        redirectIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT))
                .setContentTitle(context.getString(R.string.beacon_appeared, deviceName))
                .setStyle(new Notification.BigTextStyle().bigText(context.getString(R.string.appeared_beacon_info, deviceName,
                                                                                                                   proximityUUID,
                                                                                                                   major,
                                                                                                                   minor,
                                                                                                                   distance,
                                                                                                                   proximity.name())))
                .setSmallIcon(R.drawable.icon_launcher)
                .build();

        notificationManager.notify(info, notification);
    }

    @Override
    protected void onRegionAbandoned(int info, Region region) {
        final Context context = getContext();
        final Intent redirectIntent = new Intent(context, BackgroundScanActivity.class);
        redirectIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final String regionName = region.getIdentifier();

        final Notification notification = new Notification.Builder(context)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setTicker(context.getString(R.string.region_abandoned, regionName))
                .setContentIntent(PendingIntent.getActivity(context,
                        0,
                        redirectIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.region_abandoned, regionName))
                .setSmallIcon(R.drawable.icon_launcher)
                .build();

        notificationManager.notify(info, notification);
    }

    @Override
    protected void onRegionEntered(int info, Region region) {
        final Context context = getContext();
        final Intent redirectIntent = new Intent(context, BackgroundScanActivity.class);
        redirectIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final Notification notification = new Notification.Builder(context)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setTicker(context.getString(R.string.region_entered, region.getIdentifier()))
                .setContentIntent(PendingIntent.getActivity(context,
                        0,
                        redirectIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.region_entered, region.getIdentifier()))
                .setSmallIcon(R.drawable.icon_launcher)
                .build();

        notificationManager.notify(info, notification);
    }

    @Override
    protected void onScanStarted(int info) {
        final Context context = getContext();
        final Intent redirectIntent = new Intent(context, BackgroundScanActivity.class);

        final Notification notification = new Notification.Builder(context)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setTicker(context.getString(R.string.scan_started))
                .setContentIntent(PendingIntent.getActivity(context,
                        0,
                        redirectIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT))
                .setContentTitle(context.getString(R.string.scan_started))
                .setSmallIcon(R.drawable.icon_launcher)
                .build();

        notificationManager.notify(info, notification);
    }

    @Override
    protected void onScanStopped(int info) {
        final Context context = getContext();
        final Intent redirectIntent = new Intent(context, BackgroundScanActivity.class);
        redirectIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final Notification notification = new Notification.Builder(context)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setTicker(context.getString(R.string.scan_stopped))
                .setContentIntent(PendingIntent.getActivity(context,
                        0,
                        redirectIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT))
                .setContentTitle(context.getString(R.string.scan_stopped))
                .setSmallIcon(R.drawable.icon_launcher)
                .build();

        notificationManager.notify(info, notification);
    }
}
