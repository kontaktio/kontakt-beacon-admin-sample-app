package com.kontakt.sample.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.kontakt.sample.R;
import com.kontakt.sample.adapter.range.BaseRangeAdapter;
import com.kontakt.sample.adapter.range.EddystoneRangeAdapter;
import com.kontakt.sample.dialog.PasswordDialogFragment;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.common.interfaces.SDKBiConsumer;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;

public class EddystoneBeaconRangeActivity extends BaseBeaconRangeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpActionBarTitle(getString(R.string.range_eddystone));
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

                            final Intent intent = new Intent(EddystoneBeaconRangeActivity.this, EddystoneManagementActivity.class);
                            intent.putExtra(EddystoneManagementActivity.EDDYSTONE_DEVICE, eddystoneDevice);

                            startActivityForResult(intent, REQUEST_CODE_CONNECT_TO_DEVICE);
                        }
                    }).show(getFragmentManager(), "dialog");
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
        return new EddystoneRangeAdapter(this);
    }
}
