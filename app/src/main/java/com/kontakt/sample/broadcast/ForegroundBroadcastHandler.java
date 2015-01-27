package com.kontakt.sample.broadcast;


import android.content.Context;

import com.kontakt.sample.R;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.device.BeaconDevice;
import com.kontakt.sdk.android.device.Region;

public class ForegroundBroadcastHandler extends AbstractBroadcastHandler {
    public ForegroundBroadcastHandler(Context context) {
        super(context);
    }

    @Override
    protected void onBeaconAppeared(int info, BeaconDevice beaconDevice) {
        String proximityUUID = beaconDevice.getProximityUUID().toString();
        int major = beaconDevice.getMajor();
        int minor = beaconDevice.getMinor();
        double distance = beaconDevice.getAccuracy();

        Context context = getContext();
        Utils.showToast(context, context.getString(R.string.appeared_beacon_info, proximityUUID, major, minor, distance));
    }

    @Override
    protected void onRegionAbandoned(int info, Region region) {
        Context context = getContext();

        Utils.showToast(context, context.getString(R.string.region_abandoned, region.getIdentifier()));
    }

    @Override
    protected void onRegionEntered(int info, Region region) {
        Context context = getContext();

        Utils.showToast(context, context.getString(R.string.region_entered, region.getIdentifier()));
    }

    @Override
    protected void onScanStarted(int info) {
        Context context = getContext();
        Utils.showToast(context, context.getString(R.string.scan_started));
    }

    @Override
    protected void onScanStopped(int info) {
        Context context = getContext();
        Utils.showToast(context, context.getString(R.string.scan_stopped));
    }
}
