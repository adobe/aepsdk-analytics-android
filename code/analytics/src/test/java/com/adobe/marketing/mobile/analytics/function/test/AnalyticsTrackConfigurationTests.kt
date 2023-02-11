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

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.analytics.internal.TimeZoneHelper
import com.adobe.marketing.mobile.analytics.internal.extractContextDataFrom
import com.adobe.marketing.mobile.analytics.internal.extractQueryParamsFrom
import com.adobe.marketing.mobile.util.DataReader
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.verify
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class AnalyticsTrackConfigurationTests : AnalyticsFunctionalTestBase() {
    @Test(timeout = 10000)
    fun `optout - clear queued hits and data store`() {
        Mockito.`when`(mockedNameCollection.getString(any(), anyOrNull()))
            .thenAnswer { invocation ->
                when (invocation.arguments[0] as? String) {
                    "ADOBEMOBILE_STOREDDEFAULTS_AID" -> {
                        return@thenAnswer "testaid"
                    }
                    "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER" -> {
                        return@thenAnswer "testvid"
                    }
                    else -> {
                        return@thenAnswer ""
                    }
                }
            }

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
                "global.privacy" to "unknown",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 0,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1
            ),
            defaultIdentity()
        )

        val trackEvent1 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName"
            )
        ).build()
        val trackEvent2 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName"
            )
        ).build()
        val trackEvent3 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName"
            )
        ).build()

        analyticsExtension.handleIncomingEvent(trackEvent1)
        analyticsExtension.handleIncomingEvent(trackEvent2)
        analyticsExtension.handleIncomingEvent(trackEvent3)

        dispatchGetQueueSizeEvent(analyticsExtension)

        updateMockedSharedState(
            "com.adobe.module.configuration",
            mapOf(
                "analytics.server" to "test.com",
                "analytics.rsids" to "rsid",
                "global.privacy" to "optedout",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 0,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1
            )
        )
        analyticsExtension.handleIncomingEvent(
            Event.Builder(
                "second configuration event",
                EventType.CONFIGURATION,
                EventSource.RESPONSE_CONTENT
            ).build()
        )
        dispatchGetQueueSizeEvent(analyticsExtension)

        countDownLatch.await()

        Assert.assertEquals(3, queueSize1)
        Assert.assertEquals(0, queueSize2)

        verify(mockedNameCollection, atLeast(1)).remove("ADOBEMOBILE_STOREDDEFAULTS_AID")
        verify(
            mockedNameCollection,
            atLeast(1)
        ).remove("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER")
    }

    @Test(timeout = 10000)
    fun `unknown to optedin - send out hits`() {
        val countDownLatch = CountDownLatch(1)
        var varMap: Map<String, Any> = emptyMap()
        var contextDataMap: Map<String, Any> = emptyMap()
        monitorNetwork { request ->
            if (request.url.startsWith("https://test.com/b/ss/rsid/0/")) {
                val body = URLDecoder.decode(String(request.body), "UTF-8")
                varMap = extractQueryParamsFrom(body)
                contextDataMap = extractContextDataFrom(body)
                countDownLatch.countDown()
            }
        }

        val analyticsExtension = initializeAnalyticsExtensionWithPreset(
            mapOf(
                "analytics.server" to "test.com",
                "analytics.rsids" to "rsid",
                "global.privacy" to "unknown",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 0,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1
            ),
            defaultIdentity()
        )

        val trackEvent = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "state" to "testState",
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2"
                )
            )
        ).build()

        analyticsExtension.handleIncomingEvent(trackEvent)

        updateMockedSharedState(
            "com.adobe.module.configuration",
            mapOf(
                "analytics.server" to "test.com",
                "analytics.rsids" to "rsid",
                "global.privacy" to "optedin",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 0,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1
            )
        )
        analyticsExtension.handleIncomingEvent(
            Event.Builder(
                "configuration event",
                EventType.CONFIGURATION,
                EventSource.RESPONSE_CONTENT
            ).build()
        )

        countDownLatch.await()

        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pageName" to "testState",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "a.privacy.mode" to "unknown"
        )
        Assert.assertTrue(expectedContextData == contextDataMap)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }

    @Test(timeout = 10000)
    fun `tack hits only sent on valid configuration`() {
        val countDownLatch = CountDownLatch(1)
        var varMap: Map<String, Any> = emptyMap()
        var contextDataMap: Map<String, Any> = emptyMap()
        monitorNetwork { request ->
            if (request.url.startsWith("https://test.com/b/ss/rsid/0/")) {
                val body = URLDecoder.decode(String(request.body), "UTF-8")
                varMap = extractQueryParamsFrom(body)
                contextDataMap = extractContextDataFrom(body)
                countDownLatch.countDown()
            }
        }

        val analyticsExtension = initializeAnalyticsExtensionWithPreset(
            mapOf(
                "analytics.server" to "",
                "analytics.rsids" to "",
                "global.privacy" to "optedin",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 0,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1
            ),
            defaultIdentity()
        )

        val trackEvent1 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "state" to "testState1",
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2"
                )
            )
        ).build()

        analyticsExtension.handleIncomingEvent(trackEvent1)

        updateMockedSharedState(
            "com.adobe.module.configuration",
            mapOf(
                "analytics.server" to "test.com",
                "analytics.rsids" to "rsid",
                "global.privacy" to "optedin",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 0,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1
            )
        )
        analyticsExtension.handleIncomingEvent(
            Event.Builder(
                "configuration event",
                EventType.CONFIGURATION,
                EventSource.RESPONSE_CONTENT
            ).build()
        )

        val trackEvent2 = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "state" to "testState2",
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2"
                )
            )
        ).build()
        analyticsExtension.handleIncomingEvent(trackEvent2)
        countDownLatch.await()

        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pageName" to "testState2",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent2.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "k1" to "v1",
            "k2" to "v2"
        )
        Assert.assertTrue(expectedContextData == contextDataMap)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }

    @Test(timeout = 10000)
    fun `offline hits should not contain timestamp`() {
        val countDownLatch = CountDownLatch(1)
        var varMap: Map<String, Any> = emptyMap()
        var contextDataMap: Map<String, Any> = emptyMap()
        monitorNetwork { request ->
            if (request.url.startsWith("https://test.com/b/ss/rsid/0/")) {
                val body = URLDecoder.decode(String(request.body), "UTF-8")
                varMap = extractQueryParamsFrom(body)
                contextDataMap = extractContextDataFrom(body)
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
                "analytics.offlineEnabled" to false,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1
            ),
            defaultIdentity()
        )

        val trackEvent = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "state" to "testState",
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2"
                )
            )
        ).build()

        analyticsExtension.handleIncomingEvent(trackEvent)

        countDownLatch.await()

        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pageName" to "testState",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET
        )
        val expectedContextData: Map<String, String> = mapOf(
            "k1" to "v1",
            "k2" to "v2"
        )
        Assert.assertTrue(expectedContextData == contextDataMap)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }

    @Test(timeout = 10000)
    fun `offline hits should be dropped after 60s`() {
        val countDownLatch = CountDownLatch(1)
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
                "analytics.batchLimit" to 0,
                "analytics.offlineEnabled" to false,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1
            ),
            defaultIdentity()
        )

        val trackEvent = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "state" to "testState",
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2"
                )
            )
        ).build()

        val timestampField =
            trackEvent.javaClass.getDeclaredField("timestamp")
        timestampField.isAccessible = true
        timestampField.set(trackEvent, trackEvent.timestamp - 61 * 1000)

        analyticsExtension.handleIncomingEvent(trackEvent)

        assertFalse(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }
}
