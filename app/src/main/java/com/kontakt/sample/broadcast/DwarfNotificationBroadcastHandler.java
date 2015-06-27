package com.kontakt.sample.broadcast;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.activity.TrackDwarf;
import com.kontakt.sdk.android.device.BeaconDevice;
import com.kontakt.sdk.android.device.Region;
import com.kontakt.sdk.core.Proximity;

/**
 * Created by slovic on 27.06.15.
 */
public class DwarfNotificationBroadcastHandler extends NotificationBroadcastHandler {
    public DwarfNotificationBroadcastHandler(Context context) {
        super(context);
    }

    @Override
    protected void onBeaconAppeared(int info, BeaconDevice beaconDevice) {
        super.onBeaconAppeared(info, beaconDevice);
        final Context context = getContext();
        final Intent redirectIntent = new Intent(context, TrackDwarf.class);
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
            .setTicker(context.getString(R.string.dwarf_appeared, deviceName))
            .setContentIntent(PendingIntent.getActivity(context, 0,
                redirectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT))
            .setContentTitle(context.getString(R.string.dwarf_appeared, deviceName))
            .setStyle(new Notification.BigTextStyle().bigText(
                context.getString(R.string.appeared_beacon_info, deviceName,
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
    protected void onRegionAbandoned(int info, Region region) {/*do nothing*/}

    @Override
    protected void onRegionEntered(int info, Region region) {/*do nothing*/}

    @Override
    protected void onScanStarted(int info) {/*do nothing*/}

    @Override
    protected void onScanStopped(int info) {/*do nothing*/}
}
