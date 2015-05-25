package com.kontakt.sample.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sample.dialog.ChoiceDialogFragment;
import com.kontakt.sample.dialog.InputDialogFragment;
import com.kontakt.sample.dialog.NumericInputDialogFragment;
import com.kontakt.sample.dialog.PasswordDialogFragment;
import com.kontakt.sample.service.SyncService;
import com.kontakt.sample.ui.Entry;
import com.kontakt.sample.util.Constants;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.ble.connection.IBeaconConnection;
import com.kontakt.sdk.ble.device.IBeaconDevice;
import com.kontakt.sdk.common.IBeaconPropertyValidator;
import com.kontakt.sdk.common.interfaces.BiConsumer;
import com.kontakt.sdk.common.interfaces.Predicate;
import com.kontakt.sdk.common.model.Config;
import com.kontakt.sdk.common.model.Profile;

import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class BeaconManagementActivity extends BaseActivity {

    public static final String EXTRA_BEACON_DEVICE = "extra_beacon_device";

    public static final String EXTRA_FAILURE_MESSAGE = "extra_failure_message";

    public static final int REQUEST_CODE_OBTAIN_CONFIG = 1;

    public static final int REQUEST_CODE_OBTAIN_PROFILE = 2;

    private IBeaconDevice beacon;

    private IBeaconConnection beaconConnection;

    @InjectView(R.id.beacon_form)
    ViewGroup beaconForm;

    @InjectView(R.id.proximity_uuid)
    Entry proximityUuidEntry;

    @InjectView(R.id.major)
    Entry majorEntry;

    @InjectView(R.id.minor)
    Entry minorEntry;

    @InjectView(R.id.power_level)
    Entry powerLevelEntry;

    @InjectView(R.id.advertising_interval)
    Entry advertisingIntervalEntry;

    @InjectView(R.id.battery_level)
    Entry batteryLevelEntry;

    @InjectView(R.id.manufacturer_name)
    Entry manufacturerNameEntry;

    @InjectView(R.id.model_name)
    Entry modelNameEntry;

    @InjectView(R.id.firmware_revision)
    Entry firmwareRevisionEntry;

    @InjectView(R.id.hardware_revision)
    Entry hardwareRevisionEntry;

    @InjectView(R.id.accept_profile)
    Entry acceptProfileEntry;

    @InjectView(R.id.apply_config)
    Entry applyConfigEntry;

    @InjectView(R.id.loading_spinner)
    View progressBar;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private ProgressDialog progressDialog;

    private int animationDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacon_activity);
        ButterKnife.inject(this);
        setUpActionBar(toolbar);

        beaconForm.setVisibility(View.GONE);
        animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        setUpView();
        beacon = getIntent().getParcelableExtra(EXTRA_BEACON_DEVICE);
        setUpActionBarTitle(String.format("%s (%s)", beacon.getName(), beacon.getAddress()));

        beaconConnection = IBeaconConnection.newInstance(this, beacon, createConnectionListener());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.setOrientationChangeEnabled(false, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(! beaconConnection.isConnected()) {
            beaconConnection.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.setOrientationChangeEnabled(true, this);
    }

    @Override
    protected void onDestroy() {
        clearConnection();
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_CODE_OBTAIN_CONFIG:
                onConfigResultDelivered(resultCode, data);
                break;
            case REQUEST_CODE_OBTAIN_PROFILE:
                onProfileResultDelivered(resultCode, data);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    private void onConfigResultDelivered(final int resultCode, final Intent data) {
        if(resultCode != RESULT_CANCELED) {
            final Config config = data.getParcelableExtra(ConfigFormActivity.EXTRA_RESULT_CONFIG);
            onApplyConfig(config);
        }
    }

    private void onProfileResultDelivered(final int resultCode, final Intent data) {
        if(resultCode != RESULT_CANCELED) {
            final Profile profile = data.getParcelableExtra(ProfilesActivity.EXTRA_PROFILE);
            onAcceptProfile(profile);
        }
    }

    private IBeaconConnection.ConnectionListener createConnectionListener() {
        return new IBeaconConnection.ConnectionListener() {
            @Override
            public void onConnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onAuthenticationSuccess(final IBeaconDevice.Characteristics characteristics) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Authentication Success", Toast.LENGTH_SHORT).show();
                        fillEntries(characteristics);
                        setBeaconFormVisible(true);
                    }
                });
            }

            @Override
            public void onAuthenticationFailure(final int failureCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final Intent intent = getIntent();
                        switch(failureCode) {
                            case IBeaconConnection.FAILURE_UNKNOWN_BEACON:
                                intent.putExtra(EXTRA_FAILURE_MESSAGE, String.format("Unknown beacon: %s", beacon.getName()));
                                break;
                            case IBeaconConnection.FAILURE_WRONG_PASSWORD:
                                intent.putExtra(EXTRA_FAILURE_MESSAGE, String.format("Wrong password. Beacon will be disabled for about 20 mins."));
                                break;
                            default:
                                throw new IllegalArgumentException(String.format("Unknown beacon connection failure code: %d", failureCode));
                        }
                        setResult(RESULT_CANCELED, intent);
                        finish();
                    }
                });
            }

            @Override
            public void onCharacteristicsUpdated(final IBeaconDevice.Characteristics characteristics) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fillEntries(characteristics);
                    }
                });
            }

            @Override
            public void onErrorOccured(final int errorCode) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch(errorCode) {

                            case IBeaconConnection.ERROR_OVERWRITE_REQUEST:
                                Toast.makeText(BeaconManagementActivity.this, "Overwrite request error", Toast.LENGTH_SHORT).show();
                                break;

                            case IBeaconConnection.ERROR_SERVICES_DISCOVERY:
                                Toast.makeText(BeaconManagementActivity.this, "Services discovery error", Toast.LENGTH_SHORT).show();
                                break;

                            case IBeaconConnection.ERROR_AUTHENTICATION:
                                Toast.makeText(BeaconManagementActivity.this, "Authentication error", Toast.LENGTH_SHORT).show();
                                break;

                            case IBeaconConnection.ERROR_CHARACTERISTIC_READING:
                                Toast.makeText(BeaconManagementActivity.this, "Characteristic reading error", Toast.LENGTH_SHORT).show();
                                break;

                            default:
                                throw new IllegalStateException("Unexpected connection error occured: " + errorCode);
                        }
                    }
                });
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                        setBeaconFormVisible(false);
                    }
                });
            }
        };
    }

    private void fillEntries(IBeaconDevice.Characteristics characteristics) {
        proximityUuidEntry.setValue(characteristics.getProximityUUID().toString());
        majorEntry.setValue(String.valueOf(characteristics.getMajor()));
        minorEntry.setValue(String.valueOf(characteristics.getMinor()));
        powerLevelEntry.setValue(String.valueOf(characteristics.getPowerLevel()));
        advertisingIntervalEntry.setValue(String.format("%d ms", characteristics.getAdvertisingInterval()));
        modelNameEntry.setValue(characteristics.getModelName());
        batteryLevelEntry.setValue(characteristics.getBatteryLevel());
        manufacturerNameEntry.setValue(characteristics.getManufacturerName());
        firmwareRevisionEntry.setValue(characteristics.getFirmwareRevision());
        hardwareRevisionEntry.setValue(characteristics.getHardwareRevision());
    }

    private void setUpView() {
        batteryLevelEntry.setEnabled(false);

        acceptProfileEntry = (Entry) beaconForm.findViewById(R.id.accept_profile);
        applyConfigEntry = (Entry) beaconForm.findViewById(R.id.apply_config);

        manufacturerNameEntry.setEnabled(false);

        firmwareRevisionEntry.setEnabled(false);

        hardwareRevisionEntry.setEnabled(false);
    }
    private void setBeaconFormVisible(final boolean state) {

        final View showView;
        final View hideView;

        if(state) {
            showView = beaconForm;
            hideView = progressBar;
        } else {
            showView = progressBar;
            hideView = beaconForm;
        }

        if(showView != null && hideView != null) {
            showView.setAlpha(0f);
            showView.setVisibility(View.VISIBLE);
            showView.animate()
                    .alpha(1f)
                    .setDuration(animationDuration)
                    .setListener(null);

            hideView.animate()
                    .alpha(0f)
                    .setDuration(animationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            hideView.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void clearConnection() {
        beaconConnection.close();
        beaconConnection = null;
    }

    @OnClick(R.id.proximity_uuid)
    void writeProximityUUID() {
        InputDialogFragment.newInstance("Overwrite",
                getString(R.string.proximity_uuid),
                getString(R.string.ok),
                new BiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwriteProximityUUID(UUID.fromString(result));
                    }
                }).show(getFragmentManager().beginTransaction(), Constants.DIALOG);
    }

    @OnClick(R.id.major)
    void writeMajor() {
        NumericInputDialogFragment.newInstance("Overwrite",
                getString(R.string.major),
                getString(R.string.ok),
                new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer target) {
                        try {
                            IBeaconPropertyValidator.validateMajor(target);
                            return true;
                        } catch(Exception e) {
                            return false;
                        }
                    }
                },
                new BiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwriteMajor(Integer.parseInt(result));
                    }
                }).show(getFragmentManager().beginTransaction(), Constants.DIALOG);
    }

    @OnClick(R.id.minor)
    void writeMinor() {
        NumericInputDialogFragment.newInstance("Overwrite",
                getString(R.string.minor),
                getString(R.string.ok),
                new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer target) {
                        try {
                            IBeaconPropertyValidator.validateMinor(target);
                            return true;
                        } catch(Exception e) {
                            return false;
                        }
                    }
                },
                new BiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwriteMinor(Integer.parseInt(result));
                    }
                }).show(getFragmentManager().beginTransaction(), Constants.DIALOG);

    }

    @OnClick(R.id.power_level)
    void writePowerLevel() {
        NumericInputDialogFragment.newInstance("Overwrite",
                getString(R.string.power_level),
                getString(R.string.ok),
                new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer target) {
                        try {
                            IBeaconPropertyValidator.validatePowerLevel(target);
                            return true;
                        } catch(Exception e) {
                            return false;
                        }
                    }
                },
                new BiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwritePowerLevel(Integer.parseInt(result));
                    }
                }).show(getFragmentManager().beginTransaction(), Constants.DIALOG);
    }

    @OnClick(R.id.advertising_interval)
    void writeAdvertisingInterval() {
        NumericInputDialogFragment.newInstance("Overwrite",
                getString(R.string.advertising_interval),
                getString(R.string.ok),
                new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer target) {
                        try {
                            IBeaconPropertyValidator.validateAdvertisingInterval(target);
                            return true;
                        } catch(Exception e) {
                            return false;
                        }
                    }
                },
                new BiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwriteAdvertisingInterval(Long.parseLong(result));
                    }
                }).show(getFragmentManager().beginTransaction(), Constants.DIALOG);
    }

    @OnClick(R.id.set_password)
    void writePassword() {
        PasswordDialogFragment.newInstance("Overwrite",
                getString(R.string.set_password),
                getString(R.string.ok),
                new BiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwritePassword(result);
                    }
                }
        ).show(getFragmentManager().beginTransaction(), Constants.DIALOG);
    }

    @OnClick(R.id.model_name)
    void writeModelName() {
        InputDialogFragment.newInstance("Overwrite",
                getString(R.string.model_name),
                getString(R.string.ok),
                new BiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwriteModelName(result);
                    }
                }).show(getFragmentManager().beginTransaction(), Constants.DIALOG);
    }

    @OnClick(R.id.reset_device)
    void resetDevice() {
        ChoiceDialogFragment.newInstance("Reset device",
                "Are you sure you want to reset beacon?",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onResetDevice();
                    }
                }).show(getFragmentManager().beginTransaction(), Constants.DIALOG);
    }

    @OnClick(R.id.default_settings)
    void restoreDefaultSettings() {
        InputDialogFragment.newInstance("Restore default settings",
                "Master password",
                "Restore",
                new BiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String masterPassword) {
                        onRestoreDefaultSettings(masterPassword);
                    }
                }).show(getFragmentManager().beginTransaction(), Constants.DIALOG);
    }

    @OnClick(R.id.accept_profile)
    void acceptProfile() {
        startActivityForResult(new Intent(this, ProfilesActivity.class), REQUEST_CODE_OBTAIN_PROFILE);
    }

    @OnClick(R.id.apply_config)
    void applyConfig() {
        startActivityForResult(new Intent(this, ConfigFormActivity.class), REQUEST_CODE_OBTAIN_CONFIG);
    }

    private void onApplyConfig(final Config config) {
        beaconConnection.applyConfig(config, new IBeaconConnection.WriteBatchListener<Config>() {
            @Override
            public void onWriteBatchStart(Config batchHolder) {
                progressDialog = ProgressDialog.show(BeaconManagementActivity.this,
                        "Applying Config",
                        "Please wait...");
            }

            @Override
            public void onWriteBatchFinish(final Config batch) {
                progressDialog.dismiss();
                final Intent serviceIntent = new Intent(BeaconManagementActivity.this, SyncService.class);
                serviceIntent.putExtra(SyncService.EXTRA_REQUEST_CODE, SyncService.REQUEST_SYNC_CONFIG);
                serviceIntent.putExtra(SyncService.EXTRA_ITEM, batch);
                startService(serviceIntent);
            }

            @Override
            public void onErrorOccured(int errorCode) {
                progressDialog.dismiss();

                switch(errorCode) {
                    case IBeaconConnection.ERROR_BATCH_WRITE_TX_POWER:
                        Toast.makeText(BeaconManagementActivity.this,
                                "Error during Batch write operation - could not write Tx Power",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case IBeaconConnection.ERROR_BATCH_WRITE_INTERVAL:
                        Toast.makeText(BeaconManagementActivity.this,
                                "Error during Batch write operation - could not write Interval",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case IBeaconConnection.ERROR_BATCH_WRITE_MAJOR:
                        Toast.makeText(BeaconManagementActivity.this,
                                "Error during Batch write operation - could not write Major value",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case IBeaconConnection.ERROR_BATCH_WRITE_MINOR:
                        Toast.makeText(BeaconManagementActivity.this,
                                "Error during Batch write operation - could not write Minor value",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case IBeaconConnection.ERROR_BATCH_WRITE_PROXIMITY_UUID:
                        Toast.makeText(BeaconManagementActivity.this,
                                "Error during Batch write operation - could not write Proximity UUID",
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown error code: " + errorCode);
                }
            }
        });
    }

    private void onAcceptProfile(final Profile profile) {
        beaconConnection.acceptProfile(profile, new IBeaconConnection.WriteBatchListener<Profile>() {
            @Override
            public void onWriteBatchStart(Profile batchHolder) {
                progressDialog = ProgressDialog.show(BeaconManagementActivity.this,
                        String.format("Accepting profile - %s", profile.getName()),
                        "Please wait...");
            }

            @Override
            public void onWriteBatchFinish(Profile batchHolder) {
                progressDialog.dismiss();
            }

            @Override
            public void onErrorOccured(int errorCode) {
                progressDialog.dismiss();

                switch(errorCode) {
                    case IBeaconConnection.ERROR_BATCH_WRITE_TX_POWER:
                        Toast.makeText(BeaconManagementActivity.this,
                                "Error during Batch write operation - could not write Tx Power",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case IBeaconConnection.ERROR_BATCH_WRITE_INTERVAL:
                        Toast.makeText(BeaconManagementActivity.this,
                                "Error during Batch write operation - could not write Interval",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case IBeaconConnection.ERROR_BATCH_WRITE_PROXIMITY_UUID:
                        Toast.makeText(BeaconManagementActivity.this,
                                "Error during Batch write operation - could not write Proximity UUID",
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown error code: " + errorCode);
                }
            }
        });
    }

    private void onRestoreDefaultSettings(final String masterPassword) {
        beaconConnection.restoreDefaultSettings(masterPassword, new IBeaconConnection.WriteListener() {
            @Override
            public void onWriteSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Device restored to default settings", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onWriteFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Device could not be restored to default settings", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    private void onResetDevice() {
        beaconConnection.resetDevice(new IBeaconConnection.WriteListener() {
            @Override
            public void onWriteSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Device reset successfully", Toast.LENGTH_SHORT).show();
                        beaconConnection.connect();
                    }
                });
            }

            @Override
            public void onWriteFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Device reset error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void onOverwriteModelName(String result) {
        beaconConnection.overwriteModelName(result, new IBeaconConnection.WriteListener() {
            @Override
            public void onWriteSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Model name written successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onWriteFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Model name overwrite failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void onOverwritePassword(String result) {
        beaconConnection.overwritePassword(result, new IBeaconConnection.WriteListener() {
            @Override
            public void onWriteSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Password written successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onWriteFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Advertising Interval write failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void onOverwriteAdvertisingInterval(long value) {
        beaconConnection.overwriteAdvertisingInterval(value, new IBeaconConnection.WriteListener() {
            @Override
            public void onWriteSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Advertising Interval written successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onWriteFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Advertising Interval overwrite failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void onOverwritePowerLevel(int newPowerLevel) {
        beaconConnection.overwritePowerLevel(newPowerLevel, new IBeaconConnection.WriteListener() {
            @Override
            public void onWriteSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Power level written successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onWriteFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Power level overwrite failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void onOverwriteMinor(int newMinor) {
        beaconConnection.overwriteMinor(newMinor, new IBeaconConnection.WriteListener() {
            @Override
            public void onWriteSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Minor overwritten successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onWriteFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Minor overwrite failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void onOverwriteMajor(int newMajor) {
        beaconConnection.overwriteMajor(newMajor, new IBeaconConnection.WriteListener() {
            @Override
            public void onWriteSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Major overwritten successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onWriteFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Minor overwrite failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void onOverwriteProximityUUID(UUID uuid) {
        beaconConnection.overwriteProximity(uuid, new IBeaconConnection.WriteListener() {
            @Override
            public void onWriteSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Proximity UUID overwritten successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onWriteFailure() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BeaconManagementActivity.this, "Minor overwrite failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
