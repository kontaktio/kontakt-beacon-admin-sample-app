package com.kontakt.sample.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import com.kontakt.sample.ui.fragment.BackgroundScanFragment;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconAdvertisingPacket;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconDeviceEvent;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilter;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class BackgroundScanService extends Service implements ProximityManager.ProximityListener {

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

    private ProximityManager beaconManger;

    private final ScanContext scanContext = new ScanContext.Builder()
            .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
            .setActivityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
            .setForceScanConfiguration(ForceScanConfiguration.MINIMAL)
            .setIBeaconScanContext(new IBeaconScanContext.Builder()
                    .setEventTypes(EnumSet.of(
                            EventType.SPACE_ENTERED,
                            EventType.DEVICE_DISCOVERED,
                            EventType.SPACE_ABANDONED
                    ))
                    .setIBeaconFilters(Collections.singleton(new IBeaconFilter() {
                        @Override
                        public boolean apply(IBeaconAdvertisingPacket iBeaconAdvertisingPacket) {
                            final UUID proximityUUID = iBeaconAdvertisingPacket.getProximityUUID();
                            final double distance = iBeaconAdvertisingPacket.getDistance();

                            return proximityUUID.equals(KontaktSDK.DEFAULT_KONTAKT_BEACON_PROXIMITY_UUID) && distance <= ACCEPT_DISTANCE;
                        }
                    }))
                    .build())
            .build();

    @Override
    public void onCreate() {
        super.onCreate();

        beaconManger = new ProximityManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManger.disconnect();
        beaconManger = null;
    }

    private void startMonitoring() {
        beaconManger.initializeScan(scanContext, new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                beaconManger.attachListener(BackgroundScanService.this);
            }

            @Override
            public void onConnectionFailure() {

            }
        });
    }

    @Override
    public void onScanStart() {
        scheduleBroadcast(new BroadcastBuilder()
                .setInfo(INFO_SCAN_STARTED)
                .build());
    }

    @Override
    public void onScanStop() {
        scheduleBroadcast(new BroadcastBuilder()
                .setInfo(INFO_SCAN_STOPPED)
                .build());
    }

    @Override
    public void onEvent(BluetoothDeviceEvent event) {

        final IBeaconDeviceEvent iBeaconDeviceEvent = (IBeaconDeviceEvent) event;

        switch (event.getEventType()) {

            case SPACE_ENTERED:
                scheduleBroadcast(new BroadcastBuilder()
                        .setInfo(INFO_REGION_ENTERED)
                        .setRegion(iBeaconDeviceEvent.getRegion())
                        .build());
                break;

            case DEVICE_DISCOVERED:
                scheduleBroadcast(new BroadcastBuilder()
                        .setInfo(INFO_BEACON_APPEARED)
                        .setBeaconDevice(iBeaconDeviceEvent.getDeviceList().get(0))
                        .setRegion(iBeaconDeviceEvent.getRegion())
                        .build());
                break;

            case SPACE_ABANDONED:
                scheduleBroadcast(new BroadcastBuilder()
                        .setInfo(INFO_REGION_ABANDONED)
                        .setRegion(iBeaconDeviceEvent.getRegion())
                        .build());
                break;

            default:
                throw new IllegalStateException("This event should never occur because it is not specified in ScanContext: " + event.getEventType().name());
        }
    }

    private void stopMonitoring() {
        beaconManger.finishScan();
    }

    private void scheduleBroadcast(Intent intent) {
        sendOrderedBroadcast(intent, null);
    }

    private class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            final int messageCode = msg.what;

            switch (messageCode) {

                case BackgroundScanFragment.MESSAGE_START_SCAN:
                    startMonitoring();
                    break;

                case BackgroundScanFragment.MESSAGE_STOP_SCAN:
                    stopMonitoring();
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported message Id: " + messageCode);
            }
        }
    }

    private static class BroadcastBuilder {
        private int info;
        private IBeaconRegion region;
        private IBeaconDevice beaconDevice;

        public BroadcastBuilder setInfo(int info) {
            this.info = info;
            return this;
        }

        public BroadcastBuilder setBeaconDevice(IBeaconDevice beaconDevice) {
            this.beaconDevice = beaconDevice;
            return this;
        }

        public BroadcastBuilder setRegion(IBeaconRegion region) {
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
