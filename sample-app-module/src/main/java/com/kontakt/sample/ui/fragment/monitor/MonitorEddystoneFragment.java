package com.kontakt.sample.ui.fragment.monitor;

import android.os.Bundle;
import com.kontakt.sample.R;
import com.kontakt.sample.ui.adapter.monitor.BaseMonitorAdapter;
import com.kontakt.sample.ui.adapter.monitor.EddystoneMonitorAdapter;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.SpaceListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleSpaceListener;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;
import java.util.List;

public class MonitorEddystoneFragment extends BaseMonitorFragment {

  public static final String TAG = MonitorEddystoneFragment.class.getSimpleName();

  public static MonitorEddystoneFragment newInstance() {
    Bundle args = new Bundle();
    MonitorEddystoneFragment fragment = new MonitorEddystoneFragment();
    fragment.setHasOptionsMenu(true);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  BaseMonitorAdapter getAdapter() {
    return new EddystoneMonitorAdapter(getContext());
  }

  @Override
  public int getTitle() {
    return R.string.monitor_eddystone;
  }

  @Override
  public String getFragmentTag() {
    return TAG;
  }

  @Override
  void configureListeners() {
    proximityManager.setEddystoneListener(createEddystoneListener());
    proximityManager.setSpaceListener(createSpaceListener());
  }

  private EddystoneListener createEddystoneListener() {
    return new SimpleEddystoneListener() {
      @Override
      public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
        final int index = adapter.getGroupIndex(namespace);
        if (index != -1) {
          adapter.addOrReplaceChild(index, eddystone);
        }
      }

      @Override
      public void onEddystonesUpdated(List<IEddystoneDevice> eddystones, IEddystoneNamespace namespace) {
        final int index = adapter.getGroupIndex(namespace);
        if (index != -1) {
          adapter.replaceChildren(index, eddystones);
        }
      }
    };
  }

  private SpaceListener createSpaceListener() {
    return new SimpleSpaceListener() {

      @Override
      public void onNamespaceEntered(IEddystoneNamespace namespace) {
        if (!adapter.containsGroup(namespace)) {
          adapter.addGroup(namespace);
        }
      }

      @Override
      public void onNamespaceAbandoned(IEddystoneNamespace namespace) {
        adapter.removeGroup(namespace);
      }
    };
  }

}
