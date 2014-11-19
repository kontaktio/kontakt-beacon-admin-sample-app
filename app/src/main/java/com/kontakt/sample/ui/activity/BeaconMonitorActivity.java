package com.kontakt.sample.ui.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sample.adapter.MonitorSectionAdapter;
import com.kontakt.sdk.android.configuration.MonitorPeriod;
import com.kontakt.sdk.android.connection.OnServiceBoundListener;
import com.kontakt.sdk.android.device.Beacon;
import com.kontakt.sdk.android.device.Region;
import com.kontakt.sdk.android.manager.BeaconManager;

import java.util.List;


public class BeaconMonitorActivity extends Activity {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    private MonitorSectionAdapter adapter;

    private BeaconManager beaconManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.beacon_monitor_list_activity);

        final ExpandableListView list = (ExpandableListView) findViewById(R.id.list);

        adapter = new MonitorSectionAdapter(this);

        list.setAdapter(adapter);
        beaconManager = BeaconManager.newInstance(this);
        beaconManager.setMonitorPeriod(MonitorPeriod.MINIMAL);
        beaconManager.registerMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onMonitorStart() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setProgressBarIndeterminateVisibility(true);
                    }
                });
            }

            @Override
            public void onMonitorStop() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setProgressBarIndeterminateVisibility(false);
                    }
                });
            }

            @Override
            public void onBeaconsUpdated(final Region venue, final List<Beacon> beacons) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int index = adapter.getGroupIndex(venue);
                        if (index != -1) {
                            adapter.replaceChildren(index, beacons);
                        }
                    }
                });
            }

            @Override
            public void onBeaconAppeared(final Region region, final Beacon beacon) {
                runOnUiThread(new Runnable() {
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
            public void onRegionEntered(final Region venue) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                     if (!adapter.containsGroup(venue)) {
                        adapter.addGroup(venue);
                     }
                    }
                });
            }

            @Override
            public void onRegionAbandoned(final Region venue) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.removeGroup(venue);
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!beaconManager.isBluetoothEnabled()) {
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        } else {
            connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        beaconManager.stopMonitoring();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.disconnect();
        beaconManager = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if(resultCode == Activity.RESULT_OK) {
                connect();
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
                getActionBar().setSubtitle("Bluetooth not enabled");
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connect() {
        try {
            beaconManager.connect(new OnServiceBoundListener() {
                @Override
                public void onServiceBound() {
                    try {
                        beaconManager.startMonitoring(Region.EVERYWHERE);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (RemoteException e) {
            throw new IllegalStateException(e);
        }
    }
}
