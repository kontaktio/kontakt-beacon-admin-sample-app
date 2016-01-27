package com.kontakt.sample.ui.fragment.range;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.dialog.PasswordDialogFragment;
import com.kontakt.sample.ui.activity.management.BeaconManagementActivity;
import com.kontakt.sample.ui.activity.management.SyncableBeaconManagementActivity;
import com.kontakt.sample.ui.adapter.range.BaseRangeAdapter;
import com.kontakt.sample.ui.adapter.range.IBeaconRangeAdapter;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.common.interfaces.SDKBiConsumer;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;

public class SyncableRangeFragment extends BaseRangeFragment {

    public static SyncableRangeFragment newInstance() {
        Bundle args = new Bundle();
        SyncableRangeFragment fragment = new SyncableRangeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static final String TAG = SyncableRangeFragment.class.getSimpleName();

    @Override
    public int getTitle() {
        return R.string.syncable_connection;
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

                            final Intent intent = new Intent(getContext(), SyncableBeaconManagementActivity.class);
                            intent.putExtra(BeaconManagementActivity.EXTRA_BEACON_DEVICE, beacon);

                            startActivityForResult(intent, REQUEST_CODE_CONNECT_TO_DEVICE);
                        }
                    }).show(getActivity().getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    IBeaconScanContext getIBeaconScanContext() {
        return beaconScanContext;
    }

    @Override
    EddystoneScanContext getEddystoneScanContext() {
        return null;
    }

    @Override
    BaseRangeAdapter getAdapter() {
        return new IBeaconRangeAdapter(getContext());
    }
}
