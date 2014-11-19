package com.kontakt.sample.ui.activity;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sample.adapter.BeaconBaseAdapter;
import com.kontakt.sample.dialog.PasswordDialogFragment;
import com.kontakt.sdk.android.configuration.BeaconActivityCheckConfiguration;
import com.kontakt.sdk.android.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.connection.OnServiceBoundListener;
import com.kontakt.sdk.android.connection.ServiceConnectionChain;
import com.kontakt.sdk.android.device.Beacon;
import com.kontakt.sdk.android.device.Region;
import com.kontakt.sdk.android.manager.ActionManager;
import com.kontakt.sdk.android.manager.BeaconManager;
import com.kontakt.sdk.android.util.MemoryUnit;
import com.kontakt.sdk.core.interfaces.BiConsumer;
import com.kontakt.sdk.core.interfaces.model.Action;

import java.util.List;

public class BeaconRangeActivity extends ListActivity {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    private static final int REQUEST_CODE_CONNECT_TO_DEVICE = 2;

    private BeaconBaseAdapter adapter;
    private BeaconManager beaconManager;
    private ActionManager actionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        adapter = new BeaconBaseAdapter(this);
        actionManager = ActionManager.newInstance(this);
        actionManager.setMemoryCacheSize(20, MemoryUnit.BYTES);
        actionManager.registerActionNotifier(new ActionManager.ActionNotifier() {
            @Override
            public void onActionsFound(final List<Action<com.kontakt.sdk.android.model.Beacon>> actions) {
                final Action<com.kontakt.sdk.android.model.Beacon> action = actions.get(0);
                final com.kontakt.sdk.android.model.Beacon beacon = action.getBeacon();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final String info = String.format("%d Actions found for beacon:\nID: %s\nMajor: %d\nMinor: %d\nProximity UUID: %s\nProximity: %s",
                                actions.size(),
                                beacon.getId().toString(),
                                beacon.getMajor(),
                                beacon.getMinor(),
                                beacon.getProximityUUID().toString(),
                                action.getProximity().name());
                        Toast.makeText(BeaconRangeActivity.this, info, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        beaconManager = BeaconManager.newInstance(this);
        beaconManager.setBeaconActivityCheckConfiguration(BeaconActivityCheckConfiguration.DEFAULT);
        beaconManager.setForceScanConfiguration(ForceScanConfiguration.DISABLED);
        beaconManager.registerRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(final Region region, final List<Beacon> beacons) {
                BeaconRangeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.replaceWith(beacons);
                    }
                });
            }
        });

        setListAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(! beaconManager.isBluetoothEnabled()){
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        } else {
            connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        beaconManager.stopRanging();
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServiceConnectionChain.start()
                .disconnect(actionManager)
                .disconnect(beaconManager)
                .performQuietly();
        actionManager = null;
        beaconManager = null;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final Beacon beacon = (Beacon) adapter.getItem(position);
        PasswordDialogFragment.newInstance(getString(R.string.format_connect, beacon.getMacAddress()),
                getString(R.string.password),
                getString(R.string.connect),
                new BiConsumer<DialogInterface, String>() {
                    @Override
                    public void accept(DialogInterface dialogInterface, String password) {

                        beacon.setPassword(password.getBytes());

                        final Intent intent = new Intent(BeaconRangeActivity.this, BeaconControllerActivity.class);
                        intent.putExtra(BeaconControllerActivity.EXTRA_BEACON_DEVICE, beacon);

                        startActivityForResult(intent, REQUEST_CODE_CONNECT_TO_DEVICE);
                    }
                }).show(getFragmentManager(), "dialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if(resultCode == Activity.RESULT_OK) {
                connect();
            } else {
                Toast.makeText(BeaconRangeActivity.this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
                getActionBar().setSubtitle("Bluetooth not enabled");
            }

            return;
        }  else if(requestCode == REQUEST_CODE_CONNECT_TO_DEVICE) {
            if(resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this,
                        String.format("Beacon authentication failure: %s", data.getExtras().getString(BeaconControllerActivity.EXTRA_FAILURE_MESSAGE, "")),
                        Toast.LENGTH_SHORT).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connect() {
        setProgressBarIndeterminateVisibility(true);
        try {
            ServiceConnectionChain.start()
                    .connect(actionManager, new OnServiceBoundListener() {
                        @Override
                        public void onServiceBound() {
                            beaconManager.setActionController(actionManager.getController());
                        }
                    })
                    .connect(beaconManager, new OnServiceBoundListener() {
                        @Override
                        public void onServiceBound() {
                            try {
                                beaconManager.startRanging(Region.EVERYWHERE);
                            } catch (RemoteException e) {
                                Toast.makeText(BeaconRangeActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    })
                    .perform();
            BeaconRangeActivity.this.setProgressBarIndeterminateVisibility(true);
        } catch (RemoteException e) {
            Toast.makeText(BeaconRangeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
