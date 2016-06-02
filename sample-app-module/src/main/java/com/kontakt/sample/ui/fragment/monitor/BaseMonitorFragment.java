package com.kontakt.sample.ui.fragment.monitor;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.kontakt.sample.R;
import com.kontakt.sample.permission.PermissionChecker;
import com.kontakt.sample.ui.adapter.monitor.BaseMonitorAdapter;
import com.kontakt.sample.ui.fragment.BaseFragment;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.ble.broadcast.BluetoothStateChangeReceiver;
import com.kontakt.sdk.android.ble.broadcast.OnBluetoothStateChangeListener;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.exception.ScanError;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.listeners.ScanStatusListener;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import java.util.concurrent.TimeUnit;

public abstract class BaseMonitorFragment extends BaseFragment implements OnBluetoothStateChangeListener, ScanStatusListener {

  abstract BaseMonitorAdapter getAdapter();

  abstract void configureListeners();

  @InjectView(R.id.list) ExpandableListView deviceList;

  protected ProximityManager proximityManager;
  protected BaseMonitorAdapter adapter;
  private BluetoothStateChangeReceiver bluetoothStateChangeReceiver;
  private MenuItem bluetoothMenuItem;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.beacon_monitor_list_activity, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    ButterKnife.inject(this, getView());
    proximityManager = new ProximityManager(getActivity());
    configureProximityManager();
    configureListeners();
    adapter = getAdapter();
    deviceList.setAdapter(adapter);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.reset(this);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.range_menu, menu);
    bluetoothMenuItem = menu.findItem(R.id.change_bluetooth_state);
    setCorrectMenuItemTitle();
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.change_bluetooth_state:
        changeBluetoothState();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    bluetoothStateChangeReceiver = new BluetoothStateChangeReceiver(this);
    getActivity().registerReceiver(bluetoothStateChangeReceiver, new IntentFilter(BluetoothStateChangeReceiver.ACTION));
    setCorrectMenuItemTitle();
  }

  @Override
  public void onStop() {
    super.onStop();
    getActivity().unregisterReceiver(bluetoothStateChangeReceiver);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!BluetoothUtils.isBluetoothEnabled()) {
      Utils.showToast(getActivity(), "Please enable bluetooth");
    } else {
      startScan();
    }
  }

  @Override
  public void onPause() {
    proximityManager.stopScanning();
    super.onPause();
  }

  @Override
  public void onDestroy() {
    proximityManager.disconnect();
    super.onDestroy();
  }

  private void configureProximityManager() {
    proximityManager.configuration()
        .scanPeriod(ScanPeriod.create(TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS.toMillis(10)))
        .activityCheckConfiguration(ActivityCheckConfiguration.MINIMAL);

    proximityManager.setScanStatusListener(this);
  }

  private void setCorrectMenuItemTitle() {
    if (bluetoothMenuItem == null) {
      return;
    }
    boolean enabled = Utils.getBluetoothState();
    changeBluetoothTitle(enabled);
  }

  private void changeBluetoothTitle(boolean enabled) {
    if (enabled) {
      bluetoothMenuItem.setTitle(R.string.disable_bluetooth);
    } else {
      bluetoothMenuItem.setTitle(R.string.enable_bluetooth);
    }
  }

  private void changeBluetoothState() {
    boolean enabled = Utils.getBluetoothState();
    Utils.setBluetooth(!enabled);
  }

  private void startScan() {
    if (permissionCheckerHoster == null) {
      return;
    }
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

  @Override
  public void onScanStart() {
    if (getActivity() == null) {
      return;
    }
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        getActivity().setProgressBarIndeterminateVisibility(true);
      }
    });
  }

  @Override
  public void onScanStop() {
    if (getActivity() == null) {
      return;
    }
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        getActivity().setProgressBarIndeterminateVisibility(false);
      }
    });
  }

  @Override
  public void onScanError(ScanError exception) {

  }

  @Override
  public void onBluetoothConnecting() {

  }

  @Override
  public void onBluetoothConnected() {
    startScan();
    changeBluetoothTitle(true);
  }

  @Override
  public void onBluetoothDisconnecting() {

  }

  @Override
  public void onBluetoothDisconnected() {
    proximityManager.stopScanning();
    changeBluetoothTitle(false);
  }

  @Override
  public void onMonitoringCycleStart() {

  }

  @Override
  public void onMonitoringCycleStop() {

  }
}
