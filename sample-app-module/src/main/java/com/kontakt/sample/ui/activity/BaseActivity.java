package com.kontakt.sample.ui.activity;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.kontakt.sample.R;

public class BaseActivity extends AppCompatActivity {

    protected void setUpActionBar(final Toolbar toolbar) {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void setUpToolbarBack(@NonNull Toolbar toolbar) {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }
    protected void setUpActionBarTitle(@StringRes final int title) {
        getSupportActionBar().setTitle(title);
    }

    protected void setUpActionBarTitle(final String title) {
        getSupportActionBar().setTitle(title);
    }

}
