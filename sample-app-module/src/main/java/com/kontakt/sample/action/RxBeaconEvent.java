package com.kontakt.sample.action;

import android.support.annotation.Nullable;

import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.common.util.SDKOptional;

public class RxBeaconEvent {

    public enum Type {
        SCAN_START,
        SCAN_STOP,
        BLUETOOTH_EVENT
    }

    private SDKOptional<BluetoothDeviceEvent> bluetoothDeviceEventSDKOptional;

    private Type type;

    public RxBeaconEvent(Type type, @Nullable BluetoothDeviceEvent bluetoothDeviceEvent) {
        this.type = type;
        bluetoothDeviceEventSDKOptional = bluetoothDeviceEvent == null ? SDKOptional.<BluetoothDeviceEvent>absent() : SDKOptional.of(bluetoothDeviceEvent);
    }


    public Type getType() {
        return type;
    }

    public boolean hasBluetoothDeviceEvent() {
        return bluetoothDeviceEventSDKOptional.isPresent();
    }

    public BluetoothDeviceEvent getBluetoothDeviceEvent() {
        return bluetoothDeviceEventSDKOptional.get();
    }

    @Override
    public String toString() {
        return "RxBeaconEvent{" +
                "type=" + type +
                '}';
    }
}
