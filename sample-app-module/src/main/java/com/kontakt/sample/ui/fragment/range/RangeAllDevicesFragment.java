package com.kontakt.sample.ui.fragment.range;

import android.os.Bundle;
import com.kontakt.sample.R;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;

public class RangeAllDevicesFragment extends BaseRangeAllFragment {

  public static final String TAG = RangeAllDevicesFragment.class.getSimpleName();

  public static RangeAllDevicesFragment newInstance() {
    Bundle args = new Bundle();
    RangeAllDevicesFragment fragment = new RangeAllDevicesFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  ProximityManagerContract getProximityManager() {
    return new ProximityManager(getActivity());
  }

  @Override
  public int getTitle() {
    return R.string.range_all_beacons;
  }

  @Override
  public String getFragmentTag() {
    return TAG;
  }
}
