package com.kontakt.sample.samples.common.connection.operations;

import com.kontakt.sdk.android.ble.connection.KontaktDeviceConnection;

public interface Operation {

    void execute(KontaktDeviceConnection connection);

    void onError(int errorCode);

}
