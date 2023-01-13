# Analytics API Usage

This document details all the APIs provided by Analytics extension, along with sample code snippets on how to properly use the APIs.

## clearQueue

Clears all hits from the tracking queue and removes them from the database.

> Warning: Use caution when manually clearing the queue. This operation cannot be reverted.

### Syntax

```Java
public static void clearQueue()
```

### Example

#### Java

```Java

Analytics.clearQueue();

```

#### Kotlin

```Kotlin

Analytics.clearQueue()

```

---

## extensionVersion:

The `extensionVersion()` API returns the version of the Analytics extension.

### Syntax

```Java
public static String extensionVersion()
```

### Example

#### Java

```Java
String extensionVersion = Analytics.extensionVersion();
```

#### Kotlin

```kotlin
val extensionVersion = Analytics.extensionVersion();
```

---

## EXTENSION

Represents a reference to `AnalyticsExtension.class` that can be used to register with `MobileCore` via its `registerExtensions` api.

### Syntax

```java
public static final Class<? extends Extension> EXTENSION = AnalyticsExtension.class;
```

### Usage

#### Java

```java
MobileCore.registerExtensions(Arrays.asList(Analytics.EXTENSION, ...), new AdobeCallback<Object>() {
    // implement completion callback
});
```

#### Kotlin

```kotlin
MobileCore.registerExtensions(listOf(Analytics.EXTENSION, ...)){
    // implement completion callback
}
```

---

## getQueueSize

Retrieves the total number of Analytics hits in the tracking queue.

### Syntax

```Java
public static void getQueueSize(@NonNull final AdobeCallback<Long> callback)
```

### Example

#### Java

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

## getTrackingIdentifier

> ℹ️ Before using this API, see [Identify unique visitors](https://experienceleague.adobe.com/docs/analytics/components/metrics/unique-visitors.html).

Retrieves the Analytics tracking identifier. The identifier is only returned for existing users who had AID persisted and migrated from earlier versions of SDK. For new users, no AID is generated and should instead use [Experience Cloud ID](https://developer.adobe.com/client-sdks/documentation/mobile-core/identity/api-reference/#getexperiencecloudid) to identify visitors.

### Syntax

```Java
public static void getTrackingIdentifier(@NonNull final AdobeCallback<String> callback)
```

### Example

#### Java

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

## getVisitorIdentifier

> Before use this API, see [Identify unique visitors](https://experienceleague.adobe.com/docs/analytics/components/metrics/unique-visitors.html).

This API gets a custom Analytics visitor identifier, which has been set previously using [setVisitorIdentifier](https://github.com/adobe/aepsdk-analytics-ios/blob/main/Documentation/AEPAnalytics.md#setvisitoridentifier).

### Syntax

```Java
public static void getVisitorIdentifier(@NonNull final AdobeCallback<String> callback)
```

### Example

#### Java

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

## sendQueuedHits

Sends all queued hits in the offline queue to Analytics, regardless of the current hit batch settings.

### Syntax

```Java
public static void sendQueuedHits()
```

### Example

#### Java

```Java
Analytics.sendQueuedHits();
```

#### Kotlin

```Kotlin
Analytics.sendQueuedHits()
```

---

## setVisitorIdentifier

> Before use this API, see [Identify unique visitors](https://experienceleague.adobe.com/docs/analytics/components/metrics/unique-visitors.html).

Sets a custom Analytics visitor identifier. For more information, see [Custom Visitor ID](https://experienceleague.adobe.com/docs/analytics/implementation/vars/config-vars/visitorid.html).

### Syntax

```Java
public static void setVisitorIdentifier(@NonNull final String visitorID)
```

### Example

#### Java

```Java
Analytics.setVisitorIdentifier("vid_1");
```

#### Kotlin

```Kotlin
Analytics.setVisitorIdentifier("vid_1")
```
