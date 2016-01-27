package com.kontakt.sample.ui.fragment.range;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.kontakt.sample.R;
import com.kontakt.sample.permission.PermissionChecker;
import com.kontakt.sample.ui.adapter.monitor.AllBeaconsMonitorAdapter;
import com.kontakt.sample.ui.fragment.BaseFragment;
import com.kontakt.sample.util.Utils;
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
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.profile.DeviceProfile;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public abstract class BaseRangeAllFragment extends BaseFragment implements ProximityManager.ProximityListener {

    @InjectView(R.id.list)
    protected ExpandableListView list;

    protected AllBeaconsMonitorAdapter allBeaconsRangeAdapter;
    protected ProximityManagerContract proximityManager;

    protected ScanContext scanContext;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.beacon_monitor_list_activity, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ButterKnife.inject(this, getView());
        allBeaconsRangeAdapter = new AllBeaconsMonitorAdapter(getActivity());
        proximityManager = getProximityManager();
        list.setAdapter(allBeaconsRangeAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        proximityManager.disconnect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!BluetoothUtils.isBluetoothEnabled()) {
            Utils.showToast(getContext(), "Please enable bluetooth");
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


    abstract ProximityManagerContract getProximityManager();

    ScanContext getOrCreateScanContext() {
        if (scanContext == null) {
            scanContext = new ScanContext.Builder()
                    .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                    .setIBeaconScanContext(beaconScanContext)
                    .setEddystoneScanContext(eddystoneScanContext)
                    .setActivityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
                    .setForceScanConfiguration(ForceScanConfiguration.MINIMAL)
                    .build();
        }

        return scanContext;
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
                proximityManager.attachListener(BaseRangeAllFragment.this);
            }

            @Override
            public void onConnectionFailure() {
                Utils.showToast(getContext(), getString(R.string.unexpected_error_connection));
            }
        });
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
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                allBeaconsRangeAdapter.replaceEddystoneBeacons(event.getDeviceList());
            }
        });
    }

    private void onIBeaconDevicesList(final IBeaconDeviceEvent event) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                allBeaconsRangeAdapter.replaceIBeacons(event.getDeviceList());
            }
        });
    }
}
