package com.kontakt.sample.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

class BaseActivity extends AppCompatActivity {

    protected void setUpActionBar(final Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    protected void setUpActionBarTitle(final String title) {
        getSupportActionBar().setTitle(title);
    }

}
