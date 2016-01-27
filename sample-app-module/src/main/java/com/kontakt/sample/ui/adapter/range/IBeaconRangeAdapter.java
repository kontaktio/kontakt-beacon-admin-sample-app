package com.kontakt.sample.ui.adapter.range;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.adapter.viewholder.IBeaconItemViewHolder;
import com.kontakt.sdk.android.ble.device.BeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;

import java.text.DecimalFormat;
import java.util.List;

public class IBeaconRangeAdapter extends BaseRangeAdapter<IBeaconDevice> {

    public IBeaconRangeAdapter(final Context context) {
        super(context);
    }

    @Override
    public IBeaconDevice getItem(int position) {
        return devices.get(position);
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = getTheSameOrInflate(convertView, parent);
        final IBeaconItemViewHolder viewHolder = (IBeaconItemViewHolder) convertView.getTag();

        final BeaconDevice beacon = (BeaconDevice) getItem(position);

        viewHolder.nameTextView.setText(String.format("%s: %s(%s)", beacon.getName(),
                beacon.getAddress(),
                new DecimalFormat("#.##").format(beacon.getDistance())));
        viewHolder.majorTextView.setText(String.format("Major : %d", beacon.getMajor()));
        viewHolder.minorTextView.setText(String.format("Minor : %d", beacon.getMinor()));
        viewHolder.rssiTextView.setText(String.format("Rssi : %f", beacon.getRssi()));
        viewHolder.txPowerTextView.setText(String.format("Tx Power : %d", beacon.getTxPower()));
        viewHolder.proximityTextView.setText(String.format("Proximity: %s", beacon.getProximity().name()));
        viewHolder.firmwareVersionTextView.setText(String.format("Firmware: %s", beacon.getFirmwareVersion()));
        viewHolder.beaconUniqueIdTextView.setText(String.format("Beacon Unique Id: %s", beacon.getUniqueId()));
        viewHolder.proximityUUIDTextView.setText(String.format("Proximity UUID: %s", beacon.getProximityUUID().toString()));

        return convertView;
    }

    @Override
    public void replaceWith(List<IBeaconDevice> devices) {
        this.devices.clear();
        this.devices.addAll(devices);
        notifyDataSetChanged();
    }

    @Override
    View inflate(ViewGroup parent) {
        View view = inflater.inflate(R.layout.beacon_list_row, parent, false);
        IBeaconItemViewHolder viewHolder = new IBeaconItemViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

}
