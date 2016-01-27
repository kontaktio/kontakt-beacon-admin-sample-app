package com.kontakt.sample.action;

import android.content.Context;

import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.schedulers.Schedulers;


public class RxProximityManager {

    private ProximityManagerContract proximityManagerContract;

    private final RxProxmityListener rxProxmityListener = new RxProxmityListener();

    public RxProximityManager(Context context) {
        proximityManagerContract = new ProximityManager(context);
    }


    public Observable<RxBeaconEvent> scan(final ScanContext scanContext) {

        return Observable.create(new Observable.OnSubscribe<RxBeaconEvent>() {
            @Override
            public void call(final Subscriber<? super RxBeaconEvent> subscriber) {
                proximityManagerContract.initializeScan(scanContext, new OnServiceReadyListener() {
                    @Override
                    public void onServiceReady() {
                        rxProxmityListener.setSubscriber(subscriber);
                        proximityManagerContract.attachListener(rxProxmityListener);
                    }

                    @Override
                    public void onConnectionFailure() {

                    }
                });
            }
        }).doOnUnsubscribe(new Action0() {
            @Override
            public void call() {
                proximityManagerContract.detachListener(rxProxmityListener);
                proximityManagerContract.finishScan();
                proximityManagerContract.disconnect();
            }
        });
    }

    private static class RxProxmityListener implements ProximityManager.ProximityListener {

        private Subscriber<? super RxBeaconEvent> subscriber;

        public void setSubscriber(Subscriber<? super RxBeaconEvent> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onScanStart() {
            if (subscriber == null) {
                return;
            }
            if (subscriber.isUnsubscribed()) {
                return;
            }
            subscriber.onNext(new RxBeaconEvent(RxBeaconEvent.Type.SCAN_START, null));
        }

        @Override
        public void onScanStop() {
            if (subscriber == null) {
                return;
            }
            if (subscriber.isUnsubscribed()) {
                return;
            }
            subscriber.onNext(new RxBeaconEvent(RxBeaconEvent.Type.SCAN_STOP, null));
        }

        @Override
        public void onEvent(BluetoothDeviceEvent event) {
            if (subscriber == null) {
                return;
            }
            if (subscriber.isUnsubscribed()) {
                return;
            }

            subscriber.onNext(new RxBeaconEvent(RxBeaconEvent.Type.BLUETOOTH_EVENT, event));
        }
    }
}
