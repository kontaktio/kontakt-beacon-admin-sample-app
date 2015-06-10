package com.kontakt.sample.ui.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.Toolbar;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sample.adapter.MonitorSectionAdapter;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.ble.configuration.BeaconActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanContext;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceBoundListener;
import com.kontakt.sdk.android.ble.device.IBeaconDevice;
import com.kontakt.sdk.android.ble.device.IRegion;
import com.kontakt.sdk.android.ble.filter.Filters;
import com.kontakt.sdk.android.ble.manager.BeaconManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class BeaconMonitorActivity extends BaseActivity implements BeaconManager.MonitoringListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    private MonitorSectionAdapter adapter;

    private BeaconManager deviceManager;

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

        deviceManager = new BeaconManager(this);

        scanContext = createScanContext();
    }

    private ScanContext createScanContext() {
        return new ScanContext.Builder()
                .setScanPeriod(new ScanPeriod(TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(5)))
                .addIBeaconFilter(Filters.newProximityUUIDFilter(BeaconManager.DEFAULT_KONTAKT_BEACON_PROXIMITY_UUID))
                .setScanMode(BeaconManager.SCAN_MODE_BALANCED)
                .setBeaconActivityCheckConfiguration(BeaconActivityCheckConfiguration.DEFAULT)
                .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!deviceManager.isBluetoothEnabled()) {
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        } else if(deviceManager.isConnected()) {
            startMonitoring();
        } else {
            connectAndStartMonitoring();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        deviceManager.finishScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceManager.disconnect();
        deviceManager = null;
        ButterKnife.reset(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if(resultCode == Activity.RESULT_OK) {
                connectAndStartMonitoring();
            } else {
                final String bluetoothNotEnabledInfo = getString(R.string.bluetooth_not_enabled);
                Toast.makeText(this, bluetoothNotEnabledInfo, Toast.LENGTH_LONG).show();
                getSupportActionBar().setSubtitle(bluetoothNotEnabledInfo);
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startMonitoring() {
        try {
            deviceManager.initializeScan(scanContext);
        } catch (RemoteException e) {
            Utils.showToast(this, getString(R.string.unexpected_error_monitoring_start));
        }
    }

    private void connectAndStartMonitoring() {
            deviceManager.connect(new OnServiceBoundListener() {
                @Override
                public void onServiceBound() {
                    try {
                        deviceManager.initializeScan(scanContext);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    @Override
    public void onMonitorStart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setProgressBarIndeterminateVisibility(true);
            }
        });
    }

    @Override
    public void onMonitorStop() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setProgressBarIndeterminateVisibility(false);
            }
        });
    }

    @Override
    public void onIBeaconsUpdated(final IRegion region, final List<IBeaconDevice> beacons) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final int index = adapter.getGroupIndex(region);
                if (index != -1) {
                    adapter.replaceChildren(index, beacons);
                }
            }
        });
    }

    @Override
    public void onIBeaconAppeared(final IRegion region, final IBeaconDevice beacon) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = adapter.getGroupIndex(region);
                if (index != -1) {
                    adapter.addOrReplaceChild(index, beacon);
                }
            }
        });
    }

    @Override
    public void onRegionEntered(final IRegion venue) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (! adapter.containsGroup(venue)) {
                    adapter.addGroup(venue);
                }
            }
        });
    }

    @Override
    public void onRegionAbandoned(final IRegion region) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.removeGroup(region);
            }
        });
    }
}
