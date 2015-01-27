package com.kontakt.sample.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kontakt.sample.broadcast.AbstractBroadcastHandler;

public abstract class AbstractScanBroadcastReceiver extends BroadcastReceiver{

    private AbstractBroadcastHandler broadcastHandler = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        abortBroadcast();

        if(broadcastHandler == null) {
            broadcastHandler = createBroadcastHandler(context);
        }

        broadcastHandler.handle(intent);
    }

    protected abstract AbstractBroadcastHandler createBroadcastHandler(final Context context);
}
