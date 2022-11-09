package com.kontakt.sample.samples;

import android.annotation.SuppressLint;
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

import com.kontakt.sample.App;
import com.kontakt.sample.R;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.SecureProfileListener;
import com.kontakt.sdk.android.cloud.KontaktCloudFactory;
import com.kontakt.sdk.android.common.profile.ISecureProfile;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This is a sample of scanning for specific frame called 'Secure Profile frame'.
 * This frame can be used to connect with a beacon or acquire values like Unique ID that are not available in iBeacon/Eddystone frames.
 */
public class SecureProfileScanActivity extends AppCompatActivity implements View.OnClickListener {

  public static Intent createIntent(@NonNull Context context) {
    return new Intent(context, SecureProfileScanActivity.class);
  }

  public static final String TAG = "ProximityManager";

  private ProximityManager proximityManager;
  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_secure_profile_scan);
    progressBar = findViewById(R.id.scanning_progress);

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
    Button startScanButton = findViewById(R.id.start_scan_button);
    Button stopScanButton = findViewById(R.id.stop_scan_button);
    startScanButton.setOnClickListener(this);
    stopScanButton.setOnClickListener(this);
  }

  private void setupProximityManager() {
    proximityManager = ProximityManagerFactory.create(this, KontaktCloudFactory.create(App.API_KEY));

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
    proximityManager.connect(() -> {
      //Check if proximity manager is already scanning
      if (proximityManager.isScanning()) {
        Toast.makeText(SecureProfileScanActivity.this, "Already scanning", Toast.LENGTH_SHORT).show();
        return;
      }
      proximityManager.startScanning();
      progressBar.setVisibility(View.VISIBLE);
      Toast.makeText(SecureProfileScanActivity.this, "Scanning started", Toast.LENGTH_SHORT).show();
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
        for (ISecureProfile profile : list) {
          Log.i(TAG, "onProfileUpdated: " + profile.toString());
        }
      }

      @Override
      public void onProfileLost(ISecureProfile iSecureProfile) {
        Log.e(TAG, "onProfileLost: " + iSecureProfile.toString());
      }
    };
  }

  @SuppressLint("NonConstantResourceId")
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
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
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
