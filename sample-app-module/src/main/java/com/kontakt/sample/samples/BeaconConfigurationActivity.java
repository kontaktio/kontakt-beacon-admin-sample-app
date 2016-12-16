package com.kontakt.sample.samples;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.kontakt.sample.R;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.ScanMode;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.SecureProfileListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleSecureProfileListener;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.ISecureProfile;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;
import com.kontakt.sdk.android.common.util.SecureProfileUtils;
import java.util.concurrent.TimeUnit;

/**
 * This is an example of changing beacon's major and minor
 */
public class BeaconConfigurationActivity extends AppCompatActivity implements View.OnClickListener {

  public static Intent createIntent(@NonNull Context context) {
    return new Intent(context, BeaconConfigurationActivity.class);
  }

  public static final String TAG = "ProximityManager";
  public static final int MIN_MINOR_MAJOR = 1;
  public static final int MAX_MINOR_MAJOR = 65535;

  private ProximityManagerContract proximityManager;

  private TextView statusText;
  private EditText uniqueIdInput;
  private EditText majorInput;
  private EditText minorInput;
  private Button startButton;
  private String targetUniqueId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_beacon_config);

    statusText = (TextView) findViewById(R.id.status_text);
    uniqueIdInput = (EditText) findViewById(R.id.unique_id_edit);
    majorInput = (EditText) findViewById(R.id.major_edit);
    minorInput = (EditText) findViewById(R.id.minor_edit);

    setupToolbar();
    setupButtons();
    setupProximityManager();
  }

  @Override
  protected void onStop() {
    proximityManager.disconnect();
    super.onStop();
  }

  private void setupToolbar() {
    ActionBar supportActionBar = getSupportActionBar();
    if (supportActionBar != null) {
      supportActionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  private void setupButtons() {
    startButton = (Button) findViewById(R.id.start_button);
    startButton.setOnClickListener(this);
  }

  private void setupProximityManager() {
    proximityManager = new ProximityManager(this);

    //Configure proximity manager basic options
    proximityManager.configuration()
        //Using ranging for continuous scanning or MONITORING for scanning with intervals
        .scanPeriod(ScanPeriod.RANGING)
        //Using BALANCED for best performance/battery ratio
        .scanMode(ScanMode.BALANCED)
        //OnDeviceUpdate callback will be received with 5 seconds interval
        .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(5));

    //Setting up iBeacon and Secure Profile listeners
    proximityManager.setIBeaconListener(createIBeaconListener());
    proximityManager.setKontaktSecureProfileListener(createSecureProfileListener());
  }

  private void startConfiguration() {
    //First validate user's input.
    if (!areInputsValid()) {
      setStatus("Invalid input.");
      Toast.makeText(this, "At least one of inserted values is invalid.", Toast.LENGTH_SHORT).show();
      return;
    }
    //If everything is OK start the scanning.
    scanForDevice(uniqueIdInput.getText().toString().trim());
  }

  private boolean areInputsValid() {
    String uniqueId = uniqueIdInput.getText().toString();
    String majorText = majorInput.getText().toString();
    String minorText = minorInput.getText().toString();
    if (TextUtils.isEmpty(uniqueId) || TextUtils.isEmpty(majorText) || TextUtils.isEmpty(minorText)) {
      return false;
    }

    int major = Integer.parseInt(majorText);
    int minor = Integer.parseInt(minorText);
    if (major < MIN_MINOR_MAJOR || major > MAX_MINOR_MAJOR || minor < MIN_MINOR_MAJOR || minor > MAX_MINOR_MAJOR) {
      return false;
    }

    return true;
  }

  private void scanForDevice(String uniqueId) {
    targetUniqueId = uniqueId;
    proximityManager.connect(new OnServiceReadyListener() {
      @Override
      public void onServiceReady() {
        proximityManager.startScanning();
        setStatus("Looking for device...");
      }
    });
    startButton.setEnabled(false);
  }

  private void onDeviceDiscovered(RemoteBluetoothDevice device) {
    //Check if this is our target beacon.
    if (targetUniqueId.equalsIgnoreCase(device.getUniqueId())) {
      proximityManager.disconnect();
      String status = "Device discovered! Unique ID: " + device.getUniqueId();
      setStatus(status);
      Log.i(TAG, status);
    }
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.start_button:
        startConfiguration();
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

  private void setStatus(String text) {
    statusText.setText(text);
  }

  private IBeaconListener createIBeaconListener() {
    return new SimpleIBeaconListener() {
      @Override
      public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
        onDeviceDiscovered(ibeacon);
      }
    };
  }

  private SecureProfileListener createSecureProfileListener() {
    return new SimpleSecureProfileListener() {
      @Override
      public void onProfileDiscovered(ISecureProfile profile) {
        onDeviceDiscovered(SecureProfileUtils.asRemoteBluetoothDevice(profile));
      }
    };
  }
}
