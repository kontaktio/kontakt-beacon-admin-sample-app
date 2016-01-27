package com.kontakt.sample.ui.fragment.monitor;

import android.os.Bundle;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.adapter.monitor.BaseMonitorAdapter;
import com.kontakt.sample.ui.adapter.monitor.IBeaconMonitorAdapter;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;

public class MonitorIBeaconsFragment extends BaseMonitorFragment {

    public static final String TAG = MonitorIBeaconsFragment.class.getSimpleName();

    public static MonitorIBeaconsFragment newInstance() {
        Bundle args = new Bundle();
        MonitorIBeaconsFragment fragment = new MonitorIBeaconsFragment();
        fragment.setHasOptionsMenu(true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    IBeaconScanContext getIBeaconScanContext() {
        return iBeaconScanContext;
    }

    @Override
    EddystoneScanContext getEddystoneScanContext() {
        return null;
    }

    @Override
    BaseMonitorAdapter getAdapter() {
        return new IBeaconMonitorAdapter(getContext());
    }

    @Override
    public int getTitle() {
        return R.string.monitor_beacons;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

}
