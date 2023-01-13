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
package com.adobe.marketing.mobile.analytics.function.test

import com.adobe.marketing.mobile.*
import com.adobe.marketing.mobile.util.DataReader
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import java.util.concurrent.CountDownLatch

internal class AnalyticsQueueTests : AnalyticsFunctionalTestBase() {
    @Test(timeout = 10000)
    fun `get queue size`() {
        val countDownLatch = CountDownLatch(2)
        var queueSize1 = -1
        var queueSize2 = -1
        Mockito.`when`(mockedExtensionApi.dispatch(any())).then { invocation ->
            val event = invocation.arguments[0] as? Event
            if ((event?.type == EventType.ANALYTICS) && (event.source == EventSource.RESPONSE_CONTENT) && event.eventData.containsKey(
                    "queuesize"
                )
            ) {
                if (queueSize1 < 0) {
                    queueSize1 = DataReader.optInt(event.eventData, "queuesize", 0)
                } else {
                    queueSize2 = DataReader.optInt(event.eventData, "queuesize", 0)
                }
                countDownLatch.countDown()
            }
        }
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(
            mapOf(
                "analytics.server" to "test.com",
                "analytics.rsids" to "rsid",
                "global.privacy" to "optedin",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 0,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 5,
                "analytics.batchLimit" to 5
            ),
            mapOf(
                "mid" to "mid",
                "blob" to "blob",
                "locationhint" to "lochint"
            )
        )

        val event1 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName"
            )
        ).build()

        val event2 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName"
            )
        ).build()

        analyticsExtension.handleIncomingEvent(event1)
        analyticsExtension.handleIncomingEvent(event2)

        dispatchGetQueueSizeEvent(analyticsExtension)

        val lifecycleStartEvent = Event.Builder(
            "lifecycle start",
            EventType.GENERIC_LIFECYCLE,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "start"
            )
        ).build()
        analyticsExtension.handleIncomingEvent(lifecycleStartEvent)
        val event3 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName"
            )
        ).build()

        analyticsExtension.handleIncomingEvent(event3)
        dispatchGetQueueSizeEvent(analyticsExtension)

        countDownLatch.await()
        Assert.assertEquals(2, queueSize1)
        Assert.assertEquals(3, queueSize2)
    }

    @Test(timeout = 10000)
    fun `force hit processing`() {
        val countDownLatch = CountDownLatch(2)
        monitorNetwork { request ->
            if (request.url.startsWith("https://test.com/b/ss/rsid/0/")) {
                countDownLatch.countDown()
            }
        }

        val analyticsExtension = initializeAnalyticsExtensionWithPreset(
            mapOf(
                "analytics.server" to "test.com",
                "analytics.rsids" to "rsid",
                "global.privacy" to "optedin",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 3,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 5
            ),
            mapOf(
                "mid" to "mid",
                "blob" to "blob",
                "locationhint" to "lochint"
            )
        )

        val event1 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(mapOf("action" to "testActionName")).build()
        val event2 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(mapOf("action" to "testActionName")).build()

        analyticsExtension.handleIncomingEvent(event1)
        analyticsExtension.handleIncomingEvent(event2)

        analyticsExtension.handleIncomingEvent(
            Event.Builder(
                "ForceKickHits",
                EventType.ANALYTICS,
                EventSource.REQUEST_CONTENT
            ).setEventData(mapOf("forcekick" to true)).build()
        )

        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun `clear queue`() {
        val countDownLatch = CountDownLatch(1)
        var queueSize = -1
        Mockito.`when`(mockedExtensionApi.dispatch(any())).then { invocation ->
            val event = invocation.arguments[0] as? Event
            if ((event?.type == EventType.ANALYTICS) && (event.source == EventSource.RESPONSE_CONTENT) && event.eventData.containsKey(
                    "queuesize"
                )
            ) {
                queueSize = DataReader.optInt(event.eventData, "queuesize", 0)
                countDownLatch.countDown()
            }
        }
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(
            mapOf(
                "analytics.server" to "test.com",
                "analytics.rsids" to "rsid",
                "global.privacy" to "optedin",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 0,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 5
            ),
            mapOf(
                "mid" to "mid",
                "blob" to "blob",
                "locationhint" to "lochint"
            )
        )

        val event1 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName"
            )
        ).build()

        val event2 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName"
            )
        ).build()

        analyticsExtension.handleIncomingEvent(event1)
        analyticsExtension.handleIncomingEvent(event2)

        analyticsExtension.handleIncomingEvent(
            Event.Builder(
                "Clear hit queue",
                EventType.ANALYTICS,
                EventSource.REQUEST_CONTENT
            ).setEventData(
                mapOf(
                    "clearhitsqueue" to true
                )
            ).build()
        )

        dispatchGetQueueSizeEvent(analyticsExtension)

        countDownLatch.await()
        Assert.assertEquals(0, queueSize)
    }

    @Test(timeout = 10000)
    fun `hits batch limit`() {
        Thread.sleep(1000)
        val countDownLatch = CountDownLatch(3)
        var requestCount = 0
        monitorNetwork { request ->
            if (request.url.startsWith("https://test.com/b/ss/rsid/0/")) {
                requestCount++
                countDownLatch.countDown()
            }
        }

        val analyticsExtension = initializeAnalyticsExtensionWithPreset(
            mapOf(
                "analytics.server" to "test.com",
                "analytics.rsids" to "rsid",
                "global.privacy" to "optedin",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 0,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 5,
                "analytics.batchLimit" to 2
            ),
            mapOf(
                "mid" to "mid",
                "blob" to "blob",
                "locationhint" to "lochint"
            )
        )

        val event1 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(mapOf("action" to "testActionName")).build()
        val event2 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(mapOf("action" to "testActionName")).build()
        val event3 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(mapOf("action" to "testActionName")).build()

        analyticsExtension.handleIncomingEvent(event1)
        analyticsExtension.handleIncomingEvent(event2)
        Thread.sleep(500)
        Assert.assertEquals(0, requestCount)
        analyticsExtension.handleIncomingEvent(event3)

        countDownLatch.await()
        Assert.assertEquals(3, requestCount)
    }

    @Test(timeout = 10000)
    fun `drop hits when identity reset`() {
        val countDownLatch = CountDownLatch(2)
        var queueSize1 = -1
        var queueSize2 = -1
        Mockito.`when`(mockedExtensionApi.dispatch(any())).then { invocation ->
            val event = invocation.arguments[0] as? Event
            if ((event?.type == EventType.ANALYTICS) && (event.source == EventSource.RESPONSE_CONTENT) && event.eventData.containsKey(
                    "queuesize"
                )
            ) {
                if (queueSize1 < 0) {
                    queueSize1 = DataReader.optInt(event.eventData, "queuesize", 0)
                } else {
                    queueSize2 = DataReader.optInt(event.eventData, "queuesize", 0)
                }
                countDownLatch.countDown()
            }
        }
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(
            mapOf(
                "analytics.server" to "test.com",
                "analytics.rsids" to "rsid",
                "global.privacy" to "optedin",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 0,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1,
                "analytics.batchLimit" to 5
            ),
            mapOf(
                "mid" to "mid",
                "blob" to "blob",
                "locationhint" to "lochint"
            )
        )

        val event1 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName"
            )
        ).build()

        val event2 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName"
            )
        ).build()
        val event3 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName"
            )
        ).build()
        val event4 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName"
            )
        ).build()

        analyticsExtension.handleIncomingEvent(event1)
        analyticsExtension.handleIncomingEvent(event2)
        analyticsExtension.handleIncomingEvent(event3)
        analyticsExtension.handleIncomingEvent(event4)

        dispatchGetQueueSizeEvent(analyticsExtension)

        analyticsExtension.handleIncomingEvent(
            Event.Builder(
                "reset event",
                EventType.GENERIC_IDENTITY,
                EventSource.REQUEST_RESET
            ).build()
        )

        dispatchGetQueueSizeEvent(analyticsExtension)

        countDownLatch.await()
        Assert.assertEquals(4, queueSize1)
        Assert.assertEquals(0, queueSize2)
    }

}