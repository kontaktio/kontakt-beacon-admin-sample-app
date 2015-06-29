package com.kontakt.sample.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.kontakt.sample.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by slovic on 28.06.15.
 */
public class AchievementsActivity extends DwarfsServiceAwareActivity {


    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.achivements_activity);
        ButterKnife.inject(this);

        setUpActionBar(toolbar);
        setUpActionBarTitle(getResources().getString(R.string.achievements));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }
}
