package com.kontakt.sample;

import android.app.Application;

import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.log.LogLevel;
import com.kontakt.sdk.android.common.log.Logger;
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

        KontaktSDK.initialize(this)
                  .setDebugLoggingEnabled(BuildConfig.DEBUG)
                  .setLogLevelEnabled(LogLevel.DEBUG, true)
                  .setCrashlyticsLoggingEnabled(true);

        Logger.setDebugLoggingEnabled(false);
        ButterKnife.setDebug(BuildConfig.DEBUG);
    }
}
