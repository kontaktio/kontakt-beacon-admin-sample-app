package com.kontakt.sample.ui.fragment.range;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.adapter.range.BaseRangeAdapter;
import com.kontakt.sample.ui.adapter.range.IBeaconRangeAdapter;
import com.kontakt.sample.ui.dialog.PasswordDialogFragment;
import com.kontakt.sample.ui.activity.management.BeaconManagementActivity;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.common.interfaces.SDKBiConsumer;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;

public class RangeIBeaconsFragment extends BaseRangeFragment {

    public static final String TAG = RangeIBeaconsFragment.class.getSimpleName();

    public static RangeIBeaconsFragment newInstance() {
        Bundle args = new Bundle();
        RangeIBeaconsFragment fragment = new RangeIBeaconsFragment();
        fragment.setHasOptionsMenu(true);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public int getTitle() {
        return R.string.range_beacons;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    void callOnListItemClick(int position) {
        final IBeaconDevice beacon = (IBeaconDevice) adapter.getItem(position);
        if (beacon != null) {
            PasswordDialogFragment.newInstance(getString(R.string.format_connect, beacon.getAddress()),
                    getString(R.string.password),
                    getString(R.string.connect),
                    new SDKBiConsumer<DialogInterface, String>() {
                        @Override
                        public void accept(DialogInterface dialogInterface, String password) {

                            beacon.setPassword(password.getBytes());

                            final Intent intent = new Intent(getContext(), BeaconManagementActivity.class);
                            intent.putExtra(BeaconManagementActivity.EXTRA_BEACON_DEVICE, beacon);

                            startActivityForResult(intent, REQUEST_CODE_CONNECT_TO_DEVICE);
                        }
                    }).show(getActivity().getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    EddystoneScanContext getEddystoneScanContext() {
        return null;
    }

    @Override
    IBeaconScanContext getIBeaconScanContext() {
        return beaconScanContext;
    }

    @Override
    BaseRangeAdapter getAdapter() {
        return new IBeaconRangeAdapter(getContext());
    }
}
