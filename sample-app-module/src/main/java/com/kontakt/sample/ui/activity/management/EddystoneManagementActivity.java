package com.kontakt.sample.ui.activity.management;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.util.IBeaconPropertyValidator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class EddystoneManagementActivity extends BaseActivity implements KontaktDeviceConnection.ConnectionListener {

    public static final String EXTRA_FAILURE_MESSAGE = "extra_failure_message";

    public static final String EDDYSTONE_DEVICE = "eddystone_device";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.beacon_form)
    ViewGroup beaconForm;
    @InjectView(R.id.loading_spinner)
    View progressBar;

    @InjectView(R.id.namespace_id)
    Entry namespaceId;
    @InjectView(R.id.instance_id)
    Entry instanceId;
    @InjectView(R.id.url)
    Entry url;
    @InjectView(R.id.power_level)
    Entry powerLevel;
    @InjectView(R.id.battery_level)
    Entry batteryLevel;
    @InjectView(R.id.manufacturer_name)
    Entry manufacturerName;
    @InjectView(R.id.firmware_revision)
    Entry firmwareRevision;
    @InjectView(R.id.hardware_revision)
    Entry hardwareRevision;

    private KontaktDeviceConnection eddystoneBeaconConnection;
    private IEddystoneDevice eddystoneDevice;

    private int animationDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eddystone_activity);
        ButterKnife.inject(this);
        setUpActionBar(toolbar);
        beaconForm.setVisibility(View.GONE);
        animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        getEddystone();
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
    protected void onResume() {
        super.onResume();
        connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearConnection();
    }

    private void getEddystone() {
        eddystoneDevice = getIntent().getParcelableExtra(EDDYSTONE_DEVICE);
        eddystoneBeaconConnection = new KontaktDeviceConnection(this, eddystoneDevice, this);
        setUpActionBarTitle(String.format("%s (%s)", eddystoneDevice.getUrl(), eddystoneDevice.getAddress()));
    }


    private void connect() {
        if (eddystoneBeaconConnection != null && !eddystoneBeaconConnection.isConnected()) {
            eddystoneBeaconConnection.connect();
        }
    }

    private void clearConnection() {
        if (eddystoneBeaconConnection != null && eddystoneBeaconConnection.isConnected()) {
            eddystoneBeaconConnection.close();
            eddystoneBeaconConnection = null;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @OnClick(R.id.url)
    void overwriteUrl() {
        InputDialogFragment.newInstance("Overwrite",
                "Url",
                getString(R.string.ok),
                new SDKBiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwriteUrl(result);
                    }
                }).show(getSupportFragmentManager().beginTransaction(), Constants.DIALOG);
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

    private void onResetDevice() {
        eddystoneBeaconConnection.resetDevice(new WriteListener() {
            @Override
            public void onWriteSuccess(WriteListener.WriteResponse response) {
                showToast("Reset device success");
            }

            @Override
            public void onWriteFailure(Cause cause) {
                showToast("Reset device failure");
            }
        });
    }

    @OnClick(R.id.power_level)
    void overwritePowerLevel() {
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
                }).show(getSupportFragmentManager().beginTransaction(), Constants.DIALOG);
    }

    @OnClick(R.id.namespace_id)
    void overwriteNamespaceId() {
        InputDialogFragment.newInstance("Overwrite",
                "Namespace ID",
                getString(R.string.ok),
                new SDKBiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwriteNamespaceId(result);
                    }
                }).show(getSupportFragmentManager().beginTransaction(), Constants.DIALOG);
    }

    @OnClick(R.id.instance_id)
    void overwriteInstanceId() {
        InputDialogFragment.newInstance("Overwrite",
                "Instance ID",
                getString(R.string.ok),
                new SDKBiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String result) {
                        onOverwriteInstanceId(result);
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
        ).show(getSupportFragmentManager().beginTransaction(), Constants.DIALOG);
    }

    private void onOverwritePassword(String result) {
        eddystoneBeaconConnection.overwritePassword(result, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                showToast("New password set");
            }

            @Override
            public void onWriteFailure(Cause cause) {
                showToast("New password set failed");
            }
        });
    }

    private void onRestoreDefaultSettings(String masterPassword) {
        eddystoneBeaconConnection.restoreDefaultSettings(masterPassword, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                showToast("Restore default settings success");
            }

            @Override
            public void onWriteFailure(Cause cause) {
                showToast("Restore default settings failed");
            }
        });
    }


    private void onOverwriteInstanceId(String result) {
        eddystoneBeaconConnection.overwriteInstanceId(result, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                showToast("Overwrite instance id success");
            }

            @Override
            public void onWriteFailure(Cause cause) {
                showToast("Overwrite instance id failure");
            }
        });
    }

    private void onOverwriteNamespaceId(String result) {
        eddystoneBeaconConnection.overwriteNamespaceId(result, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                showToast("Overwrite namespace id success");
            }

            @Override
            public void onWriteFailure(Cause cause) {
                showToast("Overwrite namespace id failure");
            }
        });
    }

    private void onOverwritePowerLevel(int i) {
        eddystoneBeaconConnection.overwritePowerLevel(i, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                showToast("Overwrite power level success");
            }

            @Override
            public void onWriteFailure(Cause cause) {
                showToast("Overwrite power level failure");
            }
        });
    }

    private void onOverwriteUrl(String result) {
        eddystoneBeaconConnection.overwriteUrl(result, new WriteListener() {
            @Override
            public void onWriteSuccess(WriteResponse response) {
                showToast("Overwrite url success");
            }

            @Override
            public void onWriteFailure(Cause cause) {
                showToast("Overwrite url failure");
            }
        });
    }

    @Override
    public void onConnected() {
        showToast("Connected");
    }


    @Override
    public void onAuthenticationSuccess(IEddystoneDevice.Characteristics characteristics) {
        showToast("Authentication success");
        setBeaconFormVisible(true);
        fillEntries(characteristics);
    }

    @Override
    public void onAuthenticationFailure(final int failureCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Intent intent = getIntent();
                switch (failureCode) {
                    case KontaktDeviceConnection.FAILURE_UNKNOWN_BEACON:
                        intent.putExtra(EXTRA_FAILURE_MESSAGE, String.format("Unknown beacon: %s", eddystoneDevice.getAddress()));
                        break;
                    case KontaktDeviceConnection.FAILURE_WRONG_PASSWORD:
                        intent.putExtra(EXTRA_FAILURE_MESSAGE, "Wrong password. Beacon will be disabled for about 20 mins.");
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
    public void onCharacteristicsUpdated(IEddystoneDevice.Characteristics characteristics) {
        fillEntries(characteristics);
    }

    @Override
    public void onErrorOccured(int errorCode) {
        switch (errorCode) {
            case KontaktDeviceConnection.ERROR_OVERWRITE_REQUEST:
                showToast("Overwrite request error");
                break;

            case KontaktDeviceConnection.ERROR_SERVICES_DISCOVERY:
                showToast("Services discovery error");
                break;

            case KontaktDeviceConnection.ERROR_AUTHENTICATION:
                showToast("Authentication error");
                break;

            default:
                if (KontaktDeviceConnection.isGattError(errorCode)) {
                    showToast("Gatt error " + KontaktDeviceConnection.getGattError(errorCode));
                } else {
                    throw new IllegalStateException("Unexpected connection error occured: " + errorCode);
                }
        }
    }

    @Override
    public void onDisconnected() {
        showToast("Disconnected");
        setBeaconFormVisible(false);
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EddystoneManagementActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillEntries(final IEddystoneDevice.Characteristics characteristics) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fill(characteristics);
            }
        });
    }

    private void fill(IEddystoneDevice.Characteristics characteristics) {
        namespaceId.setValue(characteristics.getNamespaceId());
        instanceId.setValue(characteristics.getInstanceId());
        url.setValue(characteristics.getUrl());
        powerLevel.setValue(characteristics.getPowerLevel() + "");
        batteryLevel.setValue(characteristics.getBatteryLevel());
        manufacturerName.setValue(characteristics.getManufacturerName());
        firmwareRevision.setValue(characteristics.getFirmwareRevision());
        hardwareRevision.setValue(characteristics.getHardwareRevision());
    }


    private void setBeaconFormVisible(final boolean state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }
}

