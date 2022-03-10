package com.kontakt.sample.samples;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.kontakt.sample.R;
import com.kontakt.sample.service.BackgroundScanService;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;

/**
 * This is an example of implementing a background scan using Android's Service component.
 */
public class BackgroundScanActivity extends AppCompatActivity implements View.OnClickListener {

  public static Intent createIntent(@NonNull Context context) {
    return new Intent(context, BackgroundScanActivity.class);
  }

  private Intent serviceIntent;
  private TextView statusText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_background_scan);
    statusText = (TextView) findViewById(R.id.status_text);
    serviceIntent = new Intent(getApplicationContext(), BackgroundScanService.class);

    //Setup Toolbar
    setupToolbar();

    //Setup buttons
    setupButtons();
  }

  @Override
  protected void onResume() {
    super.onResume();
    //Register Broadcast receiver that will accept results from background scanning
    IntentFilter intentFilter = new IntentFilter(BackgroundScanService.ACTION_DEVICE_DISCOVERED);
    registerReceiver(scanningBroadcastReceiver, intentFilter);
  }

  @Override
  protected void onPause() {
    unregisterReceiver(scanningBroadcastReceiver);
    super.onPause();
  }

  private void setupToolbar() {
    ActionBar supportActionBar = getSupportActionBar();
    if (supportActionBar != null) {
      supportActionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  private void setupButtons() {
    Button startScanButton = (Button) findViewById(R.id.start_scan_button);
    Button stopScanButton = (Button) findViewById(R.id.stop_scan_button);
    startScanButton.setOnClickListener(this);
    stopScanButton.setOnClickListener(this);
  }

  private void startBackgroundService() {
    startService(serviceIntent);
  }

  private void stopBackgroundService() {
    stopService(serviceIntent);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start_scan_button:
        startBackgroundService();
        break;
      case R.id.stop_scan_button:
        stopBackgroundService();
        break;
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private final BroadcastReceiver scanningBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      //Device discovered!
      int devicesCount = intent.getIntExtra(BackgroundScanService.EXTRA_DEVICES_COUNT, 0);
      RemoteBluetoothDevice device = intent.getParcelableExtra(BackgroundScanService.EXTRA_DEVICE);
      statusText.setText(String.format("Total discovered devices: %d\n\nLast scanned device:\n%s", devicesCount, device.toString()));
    }
  };
}
