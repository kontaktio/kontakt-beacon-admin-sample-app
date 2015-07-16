package com.kontakt.sample.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kontakt.sample.R;

public class Header extends LinearLayout {

    private TextView title;

    public Header(Context context) {
        super(context);
    }

    public Header(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.header, this);
        title = (TextView) findViewById(R.id.title);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Header);
        final String titleString = array.getString(R.styleable.Header_header_title);
        array.recycle();
        title.setText(titleString);
    }

    public Header(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}