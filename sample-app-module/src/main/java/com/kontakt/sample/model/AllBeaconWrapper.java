package com.kontakt.sample.model;

import com.kontakt.sdk.android.common.profile.DeviceProfile;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;

public class AllBeaconWrapper {
    private IEddystoneDevice eddystoneDevice;
    private IBeaconDevice beaconDevice;
    private DeviceProfile deviceProfile;

    public AllBeaconWrapper(IEddystoneDevice eddystoneDevice, IBeaconDevice beaconDevice, DeviceProfile deviceProfile) {
        this.eddystoneDevice = eddystoneDevice;
        this.beaconDevice = beaconDevice;
        this.deviceProfile = deviceProfile;
    }

    public DeviceProfile getDeviceProfile() {
        return deviceProfile;
    }

    public IBeaconDevice getBeaconDevice() {
        return beaconDevice;
    }

    public IEddystoneDevice getEddystoneDevice() {
        return eddystoneDevice;
    }
}
