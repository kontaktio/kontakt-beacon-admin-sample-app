package com.kontakt.sample.ui.fragment;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kontakt.sample.R;
import com.kontakt.sample.action.ActionFragment;
import com.kontakt.sample.ui.fragment.monitor.MonitorEddystoneFragment;
import com.kontakt.sample.ui.fragment.monitor.MonitorIBeaconsFragment;
import com.kontakt.sample.ui.fragment.range.RangeAllDevicesFragment;
import com.kontakt.sample.ui.fragment.range.RangeEddystoneFragment;
import com.kontakt.sample.ui.fragment.range.RangeIBeaconsFragment;
import com.kontakt.sample.ui.fragment.range.ShuffledScanFragment;
import com.kontakt.sample.ui.fragment.range.SyncableRangeFragment;

import java.util.HashMap;
import java.util.Map;

public class DrawerFragmentFactory {

    private static final String TAG = DrawerFragmentFactory.class.getSimpleName();

    private Map<String, BaseFragment> baseFragmentMap = new HashMap<>();

    @IdRes
    public int getLastFragmentOrDefault() {
        return R.id.drawer_range_beacons;
    }

    public void onCreate() {
        createFragments();
    }

    @Nullable
    public BaseFragment getFragmentByMenuItemId(@IdRes int menuItemId) {
        BaseFragment fragment = null;

        switch (menuItemId) {
            case R.id.drawer_range_beacons:
                fragment = getFragment(RangeIBeaconsFragment.TAG);
                break;
            case R.id.drawer_range_eddystone:
                fragment = getFragment(RangeEddystoneFragment.TAG);
                break;
            case R.id.drawer_monitor_beacons:
                fragment = getFragment(MonitorIBeaconsFragment.TAG);
                break;
            case R.id.drawer_monitor_eddystone:
                fragment = getFragment(MonitorEddystoneFragment.TAG);
                break;
            case R.id.drawer_range_all_types:
                fragment = getFragment(RangeAllDevicesFragment.TAG);
                break;
            case R.id.drawer_foreground_background:
                fragment = getFragment(BackgroundScanFragment.TAG);
                break;
            case R.id.drawer_simultineaus_scan:
                fragment = getFragment(SimultaneousFragment.TAG);
                break;
            case R.id.drawer_syncable_connection:
                fragment = getFragment(SyncableRangeFragment.TAG);
                break;
            case R.id.drawer_shuffled:
                fragment = getFragment(ShuffledScanFragment.TAG);
                break;
            case R.id.drawer_actions:
                fragment = getFragment(ActionFragment.TAG);

        }
        return fragment;
    }

    private void createFragments() {
        putFragment(RangeIBeaconsFragment.TAG, RangeIBeaconsFragment.newInstance());
        putFragment(RangeEddystoneFragment.TAG, RangeEddystoneFragment.newInstance());
        putFragment(MonitorIBeaconsFragment.TAG, MonitorIBeaconsFragment.newInstance());
        putFragment(MonitorEddystoneFragment.TAG, MonitorEddystoneFragment.newInstance());
        putFragment(RangeAllDevicesFragment.TAG, RangeAllDevicesFragment.newInstance());
        putFragment(BackgroundScanFragment.TAG, BackgroundScanFragment.newInstance());
        putFragment(SimultaneousFragment.TAG, SimultaneousFragment.newInstance());
        putFragment(SyncableRangeFragment.TAG, SyncableRangeFragment.newInstance());
        putFragment(ShuffledScanFragment.TAG, ShuffledScanFragment.newInstance());
        putFragment(ActionFragment.TAG, ActionFragment.newInstance());
    }

    private void putFragment(@NonNull String tag, @NonNull BaseFragment baseFragment) {
        baseFragmentMap.put(tag, baseFragment);
    }

    private BaseFragment getFragment(@NonNull String tag) {
        return baseFragmentMap.get(tag);
    }
}
