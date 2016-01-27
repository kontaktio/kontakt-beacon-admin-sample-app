package com.kontakt.sample.receiver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kontakt.sample.service.BackgroundScanService;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

public abstract class AbstractBroadcastInterceptor {

    private final Context context;

    AbstractBroadcastInterceptor(final Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void handle(final Intent broadcast) {
        final Bundle extras = broadcast.getExtras();
        final int info = extras.getInt(BackgroundScanService.EXTRA_INFO);

        switch(info) {
            case BackgroundScanService.INFO_BEACON_APPEARED:
                final IBeaconDevice beaconDevice = extras.getParcelable(BackgroundScanService.EXTRA_BEACON);
                onBeaconAppeared(info, beaconDevice);
                break;

            case BackgroundScanService.INFO_REGION_ABANDONED:
                final IBeaconRegion abandonedIBeaconRegion = extras.getParcelable(BackgroundScanService.EXTRA_REGION);
                onRegionAbandoned(info, abandonedIBeaconRegion);
                break;

            case BackgroundScanService.INFO_REGION_ENTERED:
                final IBeaconRegion enteredRegion = extras.getParcelable(BackgroundScanService.EXTRA_REGION);
                onRegionEntered(info, enteredRegion);
                break;

            case BackgroundScanService.INFO_SCAN_STARTED:
                onScanStarted(info);
                break;

            case BackgroundScanService.INFO_SCAN_STOPPED:
                onScanStopped(info);
                break;

            default:
                throw new IllegalArgumentException("Unsupported notification id: " + info);
        }
    }

    protected abstract void onBeaconAppeared(final int info, final IBeaconDevice beaconDevice);

    protected abstract void onRegionAbandoned(final int info, final IBeaconRegion region);

    protected abstract void onRegionEntered(final int info, final IBeaconRegion region);

    protected abstract void onScanStarted(final int info);

    protected abstract void onScanStopped(final int info);
}
