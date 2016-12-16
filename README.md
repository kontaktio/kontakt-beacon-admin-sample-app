Kontakt.io Android SDK Samples
===============================

Simple application for developers, demonstrating Kontakt.io Android SDK features and sample implementations.

##Samples
- iBeacon and Eddystone scanning
- Beacon Pro profile scanning
- Applying regions and namespaces
- Applying scanning filters
- Background scanning
- Beacon's Configuration
- Consuming REST API with KontaktCloud

##Setup
1. Clone or download this repository.
2. Open project in Android Studio. Select top `build.gradle` file when opening the project.
3. Install any missing dependencies that Android Studio might ask you for.
4. Remember to put your API key in `KontaktSDK.initialize()` method located in `App.java` file.
```
public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    initializeDependencies();
  }

  private void initializeDependencies() {
    KontaktSDK.initialize("Put your API key here");
  }
}
```

Run the application to see if everything is set up correctly.

For full information see: https://developer.kontakt.io/android-sdk/3.2.0/quickstart/


