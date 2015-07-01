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

import com.kontakt.sample.R;
import com.kontakt.sample.dialog.ChoiceDialogFragment;
import com.kontakt.sample.dialog.InputDialogFragment;
import com.kontakt.sample.dialog.NumericInputDialogFragment;
import com.kontakt.sample.dialog.PasswordDialogFragment;
import com.kontakt.sample.ui.Entry;
import com.kontakt.sample.util.Constants;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.ble.connection.IBeaconConnection;
import com.kontakt.sdk.android.common.ibeacon.IBeaconDevice;
import com.kontakt.sdk.android.common.interfaces.SDKBiConsumer;
import com.kontakt.sdk.android.common.interfaces.SDKPredicate;
import com.kontakt.sdk.android.common.model.Config;
import com.kontakt.sdk.android.common.model.IDevice;
import com.kontakt.sdk.android.common.model.Profile;
import com.kontakt.sdk.android.common.util.IBeaconPropertyValidator;
import com.kontakt.sdk.android.connection.SyncableIBeaconConnection;
import com.kontakt.sdk.android.http.exception.ClientException;

import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SyncableBeaconManagementActivity extends BaseActivity implements IBeaconConnection.ConnectionListener {

    private static final String TAG = SyncableBeaconManagementActivity.class.getSimpleName();

    public static final String EXTRA_BEACON_DEVICE = "extra_beacon_device";

    public static final String EXTRA_FAILURE_MESSAGE = "extra_failure_message";

    public static final int REQUEST_CODE_OBTAIN_CONFIG = 1;

    public static final int REQUEST_CODE_OBTAIN_PROFILE = 2;

    private IBeaconDevice beacon;

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

    private int animationDuration;

    private SyncableIBeaconConnection syncableIBeaconConnection;

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

        syncableIBeaconConnection = new SyncableIBeaconConnection(this, beacon, this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Utils.setOrientationChangeEnabled(false, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!syncableIBeaconConnection.isConnectedToDevice()) {
            syncableIBeaconConnection.connectToDevice();
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
        switch (requestCode) {
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

    @Override
    public void onConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(SyncableBeaconManagementActivity.this, "Connected");
            }
        });
    }

    @Override
    public void onAuthenticationSuccess(final IBeaconDevice.Characteristics characteristics) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(SyncableBeaconManagementActivity.this, "Authentication Success");
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
                switch (errorCode) {

                    case IBeaconConnection.ERROR_OVERWRITE_REQUEST:
                        Utils.showToast(SyncableBeaconManagementActivity.this, "Overwrite request error");
                        break;

                    case IBeaconConnection.ERROR_SERVICES_DISCOVERY:
                        Utils.showToast(SyncableBeaconManagementActivity.this, "Services discovery error");
                        break;

                    case IBeaconConnection.ERROR_AUTHENTICATION:
                        Utils.showToast(SyncableBeaconManagementActivity.this, "Authentication error");
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
                Utils.showToast(SyncableBeaconManagementActivity.this, "Disconnected");
                setBeaconFormVisible(false);
            }
        });
    }

    private void onConfigResultDelivered(final int resultCode, final Intent data) {
        if (resultCode != RESULT_CANCELED) {
            final Config config = data.getParcelableExtra(ConfigFormActivity.EXTRA_RESULT_CONFIG);
            onApplyConfig(config);
        }
    }

    private void onProfileResultDelivered(final int resultCode, final Intent data) {
        if (resultCode != RESULT_CANCELED) {
            final Profile profile = data.getParcelableExtra(ProfilesActivity.EXTRA_PROFILE);
            onAcceptProfile(profile);
        }
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

    private void clearConnection() {
        syncableIBeaconConnection.close();
        syncableIBeaconConnection = null;
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
                }).show(getFragmentManager().beginTransaction(), Constants.DIALOG);
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
                }).show(getFragmentManager().beginTransaction(), Constants.DIALOG);
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
                }).show(getFragmentManager().beginTransaction(), Constants.DIALOG);

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
                }).show(getFragmentManager().beginTransaction(), Constants.DIALOG);
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
                }).show(getFragmentManager().beginTransaction(), Constants.DIALOG);
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
        ).show(getFragmentManager().beginTransaction(), Constants.DIALOG);
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
                new SDKBiConsumer<DialogInterface, String>() {
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
        syncableIBeaconConnection.applyConfig(config, new SyncableIBeaconConnection.SyncWriteBatchListener<Config>() {
            @Override
            public void onSyncWriteBatchStart(Config batchHolder) {
                showToast("write config batch start");
            }

            @Override
            public void onWriteFailed() {
                showToast("write config failed");
            }

            @Override
            public void onBatchWriteError(int errorCode) {
                showToast("config batch write error " + errorCode);
            }

            @Override
            public void onSyncFailed(ClientException e) {
                showToast("sync config failed");
                e.printStackTrace();
            }

            @Override
            public void onSuccess(Config batchHolder) {
                showToast("config write and sync succeed");
            }
        });
    }

    private void onAcceptProfile(final Profile profile) {
        //todo: accept profile
    }

    private void onRestoreDefaultSettings(final String masterPassword) {

    }

    private void onResetDevice() {
        syncableIBeaconConnection.resetDevice(new IBeaconConnection.WriteListener() {
            @Override
            public void onWriteSuccess() {
                showToast("Device reset success");
            }

            @Override
            public void onWriteFailure() {
                showToast("Device reset failure");
            }
        });
    }

    private void onOverwriteModelName(String result) {
        syncableIBeaconConnection.overwriteModelName(result, new SyncableIBeaconConnection.SyncWriteListener() {
            @Override
            public void onWriteFailed() {
                showToast("Overwrite model name failed");
            }

            @Override
            public void onSyncFailed(ClientException e) {
                e.printStackTrace();
                showToast("Beacon updated but sync failed");
            }

            @Override
            public void onSuccess() {
                showToast("Overwrite model name success");
            }
        });
    }

    private void onOverwritePassword(String result) {
        syncableIBeaconConnection.overwritePassword(result, new SyncableIBeaconConnection.SyncWriteListener() {
            @Override
            public void onWriteFailed() {
                showToast("Overwrite password failed");
            }

            @Override
            public void onSyncFailed(ClientException e) {
                e.printStackTrace();
                showToast("Beacon updated but sync failed");
            }

            @Override
            public void onSuccess() {
                showToast("Overwrite password succeed");
            }
        });
    }

    private void onOverwriteAdvertisingInterval(long value) {
        syncableIBeaconConnection.overwriteAdvertisingInterval(value, new SyncableIBeaconConnection.SyncWriteListener() {
            @Override
            public void onWriteFailed() {
                showToast("Overwrite interval failed");
            }

            @Override
            public void onSyncFailed(ClientException e) {
                e.printStackTrace();
                showToast("Beacon updated but sync failed");
            }

            @Override
            public void onSuccess() {
                showToast("Overwrite interval success");
            }
        });
    }

    private void onOverwritePowerLevel(int newPowerLevel) {
        syncableIBeaconConnection.overwritePowerLevel(newPowerLevel, new SyncableIBeaconConnection.SyncWriteListener() {
            @Override
            public void onWriteFailed() {
                showToast("Overwrite power level failed");
            }

            @Override
            public void onSyncFailed(ClientException e) {
                showToast("Beacon updated but sync failed");
            }

            @Override
            public void onSuccess() {
                showToast("Overwrite power level succeed");
            }
        });
    }

    private void onOverwriteMinor(int newMinor) {
        syncableIBeaconConnection.overwriteMinor(newMinor, new SyncableIBeaconConnection.SyncWriteListener() {
            @Override
            public void onWriteFailed() {
                showToast("Overwrite minor failed");
            }

            @Override
            public void onSyncFailed(ClientException e) {
                e.printStackTrace();
                showToast("Beacon updated but sync failed");
            }

            @Override
            public void onSuccess() {
                showToast("Overwrite minor success");
            }
        });
    }

    private void onOverwriteMajor(int newMajor) {
        syncableIBeaconConnection.overwriteMajor(newMajor, new SyncableIBeaconConnection.SyncWriteListener() {
            @Override
            public void onWriteFailed() {
                showToast("Overwrite major failed");
            }

            @Override
            public void onSyncFailed(ClientException e) {
                e.printStackTrace();
                showToast("Beacon updated but sync failed");
            }

            @Override
            public void onSuccess() {
                showToast("Overwrite major success");
            }
        });
    }

    private void onOverwriteProximityUUID(UUID uuid) {
        syncableIBeaconConnection.overwriteProximityUUID(uuid, new SyncableIBeaconConnection.SyncWriteListener() {
            @Override
            public void onWriteFailed() {
                showToast("Overwrite proximity failed");
            }

            @Override
            public void onSyncFailed(ClientException e) {
                e.printStackTrace();
                showToast("Beacon updated but sync failed");
            }

            @Override
            public void onSuccess() {
                showToast("Overwrite proximity succeed");
            }
        });
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showToast(SyncableBeaconManagementActivity.this, message);
            }
        });
    }

}
