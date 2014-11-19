package com.kontakt.sample.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.http.ApiClient;
import com.kontakt.sdk.android.model.Config;
import com.kontakt.sdk.core.exception.ClientException;
import com.kontakt.sdk.core.http.Result;

import java.util.UUID;

public class ConfigFormActivity extends Activity {

    public static final String EXTRA_RESULT_CONFIG = "extra_result_config";

    private Button generateProximityUUIDButton;
    private Button submitButton;
    private EditText proximityUUIDText;
    private EditText majorText;
    private EditText minorText;
    private EditText powerLevelText;
    private EditText advertisingIntervalText;
    private EditText beaconUniqueIdText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_form_activity);
        setUpViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.setOrientationChangeEnabled(false, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.setOrientationChangeEnabled(true, this);
    }

    private void setUpViews() {
        submitButton = (Button) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Config config = createConfig();
                new SyncConfigTask(ConfigFormActivity.this).execute(config);
            }
        });

        proximityUUIDText = (EditText) findViewById(R.id.proximity_uuid_text);

        generateProximityUUIDButton = (Button) findViewById(R.id.generate_button);
        generateProximityUUIDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proximityUUIDText.setText(UUID.randomUUID().toString());
            }
        });

        majorText = (EditText) findViewById(R.id.major_text);
        minorText = (EditText) findViewById(R.id.minor_text);
        powerLevelText = (EditText) findViewById(R.id.power_level_text);
        advertisingIntervalText = (EditText) findViewById(R.id.advertising_interval_text);
        beaconUniqueIdText = (EditText) findViewById(R.id.beacon_unique_id_text);
    }

    private Config createConfig() {
        return Config.builder()
                     .setProximityUUID(UUID.fromString(proximityUUIDText.getText().toString().trim()))
                     .setMajor(Integer.parseInt(majorText.getText().toString()))
                     .setMinor(Integer.parseInt(minorText.getText().toString()))
                     .setTxPower(Integer.parseInt(powerLevelText.getText().toString()))
                     .setBeaconUniqueId(beaconUniqueIdText.getText().toString().trim())
                     .setInterval(Integer.parseInt(advertisingIntervalText.getText().toString()))
                     .build();
    }

    private static class SyncConfigTask extends AsyncTask<Config, Void, Result<Config>> {

        private ProgressDialog progressDialog;
        private Activity activity;

        public SyncConfigTask(final Activity context) {
            super();
            this.activity = context;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(activity, "Sync", "Your config synchronized");
        }

        @Override
        protected Result<Config> doInBackground(Config... params) {
            ApiClient.init(activity);
            final ApiClient apiClient = ApiClient.newInstance();
            try {
                return apiClient.createConfig(params[0]);

            } catch(IllegalArgumentException e) {
                return Result.of(null, -1, e.getMessage());
            } catch(ClientException e) {
                e.printStackTrace();
                return Result.of(null, 404, e.getMessage());
            } finally {
               apiClient.close();
            }
        }

        @Override
        protected void onPostExecute(Result<Config> configResult) {
            progressDialog.dismiss();
            if(configResult.isPresent()) {
                final Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_RESULT_CONFIG, configResult.get());
                activity.setResult(Activity.RESULT_OK, resultIntent);
                activity.finish();
            } else {
                Toast.makeText(activity, configResult.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
