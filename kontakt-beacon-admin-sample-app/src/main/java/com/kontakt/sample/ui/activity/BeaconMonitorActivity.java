package com.kontakt.sample.ui.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.widget.Toolbar;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sample.adapter.MonitorSectionAdapter;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.ble.configuration.BeaconActivityCheckConfiguration;
import com.kontakt.sdk.ble.configuration.MonitorPeriod;
import com.kontakt.sdk.ble.connection.OnServiceBoundListener;
import com.kontakt.sdk.ble.device.IBeaconDevice;
import com.kontakt.sdk.ble.device.IRegion;
import com.kontakt.sdk.ble.filter.Filters;
import com.kontakt.sdk.ble.manager.BeaconManager;
import com.kontakt.sdk.ble.rssi.RssiCalculators;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class BeaconMonitorActivity extends BaseActivity {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    private MonitorSectionAdapter adapter;

    private BeaconManager beaconManager;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @InjectView(R.id.list)
    ExpandableListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacon_monitor_list_activity);
        ButterKnife.inject(this);
        setUpActionBar(toolbar);
        setUpActionBarTitle(getString(R.string.monitor_beacons));

        adapter = new MonitorSectionAdapter(this);

        list.setAdapter(adapter);
        beaconManager = BeaconManager.newInstance(this);

        beaconManager.setBeaconActivityCheckConfiguration(BeaconActivityCheckConfiguration.DEFAULT);

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

        beaconManager.addFilter(Filters.newProximityUUIDFilter(IRegion.DEFAULT_KONTAKT_BEACON_PROXIMITY_UUID)); //accept Beacons with default Proximity UUID only
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
        beaconManager.setMonitorPeriod(MonitorPeriod.MINIMAL);
        beaconManager.registerMonitoringListener(new BeaconMonitorActivity.ActivityMonitoringListener(this, adapter));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!beaconManager.isBluetoothEnabled()) {
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        } else if(beaconManager.isConnected()) {
            startMonitoring();
        } else {
            connectAndStartMonitoring();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(beaconManager.isConnected()) {
            beaconManager.stopMonitoring();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.disconnect();
        beaconManager = null;
        ButterKnife.reset(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if(resultCode == Activity.RESULT_OK) {
                connectAndStartMonitoring();
            } else {
                final String bluetoothNotEnabledInfo = getString(R.string.bluetooth_not_enabled);
                Toast.makeText(this, bluetoothNotEnabledInfo, Toast.LENGTH_LONG).show();
                getSupportActionBar().setSubtitle(bluetoothNotEnabledInfo);
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startMonitoring() {
        try {
            beaconManager.startMonitoring(); // starts monitoring everywhere

                /*final Set<Region> regionSet = new HashSet<Region>();
                                regionSet.add(new Region(UUID.randomUUID(), 333, 333, "My region"));
                                beaconManager.startMonitoring(regionSet);

                                You can monitor Beacons by specifying Region Set as it was
                                in previous versions of kontakt.io's Android SDK
                                */
        } catch (RemoteException e) {
            Utils.showToast(this, getString(R.string.unexpected_error_monitoring_start));
        }
    }

    private void connectAndStartMonitoring() {
        try {
            beaconManager.connect(new OnServiceBoundListener() {
                @Override
                public void onServiceBound() {
                    try {
                        beaconManager.startMonitoring();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (RemoteException e) {
            Utils.showToast(this, getString(R.string.unexpected_error_connection));
        }
    }

    private static class ActivityMonitoringListener extends BeaconManager.MonitoringListener {

        private Activity activity;

        private MonitorSectionAdapter adapter;

        ActivityMonitoringListener(Activity activity, MonitorSectionAdapter adapter) {
            this.activity = activity;
            this.adapter = adapter;
        }

        private ActivityMonitoringListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onMonitorStart() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setProgressBarIndeterminateVisibility(true);
                }
            });
        }

        @Override
        public void onMonitorStop() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setProgressBarIndeterminateVisibility(false);
                }
            });
        }

        @Override
        public void onIBeaconsUpdated(final IRegion region, final List<IBeaconDevice> beacons) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final int index = adapter.getGroupIndex(region);
                    if (index != -1) {
                        adapter.replaceChildren(index, beacons);
                    }
                }
            });
        }

        @Override
        public void onIBeaconAppeared(final IRegion region, final IBeaconDevice beacon) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int index = adapter.getGroupIndex(region);
                    if (index != -1) {
                        adapter.addOrReplaceChild(index, beacon);
                    }
                }
            });
        }

        @Override
        public void onRegionEntered(final IRegion venue) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (! adapter.containsGroup(venue)) {
                        adapter.addGroup(venue);
                    }
                }
            });
        }

        @Override
        public void onRegionAbandoned(final IRegion region) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.removeGroup(region);
                }
            });
        }
    }
}
