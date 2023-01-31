# Getting Started with Analytics SDK

## Before starting

Analytics extension has a dependency on [AEP Core SDK](https://github.com/adobe/aepsdk-core-android#readme) which must be installed to use the extension.

## Add Analytics extension to your app

1. Installation via [Maven](https://maven.apache.org/) & [Gradle](https://gradle.org/) is the easiest and recommended way to get the AEP SDK into your Android app. Add a dependency on Analytics and Core to your mobile application. To ensure consistent builds, it is best to explicitly specify the dependency version and update them manually.

   ```
   implementation 'com.adobe.marketing.mobile:core:2.+'
   implementation 'com.adobe.marketing.mobile:analytics:2.+'
   ```

2. Import MobileCore and Analytics extensions:

   ### Java

   ```java
   import com.adobe.marketing.mobile.MobileCore;
   import com.adobe.marketing.mobile.Analytics;
   ```

   ### Kotlin

   ```kotlin
   import com.adobe.marketing.mobile.MobileCore
   import com.adobe.marketing.mobile.Analytics
   ```

3. Import the Analytics library into your project and register it with `MobileCore`

   ### Java

   ```java
   public class MainApp extends Application {
        private static final String APP_ID = "YOUR_APP_ID";

        @Override
        public void onCreate() {
            super.onCreate();

            MobileCore.setApplication(this);
            MobileCore.setLogLevel(LoggingMode.VERBOSE);
            MobileCore.configureWithAppID(APP_ID);

            List<Class<? extends Extension>> extensions = Arrays.asList(
                    Analytics.EXTENSION,...);
            MobileCore.registerExtensions(extensions, o -> {
                Log.d(LOG_TAG, "AEP Mobile SDK is initialized");
            });
        }
    }
   ```

   ### Kotlin

   ```kotlin
   class MyApp : Application() {

       override fun onCreate() {
           super.onCreate()
           MobileCore.setApplication(this)
           MobileCore.setLogLevel(LoggingMode.VERBOSE)
           MobileCore.configureWithAppID("YOUR_APP_ID")

           val extensions = listOf(Analytics.EXTENSION, ...)
           MobileCore.registerExtensions(extensions) {
               Log.d(LOG_TAG, "AEP Mobile SDK is initialized")
           }
       }
   }
   ```

## Next Steps

Get familiar with the various APIs offered by the AEP SDK by checking out the [Analytics API reference](./api-reference.md).
