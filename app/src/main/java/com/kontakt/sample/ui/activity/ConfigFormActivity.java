package com.kontakt.sample.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kontakt.sample.R;
import com.kontakt.sdk.android.model.Config;

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
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setUpViews() {
        submitButton = (Button) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config config = createConfig();
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_RESULT_CONFIG, config);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
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
        return new Config.Builder()
                     .setProximityUUID(UUID.fromString(proximityUUIDText.getText().toString().trim()))
                     .setMajor(Integer.parseInt(majorText.getText().toString()))
                     .setMinor(Integer.parseInt(minorText.getText().toString()))
                     .setTxPower(Integer.parseInt(powerLevelText.getText().toString()))
                     .setDeviceUniqueId(beaconUniqueIdText.getText().toString().trim())
                     .setInterval(Integer.parseInt(advertisingIntervalText.getText().toString()))
                     .build();
    }
}
