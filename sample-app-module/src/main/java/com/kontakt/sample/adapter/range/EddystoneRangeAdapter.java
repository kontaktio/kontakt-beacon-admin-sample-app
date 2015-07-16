package com.kontakt.sample.adapter.range;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kontakt.sample.R;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

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

        final ViewHolder viewHolder = (ViewHolder) convertView.getTag();

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
        try {
            this.devices.clear();
            this.devices.addAll(devices);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    View inflate(ViewGroup parent) {
        View view = inflater.inflate(R.layout.eddystone_list_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.power)
        TextView txPowerTextView;
        @InjectView(R.id.namespace_id)
        TextView namespace;
        @InjectView(R.id.instance_id)
        TextView instance;
        @InjectView(R.id.url)
        TextView url;
        @InjectView(R.id.temperature)
        TextView temperature;
        @InjectView(R.id.battery_voltage)
        TextView batteryVoltage;
        @InjectView(R.id.pdu_count)
        TextView pduCount;
        @InjectView(R.id.time_since_power_up)
        TextView timeSincePowerUp;
        @InjectView(R.id.telemetry_version)
        TextView telemetryVersion;

        ViewHolder(View rootView) {
            ButterKnife.inject(this, rootView);
        }
    }
}
