package com.kontakt.sample.adapter.monitor;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kontakt.sample.R;
import com.kontakt.sdk.android.ble.device.BeaconDevice;
import com.kontakt.sdk.android.ble.device.BeaconRegion;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.text.DecimalFormat;

public class IBeaconMonitorAdapter extends BaseMonitorAdapter<IBeaconRegion, IBeaconDevice> {


    public IBeaconMonitorAdapter(final Context context) {
        super(context);
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
        final BeaconRegion region = (BeaconRegion) getGroup(groupPosition);
        if (convertView == null) {
            convertView = createHeader();
        }
        setHeaderTitle(region.getName(), convertView);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final BeaconDevice device = (BeaconDevice) getChild(groupPosition, childPosition);

        if (convertView == null) {
            convertView = createView(R.layout.beacon_list_row);
            final ChildViewHolder childViewHolder = new ChildViewHolder(convertView);
            convertView.setTag(childViewHolder);
        }

        final ChildViewHolder childViewHolder = (ChildViewHolder) convertView.getTag();
        childViewHolder.nameTextView.setText(String.format("%s: %s (%s)", device.getName(),
                device.getAddress(),
                new DecimalFormat("#.##").format(device.getDistance())));
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
            minorTextView = (TextView) view.findViewById(R.id.minor);
            txPowerTextView = (TextView) view.findViewById(R.id.power);
            rssiTextView = (TextView) view.findViewById(R.id.rssi);
            proximityUUIDTextView = (TextView) view.findViewById(R.id.proximity_uuid);
            beaconUniqueIdTextView = (TextView) view.findViewById(R.id.beacon_unique_id);
            firmwareVersionTextView = (TextView) view.findViewById(R.id.firmware_version);
            proximityTextView = (TextView) view.findViewById(R.id.proximity);
        }
    }
}
