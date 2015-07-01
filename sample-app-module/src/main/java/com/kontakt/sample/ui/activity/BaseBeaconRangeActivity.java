package com.kontakt.sample.ui.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sample.adapter.BeaconBaseAdapter;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.ble.configuration.BeaconActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.ibeacon.IBeaconDevice;
import com.kontakt.sdk.android.common.ibeacon.Region;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

public abstract class BaseBeaconRangeActivity extends BaseActivity implements ProximityManager.RangingListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    protected static final int REQUEST_CODE_CONNECT_TO_DEVICE = 2;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.device_list)
    ListView deviceList;

    protected BeaconBaseAdapter adapter;

    private ProximityManager deviceManager;

    protected ScanContext scanContext = new ScanContext.Builder()
            .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
            .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
            .setBeaconActivityCheckConfiguration(BeaconActivityCheckConfiguration.DEFAULT)
            .setForceScanConfiguration(ForceScanConfiguration.DEFAULT)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacon_range_activity);
        ButterKnife.inject(this);
        setUpActionBar(toolbar);
        setUpActionBarTitle(getString(R.string.range_beacons));

        adapter = new BeaconBaseAdapter(this);

        deviceManager = new ProximityManager(this);

        deviceList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!BluetoothUtils.isBluetoothEnabled()) {
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        } else {
            deviceManager.initializeScan(scanContext, new OnServiceReadyListener() {
                @Override
                public void onServiceReady() {
                    deviceManager.attachListener(BaseBeaconRangeActivity.this);
                }

                @Override
                public void onConnectionFailure() {
                    Utils.showToast(BaseBeaconRangeActivity.this, getString(R.string.unexpected_error_connection));
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        deviceManager.finishScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceManager.disconnect();
        deviceManager = null;
        ButterKnife.reset(this);
    }

    @OnItemClick(R.id.device_list)
    void onListItemClick(final int position) {
        callOnListItemClick(position);
    }

    abstract void callOnListItemClick(final int position);

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if (resultCode != Activity.RESULT_OK) {
                final String bluetoothNotEnabledInfo = getString(R.string.bluetooth_not_enabled);
                Toast.makeText(BaseBeaconRangeActivity.this, bluetoothNotEnabledInfo, Toast.LENGTH_LONG).show();
            }
            return;
        } else if (requestCode == REQUEST_CODE_CONNECT_TO_DEVICE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this,
                        String.format("Beacon authentication failure: %s", data.getExtras().getString(BeaconManagementActivity.EXTRA_FAILURE_MESSAGE, "")),
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onIBeaconsDiscovered(Region region, final List<IBeaconDevice> iBeaconDevices) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.replaceWith(iBeaconDevices);
            }
        });
    }
}
