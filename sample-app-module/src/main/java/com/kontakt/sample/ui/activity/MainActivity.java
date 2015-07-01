package com.kontakt.sample.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.kontakt.sample.R;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 121;

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH && resultCode == RESULT_OK) {
            startActivity(new Intent(MainActivity.this, BackgroundScanActivity.class));
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
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

    @OnClick(R.id.multiple_proximity_manager)
    void startSimultaneousScans() {
        startActivity(new Intent(MainActivity.this, SimultaneousScanActivity.class));
    }

    @OnClick(R.id.background_scan)
    void startForegroundBackgroundScan() {

        if (!BluetoothUtils.isBluetoothEnabled()) {
            startActivity(new Intent(MainActivity.this, BackgroundScanActivity.class));
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        }
    }
}
