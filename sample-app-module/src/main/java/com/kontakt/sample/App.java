package com.kontakt.sample;

import android.app.Application;

import com.kontakt.sdk.android.common.Logger;
import com.kontakt.sdk.android.http.KontaktApiClient;
import com.squareup.leakcanary.LeakCanary;

import butterknife.ButterKnife;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initializeDependencies();
    }

    private void initializeDependencies() {
        if(BuildConfig.DEBUG) {
            LeakCanary.install(this);
        }

        Logger.setDebugLoggingEnabled(BuildConfig.DEBUG);

        Logger.setCrashlyticsLoggingEnabled(true);
        KontaktApiClient.init(this);

        ButterKnife.setDebug(BuildConfig.DEBUG);
    }
}
