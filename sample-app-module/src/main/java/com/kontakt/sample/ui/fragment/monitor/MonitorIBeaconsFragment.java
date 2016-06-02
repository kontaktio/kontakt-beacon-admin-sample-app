package com.kontakt.sample.ui.fragment.monitor;

import android.os.Bundle;
import com.kontakt.sample.R;
import com.kontakt.sample.ui.adapter.monitor.BaseMonitorAdapter;
import com.kontakt.sample.ui.adapter.monitor.IBeaconMonitorAdapter;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.SpaceListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleSpaceListener;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import java.util.List;

public class MonitorIBeaconsFragment extends BaseMonitorFragment {

  public static final String TAG = MonitorIBeaconsFragment.class.getSimpleName();

  public static MonitorIBeaconsFragment newInstance() {
    Bundle args = new Bundle();
    MonitorIBeaconsFragment fragment = new MonitorIBeaconsFragment();
    fragment.setHasOptionsMenu(true);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  BaseMonitorAdapter getAdapter() {
    return new IBeaconMonitorAdapter(getContext());
  }

  @Override
  public int getTitle() {
    return R.string.monitor_beacons;
  }

  @Override
  public String getFragmentTag() {
    return TAG;
  }

  @Override
  protected void configureListeners() {
    proximityManager.setIBeaconListener(createIBeaconListener());
    proximityManager.setSpaceListener(createSpaceListener());
  }

  private IBeaconListener createIBeaconListener() {
    return new SimpleIBeaconListener() {
      @Override
      public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
        final int index = adapter.getGroupIndex(region);
        if (index != -1) {
          adapter.addOrReplaceChild(index, ibeacon);
        }
      }

      @Override
      public void onIBeaconsUpdated(List<IBeaconDevice> ibeacons, IBeaconRegion region) {
        final int index = adapter.getGroupIndex(region);
        if (index != -1) {
          adapter.replaceChildren(index, ibeacons);
        }
      }
    };
  }

  private SpaceListener createSpaceListener() {
    return new SimpleSpaceListener() {
      @Override
      public void onRegionEntered(IBeaconRegion region) {
        if (!adapter.containsGroup(region)) {
          adapter.addGroup(region);
        }
      }

      @Override
      public void onRegionAbandoned(IBeaconRegion region) {
        adapter.removeGroup(region);
      }
    };
  }
}
