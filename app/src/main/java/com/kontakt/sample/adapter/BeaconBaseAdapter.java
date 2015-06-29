package com.kontakt.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kontakt.sample.R;
import com.kontakt.sdk.android.device.BeaconDevice;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BeaconBaseAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<BeaconDevice> beacons;

    public BeaconBaseAdapter(final Context context) {
        layoutInflater = LayoutInflater.from(context);
        beacons = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return beacons.size();
    }

    @Override
    public Object getItem(int position) {
        return beacons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = getTheSameOrInflate(convertView, parent);
        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        final BeaconDevice beacon = (BeaconDevice) getItem(position);

        viewHolder.nameTextView.setText(String.format("%s: %s(%s)", beacon.getName(),
                beacon.getAddress(),
                new DecimalFormat("#.##").format(beacon.getAccuracy())));
        viewHolder.majorTextView.setText(String.format("Major : %d", beacon.getMajor()));
        viewHolder.minorTextView.setText(String.format("Minor : %d", beacon.getMinor()));
        viewHolder.rssiTextView.setText(String.format("Rssi : %f", beacon.getRssi()));
        viewHolder.txPowerTextView.setText(String.format("Tx Power : %d", beacon.getTxPower()));
        viewHolder.proximityTextView.setText(String.format("Proximity: %s", beacon.getProximity().name()));
        viewHolder.firmwareVersionTextView.setText(String.format("Firmware: %d", beacon.getFirmwareVersion()));
        viewHolder.beaconUniqueIdTextView.setText(String.format("Beacon Unique Id: %s", beacon.getUniqueId()));
        viewHolder.proximityUUIDTextView.setText(String.format("Proximity UUID: %s", beacon.getProximityUUID().toString()));

        return convertView;
    }

    public void replaceWith(final List<BeaconDevice> beacons) {
        this.beacons.clear();
        this.beacons.addAll(beacons);
        notifyDataSetChanged();
    }

    private View getTheSameOrInflate(View view, final ViewGroup parent) {
        if(view == null) {
            view = layoutInflater.inflate(R.layout.beacon_list_row, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }

        return view;
    }

    static class ViewHolder {
        final TextView nameTextView;
        final TextView majorTextView;
        final TextView minorTextView;
        final TextView beaconUniqueIdTextView;
        final TextView firmwareVersionTextView;
        final TextView txPowerTextView;
        final TextView rssiTextView;
        final TextView proximityTextView;
        final TextView proximityUUIDTextView;

        ViewHolder(View view) {
            nameTextView = (TextView) view.findViewById(R.id.device_name);
            majorTextView = (TextView) view.findViewById(R.id.major);
            minorTextView = (TextView)  view.findViewById(R.id.minor);
            txPowerTextView = (TextView) view.findViewById(R.id.power);
            rssiTextView = (TextView) view.findViewById(R.id.rssi);
            proximityTextView = (TextView) view.findViewById(R.id.proximity);
            beaconUniqueIdTextView = (TextView) view.findViewById(R.id.beacon_unique_id);
            firmwareVersionTextView = (TextView) view.findViewById(R.id.firmware_version);
            proximityUUIDTextView = (TextView) view.findViewById(R.id.proximity_uuid);
        }
    }
}
