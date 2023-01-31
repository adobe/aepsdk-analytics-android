/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile;

import androidx.annotation.NonNull;
import com.adobe.marketing.mobile.analytics.internal.AnalyticsExtension;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.DataReader;
import java.util.HashMap;
import java.util.Map;

public class Analytics {
    private static final String LOG_TAG = "Analytics";
    private static final String EXTENSION_VERSION = "2.0.0";
    // The constants for EventData keys
    private static final String ANALYTICS_ID = "aid";
    private static final String GET_QUEUE_SIZE = "getqueuesize";
    private static final String QUEUE_SIZE = "queuesize";
    private static final String CLEAR_HITS_QUEUE = "clearhitsqueue";
    private static final String FORCE_KICK_HITS = "forcekick";
    private static final String VISITOR_IDENTIFIER = "vid";

    private static final long EVENT_PROCESSING_TIMEOUT_MS = 5000L;

    public static final Class<? extends Extension> EXTENSION = AnalyticsExtension.class;

    private Analytics() {}

    /**
     * Registers the extension with the Mobile SDK. This method should be called only once in your
     * application class.
     *
     * @deprecated as of 2.0.0, use {@link MobileCore#registerExtensions(List, AdobeCallback)} with
     *     {@link Analytics#EXTENSION} instead.
     */
    @Deprecated
    public static void registerExtension() {
        MobileCore.registerExtension(
                AnalyticsExtension.class,
                extensionError -> {
                    if (extensionError == null) {
                        return;
                    }
                    Log.error(
                            LOG_TAG,
                            LOG_TAG,
                            "There was an error when registering the Analytics extension: %s",
                            extensionError.getErrorName());
                });
    }

    /**
     * Returns the version for the Analytics extension
     *
     * @return The version string
     */
    @NonNull public static String extensionVersion() {
        return EXTENSION_VERSION;
    }

    /**
     * Retrieves the analytics tracking identifier generated for this app/device instance.
     *
     * @param callback {@code AdobeCallback} invoked with the analytics identifier {@code String}
     *     value; when an {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be
     *     returned in the eventuality of an unexpected error or if the default timeout (5000ms) is
     *     met before the callback is returned with analytics tracking identifier.
     */
    @SuppressWarnings("rawtypes")
    public static void getTrackingIdentifier(@NonNull final AdobeCallback<String> callback) {
        if (callback == null) {
            return;
        }

        final AdobeCallbackWithError adobeCallbackWithError =
                callback instanceof AdobeCallbackWithError
                        ? (AdobeCallbackWithError) callback
                        : null;

        final Event analyticsReqEvent =
                new Event.Builder(
                                "GetTrackingIdentifier",
                                EventType.ANALYTICS,
                                EventSource.REQUEST_IDENTITY)
                        .build();
        MobileCore.dispatchEventWithResponseCallback(
                analyticsReqEvent,
                EVENT_PROCESSING_TIMEOUT_MS,
                new AdobeCallbackWithError<Event>() {

                    @Override
                    public void call(Event event) {
                        Map<String, Object> eventData = event.getEventData();
                        callback.call(
                                eventData != null
                                        ? DataReader.optString(eventData, ANALYTICS_ID, null)
                                        : null);
                    }

                    @Override
                    public void fail(AdobeError adobeError) {
                        if (adobeCallbackWithError != null) {
                            adobeCallbackWithError.fail(adobeError);
                        }
                    }
                });
        Log.debug(
                LOG_TAG,
                LOG_TAG,
                "getTrackingIdentifier - Get tracking identifier request event was sent");
    }

    /**
     * Retrieves the total number of analytics hits currently in the tracking queue.
     *
     * @param callback {@code AdobeCallback} invoked with the queue size {@code long} value; when an
     *     {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be returned in the
     *     eventuality of an unexpected error or if the default timeout (5000ms) is met before the
     *     callback is returned with queue size.
     */
    @SuppressWarnings("rawtypes")
    public static void getQueueSize(@NonNull final AdobeCallback<Long> callback) {
        if (callback == null) {
            return;
        }

        final AdobeCallbackWithError adobeCallbackWithError =
                callback instanceof AdobeCallbackWithError
                        ? (AdobeCallbackWithError) callback
                        : null;
        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put(GET_QUEUE_SIZE, true);
        final Event analyticsReqEvent =
                new Event.Builder("GetQueueSize", EventType.ANALYTICS, EventSource.REQUEST_CONTENT)
                        .setEventData(eventDataMap)
                        .build();
        MobileCore.dispatchEventWithResponseCallback(
                analyticsReqEvent,
                EVENT_PROCESSING_TIMEOUT_MS,
                new AdobeCallbackWithError<Event>() {

                    @Override
                    public void call(Event event) {
                        Map<String, Object> eventData = event.getEventData();
                        callback.call(
                                eventData != null
                                        ? DataReader.optLong(eventData, QUEUE_SIZE, 0L)
                                        : 0L);
                    }

                    @Override
                    public void fail(AdobeError adobeError) {
                        if (adobeCallbackWithError != null) {
                            adobeCallbackWithError.fail(adobeError);
                        }
                    }
                });
        Log.debug(LOG_TAG, LOG_TAG, "getQueueSize - Get hits queue size request event was sent");
    }

    /**
     * Clears all unsent analytics hits from the tracking queue.
     *
     * <p>NOTE: Use caution when clearing the queue manually. This process cannot be reversed.
     */
    public static void clearQueue() {
        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put(CLEAR_HITS_QUEUE, true);
        MobileCore.dispatchEvent(
                new Event.Builder(
                                "ClearHitsQueue", EventType.ANALYTICS, EventSource.REQUEST_CONTENT)
                        .setEventData(eventDataMap)
                        .build());
        Log.debug(LOG_TAG, LOG_TAG, "clearQueue - Clear hits queue event was sent");
    }

    /** Forces analytics to send all queued hits regardless of current batch options. */
    public static void sendQueuedHits() {
        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put(FORCE_KICK_HITS, true);
        MobileCore.dispatchEvent(
                new Event.Builder("ForceKickHits", EventType.ANALYTICS, EventSource.REQUEST_CONTENT)
                        .setEventData(eventDataMap)
                        .build());
        Log.debug(LOG_TAG, LOG_TAG, "sendQueuedHits - Kick all hits event was sent");
    }

    /**
     * Retrieves the visitor identifier
     *
     * @param callback {@code AdobeCallback} invoked with the visitor identifier {@code String}
     *     value; when an {@link AdobeCallbackWithError} is provided, an {@link AdobeError} can be
     *     returned in the eventuality of an unexpected error or if the default timeout (5000ms) is
     *     met before the callback is returned with visitor identifier.
     */
    @SuppressWarnings("rawtypes")
    public static void getVisitorIdentifier(@NonNull final AdobeCallback<String> callback) {
        if (callback == null) {
            return;
        }

        final Event analyticsReqEvent =
                new Event.Builder(
                                "GetVisitorIdentifier",
                                EventType.ANALYTICS,
                                EventSource.REQUEST_IDENTITY)
                        .build();

        final AdobeCallbackWithError adobeCallbackWithError =
                callback instanceof AdobeCallbackWithError
                        ? (AdobeCallbackWithError) callback
                        : null;
        MobileCore.dispatchEventWithResponseCallback(
                analyticsReqEvent,
                EVENT_PROCESSING_TIMEOUT_MS,
                new AdobeCallbackWithError<Event>() {

                    @Override
                    public void call(Event event) {
                        Map<String, Object> eventData = event.getEventData();
                        callback.call(
                                eventData != null
                                        ? DataReader.optString(eventData, VISITOR_IDENTIFIER, null)
                                        : null);
                    }

                    @Override
                    public void fail(AdobeError adobeError) {
                        if (adobeCallbackWithError != null) {
                            adobeCallbackWithError.fail(adobeError);
                        }
                    }
                });
    }

    /**
     * Retrieves the visitor identifier
     *
     * @param visitorID {@code String} new value for visitor identifier
     */
    public static void setVisitorIdentifier(@NonNull final String visitorID) {
        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put(VISITOR_IDENTIFIER, visitorID);
        MobileCore.dispatchEvent(
                new Event.Builder(
                                "UpdateVisitorIdentifier",
                                EventType.ANALYTICS,
                                EventSource.REQUEST_IDENTITY)
                        .setEventData(eventDataMap)
                        .build());
        Log.debug(LOG_TAG, LOG_TAG, "setVisitorIdentifier - vid is (%s)", visitorID);
    }
}
