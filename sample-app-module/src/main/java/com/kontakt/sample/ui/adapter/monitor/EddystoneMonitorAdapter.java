package com.kontakt.sample.ui.adapter.monitor;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.adapter.viewholder.EddystoneItemViewHolder;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.List;

public class EddystoneMonitorAdapter extends BaseMonitorAdapter<IEddystoneNamespace, List<IEddystoneDevice>> {


    public EddystoneMonitorAdapter(Context context) {
        super(context);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        IEddystoneNamespace namespace = getGroup(groupPosition);
        if (convertView == null) {
            convertView = createHeader();
        }
        setHeaderTitle(namespace.getName(), convertView);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        IEddystoneDevice eddystoneDevice = (IEddystoneDevice) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = createView(R.layout.eddystone_list_row);
            EddystoneItemViewHolder childViewHolder = new EddystoneItemViewHolder(convertView);
            convertView.setTag(childViewHolder);
        }

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

        return convertView;
    }

}
