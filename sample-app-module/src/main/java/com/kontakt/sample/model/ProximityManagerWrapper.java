package com.kontakt.sample.model;

import com.kontakt.sdk.android.ble.manager.ProximityManager;

public class ProximityManagerWrapper {

    private ProximityManager proximityManager;
    private int distance;

    private int foundBeacons;

    public ProximityManagerWrapper(int distance, ProximityManager proximityManager) {
        this.distance = distance;
        this.proximityManager = proximityManager;
    }

    public void setFoundBeacons(int foundBeacons) {
        this.foundBeacons = foundBeacons;
    }

    public ProximityManager getProximityManager() {
        return proximityManager;
    }

    public int getDistance() {
        return distance;
    }

    public int getFoundBeacons() {
        return foundBeacons;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o instanceof ProximityManagerWrapper) {
            ProximityManagerWrapper proximityManagerWrapper = (ProximityManagerWrapper) o;
            return proximityManagerWrapper.getProximityManager().equals(getProximityManager())
                    && proximityManagerWrapper.getDistance() == getDistance();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 53;
        hash += proximityManager.hashCode();
        hash += 701 * distance;
        return hash;
    }

    public void disconnect() {
        proximityManager.disconnect();
    }
}
