package com.kontakt.sample.ui.activity.management;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.dialog.ChoiceDialogFragment;
import com.kontakt.sample.ui.dialog.InputDialogFragment;
import com.kontakt.sample.ui.dialog.NumericInputDialogFragment;
import com.kontakt.sample.ui.dialog.PasswordDialogFragment;
import com.kontakt.sample.ui.activity.BaseActivity;
import com.kontakt.sample.ui.view.Entry;
import com.kontakt.sample.util.Constants;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.ble.connection.KontaktDeviceConnection;
import com.kontakt.sdk.android.ble.connection.WriteListener;
import com.kontakt.sdk.android.common.interfaces.SDKBiConsumer;
import com.kontakt.sdk.android.common.interfaces.SDKPredicate;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.util.IBeaconPropertyValidator;

import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class BeaconManagementActivity extends BaseActivity implements KontaktDeviceConnection.ConnectionListener {

    public static final String EXTRA_BEACON_DEVICE = "extra_beacon_device";

    public static final String EXTRA_FAILURE_MESSAGE = "extra_failure_message";



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

    @InjectView(R.id.loading_spinner)
    View progressBar;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private IBeaconDevice beacon;
    private ProgressDialog progressDialog;

    private KontaktDeviceConnection kontaktDeviceConnection;
    private int animationDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacon_activity);
        ButterKnife.inject(this);
        setUpToolbarBack(toolbar);
        animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        beaconForm.setVisibility(View.GONE);
        setUpView();
        beacon = getIntent().getParcelableExtra(EXTRA_BEACON_DEVICE);
        setUpActionBarTitle(String.format("%s (%s)", beacon.getName(), beacon.getAddress()));
        kontaktDeviceConnection = new KontaktDeviceConnection(this, beacon, this);
        kontaktDeviceConnection.connect();
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

    @Override
    protected void onDestroy() {
        clearConnections();
        super.onDestroy();
        ButterKnife.reset(this);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public void onConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(BeaconManagementActivity.this, "Connected");
            }
        });
    }

    @Override
    public void onAuthenticationSuccess(final IBeaconDevice.Characteristics characteristics) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(BeaconManagementActivity.this, "Authentication Success");
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
                switch (failureCode) {
                    case KontaktDeviceConnection.FAILURE_UNKNOWN_BEACON:
                        intent.putExtra(EXTRA_FAILURE_MESSAGE, String.format("Unknown beacon: %s", beacon.getName()));
                        break;
                    case KontaktDeviceConnection.FAILURE_WRONG_PASSWORD:
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
                switch (errorCode) {

                    case KontaktDeviceConnection.ERROR_OVERWRITE_REQUEST:
                        Utils.showToast(BeaconManagementActivity.this, "Overwrite request error");
                        break;

                    case KontaktDeviceConnection.ERROR_SERVICES_DISCOVERY:
                        Utils.showToast(BeaconManagementActivity.this, "Services discovery error");
                        break;

                    case KontaktDeviceConnection.ERROR_AUTHENTICATION:
                        Utils.showToast(BeaconManagementActivity.this, "Authentication error");
                        break;

                    default:
                        if (KontaktDeviceConnection.isGattError(errorCode)) {
                            Utils.showToast(BeaconManagementActivity.this, "Gatt error " + KontaktDeviceConnection.getGattError(errorCode));
                        } else {
                            throw new IllegalStateException("Unexpected connection error occured: " + errorCode);
                        }
                }
            }
        });
    }

    @Override
    public void onDisconnected() {
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
        manufacturerNameEntry.setEnabled(false);
        firmwareRevisionEntry.setEnabled(false);
        hardwareRevisionEntry.setEnabled(false);
    }

    private void setBeaconFormVisible(final boolean state) {

        final View showView;
        final View hideView;

        if (state) {
            showView = beaconForm;
            hideView = progressBar;
        } else {
            showView = progressBar;
            hideView = beaconForm;
        }

        if (showView != null && hideView != null) {
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

    private void clearConnections() {
        if (kontaktDeviceConnection != null) {
            kontaktDeviceConnection.close();
            kontaktDeviceConnection = null;
        }
    }

    @OnClick(R.id.proximity_uuid)
    void writeProximityUUID() {
        InputDialogFragment.newInstance("Overwrite",
                getString(R.string.proximity_uuid),
                getString(R.string.ok),
                new SDKBiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwriteProximityUUID(UUID.fromString(result));
                    }
                }).show(getSupportFragmentManager(), Constants.DIALOG);
    }

    @OnClick(R.id.major)
    void writeMajor() {
        NumericInputDialogFragment.newInstance("Overwrite",
                getString(R.string.major),
                getString(R.string.ok),
                new SDKPredicate<Integer>() {
                    @Override
                    public boolean test(Integer target) {
                        try {
                            IBeaconPropertyValidator.validateMajor(target);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                },
                new SDKBiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwriteMajor(Integer.parseInt(result));
                    }
                }).show(getSupportFragmentManager(), Constants.DIALOG);
    }

    @OnClick(R.id.minor)
    void writeMinor() {
        NumericInputDialogFragment.newInstance("Overwrite",
                getString(R.string.minor),
                getString(R.string.ok),
                new SDKPredicate<Integer>() {
                    @Override
                    public boolean test(Integer target) {
                        try {
                            IBeaconPropertyValidator.validateMinor(target);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                },
                new SDKBiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwriteMinor(Integer.parseInt(result));
                    }
                }).show(getSupportFragmentManager(), Constants.DIALOG);

    }

    @OnClick(R.id.power_level)
    void writePowerLevel() {
        NumericInputDialogFragment.newInstance("Overwrite",
                getString(R.string.power_level),
                getString(R.string.ok),
                new SDKPredicate<Integer>() {
                    @Override
                    public boolean test(Integer target) {
                        try {
                            IBeaconPropertyValidator.validatePowerLevel(target);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                },
                new SDKBiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwritePowerLevel(Integer.parseInt(result));
                    }
                }).show(getSupportFragmentManager(), Constants.DIALOG);
    }

    @OnClick(R.id.advertising_interval)
    void writeAdvertisingInterval() {
        NumericInputDialogFragment.newInstance("Overwrite",
                getString(R.string.advertising_interval),
                getString(R.string.ok),
                new SDKPredicate<Integer>() {
                    @Override
                    public boolean test(Integer target) {
                        try {
                            IBeaconPropertyValidator.validateAdvertisingInterval(target);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                },
                new SDKBiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwriteAdvertisingInterval(Long.parseLong(result));
                    }
                }).show(getSupportFragmentManager(), Constants.DIALOG);
    }

    @OnClick(R.id.set_password)
    void writePassword() {
        PasswordDialogFragment.newInstance("Overwrite",
                getString(R.string.set_password),
                getString(R.string.ok),
                new SDKBiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwritePassword(result);
                    }
                }
        ).show(getSupportFragmentManager(), Constants.DIALOG);
    }

    @OnClick(R.id.model_name)
    void writeModelName() {
        InputDialogFragment.newInstance("Overwrite",
                getString(R.string.model_name),
                getString(R.string.ok),
                new SDKBiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwriteModelName(result);
                    }
                }).show(getSupportFragmentManager(), Constants.DIALOG);
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
                }).show(getSupportFragmentManager().beginTransaction(), Constants.DIALOG);
    }

    @OnClick(R.id.default_settings)
    void restoreDefaultSettings() {
        InputDialogFragment.newInstance("Restore default settings",
                "Master password",
                "Restore",
                new SDKBiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String masterPassword) {
                        onRestoreDefaultSettings(masterPassword);
                    }
                }).show(getSupportFragmentManager().beginTransaction(), Constants.DIALOG);
    }

    private void onRestoreDefaultSettings(final String masterPassword) {
        kontaktDeviceConnection.restoreDefaultSettings(masterPassword, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Device restored to default settings");
                    }
                });
            }

            @Override
            public void onWriteFailure(Cause cause) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Device could not be restored to default settings");
                    }
                });

            }
        });
    }

    private void onResetDevice() {
        kontaktDeviceConnection.resetDevice(new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Device reset successfully");
                        kontaktDeviceConnection.connect();
                    }
                });
            }

            @Override
            public void onWriteFailure(Cause cause) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Device reset error");
                    }
                });
            }
        });
    }

    private void onOverwriteModelName(String result) {
        kontaktDeviceConnection.overwriteModelName(result, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse writeResponse) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Model name written successfully");
                    }
                });
            }

            @Override
            public void onWriteFailure(Cause cause) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Model name overwrite failure");
                    }
                });
            }
        });
    }

    private void onOverwritePassword(String result) {
        kontaktDeviceConnection.overwritePassword(result, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Password written successfully");
                    }
                });
            }

            @Override
            public void onWriteFailure(Cause cause) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Advertising Interval write failure");
                    }
                });
            }
        });
    }

    private void onOverwriteAdvertisingInterval(long value) {
        kontaktDeviceConnection.overwriteAdvertisingInterval(value, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Advertising Interval written successfully");
                    }
                });
            }

            @Override
            public void onWriteFailure(Cause cause) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Advertising Interval overwrite failure");
                    }
                });
            }
        });
    }

    private void onOverwritePowerLevel(int newPowerLevel) {
        kontaktDeviceConnection.overwritePowerLevel(newPowerLevel, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Power level written successfully");
                    }
                });
            }

            @Override
            public void onWriteFailure(Cause cause) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Power level overwrite failure");
                    }
                });
            }
        });
    }

    private void onOverwriteMinor(int newMinor) {
        kontaktDeviceConnection.overwriteMinor(newMinor, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Minor overwritten successfully");
                    }
                });
            }

            @Override
            public void onWriteFailure(Cause cause) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Minor overwrite failure");
                    }
                });
            }
        });
    }

    private void onOverwriteMajor(int newMajor) {
        kontaktDeviceConnection.overwriteMajor(newMajor, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Major overwritten successfully");
                    }
                });
            }

            @Override
            public void onWriteFailure(Cause cause) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Minor overwrite failure");
                    }
                });
            }
        });
    }

    private void onOverwriteProximityUUID(UUID uuid) {
        kontaktDeviceConnection.overwriteProximityUUID(uuid, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Proximity UUID overwritten successfully");
                    }
                });
            }

            @Override
            public void onWriteFailure(Cause cause) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showToast(BeaconManagementActivity.this, "Minor overwrite failure");
                    }
                });
            }
        });
    }
}
