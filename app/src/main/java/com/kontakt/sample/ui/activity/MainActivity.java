package com.kontakt.sample.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.kontakt.sample.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        setUpListeners();
    }

    private void setUpListeners() {
        findViewById(R.id.range_beacons).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BeaconRangeActivity.class));
            }
        });

        findViewById(R.id.monitor_beacons).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, BeaconMonitorActivity.class));
            }
        });
    }
}
