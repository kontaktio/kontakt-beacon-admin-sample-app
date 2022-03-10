package com.kontakt.sample.samples.android_8_screen_pause;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.kontakt.sample.R;

public class ShowNotificationService extends Service {

    private static final String TAG = ShowNotificationService.class.getSimpleName();

    public static final String NOTIFICATION_CONTENT_KEY = "notification_content_key";

    public static final int ACTION_SHOW_START_NOTIFICATION = 1;
    public static final int ACTION_CANCEL_EXISTING_NOTIFICATION = 2;

    private Notification lastNotification;
    public static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "ForegroundChannel";

    private Messenger serviceMessenger;
    private final ServiceBinder serviceBinder;
    private Handler messagingHandler;

    public ShowNotificationService() {
        messagingHandler = new MessagingHandler(this);
        serviceMessenger = new Messenger(messagingHandler);
        serviceBinder = new ServiceBinder(serviceMessenger, this);
    }

    void showNotification(String notificationContent) {
        Log.d(TAG, "Showing notification");
        Context ctx = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = ctx.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
            else{
                Log.w(TAG, "Notification manager was null, notification could not be shown");
            }
        }
        lastNotification = getNotification(ctx, notificationContent);
        NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, lastNotification);
    }

    void cancelNotification(){
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
        else {
            Log.w(TAG, "Warning: tried to cancel notification, but NotificationManager was null");
        }
        lastNotification = null;
    }

    private Notification getNotification(Context ctx, String notificationContent) {
        int flags = 0;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            flags = PendingIntent.FLAG_IMMUTABLE;
        }
        return new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setContentTitle("KontaktWakeUp test")
                .setContentText(notificationContent)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(PendingIntent.getActivity(ctx, 0, new Intent(ctx, ShowNotificationService.class), flags))
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "NOTIFICATION SERVICE: ON DESTROY");
        cancelNotification();
        messagingHandler = null;
        serviceMessenger = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    public static final class ServiceBinder extends Binder {

        private final Messenger messenger;
        private final ShowNotificationService service;

        ServiceBinder(Messenger messenger, ShowNotificationService service){
            this.service = service;
            this.messenger = messenger;
        }

        public Messenger getMessenger(){
            return messenger;
        }

        public Notification getLastNotification() {return service.lastNotification;}

    }

    private static final class MessagingHandler extends Handler {

        private final ShowNotificationService service;

        MessagingHandler(ShowNotificationService abstractBluetoothDeviceService) {
            service = abstractBluetoothDeviceService;
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case ACTION_CANCEL_EXISTING_NOTIFICATION:
                    service.cancelNotification();
                    break;
                case ACTION_SHOW_START_NOTIFICATION:
                    service.showNotification(msg.getData().getString(NOTIFICATION_CONTENT_KEY, "Default content"));
                    try {
                        msg.replyTo.send(Message.obtain(null, msg.what));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
