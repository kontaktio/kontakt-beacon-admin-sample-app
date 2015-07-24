package com.kontakt.sample.ui.activity.monitor;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.kontakt.sample.R;
import com.kontakt.sample.adapter.monitor.AllBeaconsMonitorAdapter;
import com.kontakt.sample.ui.activity.BaseActivity;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.ble.broadcast.BluetoothStateChangeReceiver;
import com.kontakt.sdk.android.ble.broadcast.OnBluetoothStateChangeListener;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.discovery.eddystone.EddystoneDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconDeviceEvent;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.profile.DeviceProfile;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AllBeaconsMonitorActivity extends BaseActivity implements ProximityManager.ProximityListener, OnBluetoothStateChangeListener {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.list)
    ExpandableListView list;


    private AllBeaconsMonitorAdapter allBeaconsRangeAdapter;

    private ProximityManager deviceManager;

    private MenuItem bluetoothMenuItem;

    private ScanContext scanContext;

    private List<EventType> eventTypes = new ArrayList<EventType>() {{
        add(EventType.DEVICES_UPDATE);
    }};

    private IBeaconScanContext beaconScanContext = new IBeaconScanContext.Builder()
            .setEventTypes(eventTypes) //only specified events we be called on callback
            .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
            .build();

    private EddystoneScanContext eddystoneScanContext = new EddystoneScanContext.Builder()
            .setEventTypes(eventTypes)
            .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
            .build();

    private BluetoothStateChangeReceiver bluetoothStateChangeReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacon_monitor_list_activity);
        ButterKnife.inject(this);
        setUpActionBar(toolbar);
        setUpActionBarTitle(getString(R.string.range_beacons));

        allBeaconsRangeAdapter = new AllBeaconsMonitorAdapter(this);

        deviceManager = new ProximityManager(this);
        list.setAdapter(allBeaconsRangeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.range_menu, menu);
        bluetoothMenuItem = menu.findItem(R.id.change_bluetooth_state);
        setCorrectMenuItemTitle();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_bluetooth_state:
                changeBluetoothState();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startScan() {
        deviceManager.initializeScan(getOrCreateScanContext(), new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                deviceManager.attachListener(AllBeaconsMonitorActivity.this);
            }

            @Override
            public void onConnectionFailure() {
                Utils.showToast(AllBeaconsMonitorActivity.this, getString(R.string.unexpected_error_connection));
            }
        });
    }

    private ScanContext getOrCreateScanContext() {
        if (scanContext == null) {
            scanContext = new ScanContext.Builder()
                    .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                    .setIBeaconScanContext(beaconScanContext)
                    .setEddystoneScanContext(eddystoneScanContext)
                    .setActivityCheckConfiguration(ActivityCheckConfiguration.DEFAULT)
                    .setForceScanConfiguration(ForceScanConfiguration.DEFAULT)
                    .build();
        }

        return scanContext;
    }

    @Override
    protected void onStart() {
        super.onStart();
        bluetoothStateChangeReceiver = new BluetoothStateChangeReceiver(this);
        registerReceiver(bluetoothStateChangeReceiver, new IntentFilter(BluetoothStateChangeReceiver.ACTION));
        setCorrectMenuItemTitle();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(bluetoothStateChangeReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!BluetoothUtils.isBluetoothEnabled()) {
            Utils.showToast(this, "Please enable bluetooth");
        } else {
            startScan();
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

    @Override
    public void onScanStart() {

    }

    @Override
    public void onScanStop() {

    }

    @Override
    public void onEvent(BluetoothDeviceEvent event) {
        switch (event.getEventType()) {
            case DEVICES_UPDATE:
                onDevicesUpdateEvent(event);
                break;
        }
    }

    @Override
    public void onBluetoothConnecting() {

    }

    @Override
    public void onBluetoothConnected() {
        startScan();
        changeBluetoothTitle(true);
    }

    @Override
    public void onBluetoothDisconnecting() {

    }

    @Override
    public void onBluetoothDisconnected() {
        deviceManager.finishScan();
        changeBluetoothTitle(false);
    }


    private void setCorrectMenuItemTitle() {
        if (bluetoothMenuItem == null) {
            return;
        }
        boolean enabled = Utils.getBluetoothState();
        changeBluetoothTitle(enabled);
    }

    private void changeBluetoothTitle(boolean enabled) {
        if (enabled) {
            bluetoothMenuItem.setTitle(R.string.disable_bluetooth);
        } else {
            bluetoothMenuItem.setTitle(R.string.enable_bluetooth);
        }
    }

    private void changeBluetoothState() {
        boolean enabled = Utils.getBluetoothState();
        Utils.setBluetooth(!enabled);
    }


    private void onDevicesUpdateEvent(BluetoothDeviceEvent event) {
        DeviceProfile deviceProfile = event.getDeviceProfile();
        switch (deviceProfile) {
            case IBEACON:
                onIBeaconDevicesList((IBeaconDeviceEvent) event);
                break;
            case EDDYSTONE:
                onEddystoneDevicesList((EddystoneDeviceEvent) event);
                break;
        }
    }

    private void onEddystoneDevicesList(final EddystoneDeviceEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                allBeaconsRangeAdapter.replaceEddystoneBeacons(event.getDeviceList());
            }
        });
    }

    private void onIBeaconDevicesList(final IBeaconDeviceEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                allBeaconsRangeAdapter.replaceIBeacons(event.getDeviceList());
            }
        });
    }

}
