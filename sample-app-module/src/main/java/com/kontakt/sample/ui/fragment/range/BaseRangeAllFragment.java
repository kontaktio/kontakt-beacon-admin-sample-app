package com.kontakt.sample.ui.fragment.range;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.kontakt.sample.R;
import com.kontakt.sample.permission.PermissionChecker;
import com.kontakt.sample.ui.adapter.monitor.AllBeaconsMonitorAdapter;
import com.kontakt.sample.ui.fragment.BaseFragment;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.scan.ScanMode;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;
import java.util.List;

public abstract class BaseRangeAllFragment extends BaseFragment {

  @InjectView(R.id.list) protected ExpandableListView list;

  protected AllBeaconsMonitorAdapter allBeaconsRangeAdapter;
  protected ProximityManagerContract proximityManager;

  abstract ProximityManagerContract getProximityManager();

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.beacon_monitor_list_activity, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    ButterKnife.inject(this, getView());
    allBeaconsRangeAdapter = new AllBeaconsMonitorAdapter(getActivity());
    proximityManager = getProximityManager();
    configureProximityManager();
    list.setAdapter(allBeaconsRangeAdapter);
  }

  @Override
  public void onDestroy() {
    proximityManager.disconnect();
    super.onDestroy();
  }

  @Override
  public void onDestroyView() {
    ButterKnife.reset(this);
    super.onDestroyView();
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!BluetoothUtils.isBluetoothEnabled()) {
      Utils.showToast(getContext(), "Please enable bluetooth");
    } else {
      startScan();
    }
  }

  @Override
  public void onPause() {
    proximityManager.stopScanning();
    super.onPause();
  }

  private void configureProximityManager() {
    proximityManager.configuration()
        .scanMode(ScanMode.BALANCED)
        .rssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
        .activityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
        .forceScanConfiguration(ForceScanConfiguration.MINIMAL);

    proximityManager.setIBeaconListener(new SimpleIBeaconListener() {
      @Override
      public void onIBeaconsUpdated(List<IBeaconDevice> ibeacons, IBeaconRegion region) {
        onIBeaconDevicesList(ibeacons);
      }
    });
    proximityManager.setEddystoneListener(new SimpleEddystoneListener() {
      @Override
      public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {
        onEddystoneDevicesList(eddystones);
      }
    });
  }

  private void startScan() {
    permissionCheckerHoster.requestPermission(new PermissionChecker.Callback() {
      @Override
      public void onPermissionGranted() {
        start();
      }

      @Override
      public void onPermissionRejected() {
        Snackbar.make(getView(), R.string.permission_rejected_message, Snackbar.LENGTH_SHORT).show();
      }
    });
  }

  private void start() {
    proximityManager.connect(new OnServiceReadyListener() {
      @Override
      public void onServiceReady() {
        proximityManager.startScanning();
      }
    });
  }

  private void onEddystoneDevicesList(final List<IEddystoneDevice> eddystones) {
    if (getActivity() == null) {
      return;
    }
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        allBeaconsRangeAdapter.replaceEddystoneBeacons(eddystones);
      }
    });
  }

  private void onIBeaconDevicesList(final List<IBeaconDevice> ibeacons) {
    if (getActivity() == null) {
      return;
    }
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        allBeaconsRangeAdapter.replaceIBeacons(ibeacons);
      }
    });
  }
}
