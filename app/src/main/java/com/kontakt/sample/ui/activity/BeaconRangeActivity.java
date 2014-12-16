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
import com.kontakt.sdk.android.data.RssiCalculator;
import com.kontakt.sdk.android.data.RssiCalculators;
import com.kontakt.sdk.android.device.Beacon;
import com.kontakt.sdk.android.device.Region;
import com.kontakt.sdk.android.factory.AdvertisingPackage;
import com.kontakt.sdk.android.factory.Filters;
import com.kontakt.sdk.android.manager.ActionManager;
import com.kontakt.sdk.android.manager.BeaconManager;
import com.kontakt.sdk.android.util.MemoryUnit;
import com.kontakt.sdk.core.interfaces.BiConsumer;
import com.kontakt.sdk.core.interfaces.model.Action;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
                final com.kontakt.sdk.android.model.Beacon beacon = action.getProximitySource();
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

        beaconManager.setScanMode(BeaconManager.SCAN_MODE_BALANCED); // Works only for Android L OS version

        beaconManager.setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5)); //Calculate rssi value basing on arithmethic mean of 5 last notified values
/*
        beaconManager.setRssiCalculator(new RssiCalculators.CustomRssiCalculator() { //Provide your own Rssi Calculator to estimate manipulate Rssi value
            @Override                                                                  //and thus Proximity from Beacon device
            public double calculateRssi(int beaconHashCode, int rssiValue) {
                return rssiValue;
            }

            @Override
            public void clear() {

            }
        });
*/

        beaconManager.addFilter(Filters.newProximityUUIDFilter(BeaconManager.DEFAULT_KONTAKT_BEACON_PROXIMITY_UUID)); //accept Beacons with default Proximity UUID only
        //(f7826da6-4fa2-4e98-8024-bc5b71e0893e)
/*
        beaconManager.addFilter(Filters.newAddressFilter("00:00:00:00:00:00")); //accept Beacons with specified MAC address only
        beaconManager.addFilter(Filters.newBeaconUniqueIdFilter("myID"));         //accept Beacons with specified Unique Id only
        beaconManager.addFilter(Filters.newDeviceNameFilter("my_beacon_name"));   //accept Beacons with specified name only
        beaconManager.addFilter(Filters.newFirmwareFilter(26));                   //accept Beacons with specified Firmware version only
        beaconManager.addFilter(Filters.newMajorFilter(666));                     //accept Beacons with specified Major only
        beaconManager.addFilter(Filters.newMinorFilter(333));                     //accept Beacons with specified Minor only
        beaconManager.addFilter(Filters.newMultiFilterBuilder()                   //accept Beacon matching constraints specified in MultiFilter
                                        .setBeaconUniqueId("Boom")
                                        .setDeviceName("device_name")
                                        .setAddress("00:00:00:00:00:00")
                                        .setFirmware(26)
                                        .setProximityUUID(UUID.randomUUID())
                                        .build());
        beaconManager.addFilter(new Filters.CustomFilter() {                      //create your customized filter
            @Override
            public boolean filter(AdvertisingPackage advertisingPackage) {
                return advertisingPackage.getAccuracy() < 5;                     //accept beacons from distance 5m at most
            }
        });
*/

        beaconManager.setBeaconActivityCheckConfiguration(BeaconActivityCheckConfiguration.DEFAULT);

        beaconManager.setForceScanConfiguration(ForceScanConfiguration.DEFAULT);

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
                final String bluetoothNotEnabledInfo = getString(R.string.bluetooth_not_enabled);
                Toast.makeText(BeaconRangeActivity.this, bluetoothNotEnabledInfo, Toast.LENGTH_LONG).show();
                getActionBar().setSubtitle(bluetoothNotEnabledInfo);
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
                                beaconManager.startRanging(); //Starts ranging everywhere

                                /*final Set<Region> regionSet = new HashSet<Region>();
                                regionSet.add(new Region(UUID.randomUUID(), 333, 333, "My region"));
                                beaconManager.startRanging(regionSet);

                                You can range Beacons by specifying Region Set as it was
                                in previous versions of kontakt.io's Android SDK
                                */
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
