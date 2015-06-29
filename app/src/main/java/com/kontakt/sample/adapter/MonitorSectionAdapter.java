package com.kontakt.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.kontakt.sample.R;
import com.kontakt.sdk.android.device.BeaconDevice;
import com.kontakt.sdk.android.device.Region;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitorSectionAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<Region> headersList;
    private final Map<Region, List<BeaconDevice>> childMap;

    public MonitorSectionAdapter(final Context context) {
        this.context = context;
        headersList = new ArrayList<>();
        childMap = new HashMap<>();
    }

    public void addGroup(final Region beaconRegion) {
        headersList.add(beaconRegion);
        childMap.put(beaconRegion, new ArrayList<BeaconDevice>());
        notifyDataSetChanged();
    }

    public int getGroupIndex(final Region beaconRegion) {
        return headersList.indexOf(beaconRegion);
    }

    public void replaceChildren(final int groupPosition, final List<BeaconDevice> devices) {
        final List<BeaconDevice> beacons = childMap.get(getGroup(groupPosition));
        beacons.clear();
        beacons.addAll(devices);
        notifyDataSetChanged();
    }

    public boolean addOrReplaceChild(final int groupPosition, final BeaconDevice device) {
        final Region region = (Region) getGroup(groupPosition);
        final List<BeaconDevice> devices = childMap.get(region);
        final int index = devices.indexOf(device);
        boolean state;

        if(index == -1) {
            devices.add(device);
            state = true;
        } else {
            devices.set(index, device);
            state = false;
        }

        notifyDataSetChanged();
        return state;
    }

    @Override
    public int getGroupCount() {
        return headersList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childMap.get(headersList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headersList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childMap.get(headersList.get(groupPosition)).get(childPosition);
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

    public void clear() {
        headersList.clear();
        childMap.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final Region region = (Region) getGroup(groupPosition);
        if(convertView == null) {
            convertView = createView(R.layout.monitor_section_list_header);
            GroupViewHolder groupViewHolder = new GroupViewHolder(convertView);
            convertView.setTag(groupViewHolder);
        }

        final GroupViewHolder groupViewHolder = (GroupViewHolder) convertView.getTag();
        groupViewHolder.header.setText(region.getIdentifier());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final BeaconDevice device = (BeaconDevice) getChild(groupPosition, childPosition);

        if(convertView == null) {
            convertView = createView(R.layout.beacon_list_row);
            final ChildViewHolder childViewHolder = new ChildViewHolder(convertView);
            convertView.setTag(childViewHolder);
        }

        final ChildViewHolder childViewHolder = (ChildViewHolder) convertView.getTag();
        childViewHolder.nameTextView.setText(String.format("%s: %s (%s)", device.getName(),
                device.getAddress(),
                new DecimalFormat("#.##").format(device.getAccuracy())));
        childViewHolder.proximityUUIDTextView.setText(String.format("Proximity UUID: %s", device.getProximityUUID().toString()));
        childViewHolder.majorTextView.setText(String.format("Major: %d", device.getMajor()));
        childViewHolder.minorTextView.setText(String.format("Minor: %d", device.getMinor()));
        childViewHolder.rssiTextView.setText(String.format("Rssi: %f", device.getRssi()));
        childViewHolder.txPowerTextView.setText(String.format("Tx Power: %d", device.getTxPower()));
        childViewHolder.beaconUniqueIdTextView.setText(String.format("Beacon Unique Id: %s", device.getUniqueId()));
        childViewHolder.firmwareVersionTextView.setText(String.format("Firmware version: %d", device.getFirmwareVersion()));
        childViewHolder.proximityTextView.setText(String.format("Proximity: %s", device.getProximity()));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private View createView(final int viewId) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(viewId, null);
    }

    public void removeGroup(Region beaconRegion) {
        headersList.remove(beaconRegion);
        childMap.remove(beaconRegion);
    }

    public boolean containsGroup(Region beaconRegion) {
        return headersList.contains(beaconRegion);
    }

    static  class GroupViewHolder {
        GroupViewHolder(View view) {
            header = (TextView) view.findViewById(R.id.header);
        }
        TextView header;
    }

    static class ChildViewHolder {
        final TextView nameTextView;
        final TextView majorTextView;
        final TextView minorTextView;
        final TextView txPowerTextView;
        final TextView rssiTextView;
        final TextView proximityUUIDTextView;
        final TextView beaconUniqueIdTextView;
        final TextView firmwareVersionTextView;
        final TextView proximityTextView;

        ChildViewHolder(View view) {
            nameTextView = (TextView) view.findViewById(R.id.device_name);
            majorTextView = (TextView) view.findViewById(R.id.major);
            minorTextView = (TextView)  view.findViewById(R.id.minor);
            txPowerTextView = (TextView) view.findViewById(R.id.power);
            rssiTextView = (TextView) view.findViewById(R.id.rssi);
            proximityUUIDTextView = (TextView) view.findViewById(R.id.proximity_uuid);
            beaconUniqueIdTextView = (TextView) view.findViewById(R.id.beacon_unique_id);
            firmwareVersionTextView = (TextView) view.findViewById(R.id.firmware_version);
            proximityTextView = (TextView) view.findViewById(R.id.proximity);
        }
    }
}
