package com.kontakt.sample.samples;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.kontakt.sample.R;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.ErrorCause;
import com.kontakt.sdk.android.ble.connection.KontaktDeviceConnection;
import com.kontakt.sdk.android.ble.connection.KontaktDeviceConnectionFactory;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.connection.WriteListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.SecureProfileListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleSecureProfileListener;
import com.kontakt.sdk.android.cloud.KontaktCloud;
import com.kontakt.sdk.android.cloud.KontaktCloudFactory;
import com.kontakt.sdk.android.cloud.response.CloudCallback;
import com.kontakt.sdk.android.cloud.response.CloudError;
import com.kontakt.sdk.android.cloud.response.CloudHeaders;
import com.kontakt.sdk.android.cloud.response.paginated.Configs;
import com.kontakt.sdk.android.common.model.Config;
import com.kontakt.sdk.android.common.model.DeviceType;
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

  private final KontaktCloud kontaktCloud = KontaktCloudFactory.create();
  private ProximityManager proximityManager;
  private KontaktDeviceConnection deviceConnection;

  private TextView statusText;
  private EditText uniqueIdInput;
  private EditText majorInput;
  private EditText minorInput;
  private Button startButton;
  private String targetUniqueId;
  private Config targetConfiguration;
  private RemoteBluetoothDevice targetDevice;

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
    if (proximityManager != null) {
      proximityManager.disconnect();
    }
    if (deviceConnection != null) {
      deviceConnection.close();
    }
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
    proximityManager = ProximityManagerFactory.create(this);

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
    proximityManager.setSecureProfileListener(createSecureProfileListener());
  }

  private void startConfiguration() {
    //First validate user's input.
    if (!areInputsValid()) {
      showError("At least one of inserted values is invalid.");
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
      targetDevice = device;
      prepareConfiguration();

      String status = "Device discovered! Unique ID: " + device.getUniqueId();
      setStatus(status);
      Log.i(TAG, status);
    }
  }

  private void prepareConfiguration() {
    //Prepare configuration
    setStatus("Preparing configuration...");
    Config config = new Config.Builder().major(Integer.parseInt(majorInput.getText().toString()))
        .minor(Integer.parseInt(minorInput.getText().toString()))
        .build();

    //Use KontaktCloud to create config and request encrypted version that will be send to the device.
    kontaktCloud.configs().create(config).forDevices(targetDevice.getUniqueId()).withType(DeviceType.BEACON).execute(new CloudCallback<Config[]>() {
      @Override
      public void onSuccess(Config[] response, CloudHeaders headers) {
        //Config has been successfully created. Now download encrypted version.
        kontaktCloud.configs().secure().withIds(targetDevice.getUniqueId()).execute(new CloudCallback<Configs>() {
          @Override
          public void onSuccess(Configs response, CloudHeaders headers) {
            setStatus("Fetching encrypted configuration...");
            targetConfiguration = response.getContent().get(0);
            onConfigurationReady();
          }

          @Override
          public void onError(CloudError error) {
            showError("Error: " + error.getMessage());
          }
        });
      }

      @Override
      public void onError(CloudError error) {
        showError("Error: " + error.getMessage());
      }
    });
  }

  private void onConfigurationReady() {
    //Initialize connection to the device
    deviceConnection = KontaktDeviceConnectionFactory.create(this, targetDevice, createConnectionListener());
    deviceConnection.connect();
    setStatus("Connecting to device...");
  }

  private void onDeviceConnected() {
    //Device connected. Start configuration...
    setStatus("Applying configuration...");
    deviceConnection.applySecureConfig(targetConfiguration.getSecureRequest(), new WriteListener() {
      @Override
      public void onWriteSuccess(WriteResponse response) {
        //Configuration has been applied. Now we need to send beacon's response back to the cloud to stay synchronized.
        setStatus("Configuration applied in the device.");
        onConfigurationApplied(response);
        deviceConnection.close();
      }

      @Override
      public void onWriteFailure(ErrorCause cause) {
        showError("Configuration error. Cause: " + cause);
        deviceConnection.close();
      }
    });
  }

  private void onConfigurationApplied(WriteListener.WriteResponse response) {
    //Configuration has been applied on the beacon. Now we should inform Cloud about it.
    setStatus("Synchronizing with Cloud...");
    targetConfiguration.applySecureResponse(response.getExtra(), response.getUnixTimestamp());
    kontaktCloud.devices().applySecureConfigs(targetConfiguration).execute(new CloudCallback<Void>() {
      @Override
      public void onSuccess(Void response, CloudHeaders headers) {
        //Success!
        setStatus("Configuration completed!");
        startButton.setEnabled(true);
      }

      @Override
      public void onError(CloudError error) {
        showError("Error: " + error.getMessage());
      }
    });
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

  private void showError(final String errorMessage) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(BeaconConfigurationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
        setStatus(errorMessage);
        startButton.setEnabled(true);
      }
    });
  }

  private void setStatus(final String text) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        statusText.setText(text);
      }
    });
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

  private KontaktDeviceConnection.ConnectionListener createConnectionListener() {
    return new KontaktDeviceConnection.ConnectionListener() {
      @Override
      public void onConnectionOpened() {

      }

      @Override
      public void onConnected() {
        onDeviceConnected();
      }

      @Override
      public void onErrorOccured(int errorCode) {
        showError("Connection error. Code: " + errorCode);
      }

      @Override
      public void onDisconnected() {
        showError("Device disconnected.");
      }
    };
  }
}
