package com.kontakt.sample.action;

import android.content.Context;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.ScanMode;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.exception.ScanError;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.ScanStatusListener;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;

public class RxProximityManager {

  private final RxProximityListener rxProximityListener = new RxProximityListener();
  private ProximityManagerContract proximityManager;

  public RxProximityManager(Context context) {
    proximityManager = new ProximityManager(context);
    proximityManager.configuration()
        .scanMode(ScanMode.BALANCED)
        .scanPeriod(ScanPeriod.create(TimeUnit.SECONDS.toMillis(15), TimeUnit.SECONDS.toMillis(5)))
        .forceScanConfiguration(ForceScanConfiguration.MINIMAL)
        .rssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5));
  }

  public Observable<RxBeaconEvent> scan() {

    return Observable.create(new Observable.OnSubscribe<RxBeaconEvent>() {
      @Override
      public void call(final Subscriber<? super RxBeaconEvent> subscriber) {
        proximityManager.setIBeaconListener(rxProximityListener);
        proximityManager.setEddystoneListener(rxProximityListener);
        proximityManager.setScanStatusListener(rxProximityListener);
        proximityManager.connect(new OnServiceReadyListener() {
          @Override
          public void onServiceReady() {
            rxProximityListener.setSubscriber(subscriber);
            proximityManager.startScanning();
          }
        });
      }
    }).doOnUnsubscribe(new Action0() {
      @Override
      public void call() {
        proximityManager.stopScanning();
        proximityManager.disconnect();
      }
    });
  }

  private static class RxProximityListener implements ScanStatusListener, IBeaconListener, EddystoneListener {

    private Subscriber<? super RxBeaconEvent> subscriber;

    public void setSubscriber(Subscriber<? super RxBeaconEvent> subscriber) {
      this.subscriber = subscriber;
    }

    @Override
    public void onScanStart() {
      if (subscriber == null || subscriber.isUnsubscribed()) {
        return;
      }
      subscriber.onNext(new RxBeaconEvent(RxBeaconEvent.Type.SCAN_START, null));
    }

    @Override
    public void onScanStop() {
      if (subscriber == null || subscriber.isUnsubscribed()) {
        return;
      }
      subscriber.onNext(new RxBeaconEvent(RxBeaconEvent.Type.SCAN_STOP, null));
    }

    @Override
    public void onScanError(ScanError exception) {
      if (subscriber == null || subscriber.isUnsubscribed()) {
        return;
      }
      subscriber.onError(new Throwable(exception.getMessage()));
    }

    @Override
    public void onMonitoringCycleStart() {

    }

    @Override
    public void onMonitoringCycleStop() {

    }

    @Override
    public void onIBeaconDiscovered(IBeaconDevice iBeacon, IBeaconRegion region) {
      if (subscriber == null || subscriber.isUnsubscribed()) {
        return;
      }
      subscriber.onNext(new RxBeaconEvent(RxBeaconEvent.Type.IBEACON_DISCOVERED, iBeacon));
    }

    @Override
    public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
      if (subscriber == null || subscriber.isUnsubscribed()) {
        return;
      }
      subscriber.onNext(new RxBeaconEvent(RxBeaconEvent.Type.EDDYSTONE_DISCOVERED, eddystone));
    }

    @Override
    public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {

    }

    @Override
    public void onEddystoneLost(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {

    }

    @Override
    public void onIBeaconsUpdated(List<IBeaconDevice> iBeacons, IBeaconRegion region) {

    }

    @Override
    public void onIBeaconLost(IBeaconDevice iBeacon, IBeaconRegion region) {

    }
  }
}
