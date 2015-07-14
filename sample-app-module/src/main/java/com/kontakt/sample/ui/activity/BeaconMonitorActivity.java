package com.kontakt.sample.ui.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sample.adapter.MonitorSectionAdapter;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconDeviceEvent;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilters;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BeaconMonitorActivity extends BaseActivity implements ProximityManager.ProximityListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    private MonitorSectionAdapter adapter;

    private ProximityManager deviceManager;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.list)
    ExpandableListView list;

    private ScanContext scanContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacon_monitor_list_activity);
        ButterKnife.inject(this);
        setUpActionBar(toolbar);
        setUpActionBarTitle(getString(R.string.monitor_beacons));

        adapter = new MonitorSectionAdapter(this);

        list.setAdapter(adapter);

        deviceManager = new ProximityManager(this);

        scanContext = createScanContext();
    }

    private ScanContext createScanContext() {
        return new ScanContext.Builder()
                .setScanPeriod(new ScanPeriod(TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(5)))
                .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                .setActivityCheckConfiguration(ActivityCheckConfiguration.DEFAULT)
                .setIBeaconScanContext(new IBeaconScanContext.Builder()
                        .setIBeaconFilters(Collections.singleton(
                                IBeaconFilters.newProximityUUIDFilter(KontaktSDK.DEFAULT_KONTAKT_BEACON_PROXIMITY_UUID)
                        ))
                        .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
                        .build())
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!BluetoothUtils.isBluetoothEnabled()) {
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        } else {
            startMonitoring();
        }
    }

    private void startMonitoring() {
        deviceManager.initializeScan(scanContext, new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                deviceManager.attachListener(BeaconMonitorActivity.this);
            }

            @Override
            public void onConnectionFailure() {
                Utils.showToast(BeaconMonitorActivity.this, getString(R.string.unexpected_error_connection));
            }
        });
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (level == TRIM_MEMORY_UI_HIDDEN) {
            deviceManager.detachListener(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        deviceManager.finishScan();
    }

    @Override
    protected void onDestroy() {
        deviceManager.disconnect();
        deviceManager = null;
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                startMonitoring();
            } else {
                final String bluetoothNotEnabledInfo = getString(R.string.bluetooth_not_enabled);
                Toast.makeText(this, bluetoothNotEnabledInfo, Toast.LENGTH_LONG).show();
                getSupportActionBar().setSubtitle(bluetoothNotEnabledInfo);
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onScanStart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setProgressBarIndeterminateVisibility(true);
            }
        });
    }

    @Override
    public void onScanStop() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setProgressBarIndeterminateVisibility(false);
            }
        });
    }

    @Override
    public void onEvent(BluetoothDeviceEvent event) {

        final IBeaconDeviceEvent iBeaconDeviceEvent = (IBeaconDeviceEvent) event;

        final IBeaconRegion region = iBeaconDeviceEvent.getRegion();

        final List<IBeaconDevice> deviceList = iBeaconDeviceEvent.getDeviceList();

        switch (iBeaconDeviceEvent.getEventType()) {

            case SPACE_ENTERED:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!adapter.containsGroup(region)) {
                            adapter.addGroup(region);
                        }
                    }
                });
                break;

            case DEVICE_DISCOVERED:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int index = adapter.getGroupIndex(region);
                        if (index != -1) {
                            adapter.addOrReplaceChild(index, deviceList.get(0));
                        }
                    }
                });
                break;

            case DEVICES_UPDATE:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int index = adapter.getGroupIndex(region);
                        if (index != -1) {
                            adapter.replaceChildren(index, deviceList);
                        }
                    }
                });
                break;

            case SPACE_ABANDONED:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.removeGroup(region);
                    }
                });
                break;

            default:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        throw new IllegalStateException("This event should never occur becuase it is not specified in ScanContext: " + iBeaconDeviceEvent.getEventType().name());
                    }
                });


        }

    }
}
