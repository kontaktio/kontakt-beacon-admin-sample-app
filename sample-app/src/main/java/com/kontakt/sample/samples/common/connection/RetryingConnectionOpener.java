package com.kontakt.sample.samples.common.connection;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.kontakt.sdk.android.ble.connection.DeviceConnectionError;
import com.kontakt.sdk.android.ble.connection.KontaktDeviceConnection;
import com.kontakt.sdk.android.ble.connection.KontaktDeviceConnectionFactory;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;

import java.util.concurrent.TimeUnit;

public class RetryingConnectionOpener<Connection extends KontaktDeviceConnection> {

    private static final String TAG = RetryingConnectionOpener.class.getSimpleName();

    private static final long CONNECTION_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(20);
    private static final long RETRY_TIMEOUT = TimeUnit.SECONDS.toMillis(2);
    private static final int MAX_RETRIES = 3;

    private RemoteBluetoothDevice device;
    private Connection kontaktDeviceConnection;
    private int connectionRetries;

    private final Context context;
    private final OperationListener<Connection> operation;
    private final Handler handler;

    public RetryingConnectionOpener(Context context, OperationListener<Connection> operationListener) {
        this.context = context.getApplicationContext();
        this.operation = operationListener;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void closeConnection() {
        if (kontaktDeviceConnection != null) {
            kontaktDeviceConnection.close();
            kontaktDeviceConnection = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    public void onDestroy() {
        closeConnection();
        handler.removeCallbacksAndMessages(null);
    }

    public void start(RemoteBluetoothDevice device){
        this.device = device;
        openConnection();
    }

    private void openConnection() {
        if (kontaktDeviceConnection == null) {
            //noinspection unchecked
            kontaktDeviceConnection = (Connection) KontaktDeviceConnectionFactory.create(context, device, connectionListener);
        }
        handler.post(() -> {
            if (isDisconnected()) {
                kontaktDeviceConnection.connect(device);
                handler.postDelayed(connectionTimeoutRunnable, CONNECTION_TIMEOUT_MILLIS);
            } else {
                operation.execute(kontaktDeviceConnection);
                resetRetriesCounter();
            }
        });
    }

    private boolean isDisconnected() {
        return device.getAddress() != null && (!kontaktDeviceConnection.isConnected() || kontaktDeviceConnection.isClosed());
    }

    private void resetRetriesCounter() {
        connectionRetries = 0;
    }

    private final KontaktDeviceConnection.ConnectionListener connectionListener = new KontaktDeviceConnection.ConnectionListener() {
        @Override
        public void onConnectionOpened() {
            handler.removeCallbacks(connectionTimeoutRunnable);
        }

        @Override
        public void onConnected() {
            handler.removeCallbacks(connectionTimeoutRunnable);
            operation.execute(kontaktDeviceConnection);
            resetRetriesCounter();
        }

        @Override
        public void onErrorOccured(int errorCode) {
            handler.removeCallbacks(connectionTimeoutRunnable);

            // Retry connection before throwing an error
            if (connectionRetries < MAX_RETRIES) {
                connectionRetries++;
                closeConnection();
                handler.postDelayed(RetryingConnectionOpener.this::openConnection, RETRY_TIMEOUT);
                Log.w(TAG, "onErrorOccured. Retrying: " + connectionRetries);
                return;
            }
            if (DeviceConnectionError.isGattError(errorCode)) {
                operation.onError(DeviceConnectionError.getGattError(errorCode));
            } else {
                operation.onError(errorCode);
            }
            resetRetriesCounter();
            closeConnection();
        }

        @Override
        public void onDisconnected() {
            handler.removeCallbacks(connectionTimeoutRunnable);
            closeConnection();
            Log.d(TAG, "On disconnected");
            operation.onError(666);
        }
    };

    private final Runnable connectionTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            operation.onTimeout();
        }
    };

    public interface OperationListener<Connection extends KontaktDeviceConnection> {

        void execute(Connection connection);

        void onTimeout();

        void onError(int errorCode);
    }

}
