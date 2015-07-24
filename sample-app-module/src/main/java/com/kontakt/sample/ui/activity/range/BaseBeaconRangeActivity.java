package com.kontakt.sample.ui.activity.range;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sample.adapter.range.BaseRangeAdapter;
import com.kontakt.sample.ui.activity.BaseActivity;
import com.kontakt.sample.ui.activity.management.BeaconManagementActivity;
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
import com.kontakt.sdk.android.ble.discovery.eddystone.EddystoneURLAdvertisingPacket;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconDeviceEvent;
import com.kontakt.sdk.android.ble.filter.eddystone.URLFilter;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.profile.DeviceProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

public abstract class BaseBeaconRangeActivity extends BaseActivity implements ProximityManager.ProximityListener, OnBluetoothStateChangeListener {

    abstract void callOnListItemClick(final int position);

    abstract EddystoneScanContext getEddystoneScanContext();

    abstract IBeaconScanContext getIBeaconScanContext();

    abstract BaseRangeAdapter getAdapter();


    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    protected static final int REQUEST_CODE_CONNECT_TO_DEVICE = 2;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.device_list)
    ListView deviceList;

    protected BaseRangeAdapter adapter;

    private ProximityManager deviceManager;

    private MenuItem bluetoothMenuItem;

    private ScanContext scanContext;

    private List<EventType> eventTypes = new ArrayList<EventType>() {{
        add(EventType.DEVICES_UPDATE);
    }};

    protected List<URLFilter> urlFilters = new ArrayList<URLFilter>() {{
        add(new URLFilter() {
            @Override
            public boolean apply(EddystoneURLAdvertisingPacket eddystoneURLAdvertisingPacket) {
                return eddystoneURLAdvertisingPacket.getUrl().contains("kontakt.io");
            }
        });
    }};


    protected IBeaconScanContext beaconScanContext = new IBeaconScanContext.Builder()
            .setEventTypes(eventTypes) //only specified events we be called on callback
            .setDevicesUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(2)) //how often DEVICES_UPDATE will be called
            .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
            .build();

    protected EddystoneScanContext eddystoneScanContext = new EddystoneScanContext.Builder()
            .setEventTypes(eventTypes)
            .setDevicesUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(2))
            .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
            .setURLFilters(urlFilters)
            .build();


    private BluetoothStateChangeReceiver bluetoothStateChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacon_range_activity);
        ButterKnife.inject(this);
        setUpActionBar(toolbar);
        setUpActionBarTitle(getString(R.string.range_beacons));

        adapter = getAdapter();

        deviceManager = new ProximityManager(this);

        deviceList.setAdapter(adapter);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (!BluetoothUtils.isBluetoothEnabled()) {
            Utils.showToast(this, "Please enable bluetooth");
        } else {
            startScan();
        }
    }

    private void startScan() {
        deviceManager.initializeScan(getOrCreateScanContext(), new OnServiceReadyListener() {
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

    private ScanContext getOrCreateScanContext() {
        if (scanContext == null) {
            scanContext = new ScanContext.Builder()
                    .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                    .setIBeaconScanContext(getIBeaconScanContext())
                    .setEddystoneScanContext(getEddystoneScanContext())
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

    //we will be notified only with events that we added to ScanContext
    @Override
    public void onEvent(BluetoothDeviceEvent event) {
        switch (event.getEventType()) {
            case DEVICES_UPDATE:
                onDevicesUpdateEvent(event);
                break;
        }
    }

    @Override
    public void onScanStart() {

    }

    @Override
    public void onScanStop() {

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

    private void onDevicesUpdateEvent(BluetoothDeviceEvent event) {
        DeviceProfile deviceProfile = event.getDeviceProfile();
        switch (deviceProfile) {
            case IBEACON:
                onIBeaconDevicesList(event);
                break;
            case EDDYSTONE:
                onEddystoneDevicesList(event);
                break;
        }
    }

    private void onIBeaconDevicesList(BluetoothDeviceEvent event) {
        final IBeaconDeviceEvent beaconDeviceEvent = (IBeaconDeviceEvent) event;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.replaceWith(beaconDeviceEvent.getDeviceList());
            }
        });
    }

    private void onEddystoneDevicesList(BluetoothDeviceEvent event) {
        final EddystoneDeviceEvent eddystoneDeviceEvent = (EddystoneDeviceEvent) event;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.replaceWith(eddystoneDeviceEvent.getDeviceList());
            }
        });
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
}
