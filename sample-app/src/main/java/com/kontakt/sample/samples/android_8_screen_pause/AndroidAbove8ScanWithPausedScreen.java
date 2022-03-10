package com.kontakt.sample.samples.android_8_screen_pause;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kontakt.sample.R;
import com.kontakt.sdk.android.ble.configuration.KontaktScanFilter;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.kontakt.sample.samples.android_8_screen_pause.ShowNotificationService.ACTION_CANCEL_EXISTING_NOTIFICATION;
import static com.kontakt.sample.samples.android_8_screen_pause.ShowNotificationService.ACTION_SHOW_START_NOTIFICATION;
import static com.kontakt.sample.samples.android_8_screen_pause.ShowNotificationService.NOTIFICATION_CONTENT_KEY;
import static com.kontakt.sample.samples.android_8_screen_pause.ShowNotificationService.NOTIFICATION_ID;

public class AndroidAbove8ScanWithPausedScreen extends AppCompatActivity implements View.OnClickListener {

    public static Intent createIntent(@NonNull Context context) {
        return new Intent(context, AndroidAbove8ScanWithPausedScreen.class);
    }

    public static final String TAG = AndroidAbove8ScanWithPausedScreen.class.getSimpleName();

    private ServiceConnection serviceConnection;
    private Messenger serviceMessenger;
    private Messenger activityMessenger = new Messenger(new MyHandler(new WeakReference<>(this)));
    private ShowNotificationService.ServiceBinder serviceBinder;

    private ProximityManager proximityManager;
    private ProgressBar progressBar;

    private class MyHandler extends Handler {

        private final WeakReference<AndroidAbove8ScanWithPausedScreen> activityReference;

        private MyHandler(WeakReference<AndroidAbove8ScanWithPausedScreen> activityReference) {
            this.activityReference = activityReference;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == ACTION_SHOW_START_NOTIFICATION) {
                setupFilters(serviceBinder.getLastNotification());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_above8_scan_with_paused_screen);
        progressBar = (ProgressBar) findViewById(R.id.scanning_progress);

        //Setup Toolbar
        setupToolbar();

        //Setup buttons
        setupButtons();

        //Initialize and configure proximity manager
        setupProximityManager();
        bindNotificationService();
    }

    private void bindNotificationService() {
        Log.d(TAG, "Binding ShowNotificatinoService");
        Intent serviceIntent = new Intent(this, ShowNotificationService.class);
        setupServiceConnection();
        boolean isServiceBoundCorrectly = bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        if(!isServiceBoundCorrectly){
            String message =
                    "Showing notifications is not possible at the moment, " +
                            "restart application if you want this feature to work";
            Log.e(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void setupServiceConnection() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                serviceBinder = (ShowNotificationService.ServiceBinder) iBinder;
                serviceMessenger = serviceBinder.getMessenger();
                showNotification();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "Show Notification Service disconnected.");
            }
        };
    }

    private void showNotification() {
        Message message = Message.obtain(null, ACTION_SHOW_START_NOTIFICATION);
        Bundle data = new Bundle();
        data.putString(NOTIFICATION_CONTENT_KEY, "Starting scan");
        message.setData(data);
        message.replyTo = activityMessenger;
        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setupToolbar() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupButtons() {
        Button startScanButton = (Button) findViewById(R.id.start_scan_button);
        Button stopScanButton = (Button) findViewById(R.id.stop_scan_button);
        startScanButton.setOnClickListener(this);
        stopScanButton.setOnClickListener(this);
        enableButtons(false);
    }

    private void enableButtons(boolean enable) {
        Button startScanButton = (Button) findViewById(R.id.start_scan_button);
        Button stopScanButton = (Button) findViewById(R.id.stop_scan_button);
        startScanButton.setEnabled(enable);
        stopScanButton.setEnabled(enable);
    }

    private void setupProximityManager() {
        proximityManager = ProximityManagerFactory.create(this);

        //Configure proximity manager basic options
        proximityManager.configuration()
                //Using ranging for continuous scanning or MONITORING for scanning with intervals
                .scanPeriod(ScanPeriod.RANGING)
                //Using BALANCED for best performance/battery ratio
                .scanMode(ScanMode.BALANCED)
                //OnDeviceUpdate callback will be received with 5 seconds interval
                .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(5));

        //Setting up iBeacon  listeners
        proximityManager.setIBeaconListener(createIBeaconListener());
    }

    private void setupFilters(Notification notification) {
        proximityManager.configuration().kontaktScanFilters(Collections.singletonList(KontaktScanFilter.IBEACON_MANUFACTURER_DATA_FILTER)); //todo - try commenting out this line and see what happens on Androids 8 and higher.
        proximityManager.setForegroundNotification(notification, NOTIFICATION_ID);
        enableButtons(true);
    }

    private void startScanning() {
        //Connect to scanning service and start scanning when ready
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                //Check if proximity manager is already scanning
                if (proximityManager.isScanning()) {
                    Toast.makeText(AndroidAbove8ScanWithPausedScreen.this, "Already scanning", Toast.LENGTH_SHORT).show();
                    return;
                }
                proximityManager.startScanning();
                progressBar.setVisibility(View.VISIBLE);
                Toast.makeText(AndroidAbove8ScanWithPausedScreen.this, "Scanning started", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void stopScanning() {
        //Stop scanning if scanning is in progress
        if (proximityManager.isScanning()) {
            proximityManager.stopScanning();
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Scanning stopped", Toast.LENGTH_SHORT).show();
        }
    }

    private IBeaconListener createIBeaconListener() {
        return new IBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice iBeacon, IBeaconRegion region) {
                Log.i(TAG, "onIBeaconDiscovered: " + iBeacon.toString());
            }

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> iBeacons, IBeaconRegion region) {
                Log.i(TAG, "onIBeaconsUpdated: " + iBeacons.size());
            }

            @Override
            public void onIBeaconLost(IBeaconDevice iBeacon, IBeaconRegion region) {
                Log.e(TAG, "onIBeaconLost: " + iBeacon.toString());
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_scan_button:
                startScanning();
                break;
            case R.id.stop_scan_button:
                stopScanning();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        //Remember to disconnect when finished.
        if(serviceConnection != null) unbindService(serviceConnection);

        if(serviceMessenger != null) {
            try {
                serviceMessenger.send(Message.obtain(null, ACTION_CANCEL_EXISTING_NOTIFICATION));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        proximityManager.clearForegroundNotification();
        proximityManager.disconnect();
        super.onDestroy();
    }
}