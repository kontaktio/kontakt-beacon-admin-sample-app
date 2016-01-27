package com.kontakt.sample.ui.fragment.monitor;

import android.os.Bundle;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.adapter.monitor.BaseMonitorAdapter;
import com.kontakt.sample.ui.adapter.monitor.EddystoneMonitorAdapter;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;

public class MonitorEddystoneFragment extends BaseMonitorFragment {

    public static final String TAG = MonitorEddystoneFragment.class.getSimpleName();

    public static MonitorEddystoneFragment newInstance() {
        Bundle args = new Bundle();
        MonitorEddystoneFragment fragment = new MonitorEddystoneFragment();
        fragment.setHasOptionsMenu(true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    IBeaconScanContext getIBeaconScanContext() {
        return null;
    }

    @Override
    EddystoneScanContext getEddystoneScanContext() {
        return eddystoneScanContext;
    }

    @Override
    BaseMonitorAdapter getAdapter() {
        return new EddystoneMonitorAdapter(getContext());
    }

    @Override
    public int getTitle() {
        return R.string.monitor_eddystone;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }
}
