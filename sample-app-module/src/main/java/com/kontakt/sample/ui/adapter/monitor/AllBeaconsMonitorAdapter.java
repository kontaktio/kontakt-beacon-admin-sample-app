package com.kontakt.sample.ui.adapter.monitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.adapter.viewholder.EddystoneItemViewHolder;
import com.kontakt.sample.ui.adapter.viewholder.GroupViewHolder;
import com.kontakt.sample.ui.adapter.viewholder.IBeaconItemViewHolder;
import com.kontakt.sample.model.AllBeaconWrapper;
import com.kontakt.sdk.android.common.profile.DeviceProfile;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllBeaconsMonitorAdapter extends BaseExpandableListAdapter {

    private Context context;

    private List<DeviceProfile> groupList;
    private Map<DeviceProfile, List<AllBeaconWrapper>> childMap;

    public AllBeaconsMonitorAdapter(Context context) {
        this.context = context;
        groupList = new ArrayList<>();
        childMap = new HashMap<>();
        createGroups();
    }

    private void createGroups() {
        groupList.add(DeviceProfile.IBEACON);
        groupList.add(DeviceProfile.EDDYSTONE);
        childMap.put(DeviceProfile.IBEACON, new ArrayList<AllBeaconWrapper>());
        childMap.put(DeviceProfile.EDDYSTONE, new ArrayList<AllBeaconWrapper>());
    }

    public void replaceIBeacons(List<IBeaconDevice> iBeacons) {
        List<AllBeaconWrapper> beaconWrappers = childMap.get(DeviceProfile.IBEACON);
        beaconWrappers.clear();
        for (IBeaconDevice iBeacon : iBeacons) {
            beaconWrappers.add(new AllBeaconWrapper(null, iBeacon, DeviceProfile.IBEACON));
        }
        notifyDataSetChanged();
    }

    public void replaceEddystoneBeacons(List<IEddystoneDevice> eddystoneDevices) {
        List<AllBeaconWrapper> eddystonesWrappers = childMap.get(DeviceProfile.EDDYSTONE);
        eddystonesWrappers.clear();
        for (IEddystoneDevice eddystoneDevice : eddystoneDevices) {
            eddystonesWrappers.add(new AllBeaconWrapper(eddystoneDevice, null, DeviceProfile.EDDYSTONE));
        }
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childMap.get(getGroup(groupPosition)).size();
    }

    @Override
    public DeviceProfile getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public AllBeaconWrapper getChild(int groupPosition, int childPosition) {
        return childMap.get(getGroup(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        DeviceProfile group = getGroup(groupPosition);
        if (convertView == null) {
            convertView = createHeader();
        }

        setHeaderTitle(group.name(), convertView, groupPosition);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        AllBeaconWrapper child = getChild(groupPosition, childPosition);
        if (DeviceProfile.IBEACON == child.getDeviceProfile()) {
            if (convertView == null || (!(convertView.getTag() instanceof IBeaconItemViewHolder))) {
                convertView = createView(R.layout.beacon_list_row);
                final IBeaconItemViewHolder childViewHolder = new IBeaconItemViewHolder(convertView);
                convertView.setTag(childViewHolder);
            }
            IBeaconDevice device = child.getBeaconDevice();
            final IBeaconItemViewHolder childViewHolder = (IBeaconItemViewHolder) convertView.getTag();
            childViewHolder.nameTextView.setText(String.format("%s: %s (%s)", device.getName(),
                    device.getAddress(),
                    new DecimalFormat("#.##").format(device.getDistance())));
            childViewHolder.proximityUUIDTextView.setText(String.format("Proximity UUID: %s", device.getProximityUUID().toString()));
            childViewHolder.majorTextView.setText(String.format("Major: %d", device.getMajor()));
            childViewHolder.minorTextView.setText(String.format("Minor: %d", device.getMinor()));
            childViewHolder.rssiTextView.setText(String.format("Rssi: %f", device.getRssi()));
            childViewHolder.txPowerTextView.setText(String.format("Tx Power: %d", device.getTxPower()));
            childViewHolder.beaconUniqueIdTextView.setText(String.format("Beacon Unique Id: %s", device.getUniqueId()));
            childViewHolder.firmwareVersionTextView.setText(String.format("Firmware version: %s", device.getFirmwareVersion()));
            childViewHolder.proximityTextView.setText(String.format("Proximity: %s", device.getProximity()));
        } else if (DeviceProfile.EDDYSTONE == child.getDeviceProfile()) {
            if (convertView == null || (!(convertView.getTag() instanceof EddystoneItemViewHolder))) {
                convertView = createView(R.layout.eddystone_list_row);
                EddystoneItemViewHolder childViewHolder = new EddystoneItemViewHolder(convertView);
                convertView.setTag(childViewHolder);
            }
            IEddystoneDevice eddystoneDevice = child.getEddystoneDevice();
            EddystoneItemViewHolder viewHolder = (EddystoneItemViewHolder) convertView.getTag();
            Context context = convertView.getContext();
            viewHolder.namespace.setText(context.getString(R.string.namespace, eddystoneDevice.getNamespaceId()));
            viewHolder.instance.setText(context.getString(R.string.instance, eddystoneDevice.getInstanceId()));
            viewHolder.url.setText(context.getString(R.string.url, eddystoneDevice.getUrl()));
            viewHolder.txPowerTextView.setText(context.getString(R.string.tx_power_level, eddystoneDevice.getTxPower()));
            viewHolder.temperature.setText(context.getString(R.string.temperature, eddystoneDevice.getTemperature()));
            viewHolder.batteryVoltage.setText(context.getString(R.string.battery_voltage, eddystoneDevice.getBatteryVoltage()));
            viewHolder.pduCount.setText(context.getString(R.string.pdu_count, eddystoneDevice.getPduCount()));
            viewHolder.timeSincePowerUp.setText(context.getString(R.string.time_since_power_up, eddystoneDevice.getTimeSincePowerUp()));
            viewHolder.telemetryVersion.setText(context.getString(R.string.telemetry_version, eddystoneDevice.getTelemetryVersion()));

        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    View createView(final int viewId) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(viewId, null);
    }


    protected View createHeader() {
        View convertView = createView(R.layout.monitor_section_list_header);
        GroupViewHolder groupViewHolder = new GroupViewHolder(convertView);
        convertView.setTag(groupViewHolder);
        return convertView;
    }

    void setHeaderTitle(String title, View convertView, int groupPosition) {
        final GroupViewHolder groupViewHolder = (GroupViewHolder) convertView.getTag();
        groupViewHolder.header.setText(title + "( " + getChildrenCount(groupPosition) + " )");
    }

}

