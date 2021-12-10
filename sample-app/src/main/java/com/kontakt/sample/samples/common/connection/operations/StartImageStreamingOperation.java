package com.kontakt.sample.samples.common.connection.operations;

import com.kontakt.sdk.android.ble.connection.KontaktDeviceConnection;
import com.kontakt.sdk.android.ble.image_streaming.ImageStreamingListener;
import com.kontakt.sdk.android.ble.image_streaming.RecognitionBox;
import com.kontakt.sdk.android.cloud.KontaktCloud;

import java.util.List;

public class StartImageStreamingOperation implements Operation {

    private KontaktDeviceConnection connection;
    private final KontaktCloud cloud;
    private final ImageStreamingListener listener;

    public StartImageStreamingOperation(KontaktCloud cloud, ImageStreamingListener listener) {
        this.cloud = cloud;
        this.listener = listener;
    }

    @Override
    public void execute(KontaktDeviceConnection connection) {
        this.connection = connection;
        this.connection.startImageStreaming(cloud, internalImageStreamingListener);
    }

    @Override
    public void onError(int errorCode) {
        internalImageStreamingListener.onError("Error opening connection: code: " + errorCode);
    }

    private final ImageStreamingListener internalImageStreamingListener = new ImageStreamingListener() {
        @Override
        public void onImage(int[][] ints, List<RecognitionBox> boxes) {
            listener.onImage(ints, boxes);
        }

        @Override
        public void startedStreaming() {
            listener.startedStreaming();
        }

        @Override
        public void onError(String msg) {
            if(connection != null) connection.close();
            listener.onError(msg);
        }
    };
}