package com.kontakt.sample;

import android.app.Application;
import com.kontakt.sdk.android.common.KontaktSDK;

public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    initializeDependencies();
  }

  private void initializeDependencies() {
    KontaktSDK.initialize("");
  }
}
