package com.kontakt.sample.samples.common.connection;

import android.content.Context;

import com.kontakt.sample.samples.common.connection.operations.Operation;
import com.kontakt.sample.samples.common.connection.operations.StartImageStreamingOperation;
import com.kontakt.sdk.android.ble.connection.KontaktDeviceConnection;
import com.kontakt.sdk.android.ble.image_streaming.ImageStreamingListener;
import com.kontakt.sdk.android.cloud.KontaktCloud;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;

/**
 * Class is responsible for opening, creating, closing connection with secure devices.
 * It can perform various actions like reading and writing configs. Retry policies are provided in case of errors (like GATT 133 error).
 * Last operation will be retried specified number of times before invoking listener's error callback.
 */
public class Connection {

    private Operation operation;
    private RemoteBluetoothDevice device;

    private final RetryingConnectionOpener<KontaktDeviceConnection> opener;

    public Connection(Context context, ConnectionTimeoutListener connectionTimeoutListener) {
        this.opener = new RetryingConnectionOpener<>(context, operationListener(connectionTimeoutListener));
    }

    private RetryingConnectionOpener.OperationListener<KontaktDeviceConnection> operationListener(ConnectionTimeoutListener connectionTimeoutListener) {
        return new RetryingConnectionOpener.OperationListener<KontaktDeviceConnection>() {
            @Override
            public void execute(KontaktDeviceConnection connection) {
                operation.execute(connection);
            }

            @Override
            public void onTimeout() {
                connectionTimeoutListener.onTimeout();
            }

            @Override
            public void onError(int errorCode) {
                operation.onError(errorCode);
            }
        };
    }

    public void onDestroy() {
        opener.onDestroy();
        operation = null;
    }

    public void closeConnection(){
        opener.closeConnection();
    }

    public void startImageStreaming(RemoteBluetoothDevice device, KontaktCloud cloud, ImageStreamingListener listener){
        this.operation = new StartImageStreamingOperation(cloud, listener);
        this.device = device;
        openConnection();
    }

    private void openConnection() {
        opener.start(device);
    }
}
