package com.kontakt.sample.ui.fragment.range;

import android.os.Bundle;
import com.kontakt.sample.R;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;

public class ShuffledScanFragment extends BaseRangeAllFragment {

  public static final String TAG = ShuffledScanFragment.class.getSimpleName();

  public static ShuffledScanFragment newInstance() {
    Bundle args = new Bundle();
    ShuffledScanFragment fragment = new ShuffledScanFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public int getTitle() {
    return R.string.shuffled_beacons;
  }

  @Override
  public String getFragmentTag() {
    return TAG;
  }

  @Override
  ProximityManagerContract getProximityManager() {
    //To resolve shuffled beacons you need to put your api key into AndroidManifest. Only your beacons will be resolved
    ProximityManagerContract proximityManager = new ProximityManager(getContext());
    proximityManager.configuration()
        .cacheFileName("shuffled_file_cache")
        .monitoringEnabled(true)
        .monitoringSyncInterval(10)
        .resolveShuffledInterval(5);
    return proximityManager;
  }
}
