package com.kontakt.sample.ui.activity;

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
import android.support.v7.widget.Toolbar;

import com.kontakt.sample.R;
import com.kontakt.sample.broadcast.AbstractBroadcastHandler;
import com.kontakt.sample.broadcast.ForegroundBroadcastHandler;
import com.kontakt.sample.receiver.AbstractScanBroadcastReceiver;
import com.kontakt.sample.service.BackgroundScanService;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.util.Logger;
import com.kontakt.sdk.core.util.Preconditions;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class BackgroundScanActivity extends BaseActivity {

    public static final String TAG = BackgroundScanActivity.class.getSimpleName();

    public static final int MESSAGE_START_SCAN = 16;
    public static final int MESSAGE_STOP_SCAN = 25;

    private static final IntentFilter SCAN_INTENT_FILTER;

    static {
        SCAN_INTENT_FILTER = new IntentFilter(BackgroundScanService.BROADCAST);
        SCAN_INTENT_FILTER.setPriority(2);
    }

    private ServiceConnection serviceConnection;

    private Messenger serviceMessenger;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private final BroadcastReceiver scanReceiver = new ForegrondScanReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.background_scan_activity);
        ButterKnife.inject(this);

        setUpActionBar(toolbar);
        setUpActionBarTitle(getString(R.string.foreground_background_scan));

        serviceConnection = createServiceConnection();

        bindServiceAndStartMonitoring();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.cancelNotifications(this, BackgroundScanService.INFO_LIST);
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
        ButterKnife.reset(this);
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
