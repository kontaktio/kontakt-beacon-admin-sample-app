package com.kontakt.sample.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Messenger;
import android.support.v7.widget.Toolbar;

import com.kontakt.sample.App;
import com.kontakt.sample.R;
import com.kontakt.sample.ui.KenBurnsNetImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by slovic on 27.06.15.
 */
public class TrackDwarf extends BaseActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int MESSAGE_START_SCAN = 16;
    public static final int MESSAGE_STOP_SCAN = 25;


    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 121;

    @InjectView(R.id.banner)
    KenBurnsNetImageView banner;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);

        setUpActionBar(toolbar);
        setUpActionBarTitle(getString(R.string.app_name));
        banner.setImageUrl(
                "http://krasnale.pl/wp-content/uploads/2012/01/Mi%C5%82os%CC%81nik2-Siem.jpg",
                ((App) getApplication()).getImageLoader()
        );
    }

}
