package com.kontakt.sample.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;

import com.kontakt.sample.App;
import com.kontakt.sample.R;
import com.kontakt.sample.ui.KenBurnsNetImageView;
import com.kontakt.sample.util.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends DwarfsServiceAwareActivity {

    public static final String TAG = MainActivity.class.getSimpleName();


    @InjectView(R.id.banner)
    KenBurnsNetImageView banner;

    @InjectView(R.id.background_scan)
    Button dwarfScaning;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);

        setUpActionBar(toolbar);
        setUpActionBarTitle(getString(R.string.app_name));
        banner.setImageUrl(
                "http://krasnale.pl/wp-content/uploads/2012/01/Mi%C5%82os%CC%81nik2-Siem.jpg",
                ((App) getApplication()).getImageLoader()
        );
        invalidateIcon(isDwarfServiceRunning());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @OnClick(R.id.range_beacons)
    void startRanging() {
        startActivity(new Intent(MainActivity.this, AchievementsActivity.class));
    }

    @OnClick(R.id.monitor_beacons)
    void startMonitoring() {
        startActivity(new Intent(MainActivity.this, BeaconMonitorActivity.class));
    }

    @OnClick(R.id.background_scan)
    void startDwarfScan() {
        if(!isDwarfServiceRunning()) {
            Log.d(TAG, "startDwarfScan: start dwarf service");
            startDwarfServiceOrDieTrying();
            invalidateIcon(true);
        } else {
            Log.d(TAG, "startDwarfScan: stop dwarf service");
            stopDwarfService();
            invalidateIcon(false);
        }
    }

    void invalidateIcon(boolean isServiceTurnedOn) {
        if (isServiceTurnedOn) {
            dwarfScaning.setText(R.string.foreground_background_scan_off);
            dwarfScaning.setBackgroundResource(R.drawable.button_warning);
        } else {
            dwarfScaning.setText(R.string.foreground_background_scan_on);
            dwarfScaning.setBackgroundResource(R.drawable.button_green);
        }

    }
}
