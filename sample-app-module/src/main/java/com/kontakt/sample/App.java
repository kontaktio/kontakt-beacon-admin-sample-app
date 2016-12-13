package com.kontakt.sample;

import android.app.Application;
import com.kontakt.sdk.android.common.KontaktSDK;

public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    initializeDependencies();
  }

  //Initializing Kontakt SDK. Insert your API key here to allow all samples to work correctly
  private void initializeDependencies() {
    KontaktSDK.initialize("Put your API key here");
  }
}
