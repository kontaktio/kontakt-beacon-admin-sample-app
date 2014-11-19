package com.kontakt.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kontakt.sample.R;
import com.kontakt.sdk.android.device.Beacon;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class BeaconBaseAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<Beacon> beacons;

    public BeaconBaseAdapter(final Context context) {
        layoutInflater = LayoutInflater.from(context);
        beacons = new ArrayList<Beacon>();
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

        final Beacon beacon = (Beacon) getItem(position);

        viewHolder.macTextView.setText(String.format("%s: %s(%s)", beacon.getName(),
                beacon.getMacAddress(),
                new DecimalFormat("#.##").format(beacon.getAccuracy())));
        viewHolder.majorTextView.setText(String.format("Major : %d", beacon.getMajor()));
        viewHolder.minorTextView.setText(String.format("Minor : %d", beacon.getMinor()));
        viewHolder.rssiTextView.setText(String.format("Rssi : %f", beacon.getRssi()));
        viewHolder.txPowerTextView.setText(String.format("Tx Power : %d", beacon.getTxPower()));
        viewHolder.proximityTextView.setText(String.format("Proximity: %s", beacon.getProximity().name()));

        return convertView;
    }

    public void replaceWith(final List<Beacon> beacons) {
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
        final TextView macTextView;
        final TextView majorTextView;
        final TextView minorTextView;
        final TextView txPowerTextView;
        final TextView rssiTextView;
        final TextView proximityTextView;

        ViewHolder(View view) {
            macTextView = (TextView) view.findViewWithTag("mac");
            majorTextView = (TextView) view.findViewWithTag("major");
            minorTextView = (TextView) view.findViewWithTag("minor");
            txPowerTextView = (TextView) view.findViewWithTag("mpower");
            rssiTextView = (TextView) view.findViewWithTag("rssi");
            proximityTextView = (TextView) view.findViewWithTag("proximity");
        }
    }
}
