package com.kontakt.sample.ui.adapter.range;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRangeAdapter<T> extends BaseAdapter {

    protected List<T> devices;

    protected LayoutInflater inflater;

    public BaseRangeAdapter(final Context context) {
        inflater = LayoutInflater.from(context);
        devices = new ArrayList<>();
    }
    

    public abstract void replaceWith(final List<T> devices);

    public View getTheSameOrInflate(View view, final ViewGroup parent) {
        if (view == null) {
            return inflate(parent);
        }
        return view;
    }

    abstract View inflate(ViewGroup parent);

}
