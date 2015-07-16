package com.kontakt.sample.ui.activity.monitor;

import android.os.Bundle;

import com.kontakt.sample.R;
import com.kontakt.sample.adapter.monitor.BaseMonitorAdapter;
import com.kontakt.sample.adapter.monitor.EddystoneMonitorAdapter;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;

public class EddystoneMonitorActivity extends BaseBeaconMonitorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpActionBarTitle(getString(R.string.monitor_eddystone));
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
        return new EddystoneMonitorAdapter(this);
    }
}
