# Analytics API Usage

## Prerequisites

Refer to the [Getting started guide](getting-started.md).

## API reference

- [clearQueue](#clearQueue)
- [extensionVersion](#extensionVersion)
- [EXTENSION](#EXTENSION)
- [getQueueSize](#getQueueSize)
- [getTrackingIdentifier](#getTrackingIdentifier)
- [getVisitorIdentifier](#getVisitorIdentifier)
- [sendQueuedHits](#sendQueuedHits)
- [setVisitorIdentifier](#setVisitorIdentifier)
------


This document details all the APIs provided by Analytics extension, along with sample code snippets on how to properly use the APIs.

### clearQueue

Clears all hits from the tracking queue and removes them from the database.

> Warning: Use caution when manually clearing the queue. This operation cannot be reverted.

#### Java

##### Syntax
```Java
public static void clearQueue()
```

##### Example
```Java

Analytics.clearQueue();

```
#### Kotlin

##### Syntax
```Kotlin
fun clearQueue()
```

##### Example
```Kotlin

Analytics.clearQueue()

```

---

### extensionVersion:

The `extensionVersion()` API returns the version of the Analytics extension.

#### Java

##### Syntax
```Java
public static String extensionVersion()
```

##### Example
```Java
String extensionVersion = Analytics.extensionVersion();
```

#### Kotlin

##### Syntax
```kotlin
fun extensionVersion(): String
```
##### Example
```kotlin
val extensionVersion = Analytics.extensionVersion();
```

---

### EXTENSION

Represents a reference to `AnalyticsExtension.class` that can be used to register with `MobileCore` via its `registerExtensions` api.

#### Java

##### Syntax
```java
public static final Class<? extends Extension> EXTENSION = AnalyticsExtension.class;
```

##### Example
```java
MobileCore.registerExtensions(Arrays.asList(Analytics.EXTENSION, ...), new AdobeCallback<Object>() {
    // implement completion callback
});
```

#### Kotlin

##### Syntax
```kotlin
val EXTENSION: Class<out Extension?> = AnalyticsExtension::class.java
}
```

##### Example
```kotlin
MobileCore.registerExtensions(listOf(Analytics.EXTENSION, ...)){
    // implement completion callback
}
```

---

### getQueueSize

Retrieves the total number of Analytics hits in the tracking queue.

#### Java

##### Syntax
```Java
public static void getQueueSize(@NonNull final AdobeCallback<Long> callback)
```

##### Example
```Java
Analytics.getQueueSize(new AdobeCallbackWithError<Long>() {
    @Override
    public void fail(AdobeError adobeError) {
        // Handle the error
    }

    @Override
    public void call(Long size) {
        // Handle the queue size
    }
});
```

#### Kotlin

##### Syntax
```kotlin
 fun getQueueSize(callback: AdobeCallback<Long?>)
```

##### Example
```kotlin
Analytics.getQueueSize(object: AdobeCallbackWithError<Long> {
    override fun call(size: Long?) {
        // Handle the queue size
    }

    override fun fail(error: AdobeError?) {
        // Handle the error
    }
})
```

---

### getTrackingIdentifier

> ℹ️ Before using this API, see [Identify unique visitors](https://experienceleague.adobe.com/docs/analytics/components/metrics/unique-visitors.html).

Retrieves the Analytics tracking identifier. The identifier is only returned for existing users who had AID persisted and migrated from earlier versions of SDK. For new users, no AID is generated and should instead use [Experience Cloud ID](https://developer.adobe.com/client-sdks/documentation/mobile-core/identity/api-reference) to identify visitors.

#### Java

##### Syntax
```Java
public static void getTrackingIdentifier(@NonNull final AdobeCallback<String> callback)
```

##### Example
```Java
Analytics.getTrackingIdentifier(new AdobeCallbackWithError<String>() {
    @Override
    public void fail(AdobeError adobeError) {
        // Handle the error
    }

    @Override
    public void call(String s) {
        // Handle the Experience Cloud ID
    }
});
```

#### Kotlin

##### Syntax
```Kotlin
fun getTrackingIdentifier(callback: AdobeCallback<String?>)
```

##### Example

```Kotlin
Analytics.getTrackingIdentifier(object: AdobeCallbackWithError<String> {
    override fun call(id: String?) {
        // Handle the Experience Cloud ID
    }

    override fun fail(error: AdobeError?) {
        // Handle the error
    }
})
```

---

### getVisitorIdentifier

> Before use this API, see [Identify unique visitors](https://experienceleague.adobe.com/docs/analytics/components/metrics/unique-visitors.html).

This API gets a custom Analytics visitor identifier, which has been set previously using [setVisitorIdentifier](#setvisitoridentifier).

#### Java

##### Syntax
```Java
public static void getVisitorIdentifier(@NonNull final AdobeCallback<String> callback)
```

##### Example
```Java
Analytics.getVisitorIdentifier(new AdobeCallbackWithError<String>() {
    @Override
    public void fail(AdobeError adobeError) {
        // Handle the error
    }

    @Override
    public void call(String s) {
        // Handle the Visitor ID
    }
});
```

#### Kotlin

##### Syntax
```Kotlin
 fun getVisitorIdentifier(callback: AdobeCallback<String?>) 
```
##### Example
```Kotlin
Analytics.getVisitorIdentifier(object: AdobeCallbackWithError<String> {
    override fun call(id: String?) {
        // Handle the Visitor ID
    }

    override fun fail(error: AdobeError?) {
        // Handle the error
    }
})
```

---

### sendQueuedHits

Sends all queued hits in the offline queue to Analytics, regardless of the current hit batch settings.

#### Java

##### Syntax
```Java
public static void sendQueuedHits()
```

##### Example
```Java
Analytics.sendQueuedHits();
```

#### Kotlin

##### Syntax
```Kotlin
fun sendQueuedHits()
```

##### Example
```Kotlin
Analytics.sendQueuedHits()
```

---

### setVisitorIdentifier

> Before use this API, see [Identify unique visitors](https://experienceleague.adobe.com/docs/analytics/components/metrics/unique-visitors.html).

Sets a custom Analytics visitor identifier. For more information, see [Custom Visitor ID](https://experienceleague.adobe.com/docs/analytics/implementation/vars/config-vars/visitorid.html).

#### Java

##### Syntax
```Java
public static void setVisitorIdentifier(@NonNull final String visitorID)
```

##### Example
```Java
Analytics.setVisitorIdentifier("vid_1");
```

#### Kotlin

##### Syntax
```Kotlin
fun setVisitorIdentifier(visitorID: String)
```

##### Example
```Kotlin
Analytics.setVisitorIdentifier("vid_1")
```