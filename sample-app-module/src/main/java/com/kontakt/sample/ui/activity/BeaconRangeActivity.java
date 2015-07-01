package com.kontakt.sample.ui.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sample.adapter.BeaconBaseAdapter;
import com.kontakt.sample.dialog.PasswordDialogFragment;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.ble.configuration.BeaconActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.common.ibeacon.IBeaconDevice;
import com.kontakt.sdk.android.common.ibeacon.Region;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.interfaces.SDKBiConsumer;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

public class BeaconRangeActivity extends BaseActivity implements ProximityManager.RangingListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    private static final int REQUEST_CODE_CONNECT_TO_DEVICE = 2;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.device_list)
    ListView deviceList;

    private BeaconBaseAdapter adapter;

    private ProximityManager deviceManager;

    private final ScanContext scanContext = new ScanContext.Builder()
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

        if(!BluetoothUtils.isBluetoothEnabled()){
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        } else {
            deviceManager.initializeScan(scanContext, new OnServiceReadyListener() {
                @Override
                public void onServiceReady() {
                    deviceManager.attachListener(BeaconRangeActivity.this);
                }

                @Override
                public void onConnectionFailure() {
                    Utils.showToast(BeaconRangeActivity.this, getString(R.string.unexpected_error_connection));
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
    public void onTrimMemory(int level) {
        if(level == TRIM_MEMORY_UI_HIDDEN) {
            deviceManager.detachListener(this);
        }
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
        final IBeaconDevice beacon = (IBeaconDevice) adapter.getItem(position);
        if(beacon != null) {
            PasswordDialogFragment.newInstance(getString(R.string.format_connect, beacon.getAddress()),
                    getString(R.string.password),
                    getString(R.string.connect),
                    new SDKBiConsumer<DialogInterface, String>() {
                        @Override
                        public void accept(DialogInterface dialogInterface, String password) {

                            beacon.setPassword(password.getBytes());

                            final Intent intent = new Intent(BeaconRangeActivity.this, BeaconManagementActivity.class);
                            intent.putExtra(BeaconManagementActivity.EXTRA_BEACON_DEVICE, beacon);

                            startActivityForResult(intent, REQUEST_CODE_CONNECT_TO_DEVICE);
                        }
                    }).show(getFragmentManager(), "dialog");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if(resultCode != Activity.RESULT_OK) {
                final String bluetoothNotEnabledInfo = getString(R.string.bluetooth_not_enabled);
                Toast.makeText(BeaconRangeActivity.this, bluetoothNotEnabledInfo, Toast.LENGTH_LONG).show();
            }
            return;
        }  else if(requestCode == REQUEST_CODE_CONNECT_TO_DEVICE) {
            if(resultCode == Activity.RESULT_CANCELED) {
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
