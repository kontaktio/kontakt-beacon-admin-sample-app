package com.kontakt.sample.ui.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sample.adapter.ProximityManagerAdapter;
import com.kontakt.sdk.android.ble.configuration.BeaconActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanContext;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.IBeaconAdvertisingPacket;
import com.kontakt.sdk.android.ble.filter.CustomFilter;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.ibeacon.IBeaconDevice;
import com.kontakt.sdk.android.common.ibeacon.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SimultaneousScanActivity extends BaseActivity implements ProximityManagerAdapter.RemoveManagerListener {

    private static final String TAG = SimultaneousScanActivity.class.getSimpleName();

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.managers_list)
    ListView managersList;
    @InjectView(R.id.count_of_managers)
    TextView countOfManagers;
    @InjectView(R.id.create_manager)
    Button createManager;
    @InjectView(R.id.distance)
    EditText distance;

    private List<ProximityManagerWrapper> proximityManagerWrapperList = new ArrayList<>();

    private ProximityManagerAdapter proximityManagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simultaneous_scan_activity);
        ButterKnife.inject(this);
        setUpActionBar(toolbar);
        setUpActionBarTitle(getString(R.string.simultaneous_scans));
        proximityManagerAdapter = new ProximityManagerAdapter(this, proximityManagerWrapperList);
        proximityManagerAdapter.setRemoveManagerListener(this);
        managersList.setAdapter(proximityManagerAdapter);
    }

    @OnClick(R.id.create_manager)
    void onCreateManagerClicked() {
        createProximityManager();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!BluetoothUtils.isBluetoothEnabled()) {
            createManager.setEnabled(false);
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        }
        updateProximityManagersCount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAllProximityManagers();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                createManager.setEnabled(true);
            } else {
                final String bluetoothNotEnabledInfo = getString(R.string.bluetooth_not_enabled);
                Toast.makeText(this, bluetoothNotEnabledInfo, Toast.LENGTH_LONG).show();
                getSupportActionBar().setSubtitle(bluetoothNotEnabledInfo);
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void removeManager(ProximityManagerWrapper proximityManagerWrapper) {

        proximityManagerWrapper.proximityManager.disconnect();

        proximityManagerWrapperList.remove(proximityManagerWrapper);
        updateList();
        updateProximityManagersCount();
    }

    private void stopAllProximityManagers() {
        for (ProximityManagerWrapper proximityManagerWrapper : proximityManagerWrapperList) {
            proximityManagerWrapper.getProximityManager().finishScan();
            proximityManagerWrapper.getProximityManager().disconnect();
        }
    }

    private void createProximityManager() {
        String distanceText = distance.getText().toString();
        Integer integer;
        try {
            integer = Integer.valueOf(distanceText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.incorrect_distance, Toast.LENGTH_SHORT).show();
            return;
        }
        ManagerCreatorTask managerCreatorTask = new ManagerCreatorTask(integer, this);
        managerCreatorTask.execute();
    }

    private void updateProximityManagersCount() {
        countOfManagers.setText(getString(R.string.count_of_managers, proximityManagerWrapperList.size()));
    }

    private void updateList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                proximityManagerAdapter.notifyDataSetChanged();
            }
        });
    }

    private ScanContext createScanContext(final int distance) {
        return new ScanContext.Builder()
                .setScanPeriod(new ScanPeriod(TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(5)))
                .addIBeaconFilter(new CustomFilter() {
                    @Override
                    public boolean apply(IBeaconAdvertisingPacket iBeaconAdvertisingPacket) {
                        return iBeaconAdvertisingPacket.getDistance() < distance;
                    }
                })
                .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                .setBeaconActivityCheckConfiguration(BeaconActivityCheckConfiguration.DEFAULT)
                .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
                .build();
    }

    private class ManagerCreatorTask extends AsyncTask<Void, Void, Void> {
        private int distance;
        private Context context;

        ProximityManager proximityManager;

        public ManagerCreatorTask(int distance, Context context) {
            this.distance = distance;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            proximityManager = new ProximityManager(context);
        }

        @Override
        protected Void doInBackground(Void... params) {
            final ProximityManagerWrapper proximityManagerWrapper = new ProximityManagerWrapper(distance, proximityManager);
            updateList();
            proximityManagerWrapperList.add(proximityManagerWrapper);
            proximityManager.initializeScan(createScanContext(distance), new OnServiceReadyListener() {
                @Override
                public void onServiceReady() {
                    proximityManager.attachListener(new ProximityManager.RangingListener() {
                        @Override
                        public void onIBeaconsDiscovered(Region region, List<IBeaconDevice> iBeaconDevices) {
                            int indexOf = proximityManagerWrapperList.indexOf(proximityManagerWrapper);
                            ProximityManagerWrapper currentWrapper = proximityManagerWrapperList.get(indexOf);
                            currentWrapper.setFoundBeacons(iBeaconDevices.size());
                            proximityManagerWrapperList.set(indexOf, currentWrapper);
                            updateList();
                        }
                    });
                }

                @Override
                public void onConnectionFailure() {

                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateProximityManagersCount();
        }
    }

    public static class ProximityManagerWrapper {

        private ProximityManager proximityManager;
        private int distance;

        private int foundBeacons;

        public ProximityManagerWrapper(int distance, ProximityManager proximityManager) {
            this.distance = distance;
            this.proximityManager = proximityManager;
        }

        public void setFoundBeacons(int foundBeacons) {
            this.foundBeacons = foundBeacons;
        }

        public ProximityManager getProximityManager() {
            return proximityManager;
        }

        public int getDistance() {
            return distance;
        }

        public int getFoundBeacons() {
            return foundBeacons;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (o == this) {
                return true;
            }
            if (o instanceof ProximityManagerWrapper) {
                ProximityManagerWrapper proximityManagerWrapper = (ProximityManagerWrapper) o;
                return proximityManagerWrapper.getProximityManager().equals(getProximityManager())
                        && proximityManagerWrapper.getDistance() == getDistance();
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 53;
            hash += proximityManager.hashCode();
            hash += 701 * distance;
            return hash;
        }
    }
}
