package com.kontakt.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.activity.SimultaneousScanActivity;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProximityManagerAdapter extends ArrayAdapter<SimultaneousScanActivity.ProximityManagerWrapper> {

    public interface RemoveManagerListener {
        void removeManager(SimultaneousScanActivity.ProximityManagerWrapper proximityManager);
    }

    private RemoveManagerListener removeManagerListener;

    public ProximityManagerAdapter(Context context, List<SimultaneousScanActivity.ProximityManagerWrapper> proximityManagerWrappers) {
        super(context, R.layout.proximity_manager_row, proximityManagerWrappers);
    }

    public void setRemoveManagerListener(RemoveManagerListener removeManagerListener) {
        this.removeManagerListener = removeManagerListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            row = LayoutInflater.from(getContext()).inflate(R.layout.proximity_manager_row, parent, false);
            ViewHolder viewHolder = new ViewHolder(row);
            row.setTag(viewHolder);
        }

        final SimultaneousScanActivity.ProximityManagerWrapper item = getItem(position);

        ViewHolder viewHolder = (ViewHolder) row.getTag();

        viewHolder.managerId.setText(getContext().getString(R.string.manager_id, item.getProximityManager().getId()));
        viewHolder.beaconCount.setText(getContext().getString(R.string.beacons_found_count, item.getFoundBeacons()));
        viewHolder.distance.setText(getContext().getString(R.string.distance_filter, item.getDistance()));
        viewHolder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (removeManagerListener != null) {
                    removeManagerListener.removeManager(item);
                }
            }
        });
        return row;
    }

    static class ViewHolder {

        @InjectView(R.id.proximity_manager_row_id)
        TextView managerId;

        @InjectView(R.id.proximity_manager_row_beacon_count)
        TextView beaconCount;

        @InjectView(R.id.proximity_manager_row_distance)
        TextView distance;

        @InjectView(R.id.proximity_manager_row_distance_remove)
        Button remove;

        public ViewHolder(View source) {
            ButterKnife.inject(this, source);
        }

    }
}
