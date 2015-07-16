package com.kontakt.sample.ui.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kontakt.sample.R;


public class Entry extends LinearLayout {

    private TextView key;
    private TextView value;

    public Entry(Context context) {
        super(context);
    }

    public Entry(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.entry, this);
        key = (TextView) findViewById(R.id.key);
        value = (TextView) findViewById(R.id.value);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Entry);
        final String keyString = array.getString(R.styleable.Entry_key);
        key.setText(keyString);
        array.recycle();
    }

    public Entry(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setValue(final String valueText) {
        value.setText(valueText);
    }

    @Override
    public void setEnabled(final boolean state) {
        super.setEnabled(state);
        final Resources resources = getResources();
        key.setTextColor(state ? resources.getColor(R.color.black) : resources.getColor(R.color.grey));
        value.setTextColor(state ? resources.getColor(R.color.black) : resources.getColor(R.color.grey));
    }
}
