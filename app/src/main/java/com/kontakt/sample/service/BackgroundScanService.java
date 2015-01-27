package com.kontakt.sample.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kontakt.sample.ui.activity.BackgroundScanActivity;
import com.kontakt.sdk.android.configuration.BeaconActivityCheckConfiguration;
import com.kontakt.sdk.android.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.connection.OnServiceBoundListener;
import com.kontakt.sdk.android.device.BeaconDevice;
import com.kontakt.sdk.android.device.Region;
import com.kontakt.sdk.android.factory.AdvertisingPackage;
import com.kontakt.sdk.android.factory.Filters;
import com.kontakt.sdk.android.manager.BeaconManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BackgroundScanService extends Service implements BeaconManager.MonitoringListener {

    public static final String BROADCAST = String.format("%s.%s", BackgroundScanService.class.getName(), "BROADCAST");

    public static final String EXTRA_BEACON = "extra_new_beacon";

    public static final String EXTRA_INFO = "extra_info";

    public static final String EXTRA_REGION = "extra_region";

    public static final int INFO_REGION_ENTERED = 100;
    public static final int INFO_REGION_ABANDONED = 121;
    public static final int INFO_BEACON_APPEARED = 144;
    public static final int INFO_SCAN_STARTED = 169;
    public static final int INFO_SCAN_STOPPED = 196;

    public static final List<Integer> INFO_LIST;

    private static final double ACCEPT_DISTANCE = 1.5;//[m]

    static {
        INFO_LIST = Collections.unmodifiableList(Arrays.asList(INFO_BEACON_APPEARED,
                                                               INFO_REGION_ABANDONED,
                                                               INFO_REGION_ENTERED,
                                                               INFO_SCAN_STARTED,
                                                               INFO_SCAN_STOPPED));
    }

    private final Messenger serviceMessenger = new Messenger(new ServiceHandler());

    private BeaconManager beaconManager;

    @Override
    public void onCreate() {
        super.onCreate();

        beaconManager = BeaconManager.newInstance(this);
        beaconManager.setScanMode(BeaconManager.SCAN_MODE_BALANCED);
        beaconManager.setBeaconActivityCheckConfiguration(BeaconActivityCheckConfiguration.DEFAULT);
        beaconManager.setForceScanConfiguration(ForceScanConfiguration.DEFAULT);
        beaconManager.registerMonitoringListener(this);

        beaconManager.addFilter(new Filters.CustomFilter() {
            @Override
            public Boolean apply(AdvertisingPackage object) {
                final UUID proximityUUID = object.getProximityUUID();
                final double distance = object.getAccuracy();

                return proximityUUID.equals(BeaconManager.DEFAULT_KONTAKT_BEACON_PROXIMITY_UUID) && distance <= ACCEPT_DISTANCE;
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onMonitorStart() {
        sendScanBroadcast(new BroadcastBuilder()
                            .setInfo(INFO_SCAN_STARTED)
                            .build());
    }

    @Override
    public void onMonitorStop() {
        sendScanBroadcast(new BroadcastBuilder()
                            .setInfo(INFO_SCAN_STOPPED)
                            .build());
    }

    @Override
    public void onBeaconsUpdated(Region region, List<BeaconDevice> beaconDevices) {
        /*You can send broadcast with entire list of Beacon devices.
        * However, please be aware of Bundle limitations.*/
    }

    @Override
    public void onBeaconAppeared(Region region, BeaconDevice beaconDevice) {
        sendScanBroadcast(new BroadcastBuilder()
                            .setInfo(INFO_BEACON_APPEARED)
                            .setBeaconDevice(beaconDevice)
                            .setRegion(region)
                            .build());
    }

    @Override
    public void onRegionEntered(Region region) {
        sendScanBroadcast(new BroadcastBuilder()
                                .setInfo(INFO_REGION_ENTERED)
                                .setRegion(region)
                                .build());
    }

    @Override
    public void onRegionAbandoned(Region region) {
        sendScanBroadcast(new BroadcastBuilder()
                                .setInfo(INFO_REGION_ABANDONED)
                                .setRegion(region)
                                .build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.disconnect();
        beaconManager = null;
    }

    private void startMonitoring() {
        try {
            if(!beaconManager.isConnected()) {
                beaconManager.connect(new OnServiceBoundListener() {
                    @Override
                    public void onServiceBound() throws RemoteException {
                        beaconManager.startMonitoring();
                    }
                });
            }
        } catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
    }

    private void stopMonitoring() {
        beaconManager.stopMonitoring();
    }

    private void sendScanBroadcast(final Intent intent) {
        sendOrderedBroadcast(intent, null);
    }

    private class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            final int messageCode = msg.what;

            switch(messageCode) {

                case BackgroundScanActivity.MESSAGE_START_SCAN:
                    startMonitoring();
                    break;

                case BackgroundScanActivity.MESSAGE_STOP_SCAN:
                    stopMonitoring();
                    break;
                
                default:
                    throw new IllegalArgumentException("Unsupported message Id: " + messageCode);
            }
        }
    }

    private static class BroadcastBuilder {
        private int info;
        private Region region;
        private BeaconDevice beaconDevice;

        public BroadcastBuilder setInfo(int info) {
            this.info = info;
            return this;
        }

        public BroadcastBuilder setBeaconDevice(BeaconDevice beaconDevice) {
            this.beaconDevice = beaconDevice;
            return this;
        }

        public BroadcastBuilder setRegion(Region region) {
            this.region = region;
            return this;
        }

        Intent build() {
            final Intent broadcast = new Intent(BROADCAST);
            broadcast.putExtra(EXTRA_BEACON, beaconDevice);
            broadcast.putExtra(EXTRA_INFO, info);
            broadcast.putExtra(EXTRA_REGION, region);

            return broadcast;
        }
    }
}
