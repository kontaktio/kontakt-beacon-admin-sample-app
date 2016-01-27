package com.kontakt.sample.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class AbstractScanBroadcastReceiver extends BroadcastReceiver{

    private AbstractBroadcastInterceptor broadcastHandler = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        abortBroadcast();

        if(broadcastHandler == null) {
            broadcastHandler = createBroadcastHandler(context);
        }

        broadcastHandler.handle(intent);
    }

    protected abstract AbstractBroadcastInterceptor createBroadcastHandler(final Context context);
}
