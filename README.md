Kontakt.io Android SDK Samples
===============================

Simple application for developers, demonstrating Kontakt.io Android SDK features and sample implementations.

## Samples
- iBeacon and Eddystone scanning
- Beacon Pro profile scanning
- Applying regions and namespaces
- Applying scanning filters
- Background scanning
- Beacon's Configuration
- Reading Beacon's Pro light sensor
- Consuming REST API with KontaktCloud

## Setup
1. Clone or download this repository.
2. Open project in Android Studio (select top `build.gradle` file when opening the project).
3. Install any missing dependencies that Android Studio might ask you for.
4. Remember to put your API key in `KontaktSDK.initialize()` method located in `App.java` file:
```
public class App extends Application {

  private static final String API_KEY = "Your Api Key here";

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
```

Run the application to see if everything is set up correctly.

For full information see: https://developer.kontakt.io/mobile/android/qsg/setup/

*NOTE:* The old, deprecated samples app is available in the *archive* branch (if you require it for any reason).



