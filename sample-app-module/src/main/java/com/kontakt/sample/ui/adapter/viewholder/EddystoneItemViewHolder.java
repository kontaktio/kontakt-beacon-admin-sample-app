package com.kontakt.sample.ui.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import com.kontakt.sample.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class EddystoneItemViewHolder {
    @InjectView(R.id.power)
    public TextView txPowerTextView;
    @InjectView(R.id.namespace_id)
    public TextView namespace;
    @InjectView(R.id.instance_id)
    public TextView instance;
    @InjectView(R.id.url)
    public TextView url;
    @InjectView(R.id.temperature)
    public TextView temperature;
    @InjectView(R.id.battery_voltage)
    public TextView batteryVoltage;
    @InjectView(R.id.pdu_count)
    public TextView pduCount;
    @InjectView(R.id.time_since_power_up)
    public TextView timeSincePowerUp;
    @InjectView(R.id.telemetry_version)
    public TextView telemetryVersion;

    public EddystoneItemViewHolder(View rootView) {
        ButterKnife.inject(this, rootView);
    }
}
