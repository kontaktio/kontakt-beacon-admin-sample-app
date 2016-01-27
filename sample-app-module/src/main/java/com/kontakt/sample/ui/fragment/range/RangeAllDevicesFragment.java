package com.kontakt.sample.ui.fragment.range;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.kontakt.sample.R;
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

public class RangeAllDevicesFragment extends BaseRangeAllFragment implements ProximityManager.ProximityListener{

    public static final String TAG = RangeAllDevicesFragment.class.getSimpleName();

    public static RangeAllDevicesFragment newInstance() {
        Bundle args = new Bundle();
        RangeAllDevicesFragment fragment = new RangeAllDevicesFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    ProximityManagerContract getProximityManager() {
        return new ProximityManager(getActivity());
    }

    @Override
    public int getTitle() {
        return R.string.range_all_beacons;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }
}
