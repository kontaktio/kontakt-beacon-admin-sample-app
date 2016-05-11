package com.kontakt.sample.action;

import android.support.annotation.Nullable;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;
import com.kontakt.sdk.android.common.util.SDKOptional;

public class RxBeaconEvent {

  public enum Type {
    SCAN_START,
    SCAN_STOP,
    IBEACON_DISCOVERED,
    EDDYSTONE_DISCOVERED
  }

  private final SDKOptional<RemoteBluetoothDevice> device;
  private final Type type;

  public RxBeaconEvent(Type type, @Nullable RemoteBluetoothDevice bluetoothDevice) {
    this.type = type;
    this.device = bluetoothDevice == null ? SDKOptional.<RemoteBluetoothDevice>absent() : SDKOptional.of(bluetoothDevice);
  }

  public boolean hasDevice() {
    return device.isPresent();
  }

  public RemoteBluetoothDevice getDevice() {
    return device.get();
  }

  @Override
  public String toString() {
    return "RxBeaconEvent{" +
        "type=" + type +
        '}';
  }
}
