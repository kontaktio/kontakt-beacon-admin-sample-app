package com.kontakt.sample;

import android.app.Application;

import com.kontakt.sdk.android.BuildConfig;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.log.LogLevel;
import com.kontakt.sdk.android.common.log.Logger;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initializeDependencies();
    }

    private void initializeDependencies() {
        KontaktSDK.initialize(this)
                  .setDebugLoggingEnabled(BuildConfig.DEBUG)
                  .setLogLevelEnabled(LogLevel.DEBUG, true)
                  .setCrashlyticsLoggingEnabled(true);

        Logger.setDebugLoggingEnabled(false);
    }
}
