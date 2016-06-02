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
import com.kontakt.sdk.android.ble.configuration.scan.ScanMode;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconAdvertisingPacket;
import com.kontakt.sdk.android.ble.exception.ScanError;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilter;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.ScanStatusListener;
import com.kontakt.sdk.android.ble.manager.listeners.SpaceListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleSpaceListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class BackgroundScanService extends Service implements ScanStatusListener {

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

  private final Messenger serviceMessenger = new Messenger(new ServiceHandler());

  static {
    INFO_LIST = Collections.unmodifiableList(
        Arrays.asList(INFO_BEACON_APPEARED, INFO_REGION_ABANDONED, INFO_REGION_ENTERED, INFO_SCAN_STARTED, INFO_SCAN_STOPPED));
  }

  private ProximityManager proximityManager;

  @Override
  public void onCreate() {
    super.onCreate();
    proximityManager = new ProximityManager(this);
    proximityManager.setScanStatusListener(this);
    proximityManager.setIBeaconListener(createIBeaconListener());
    proximityManager.setSpaceListener(createSpaceListener());
    proximityManager.configuration()
        .scanMode(ScanMode.BALANCED)
        .activityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
        .forceScanConfiguration(ForceScanConfiguration.MINIMAL);

    proximityManager.filters().iBeaconFilter(new IBeaconFilter() {
      @Override
      public boolean apply(IBeaconAdvertisingPacket iBeaconAdvertisingPacket) {
        final UUID proximityUUID = iBeaconAdvertisingPacket.getProximityUUID();
        return proximityUUID.equals(KontaktSDK.DEFAULT_KONTAKT_BEACON_PROXIMITY_UUID);
      }
    });
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return serviceMessenger.getBinder();
  }

  @Override
  public void onDestroy() {
    proximityManager.disconnect();
    proximityManager = null;
    super.onDestroy();
  }

  private void startMonitoring() {
    proximityManager.connect(new OnServiceReadyListener() {
      @Override
      public void onServiceReady() {
        proximityManager.startScanning();
      }
    });
  }

  @Override
  public void onScanStart() {
    scheduleBroadcast(new BroadcastBuilder().setInfo(INFO_SCAN_STARTED).build());
  }

  @Override
  public void onScanStop() {
    scheduleBroadcast(new BroadcastBuilder().setInfo(INFO_SCAN_STOPPED).build());
  }

  @Override
  public void onScanError(ScanError exception) {
    //Ignore
  }

  @Override
  public void onMonitoringCycleStart() {
    //Ignore
  }

  @Override
  public void onMonitoringCycleStop() {
    //Igonre
  }

  private IBeaconListener createIBeaconListener() {
    return new SimpleIBeaconListener() {
      @Override
      public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
        scheduleBroadcast(new BroadcastBuilder().setInfo(INFO_BEACON_APPEARED).setBeaconDevice(ibeacon).setRegion(region).build());
      }
    };
  }

  private SpaceListener createSpaceListener() {
    return new SimpleSpaceListener() {
      @Override
      public void onRegionEntered(IBeaconRegion region) {
        scheduleBroadcast(new BroadcastBuilder().setInfo(INFO_REGION_ENTERED).setRegion(region).build());
      }

      @Override
      public void onRegionAbandoned(IBeaconRegion region) {
        scheduleBroadcast(new BroadcastBuilder().setInfo(INFO_REGION_ABANDONED).setRegion(region).build());
      }
    };
  }

  private void stopMonitoring() {
    proximityManager.stopScanning();
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
