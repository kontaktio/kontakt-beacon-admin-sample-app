package com.kontakt.sample.samples;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.kontakt.sample.R;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.SecureProfileListener;
import com.kontakt.sdk.android.common.profile.ISecureProfile;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is a sample of scanning for Beacon Pro specific frame called 'Secure Profile frame'.
 * This frame can be used to connect with Beacon Pro or acquire values like Unique ID that are not available in Beacon's Pro iBeacon/Eddystone frames.
 */
public class BeaconProScanActivity extends AppCompatActivity implements View.OnClickListener {

  public static Intent createIntent(@NonNull Context context) {
    return new Intent(context, BeaconProScanActivity.class);
  }

  public static final String TAG = "ProximityManager";

  private ProximityManager proximityManager;
  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_beacon_pro_scan);
    progressBar = (ProgressBar) findViewById(R.id.scanning_progress);

    //Setup Toolbar
    setupToolbar();

    //Setup buttons
    setupButtons();

    //Initialize and configure proximity manager
    setupProximityManager();
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

  private void setupProximityManager() {
    proximityManager = ProximityManagerFactory.create(this);

    //Configure proximity manager basic options
    proximityManager.configuration()
        //Using ranging for continuous scanning or MONITORING for scanning with intervals
        .scanPeriod(ScanPeriod.RANGING)
        //Using BALANCED for best performance/battery ratio
        .scanMode(ScanMode.BALANCED)
        //OnDeviceUpdate callback will be received with 5 seconds interval
        .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(5));

    //Setting up Secure Profile listener
    proximityManager.setSecureProfileListener(createSecureProfileListener());
  }

  private void startScanning() {
    //Connect to scanning service and start scanning when ready
    proximityManager.connect(new OnServiceReadyListener() {
      @Override
      public void onServiceReady() {
        //Check if proximity manager is already scanning
        if (proximityManager.isScanning()) {
          Toast.makeText(BeaconProScanActivity.this, "Already scanning", Toast.LENGTH_SHORT).show();
          return;
        }
        proximityManager.startScanning();
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(BeaconProScanActivity.this, "Scanning started", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void stopScanning() {
    //Stop scanning if scanning is in progress
    if (proximityManager.isScanning()) {
      proximityManager.stopScanning();
      progressBar.setVisibility(View.GONE);
      Toast.makeText(this, "Scanning stopped", Toast.LENGTH_SHORT).show();
    }
  }

  private SecureProfileListener createSecureProfileListener() {
    return new SecureProfileListener() {
      @Override
      public void onProfileDiscovered(ISecureProfile iSecureProfile) {
        Log.i(TAG, "onProfileDiscovered: " + iSecureProfile.toString());
      }

      @Override
      public void onProfilesUpdated(List<ISecureProfile> list) {
        Log.i(TAG, "onProfilesUpdated: " + list.size());
      }

      @Override
      public void onProfileLost(ISecureProfile iSecureProfile) {
        Log.e(TAG, "onProfileLost: " + iSecureProfile.toString());
      }
    };
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start_scan_button:
        startScanning();
        break;
      case R.id.stop_scan_button:
        stopScanning();
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

  @Override
  protected void onStop() {
    //Stop scanning when leaving screen.
    stopScanning();
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    //Remember to disconnect when finished.
    proximityManager.disconnect();
    super.onDestroy();
  }
}
