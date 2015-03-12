package com.kontakt.sample.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.kontakt.sample.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);

        setUpActionBar(toolbar);
        setUpActionBarTitle(getString(R.string.app_name));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.range_beacons)
    void startRanging() {
        startActivity(new Intent(MainActivity.this, BeaconRangeActivity.class));
    }

    @OnClick(R.id.monitor_beacons)
    void startMonitoring() {
        startActivity(new Intent(MainActivity.this, BeaconMonitorActivity.class));
    }

    @OnClick(R.id.background_scan)
    void startForegroundBackgroundScan() {
        startActivity(new Intent(MainActivity.this, BackgroundScanActivity.class));
    }
}
