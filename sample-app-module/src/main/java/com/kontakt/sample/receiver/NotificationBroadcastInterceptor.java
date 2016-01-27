package com.kontakt.sample.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.activity.MainActivity;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.Proximity;

public class NotificationBroadcastInterceptor extends AbstractBroadcastInterceptor {

    private final NotificationManager notificationManager;

    public NotificationBroadcastInterceptor(Context context) {
        super(context);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    protected void onBeaconAppeared(int info, IBeaconDevice beaconDevice) {
        final Context context = getContext();
        final Intent redirectIntent = new Intent(context, MainActivity.class);
        redirectIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final String deviceName = beaconDevice.getName();
        final String proximityUUID = beaconDevice.getProximityUUID().toString();
        final int major = beaconDevice.getMajor();
        final int minor = beaconDevice.getMinor();
        final double distance = beaconDevice.getDistance();
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
                .setSmallIcon(R.drawable.beacon)
                .setStyle(new Notification.BigTextStyle().bigText(context.getString(R.string.appeared_beacon_info, deviceName,
                        proximityUUID,
                        major,
                        minor,
                        distance,
                        proximity.name())))
                .build();

        notificationManager.notify(info, notification);
    }

    @Override
    protected void onRegionAbandoned(int info, IBeaconRegion region) {
        final Context context = getContext();
        final Intent redirectIntent = new Intent(context, MainActivity.class);
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
                .setSmallIcon(R.drawable.region)
                .build();

        notificationManager.notify(info, notification);
    }

    @Override
    protected void onRegionEntered(int info, IBeaconRegion region) {
        final Context context = getContext();
        final Intent redirectIntent = new Intent(context, MainActivity.class);
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
                .setSmallIcon(R.drawable.region)
                .build();

        notificationManager.notify(info, notification);
    }

    @Override
    protected void onScanStarted(int info) {
        final Context context = getContext();
        final Intent redirectIntent = new Intent(context, MainActivity.class);

        final Notification notification = new Notification.Builder(context)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setTicker(context.getString(R.string.scan_started))
                .setContentIntent(PendingIntent.getActivity(context,
                        0,
                        redirectIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT))
                .setContentTitle(context.getString(R.string.scan_started))
                .setSmallIcon(R.drawable.beacon)
                .build();

        notificationManager.notify(info, notification);
    }

    @Override
    protected void onScanStopped(int info) {
        final Context context = getContext();
        final Intent redirectIntent = new Intent(context, MainActivity.class);
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
                .setSmallIcon(R.drawable.beacon)
                .build();

        notificationManager.notify(info, notification);
    }
}
