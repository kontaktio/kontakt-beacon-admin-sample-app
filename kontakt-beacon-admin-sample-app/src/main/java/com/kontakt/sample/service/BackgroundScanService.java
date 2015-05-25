package com.kontakt.sample.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.kontakt.sample.ui.activity.BackgroundScanActivity;
import com.kontakt.sdk.ble.configuration.BeaconActivityCheckConfiguration;
import com.kontakt.sdk.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.ble.connection.OnServiceBoundListener;
import com.kontakt.sdk.ble.device.IBeaconDevice;
import com.kontakt.sdk.ble.device.IRegion;
import com.kontakt.sdk.ble.discovery.IBeaconAdvertisingPacket;
import com.kontakt.sdk.ble.filter.CustomFilter;
import com.kontakt.sdk.ble.manager.BeaconManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BackgroundScanService extends Service implements BroadcastScheduler {

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
        beaconManager.registerMonitoringListener(new BackgroundMonitoringListener(this));

        beaconManager.addFilter(new CustomFilter() {
            @Override
            public boolean apply(IBeaconAdvertisingPacket object) {
                final UUID proximityUUID = object.getProximityUUID();
                final double distance = object.getDistance();

                return proximityUUID.equals(IRegion.DEFAULT_KONTAKT_BEACON_PROXIMITY_UUID) && distance <= ACCEPT_DISTANCE;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
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

    @Override
    public void scheduleBroadcast(Intent intent) {
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

    private static class BackgroundMonitoringListener extends BeaconManager.MonitoringListener {

        private BroadcastScheduler scheduler;

        BackgroundMonitoringListener(final BroadcastScheduler scheduler) {
            this.scheduler = scheduler;
        }

        @Override
        public void onMonitorStart() {
            scheduler.scheduleBroadcast(new BroadcastBuilder()
                    .setInfo(INFO_SCAN_STARTED)
                    .build());
        }

        @Override
        public void onMonitorStop() {
            scheduler.scheduleBroadcast(new BroadcastBuilder()
                    .setInfo(INFO_SCAN_STOPPED)
                    .build());
        }

        @Override
        public void onIBeaconsUpdated(IRegion region, List<IBeaconDevice> beaconDevices) {
        /*You can send broadcast with entire list of Beacon devices.
        * However, please be aware of Bundle limitations.*/
        }

        @Override
        public void onIBeaconAppeared(IRegion region, IBeaconDevice beaconDevice) {
            scheduler.scheduleBroadcast(new BroadcastBuilder()
                    .setInfo(INFO_BEACON_APPEARED)
                    .setBeaconDevice(beaconDevice)
                    .setRegion(region)
                    .build());
        }

        @Override
        public void onRegionEntered(IRegion region) {
            scheduler.scheduleBroadcast(new BroadcastBuilder()
                    .setInfo(INFO_REGION_ENTERED)
                    .setRegion(region)
                    .build());
        }

        @Override
        public void onRegionAbandoned(IRegion region) {
            scheduler.scheduleBroadcast(new BroadcastBuilder()
                    .setInfo(INFO_REGION_ABANDONED)
                    .setRegion(region)
                    .build());
        }
    }

    private static class BroadcastBuilder {
        private int info;
        private IRegion region;
        private IBeaconDevice beaconDevice;

        public BroadcastBuilder setInfo(int info) {
            this.info = info;
            return this;
        }

        public BroadcastBuilder setBeaconDevice(IBeaconDevice beaconDevice) {
            this.beaconDevice = beaconDevice;
            return this;
        }

        public BroadcastBuilder setRegion(IRegion region) {
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
