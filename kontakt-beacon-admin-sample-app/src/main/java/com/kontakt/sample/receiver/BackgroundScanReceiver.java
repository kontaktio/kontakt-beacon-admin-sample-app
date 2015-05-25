package com.kontakt.sample.receiver;

import android.content.Context;

import com.kontakt.sample.broadcast.AbstractBroadcastInterceptor;
import com.kontakt.sample.broadcast.NotificationBroadcastInterceptor;

public final class BackgroundScanReceiver extends AbstractScanBroadcastReceiver {

    @Override
    protected AbstractBroadcastInterceptor createBroadcastHandler(Context context) {
        return new NotificationBroadcastInterceptor(context);
    }
}
