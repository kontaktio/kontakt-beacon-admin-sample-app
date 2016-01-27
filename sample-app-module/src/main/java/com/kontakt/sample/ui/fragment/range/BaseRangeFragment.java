package com.kontakt.sample.ui.fragment.range;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.kontakt.sample.R;
import com.kontakt.sample.permission.PermissionChecker;
import com.kontakt.sample.ui.adapter.range.BaseRangeAdapter;
import com.kontakt.sample.ui.fragment.BaseFragment;
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
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;

public abstract class BaseRangeFragment extends BaseFragment implements ProximityManager.ProximityListener, OnBluetoothStateChangeListener {

    abstract void callOnListItemClick(final int position);

    abstract EddystoneScanContext getEddystoneScanContext();

    abstract IBeaconScanContext getIBeaconScanContext();

    abstract BaseRangeAdapter getAdapter();

    protected static final int REQUEST_CODE_CONNECT_TO_DEVICE = 2;

    @InjectView(R.id.device_list)
    ListView deviceList;

    private MenuItem bluetoothMenuItem;
    private ScanContext scanContext;
    private ProximityManager proximityManager;
    private BluetoothStateChangeReceiver bluetoothStateChangeReceiver;

    protected BaseRangeAdapter adapter;

    private List<EventType> eventTypes = new ArrayList<EventType>() {{
        add(EventType.DEVICES_UPDATE);
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
            .build();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.beacon_range_activity, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ButterKnife.inject(this, getView());
        proximityManager = new ProximityManager(getActivity());
        adapter = getAdapter();
        deviceList.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.range_menu, menu);
        bluetoothMenuItem = menu.findItem(R.id.change_bluetooth_state);
        setCorrectMenuItemTitle();
        super.onCreateOptionsMenu(menu, inflater);
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
    public void onStart() {
        super.onStart();
        bluetoothStateChangeReceiver = new BluetoothStateChangeReceiver(this);
        getActivity().registerReceiver(bluetoothStateChangeReceiver, new IntentFilter(BluetoothStateChangeReceiver.ACTION));
        setCorrectMenuItemTitle();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(bluetoothStateChangeReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!BluetoothUtils.isBluetoothEnabled()) {
            Utils.showToast(getActivity(), "Please enable bluetooth");
        } else {
            startScan();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        proximityManager.detachListener(this);
        proximityManager.finishScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        proximityManager.disconnect();
    }

    @OnItemClick(R.id.device_list)
    void onListItemClick(final int position) {
        callOnListItemClick(position);
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

    private void startScan() {
        permissionCheckerHoster.requestPermission(new PermissionChecker.Callback() {
            @Override
            public void onPermisionGranted() {
                start();
            }

            @Override
            public void onPermissionRejected() {
                Snackbar.make(getView(), R.string.permission_rejected_message, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void start() {
        proximityManager.initializeScan(getOrCreateScanContext(), new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.attachListener(BaseRangeFragment.this);
            }

            @Override
            public void onConnectionFailure() {
                Utils.showToast(getActivity(), getString(R.string.unexpected_error_connection));
            }
        });
    }

    private ScanContext getOrCreateScanContext() {
        if (scanContext == null) {
            scanContext = new ScanContext.Builder()
                    .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                    .setIBeaconScanContext(getIBeaconScanContext())
                    .setEddystoneScanContext(getEddystoneScanContext())
                    .setActivityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
                    .setForceScanConfiguration(ForceScanConfiguration.MINIMAL)
                    .build();
        }

        return scanContext;
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
        proximityManager.detachListener(this);
        proximityManager.finishScan();
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
        if (getActivity() == null) {
            return;
        }
        final IBeaconDeviceEvent beaconDeviceEvent = (IBeaconDeviceEvent) event;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.replaceWith(beaconDeviceEvent.getDeviceList());
            }
        });
    }

    private void onEddystoneDevicesList(BluetoothDeviceEvent event) {
        if (getActivity() == null) {
            return;
        }
        final EddystoneDeviceEvent eddystoneDeviceEvent = (EddystoneDeviceEvent) event;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.replaceWith(eddystoneDeviceEvent.getDeviceList());
            }
        });
    }
}
