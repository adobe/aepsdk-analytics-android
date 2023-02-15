# Analytics API Reference

## Prerequisites

Refer to the [Getting started guide](getting-started.md).

## API reference

This document details all the APIs provided by the Analytics extension, along with the sample code snippets.

- [clearQueue](#clearQueue)
- [extensionVersion](#extensionVersion)
- [EXTENSION](#EXTENSION)
- [getQueueSize](#getQueueSize)
- [getTrackingIdentifier](#getTrackingIdentifier)
- [getVisitorIdentifier](#getVisitorIdentifier)
- [sendQueuedHits](#sendQueuedHits)
- [setVisitorIdentifier](#setVisitorIdentifier)
------

### clearQueue

Clears all hits from the tracking queue and removes them from the database.

> Warning: Use caution when manually clearing the queue. This operation cannot be reverted.

#### Java

##### Syntax
```java
public static void clearQueue()
```

##### Example
```java

Analytics.clearQueue();

```
#### Kotlin

##### Example
```kotlin

Analytics.clearQueue()

```

---

### extensionVersion:

The `extensionVersion()` API returns the version of the Analytics extension.

#### Java

##### Syntax
```java
public static String extensionVersion()
```

##### Example
```java
String extensionVersion = Analytics.extensionVersion();
```

#### Kotlin

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
```java
public static void getQueueSize(@NonNull final AdobeCallback<Long> callback)
```

##### Example
```java
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

> **Note**
Before using this API, see [Identify unique visitors](https://experienceleague.adobe.com/docs/analytics/components/metrics/unique-visitors.html).

Retrieves the Analytics tracking identifier. The identifier is only returned for existing users who had AID persisted and migrated from earlier versions of SDK. For new users, no AID is generated and should instead use [Experience Cloud ID](https://developer.adobe.com/client-sdks/documentation/mobile-core/identity/api-reference) to identify visitors.

#### Java

##### Syntax
```java
public static void getTrackingIdentifier(@NonNull final AdobeCallback<String> callback)
```

##### Example
```java
Analytics.getTrackingIdentifier(new AdobeCallbackWithError<String>() {
    @Override
    public void fail(AdobeError adobeError) {
        // Handle the error
    }

    @Override
    public void call(String aid) {
        //Handle the tracking identifier (AID)
    }
});
```

#### Kotlin

##### Example

```kotlin
Analytics.getTrackingIdentifier(object: AdobeCallbackWithError<String> {
    override fun call(aid: String?) {
        //Handle the tracking identifier (AID)
    }

    override fun fail(error: AdobeError?) {
        // Handle the error
    }
})
```

---

### getVisitorIdentifier

> **Note**
Before using this API, see [Identify unique visitors](https://experienceleague.adobe.com/docs/analytics/components/metrics/unique-visitors.html).

This API gets a custom Analytics visitor identifier, which has been set previously using [setVisitorIdentifier](#setvisitoridentifier).

#### Java

##### Syntax
```java
public static void getVisitorIdentifier(@NonNull final AdobeCallback<String> callback)
```

##### Example
```java
Analytics.getVisitorIdentifier(new AdobeCallbackWithError<String>() {
    @Override
    public void fail(AdobeError adobeError) {
        // Handle the error
    }

    @Override
    public void call(String vid) {
        // Handle the Visitor ID
    }
});
```

#### Kotlin

##### Example
```kotlin
Analytics.getVisitorIdentifier(object: AdobeCallbackWithError<String> {
    override fun call(vid: String?) {
        // Handle the Visitor ID
    }

    override fun fail(error: AdobeError?) {
        // Handle the error
    }
})
```

---

### sendQueuedHits

Sends all the queued hits in the offline queue to Analytics, regardless of the current hit batch settings.

#### Java

##### Syntax
```java
public static void sendQueuedHits()
```

##### Example
```java
Analytics.sendQueuedHits();
```

#### Kotlin

##### Example
```kotlin
Analytics.sendQueuedHits()
```

---

### setVisitorIdentifier

> **Note** 
Before using this API, see [Identify unique visitors](https://experienceleague.adobe.com/docs/analytics/components/metrics/unique-visitors.html).

Sets a custom Analytics visitor identifier. For more information, see [Custom Visitor ID](https://experienceleague.adobe.com/docs/analytics/implementation/vars/config-vars/visitorid.html).

Setting `null` or an empty string clears the current visitor identifier.

#### Java

##### Syntax
```java
public static void setVisitorIdentifier(@Nullable final String visitorID)
```

##### Example
```java
Analytics.setVisitorIdentifier("vid_1");
```

#### Kotlin

##### Example
```kotlin
Analytics.setVisitorIdentifier("vid_1")
```