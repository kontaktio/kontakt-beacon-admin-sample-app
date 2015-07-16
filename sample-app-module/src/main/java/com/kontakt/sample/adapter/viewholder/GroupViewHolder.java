package com.kontakt.sample.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import com.kontakt.sample.R;

public class GroupViewHolder {

    public TextView header;

    public GroupViewHolder(View view) {
        header = (TextView) view.findViewById(R.id.header);
    }


}
