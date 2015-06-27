package com.kontakt.sample.receiver;

import android.content.Context;

import com.kontakt.sample.broadcast.AbstractBroadcastHandler;
import com.kontakt.sample.broadcast.NotificationBroadcastHandler;

public final class BackgroundScanReceiver extends AbstractScanBroadcastReceiver {

    @Override
    protected AbstractBroadcastHandler createBroadcastHandler(Context context) {
        return new NotificationBroadcastHandler(context);
    }
}
