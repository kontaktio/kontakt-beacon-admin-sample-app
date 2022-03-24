Kontakt.io Android SDK Samples
===============================

NOTE: This branch is for clients willing to use the samples in their own applications, but not wanting to immediately migrate to Android 12 permission's model.
If you have this app already installed from the master branch, it may be important to perform the general cleaning:
    ./gradlew clean
    removing build folder
    uninstalling the existing app.
As otherwise the system may still have the cached information regarding the target = 31 from the previous installation.

Simple application for developers, demonstrating Kontakt.io Android SDK features and sample implementations.

## Samples
- iBeacon and Eddystone scanning
- Beacon Pro profile scanning
- Applying regions and namespaces
- Applying scanning filters
- Background scanning (including ScanFilters required for scanning with the screen off after Android 8)
- Beacon's Configuration
- Reading Beacon's Pro light sensor
- Consuming REST API with KontaktCloud
- Consuming REST API with KontaktCloud using coroutines

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

For full information see: https://kontakt-api-docs.stoplight.io/docs/dev-ctr-sdks/ZG9jOjMwMzg3NjUz-getting-started

*NOTE:* The old, deprecated samples app is available in the *archive* branch (if you require it for any reason).



