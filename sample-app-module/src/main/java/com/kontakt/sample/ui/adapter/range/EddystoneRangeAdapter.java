package com.kontakt.sample.ui.adapter.range;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.adapter.viewholder.EddystoneItemViewHolder;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;

import java.util.List;

public class EddystoneRangeAdapter extends BaseRangeAdapter<IEddystoneDevice> {

    public EddystoneRangeAdapter(Context context) {
        super(context);
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
    public IEddystoneDevice getItem(int position) {
        return devices.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = getTheSameOrInflate(convertView, parent);

        final EddystoneItemViewHolder viewHolder = (EddystoneItemViewHolder) convertView.getTag();

        final IEddystoneDevice eddystoneDevice = getItem(position);
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
        return convertView;
    }

    @Override
    public void replaceWith(List<IEddystoneDevice> devices) {
        this.devices.clear();
        this.devices.addAll(devices);
        notifyDataSetChanged();
    }

    @Override
    View inflate(ViewGroup parent) {
        View view = inflater.inflate(R.layout.eddystone_list_row, parent, false);
        EddystoneItemViewHolder viewHolder = new EddystoneItemViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

}
