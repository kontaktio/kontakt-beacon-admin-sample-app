package com.kontakt.sample.ui.adapter.monitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.kontakt.sample.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseMonitorAdapter<T, K> extends BaseExpandableListAdapter {

    protected Context context;
    private List<T> groupList;
    private Map<T, List<K>> childMap;

    public BaseMonitorAdapter(Context context) {
        this.context = context;
        groupList = new ArrayList<>();
        childMap = new HashMap<>();
    }


    public void addGroup(T group) {
        groupList.add(group);
        childMap.put(group, new ArrayList<K>());
    }

    public void removeGroup(T group) {
        groupList.remove(group);
        childMap.remove(group);
        notifyDataSetChanged();
    }

    public int getGroupIndex(T group) {
        return groupList.indexOf(group);
    }

    public void replaceChildren(int groupPosition, List<K> devicesList) {
        List<K> devices = childMap.get(getGroup(groupPosition));
        devices.clear();
        devices.addAll(devicesList);
        notifyDataSetChanged();
    }

    public boolean addOrReplaceChild(int groupPosition, K device) {
        T group = getGroup(groupPosition);
        List<K> devices = childMap.get(group);
        int index = devices.indexOf(device);
        boolean state;
        if (index == -1) {
            devices.add(device);
            state = true;
        } else {
            devices.set(index, device);
            state = false;
        }

        notifyDataSetChanged();
        return state;
    }

    public boolean containsGroup(T group) {
        return groupList.contains(group);
    }

    public void clear() {
        groupList.clear();
        childMap.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childMap.get(getGroup(groupPosition)).size();
    }

    @Override
    public T getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public K getChild(int groupPosition, int childPosition) {
        return childMap.get(getGroup(groupPosition)).get(childPosition);
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
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    View createView(final int viewId) {
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(viewId, null);
    }


    protected View createHeader() {
        View convertView = createView(R.layout.monitor_section_list_header);
        GroupViewHolder groupViewHolder = new GroupViewHolder(convertView);
        convertView.setTag(groupViewHolder);
        return convertView;
    }

    void setHeaderTitle(String title, View convertView) {
        final GroupViewHolder groupViewHolder = (GroupViewHolder) convertView.getTag();
        groupViewHolder.header.setText(title);
    }

    static class GroupViewHolder {
        GroupViewHolder(View view) {
            header = (TextView) view.findViewById(R.id.header);
        }

        TextView header;
    }
}
