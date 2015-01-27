package com.kontakt.sample;

import android.app.Application;

import com.kontakt.sdk.android.http.KontaktApiClient;
import com.kontakt.sdk.android.util.Logger;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.setDebugLoggingEnabled(BuildConfig.DEBUG);
        Logger.setCrashlyticsLoggingEnabled(true);
        KontaktApiClient.init(this);
    }
}
