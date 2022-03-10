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
import com.kontakt.sdk.android.ble.device.BeaconRegion;
import com.kontakt.sdk.android.ble.device.EddystoneNamespace;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.SpaceListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * This is an example of using Proximity Manager's regions/namespaces (spaces) options.
 */
public class ScanRegionsActivity extends AppCompatActivity implements View.OnClickListener {

  public static Intent createIntent(@NonNull Context context) {
    return new Intent(context, ScanRegionsActivity.class);
  }

  public static final String TAG = "ProximityManager";

  private ProximityManager proximityManager;
  private ProgressBar progressBar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_regions);
    progressBar = (ProgressBar) findViewById(R.id.scanning_progress);

    //Setup Toolbar
    setupToolbar();

    //Setup buttons
    setupButtons();

    //Initialize and configure proximity manager
    setupProximityManager();

    //Setup iBeacon and Eddystone regions/namespaces (spaces)
    setupSpaces();
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

    //Setting up iBeacon and Eddystone spaces listeners
    proximityManager.setSpaceListener(createSpaceListener());

    //Setting up empty no-operation iBeacon and Eddystone listeners as in this example we are not interested in particular devices events.
    //Those listeners can't be null though, because ProximityManager must be aware that it needs to look for both iBeacons and Eddystones
    proximityManager.setIBeaconListener(new SimpleIBeaconListener() {
    });
    proximityManager.setEddystoneListener(new SimpleEddystoneListener() {
    });
  }

  private void setupSpaces() {
    //Setting up single iBeacon region. Put your own desired values here.
    IBeaconRegion region = new BeaconRegion.Builder().identifier("My Region") //Region identifier is mandatory.
        .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) //Default Kontakt.io proximity.
        //Optional major and minor values
        //.major(1)
        //.minor(1)
        .build();

    proximityManager.spaces().iBeaconRegion(region)
    .forceResolveRegions(Collections.singleton(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")));

    //Setting up single Eddystone namespace. Put your own desired values here.
    IEddystoneNamespace namespace = new EddystoneNamespace.Builder().identifier("My Namespace") //Namespace identifier is mandatory.
        .namespace("f7826da64fa24e988024") //Default Kontakt.io namespace.
        //Optional instance id value
        //.instanceId("instanceId")
        .build();
    proximityManager.spaces().eddystoneNamespace(namespace).forceResolveNamespaces(Collections.singletonList("f7826da64fa24e988024"));
  }

  private void startScanning() {
    //Connect to scanning service and start scanning when ready
    proximityManager.connect(new OnServiceReadyListener() {
      @Override
      public void onServiceReady() {
        //Check if proximity manager is already scanning
        if (proximityManager.isScanning()) {
          Toast.makeText(ScanRegionsActivity.this, "Already scanning", Toast.LENGTH_SHORT).show();
          return;
        }
        proximityManager.startScanning();
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(ScanRegionsActivity.this, "Scanning started", Toast.LENGTH_SHORT).show();
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

  private SpaceListener createSpaceListener() {
    return new SpaceListener() {
      @Override
      public void onRegionEntered(IBeaconRegion region) {
        Log.i(TAG, "New Region entered: " + region.getIdentifier());
      }

      @Override
      public void onRegionAbandoned(IBeaconRegion region) {
        Log.e(TAG, "Region abandoned " + region.getIdentifier());
      }

      @Override
      public void onNamespaceEntered(IEddystoneNamespace namespace) {
        Log.i(TAG, "New Namespace entered: " + namespace.getIdentifier());
      }

      @Override
      public void onNamespaceAbandoned(IEddystoneNamespace namespace) {
        Log.i(TAG, "Namespace abandoned: " + namespace.getIdentifier());
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
