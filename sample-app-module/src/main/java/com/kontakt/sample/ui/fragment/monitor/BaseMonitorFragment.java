package com.kontakt.sample.ui.fragment.monitor;

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
import android.widget.ExpandableListView;

import com.kontakt.sample.R;
import com.kontakt.sample.permission.PermissionChecker;
import com.kontakt.sample.ui.adapter.monitor.BaseMonitorAdapter;
import com.kontakt.sample.ui.fragment.BaseFragment;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.ble.broadcast.BluetoothStateChangeReceiver;
import com.kontakt.sdk.android.ble.broadcast.OnBluetoothStateChangeListener;
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

public abstract class BaseMonitorFragment extends BaseFragment implements ProximityManager.ProximityListener, OnBluetoothStateChangeListener {

    abstract IBeaconScanContext getIBeaconScanContext();

    abstract EddystoneScanContext getEddystoneScanContext();

    abstract BaseMonitorAdapter getAdapter();

    @InjectView(R.id.list)
    ExpandableListView deviceList;

    private BluetoothStateChangeReceiver bluetoothStateChangeReceiver;
    private MenuItem bluetoothMenuItem;
    private BaseMonitorAdapter adapter;
    private ProximityManager proximityManager;
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.beacon_monitor_list_activity, container, false);
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
                proximityManager.attachListener(BaseMonitorFragment.this);
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
                    .setScanPeriod(new ScanPeriod(TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(5)))
                    .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                    .setActivityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
                    .setIBeaconScanContext(getIBeaconScanContext())
                    .setEddystoneScanContext(getEddystoneScanContext())
                    .build();
        }

        return scanContext;
    }

    @Override
    public void onScanStart() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setProgressBarIndeterminateVisibility(true);
            }
        });
    }

    @Override
    public void onScanStop() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setProgressBarIndeterminateVisibility(false);
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

    private void onIBeaconEvent(final BluetoothDeviceEvent event) {
        IBeaconDeviceEvent iBeaconDeviceEvent = (IBeaconDeviceEvent) event;
        final IBeaconRegion region = iBeaconDeviceEvent.getRegion();
        final List<IBeaconDevice> deviceList = iBeaconDeviceEvent.getDeviceList();
        final int index = adapter.getGroupIndex(region);

        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
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

        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
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
}
