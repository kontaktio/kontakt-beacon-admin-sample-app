package com.kontakt.sample;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.log.KontaktSdkLogger;
import com.kontakt.sdk.android.common.log.LogLevel;
import com.kontakt.sdk.android.common.log.Logger;

public class App extends Application {

  // TODO Put your API key here
  public static final String API_KEY = "Your_Api_Key";

  @Override
  public void onCreate() {
    super.onCreate();
    initializeDependencies();
    Logger.enableAllLoggerLevels(true);
  }

  //Initializing Kontakt SDK. Insert your API key to allow all samples to work correctly
  private void initializeDependencies() {
    KontaktSDK.initialize(API_KEY).setKontaktSdkLogger(new KontaktSdkLogger() {
      @Override
      public boolean isLoggable(@NonNull LogLevel logLevel, @Nullable String tag) {
        return true;
      }

      @Override
      public void log(@NonNull LogLevel logLevel, @Nullable String msg, @Nullable String tag, @Nullable Throwable throwable) {
        Log.d(tag, msg, throwable);
      }
    });
  }
}
