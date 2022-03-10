package com.kontakt.sample.samples;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.kontakt.sample.R;
import com.kontakt.sdk.android.cloud.KontaktCloud;
import com.kontakt.sdk.android.cloud.KontaktCloudFactory;
import com.kontakt.sdk.android.cloud.response.CloudCallback;
import com.kontakt.sdk.android.cloud.response.CloudError;
import com.kontakt.sdk.android.cloud.response.CloudHeaders;
import com.kontakt.sdk.android.cloud.response.paginated.Configs;
import com.kontakt.sdk.android.cloud.response.paginated.Devices;
import com.kontakt.sdk.android.cloud.response.paginated.Managers;
import com.kontakt.sdk.android.common.model.Device;
import com.kontakt.sdk.android.common.model.DeviceType;
import com.kontakt.sdk.android.common.model.Manager;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * This is an example of using KontaktCloud to consume Kontakt.io REST API.
 */
public class KontaktCloudActivity extends AppCompatActivity implements View.OnClickListener {

  public static Intent createIntent(@NonNull Context context) {
    return new Intent(context, KontaktCloudActivity.class);
  }

  private final KontaktCloud kontaktCloud = KontaktCloudFactory.create();

  private Button getDevicesButton;
  private Button getConfigsButton;
  private Button getManagersButton;
  private ProgressBar progressBar;
  private TextView statusText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_kontakt_cloud);

    progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    statusText = (TextView) findViewById(R.id.status_text);

    setupToolbar();
    setupButtons();
  }

  private void setupToolbar() {
    ActionBar supportActionBar = getSupportActionBar();
    if (supportActionBar != null) {
      supportActionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  private void setupButtons() {
    getDevicesButton = (Button) findViewById(R.id.get_devices_button);
    getConfigsButton = (Button) findViewById(R.id.get_configs_button);
    getManagersButton = (Button) findViewById(R.id.get_managers_button);

    getDevicesButton.setOnClickListener(this);
    getConfigsButton.setOnClickListener(this);
    getManagersButton.setOnClickListener(this);
  }

  private void fetchDevices() {
    onRequestInProgress();
    //Request list of all devices. Max results are set to 50 by default. If there are more than 50 devices on your account results will be paginated.
    kontaktCloud.devices().fetch().execute(new CloudCallback<Devices>() {
      @Override
      public void onSuccess(Devices response, CloudHeaders headers) {
        enableButtons();
        progressBar.setVisibility(GONE);
        if (response != null && response.getContent() != null) {
          //Do something with your devices list
          statusText.setText("Devices fetched:\n\n");
          for (Device device : response.getContent()) {
            statusText.append(String.format("ID: %s,  Model: %s\n", device.getUniqueId(), device.getModel().toString()));
          }
        }
      }

      @Override
      public void onError(CloudError error) {
        onRequestError(error);
      }
    });
  }

  private void fetchConfigs() {
    onRequestInProgress();
    //Fetch list of all pending configurations.
    kontaktCloud.configs().fetch().type(DeviceType.BEACON).execute(new CloudCallback<Configs>() {
      @Override
      public void onSuccess(Configs response, CloudHeaders headers) {
        enableButtons();
        progressBar.setVisibility(GONE);
        if (response != null && response.getContent() != null) {
          //Do something with your configs list
          statusText.setText(String.format("Configurations fetched! There are %d pending configurations.", response.getContent().size()));
        }
      }

      @Override
      public void onError(CloudError error) {
        onRequestError(error);
      }
    });
  }

  private void fetchManagers() {
    onRequestInProgress();
    //Fetch list of all account's managers.
    kontaktCloud.managers().fetch().execute(new CloudCallback<Managers>() {
      @Override
      public void onSuccess(Managers response, CloudHeaders headers) {
        enableButtons();
        progressBar.setVisibility(GONE);
        if (response != null && response.getContent() != null) {
          statusText.setText("Managers fetched:\n\n");
          for (Manager manager : response.getContent()) {
            statusText.append(
                String.format("Name: %s, Surname: %s, Role: %s\n", manager.getFirstName(), manager.getLastName(), manager.getRole().toString()));
          }
        }
      }

      @Override
      public void onError(CloudError error) {
        onRequestError(error);
      }
    });
  }

  private void onRequestInProgress() {
    disableButtons();
    progressBar.setVisibility(VISIBLE);
    statusText.setText("");
  }

  private void onRequestError(CloudError error) {
    enableButtons();
    progressBar.setVisibility(GONE);
    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
  }

  private void enableButtons() {
    getDevicesButton.setEnabled(true);
    getConfigsButton.setEnabled(true);
    getManagersButton.setEnabled(true);
  }

  private void disableButtons() {
    getDevicesButton.setEnabled(false);
    getConfigsButton.setEnabled(false);
    getManagersButton.setEnabled(false);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.get_devices_button:
        fetchDevices();
        break;
      case R.id.get_configs_button:
        fetchConfigs();
        break;
      case R.id.get_managers_button:
        fetchManagers();
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
}
