package com.kontakt.sample.ui.fragment.range;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.adapter.range.BaseRangeAdapter;
import com.kontakt.sample.ui.adapter.range.EddystoneRangeAdapter;
import com.kontakt.sample.ui.dialog.PasswordDialogFragment;
import com.kontakt.sample.ui.activity.management.EddystoneManagementActivity;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.common.interfaces.SDKBiConsumer;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;

public class RangeEddystoneFragment extends BaseRangeFragment {

    public static final String TAG = RangeEddystoneFragment.class.getSimpleName();

    public static RangeEddystoneFragment newInstance() {
        Bundle args = new Bundle();
        RangeEddystoneFragment fragment = new RangeEddystoneFragment();
        fragment.setHasOptionsMenu(true);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void callOnListItemClick(int position) {
        final IEddystoneDevice eddystoneDevice = (IEddystoneDevice) adapter.getItem(position);
        if (eddystoneDevice != null) {
            PasswordDialogFragment.newInstance(getString(R.string.format_connect, eddystoneDevice.getAddress()),
                    getString(R.string.password),
                    getString(R.string.connect),
                    new SDKBiConsumer<DialogInterface, String>() {
                        @Override
                        public void accept(DialogInterface dialogInterface, String password) {
                            eddystoneDevice.setPassword(password.getBytes());

                            final Intent intent = new Intent(getContext(), EddystoneManagementActivity.class);
                            intent.putExtra(EddystoneManagementActivity.EDDYSTONE_DEVICE, eddystoneDevice);

                            startActivityForResult(intent, REQUEST_CODE_CONNECT_TO_DEVICE);
                        }
                    }).show(getActivity().getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    EddystoneScanContext getEddystoneScanContext() {
        return eddystoneScanContext;
    }

    @Override
    IBeaconScanContext getIBeaconScanContext() {
        return null;
    }

    @Override
    BaseRangeAdapter getAdapter() {
        return new EddystoneRangeAdapter(getContext());
    }

    @Override
    public int getTitle() {
        return R.string.range_eddystone;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }
}
