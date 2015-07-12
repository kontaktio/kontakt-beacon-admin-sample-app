package com.kontakt.sample.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.kontakt.sample.R;
import com.kontakt.sample.dialog.PasswordDialogFragment;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconAdvertisingPacket;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilter;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.interfaces.SDKBiConsumer;

import java.util.Collections;

public class BeaconRangeSyncableActivity extends BaseBeaconRangeActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNewScanContext();
    }

    //todo: remove
    private void createNewScanContext() {
        scanContext = new ScanContext.Builder()
                .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                .setActivityCheckConfiguration(ActivityCheckConfiguration.DEFAULT)
                .setForceScanConfiguration(ForceScanConfiguration.DEFAULT)
                .setScanPeriod(new ScanPeriod(15000, 5000))
                .setIBeaconScanContext(new IBeaconScanContext.Builder()
                        .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
                        .setIBeaconFilters(Collections.singleton(new IBeaconFilter() {
                            @Override
                            public boolean apply(IBeaconAdvertisingPacket iBeaconAdvertisingPacket) {
                                return iBeaconAdvertisingPacket.getBeaconUniqueId().equals("aMUi");
                            }
                        }))
                        .build())
                .build();
    }

    @Override
    void callOnListItemClick(int position) {
        final IBeaconDevice beacon = (IBeaconDevice) adapter.getItem(position);
        if (beacon != null) {
            PasswordDialogFragment.newInstance(getString(R.string.format_connect, beacon.getAddress()),
                    getString(R.string.password),
                    getString(R.string.connect),
                    new SDKBiConsumer<DialogInterface, String>() {
                        @Override
                        public void accept(DialogInterface dialogInterface, String password) {

                            beacon.setPassword(password.getBytes());

                            final Intent intent = new Intent(BeaconRangeSyncableActivity.this, SyncableBeaconManagementActivity.class);
                            intent.putExtra(BeaconManagementActivity.EXTRA_BEACON_DEVICE, beacon);

                            startActivityForResult(intent, REQUEST_CODE_CONNECT_TO_DEVICE);
                        }
                    }).show(getFragmentManager(), "dialog");
        }
    }
}
