package com.kontakt.sample.ui.activity.monitor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sample.adapter.monitor.BaseMonitorAdapter;
import com.kontakt.sample.ui.activity.BaseActivity;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.discovery.eddystone.EddystoneDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconDeviceEvent;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilters;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.DeviceProfile;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;

public abstract class BaseBeaconMonitorActivity extends BaseActivity implements ProximityManager.ProximityListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    private BaseMonitorAdapter adapter;

    private ProximityManager deviceManager;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.list)
    ExpandableListView list;

    private ScanContext scanContext;

    private List<EventType> eventTypes = Arrays.asList(EventType.SPACE_ENTERED,
            EventType.SPACE_ABANDONED,
            EventType.DEVICE_DISCOVERED,
            EventType.DEVICES_UPDATE);

    protected IBeaconScanContext iBeaconScanContext = new IBeaconScanContext.Builder()
            .setIBeaconFilters(Collections.singleton(
                    IBeaconFilters.newProximityUUIDFilter(KontaktSDK.DEFAULT_KONTAKT_BEACON_PROXIMITY_UUID)
            ))
            .setEventTypes(eventTypes)
            .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
            .build();

    protected EddystoneScanContext eddystoneScanContext = new EddystoneScanContext.Builder()
            .setEventTypes(eventTypes)
            .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacon_monitor_list_activity);
        ButterKnife.inject(this);
        setUpActionBar(toolbar);
        setUpActionBarTitle(getString(R.string.monitor_beacons));

        adapter = getAdapter();

        list.setAdapter(adapter);

        deviceManager = new ProximityManager(this);

        scanContext = createScanContext();
    }


    private ScanContext createScanContext() {
        return new ScanContext.Builder()
                .setScanPeriod(new ScanPeriod(TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(5)))
                .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                .setActivityCheckConfiguration(ActivityCheckConfiguration.DEFAULT)
                .setIBeaconScanContext(getIBeaconScanContext())
                .setEddystoneScanContext(getEddystoneScanContext())
                .build();
    }

    abstract IBeaconScanContext getIBeaconScanContext();

    abstract EddystoneScanContext getEddystoneScanContext();

    abstract BaseMonitorAdapter getAdapter();

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
                deviceManager.attachListener(BaseBeaconMonitorActivity.this);
            }

            @Override
            public void onConnectionFailure() {
                Utils.showToast(BaseBeaconMonitorActivity.this, getString(R.string.unexpected_error_connection));
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

    private void onIBeaconEvent(final BluetoothDeviceEvent event) {
        IBeaconDeviceEvent iBeaconDeviceEvent = (IBeaconDeviceEvent) event;
        final IBeaconRegion region = iBeaconDeviceEvent.getRegion();
        final List<IBeaconDevice> deviceList = iBeaconDeviceEvent.getDeviceList();
        final int index = adapter.getGroupIndex(region);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (event.getEventType()) {
                    case SPACE_ENTERED:
                        if (!adapter.containsGroup(region)) {
                            adapter.addGroup(region);
                        }
                        break;
                    case DEVICE_DISCOVERED:
                        if (index != -1) {
                            adapter.addOrReplaceChild(index, deviceList.get(0));
                        }
                        break;
                    case DEVICES_UPDATE:
                        if (index != -1) {
                            adapter.replaceChildren(index, deviceList);
                        }
                        break;
                    case SPACE_ABANDONED:
                        adapter.removeGroup(region);
                        break;
                }
            }
        });
    }


    private void onEddystoneEvent(final BluetoothDeviceEvent event) {
        EddystoneDeviceEvent eddystoneDeviceEvent = (EddystoneDeviceEvent) event;
        final List<IEddystoneDevice> deviceList = eddystoneDeviceEvent.getDeviceList();
        final IEddystoneNamespace namespace = eddystoneDeviceEvent.getNamespace();
        final int index = adapter.getGroupIndex(namespace);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (event.getEventType()) {
                    case SPACE_ENTERED:
                        if (!adapter.containsGroup(namespace)) {
                            adapter.addGroup(namespace);
                        }
                        break;
                    case DEVICE_DISCOVERED:
                        if (index != -1) {
                            adapter.addOrReplaceChild(index, deviceList.get(0));
                        }
                        break;
                    case DEVICES_UPDATE:
                        if (index != -1) {
                            adapter.replaceChildren(index, deviceList);
                        }
                        break;
                    case SPACE_ABANDONED:
                        adapter.removeGroup(namespace);
                        break;
                }
            }
        });
    }

    @Override
    public void onEvent(BluetoothDeviceEvent event) {

        DeviceProfile deviceProfile = event.getDeviceProfile();

        switch (deviceProfile) {
            case IBEACON:
                onIBeaconEvent(event);
                break;
            case EDDYSTONE:
                onEddystoneEvent(event);
                break;
        }

    }
}
