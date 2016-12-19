package com.kontakt.sample;

import android.app.Application;
import com.kontakt.sdk.android.common.KontaktSDK;

public class App extends Application {

  private static final String API_KEY = "Put your API key here";

  @Override
  public void onCreate() {
    super.onCreate();
    initializeDependencies();
  }

  //Initializing Kontakt SDK. Insert your API key to allow all samples to work correctly
  private void initializeDependencies() {
    KontaktSDK.initialize(API_KEY);
  }
}
