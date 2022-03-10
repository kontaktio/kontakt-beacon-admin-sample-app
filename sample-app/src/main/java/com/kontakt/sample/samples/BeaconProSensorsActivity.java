package com.kontakt.sample.samples;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.AuthorizationCallback;
import com.kontakt.sdk.android.ble.connection.ErrorCause;
import com.kontakt.sdk.android.ble.connection.KontaktDeviceConnection;
import com.kontakt.sdk.android.ble.connection.KontaktDeviceConnectionFactory;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.connection.ReadListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.SecureProfileListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleSecureProfileListener;
import com.kontakt.sdk.android.ble.security.auth.AuthToken;
import com.kontakt.sdk.android.cloud.KontaktCloud;
import com.kontakt.sdk.android.cloud.KontaktCloudFactory;
import com.kontakt.sdk.android.common.profile.ISecureProfile;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;

import java.util.concurrent.TimeUnit;

/**
 * This is a sample of reading Beacon Pro light sensor and configuration.
 * We scan for a particular Beacon Pro with given UniqueID. Then connect, obtainAuthToken and read the data.
 * <p>
 * Reading light sensors is available for firmwares 1.7 or higher.
 */
public class BeaconProSensorsActivity extends AppCompatActivity implements View.OnClickListener {

  public static Intent createIntent(@NonNull Context context) {
    return new Intent(context, BeaconProSensorsActivity.class);
  }

  public static final String TAG = "ProximityManager";

  private ProximityManager proximityManager;
  private KontaktCloud kontaktCloud;
  private KontaktDeviceConnection deviceConnection;

  private Button readLightButton;
  private ProgressBar progressBar;
  private EditText uniqueIdInput;
  private TextView statusText;

  private String targetUniqueId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_beacon_pro_sensors);

    uniqueIdInput = (EditText) findViewById(R.id.unique_id_edit);
    progressBar = (ProgressBar) findViewById(R.id.scanning_progress);
    statusText = (TextView) findViewById(R.id.status_text);

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
    readLightButton = (Button) findViewById(R.id.read_light_button);
    readLightButton.setOnClickListener(this);
  }

  private void setupProximityManager() {
    proximityManager = ProximityManagerFactory.create(this);
    kontaktCloud = KontaktCloudFactory.create();

    //Configure proximity manager basic options
    proximityManager.configuration()
        //Using ranging for continuous scanning or MONITORING for scanning with intervals
        .scanPeriod(ScanPeriod.RANGING)
        //Using BALANCED for best performance/battery ratio
        .scanMode(ScanMode.BALANCED)
        //OnDeviceUpdate callback will be received with minimum 5 seconds interval
        .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(5));

    //Setting up Secure Profile listener
    proximityManager.setSecureProfileListener(createSecureProfileListener());
  }

  private void startScanning(String uniqueId) {
    if (TextUtils.isEmpty(uniqueId.trim())) {
      Toast.makeText(this, "Please enter Unique ID", Toast.LENGTH_SHORT).show();
      return;
    }

    closeConnection();

    targetUniqueId = uniqueId;
    //Connect to scanning service and start scanning when ready
    proximityManager.connect(new OnServiceReadyListener() {
      @Override
      public void onServiceReady() {
        //Check if proximity manager is already scanning
        if (proximityManager.isScanning()) {
          Toast.makeText(BeaconProSensorsActivity.this, "Already scanning", Toast.LENGTH_SHORT).show();
          return;
        }
        proximityManager.startScanning();
        progressBar.setVisibility(View.VISIBLE);
        readLightButton.setEnabled(false);
        Toast.makeText(BeaconProSensorsActivity.this, "Scanning started", Toast.LENGTH_SHORT).show();
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
    return new SimpleSecureProfileListener() {
      @Override
      public void onProfileDiscovered(ISecureProfile profile) {
        if (targetUniqueId.equals(profile.getUniqueId())) {
          Log.i(TAG, "Beacon discovered. Unique ID: " + profile.getUniqueId());
          proximityManager.disconnect();
          onBeaconProDiscovered(profile);
        }
      }
    };
  }

  private void onBeaconProDiscovered(ISecureProfile profile) {
    deviceConnection = KontaktDeviceConnectionFactory.create(getApplicationContext(), profile, createConnectionListener());
    deviceConnection.connect();
    progressBar.setVisibility(View.VISIBLE);
    Log.i(TAG, "Connecting to Beacon Pro with ID: " + profile.getUniqueId());
  }

  private KontaktDeviceConnection.ConnectionListener createConnectionListener() {
    return new KontaktDeviceConnection.ConnectionListener() {
      @Override
      public void onConnectionOpened() {

      }

      @Override
      public void onConnected() {
        Log.i(TAG, "Connection successful.");
        obtainAuthToken();
      }

      @Override
      public void onErrorOccured(int errorCode) {
        BeaconProSensorsActivity.this.onErrorOccured("Connection error: " + errorCode);
      }

      @Override
      public void onDisconnected() {
        BeaconProSensorsActivity.this.onErrorOccured("Device disconnected");
      }
    };
  }

  private void obtainAuthToken() {
    Log.i(TAG, "Obtaining AuthToken...");
    AuthToken.obtain(targetUniqueId, kontaktCloud, new AuthToken.AuthTokenCallback() {
      @Override
      public void onSuccess(AuthToken token) {
        Log.i(TAG, "AuthToken obtained.");
        authorize(token);
      }

      @Override
      public void onError(String error) {
        onErrorOccured("AuthToken error: " + error);
      }
    });
  }

  private void authorize(final AuthToken token) {
    Log.i(TAG, "Authorizing...");
    deviceConnection.authorize(token, new AuthorizationCallback() {
      @Override
      public void onSuccess() {
        Log.i(TAG, "Beacon Pro authorized.");
        readLightSensor();
      }

      @Override
      public void onFailure(ErrorCause cause) {
        onErrorOccured("Authorization error: " + cause.name());
      }
    });
  }

  private void readLightSensor() {
    Log.i(TAG, "Reading light sensor...");
    deviceConnection.readLightSensor(new ReadListener<Integer>() {
      @Override
      public void onReadSuccess(Integer value) {
        setLightSensorValue(value);
        Log.i(TAG, "Light Sensor value: " + value);
      }

      @Override
      public void onReadFailure(ErrorCause cause) {
        onErrorOccured("Read light sensor error: " + cause.name());
      }
    });
  }

  private void setLightSensorValue(final Integer value) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        statusText.setText("Light Sensor value: " + value);
      }
    });
  }

  private void closeConnection() {
    if (deviceConnection != null) {
      deviceConnection.close();
    }
  }

  private void onErrorOccured(final String error) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        closeConnection();
        readLightButton.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        statusText.setText("");
        Toast.makeText(BeaconProSensorsActivity.this, error, Toast.LENGTH_SHORT).show();
        Log.e(TAG, error);
      }
    });
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.read_light_button:
        startScanning(uniqueIdInput.getText().toString());
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
    //Remember to stop scanning/disconnect when leaving screen.
    stopScanning();
    closeConnection();
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    //Remember to disconnect when finished.
    proximityManager.disconnect();
    closeConnection();
    super.onDestroy();
  }

}
