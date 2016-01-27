package com.kontakt.sample.receiver;

import android.content.Context;

public final class BackgroundScanReceiver extends AbstractScanBroadcastReceiver {

    @Override
    protected AbstractBroadcastInterceptor createBroadcastHandler(Context context) {
        return new NotificationBroadcastInterceptor(context);
    }
}
