package com.kontakt.sample.ui.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.kontakt.sample.R;
import com.kontakt.sample.broadcast.AbstractBroadcastHandler;
import com.kontakt.sample.broadcast.ForegroundBroadcastHandler;
import com.kontakt.sample.receiver.AbstractScanBroadcastReceiver;
import com.kontakt.sample.service.BackgroundScanService;
import com.kontakt.sdk.android.util.Logger;
import com.kontakt.sdk.core.util.Preconditions;

import java.util.List;

public class BackgroundScanActivity extends ActionBarActivity {

    public static final String TAG = BackgroundScanActivity.class.getSimpleName();

    public static final int MESSAGE_START_SCAN = 16;
    public static final int MESSAGE_STOP_SCAN = 25;

    private static final IntentFilter SCAN_INTENT_FILTER;

    private NotificationManager notificationManager;

    private ServiceConnection serviceConnection;

    private Messenger serviceMessenger;

    static {
        SCAN_INTENT_FILTER = new IntentFilter(BackgroundScanService.BROADCAST);
        SCAN_INTENT_FILTER.setPriority(2);
    }

    private final BroadcastReceiver scanReceiver = new ForegrondScanReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.background_scan_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        serviceConnection = createServiceConnection();

        bindServiceAndStartMonitoring();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cancelNotifications();
        registerReceiver(scanReceiver, SCAN_INTENT_FILTER);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(scanReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendMessage(Message.obtain(null, MESSAGE_STOP_SCAN));
        serviceMessenger = null;
        unbindService(serviceConnection);
        serviceConnection = null;
    }

    private void cancelNotifications() {
        final List<Integer> infoList = BackgroundScanService.INFO_LIST;

        for(final Integer info : infoList) {
            notificationManager.cancel(info);
        }
    }

    private void bindServiceAndStartMonitoring() {
        final Intent intent = new Intent(this, BackgroundScanService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection createServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceMessenger = new Messenger(service);

                sendMessage(Message.obtain(null, MESSAGE_START_SCAN));
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    private void sendMessage(final Message message) {
        Preconditions.checkNotNull(serviceMessenger, "ServiceMessenger is null.");
        Preconditions.checkNotNull(message, "Message is null");

        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            Logger.d(TAG, ": message not sent(", message.toString(), ")");
        }
    }

    private static class ForegrondScanReceiver extends AbstractScanBroadcastReceiver {

        @Override
        protected AbstractBroadcastHandler createBroadcastHandler(Context context) {
            return new ForegroundBroadcastHandler(context);
        }
    }
}
