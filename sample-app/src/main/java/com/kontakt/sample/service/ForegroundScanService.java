package com.kontakt.sample.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;

public class ForegroundScanService extends Service {

  public static final String TAG = ForegroundScanService.class.getSimpleName();

  public static final String ACTION_DEVICE_DISCOVERED = "DEVICE_DISCOVERED_ACTION";
  public static final String EXTRA_DEVICE = "DeviceExtra";
  public static final String EXTRA_DEVICES_COUNT = "DevicesCountExtra";

  private static final String STOP_SERVICE_ACTION = "STOP_SERVICE_ACTION";

  private static final String NOTIFICATION_CHANEL_NAME = "Kontakt SDK Samples";
  private static final String NOTIFICATION_CHANEL_ID = "scanning_service_channel_id";

  private ProximityManager proximityManager;
  private boolean isRunning; // Flag indicating if service is already running.
  private int devicesCount; // Total discovered devices count

  public static Intent createIntent(final Context context) {
    return new Intent(context, ForegroundScanService.class);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    setupProximityManager();
    isRunning = false;
  }

  private void setupProximityManager() {
    // Create proximity manager instance
    proximityManager = ProximityManagerFactory.create(this);

    // Configure proximity manager basic options
    proximityManager.configuration()
        //Using ranging for continuous scanning or MONITORING for scanning with intervals
        .scanPeriod(ScanPeriod.RANGING)
        //Using BALANCED for best performance/battery ratio
        .scanMode(ScanMode.BALANCED);

    // Set up iBeacon listener
    proximityManager.setIBeaconListener(createIBeaconListener());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (STOP_SERVICE_ACTION.equals(intent.getAction())) {
      stopSelf();
      return START_NOT_STICKY;
    }

    // Check if service is already active
    if (isRunning) {
      Toast.makeText(this, "Service is already running.", Toast.LENGTH_SHORT).show();
      return START_STICKY;
    }

    startInForeground();
    startScanning();
    isRunning = true;
    return START_STICKY;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onDestroy() {
    if (proximityManager != null) {
      proximityManager.disconnect();
      proximityManager = null;
    }
    Toast.makeText(this, "Scanning service stopped.", Toast.LENGTH_SHORT).show();
    super.onDestroy();
  }

  private void startInForeground() {
    // Create notification intent
    final Intent notificationIntent = new Intent();
    final PendingIntent pendingIntent = PendingIntent.getActivity(
        this,
        0,
        notificationIntent,
        0
    );

    // Create stop intent with action
    final Intent intent = ForegroundScanService.createIntent(this);
    intent.setAction(STOP_SERVICE_ACTION);
    final PendingIntent stopIntent = PendingIntent.getService(
        this,
        0,
        intent,
        PendingIntent.FLAG_CANCEL_CURRENT
    );

    // Create notification channel
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createNotificationChannel();
    }

    // Build notification
    final NotificationCompat.Action action = new NotificationCompat.Action(0, "Stop", stopIntent);
    final Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANEL_ID)
        .setContentTitle("Scan service")
        .setContentText("Actively scanning iBeacons")
        .addAction(action)
        .setSmallIcon(android.R.mipmap.sym_def_app_icon)
        .setContentIntent(pendingIntent)
        .build();

    // Start foreground service
    startForeground(1, notification);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void createNotificationChannel() {
    final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    if (notificationManager == null) return;

    final NotificationChannel channel = new NotificationChannel(
        NOTIFICATION_CHANEL_ID,
        NOTIFICATION_CHANEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
    );
    notificationManager.createNotificationChannel(channel);
  }

  private void startScanning() {
    proximityManager.connect(new OnServiceReadyListener() {
      @Override
      public void onServiceReady() {
        proximityManager.startScanning();
        devicesCount = 0;
        Toast.makeText(ForegroundScanService.this, "Scanning service started.", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private IBeaconListener createIBeaconListener() {
    return new SimpleIBeaconListener() {
      @Override
      public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
        onDeviceDiscovered(ibeacon);
        Log.i(TAG, "onIBeaconDiscovered: " + ibeacon.toString());
      }

      @Override
      public void onIBeaconLost(IBeaconDevice ibeacon, IBeaconRegion region) {
        super.onIBeaconLost(ibeacon, region);
        Log.e(TAG, "onIBeaconLost: " + ibeacon.toString());
      }
    };
  }

  private void onDeviceDiscovered(final RemoteBluetoothDevice device) {
    devicesCount++;
    //Send a broadcast with discovered device
    Intent intent = new Intent();
    intent.setAction(ACTION_DEVICE_DISCOVERED);
    intent.putExtra(EXTRA_DEVICE, device);
    intent.putExtra(EXTRA_DEVICES_COUNT, devicesCount);
    sendBroadcast(intent);
  }

}
