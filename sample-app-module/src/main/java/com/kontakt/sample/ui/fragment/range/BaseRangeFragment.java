package com.kontakt.sample.ui.fragment.range;

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
import android.widget.ListView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import com.kontakt.sample.R;
import com.kontakt.sample.permission.PermissionChecker;
import com.kontakt.sample.ui.adapter.range.BaseRangeAdapter;
import com.kontakt.sample.ui.fragment.BaseFragment;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.ble.broadcast.BluetoothStateChangeReceiver;
import com.kontakt.sdk.android.ble.broadcast.OnBluetoothStateChangeListener;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.scan.ScanMode;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;

public abstract class BaseRangeFragment extends BaseFragment implements OnBluetoothStateChangeListener {

  abstract void callOnListItemClick(final int position);

  abstract BaseRangeAdapter getAdapter();

  abstract void configureListeners();

  protected static final int REQUEST_CODE_CONNECT_TO_DEVICE = 2;

  @InjectView(R.id.device_list) ListView deviceList;

  protected BaseRangeAdapter adapter;
  protected ProximityManager proximityManager;
  private MenuItem bluetoothMenuItem;

  private BluetoothStateChangeReceiver bluetoothStateChangeReceiver;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.beacon_range_activity, container, false);
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
    ButterKnife.reset(this);
    super.onDestroyView();
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
    getActivity().unregisterReceiver(bluetoothStateChangeReceiver);
    super.onStop();
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

  @OnItemClick(R.id.device_list)
  void onListItemClick(final int position) {
    callOnListItemClick(position);
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

  private void configureProximityManager() {
    proximityManager.configuration()
        .scanMode(ScanMode.BALANCED)
        .activityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
        .forceScanConfiguration(ForceScanConfiguration.MINIMAL)
        .deviceUpdateCallbackInterval(2000)
        .rssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5));
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
}
