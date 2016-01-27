package com.kontakt.sample.ui.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.kontakt.sample.R;
import com.kontakt.sample.permission.PermissionChecker;
import com.kontakt.sample.ui.adapter.ProximityManagerAdapter;
import com.kontakt.sample.model.ProximityManagerWrapper;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconAdvertisingPacket;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconDeviceEvent;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilter;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SimultaneousFragment extends BaseFragment implements ProximityManagerAdapter.RemoveManagerListener {

    public static final String TAG = SimultaneousFragment.class.getSimpleName();

    private List<ProximityManagerWrapper> proximityManagerWrapperList = new ArrayList<>();

    private ProximityManagerAdapter proximityManagerAdapter;

    @InjectView(R.id.managers_list)
    ListView managersList;
    @InjectView(R.id.count_of_managers)
    TextView countOfManagers;
    @InjectView(R.id.create_manager)
    Button createManager;
    @InjectView(R.id.distance)
    EditText distance;

    public static SimultaneousFragment newInstance() {
        Bundle args = new Bundle();
        SimultaneousFragment fragment = new SimultaneousFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.simultaneous_scan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        proximityManagerAdapter = new ProximityManagerAdapter(getContext(), proximityManagerWrapperList);
        proximityManagerAdapter.setRemoveManagerListener(this);
        managersList.setAdapter(proximityManagerAdapter);
        if (!BluetoothUtils.isBluetoothEnabled()) {
            createManager.setEnabled(false);
            Utils.showToast(getContext(), "Enable bleutooth");
        }
        updateProximityManagersCount();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopAllProximityManagers();
    }


    @OnClick(R.id.create_manager)
    void onCreateManagerClicked() {
        createProximityManager();
    }

    @Override
    public int getTitle() {
        return R.string.simultaneous_scans;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public void removeManager(ProximityManagerWrapper proximityManager) {

        proximityManager.disconnect();

        proximityManagerWrapperList.remove(proximityManager);
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
        final Integer integer;
        try {
            integer = Integer.valueOf(distanceText);
        } catch (NumberFormatException e) {
            Utils.showToast(getContext(), getString(R.string.incorrect_distance));
            return;
        }
        permissionCheckerHoster.requestPermission(new PermissionChecker.Callback() {
            @Override
            public void onPermisionGranted() {
                ManagerCreatorTask managerCreatorTask = new ManagerCreatorTask(integer, getContext());
                managerCreatorTask.execute();
            }

            @Override
            public void onPermissionRejected() {
                Snackbar.make(getView(), R.string.permission_rejected_message, Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    private void updateList() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                proximityManagerAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateProximityManagersCount() {
        countOfManagers.setText(getString(R.string.count_of_managers, proximityManagerWrapperList.size()));
    }

    private ScanContext createScanContext(final int distance) {
        return new ScanContext.Builder()
                .setScanPeriod(new ScanPeriod(TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(5)))
                .setIBeaconScanContext(new IBeaconScanContext.Builder()
                        .setIBeaconFilters(Collections.singletonList(new IBeaconFilter() {
                            @Override
                            public boolean apply(IBeaconAdvertisingPacket iBeaconAdvertisingPacket) {
                                return iBeaconAdvertisingPacket.getDistance() < distance;
                            }
                        }))
                        .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
                        .build())
                .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
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
                    proximityManager.attachListener(new ProximityManager.ProximityListener() {
                        @Override
                        public void onScanStop() {

                        }

                        @Override
                        public void onScanStart() {

                        }

                        @Override
                        public void onEvent(BluetoothDeviceEvent event) {
                            if (event.getEventType() == EventType.DEVICES_UPDATE) {

                                final IBeaconDeviceEvent iBeaconEvent = (IBeaconDeviceEvent) event;

                                int indexOf = proximityManagerWrapperList.indexOf(proximityManagerWrapper);
                                ProximityManagerWrapper currentWrapper = proximityManagerWrapperList.get(indexOf);
                                currentWrapper.setFoundBeacons(iBeaconEvent.getDeviceList().size());
                                proximityManagerWrapperList.set(indexOf, currentWrapper);
                                updateList();
                            }
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
}
