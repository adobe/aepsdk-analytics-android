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
import com.adobe.marketing.mobile.analytics.TimeZone
import com.adobe.marketing.mobile.analytics.extractContextDataFrom
import com.adobe.marketing.mobile.analytics.extractQueryParamsFrom
import org.junit.Assert
import org.junit.Test
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch

internal class HitReorderTests : AnalyticsFunctionalTestBase() {

    @Test(timeout = 10000)
    fun `lifecycle and acquisition data should append to the first analytics hit`() {
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
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1
            ),
            mapOf(
                "mid" to "mid",
                "blob" to "blob",
                "locationhint" to "lochint"
            )
        )
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

        val trackEvent = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "state" to "testState",
                "contextdata" to mapOf(
                    "k1" to "v1"
                )
            )
        ).build()

        analyticsExtension.handleIncomingEvent(trackEvent)

        val lifecycleResponseData = mapOf(
            "lifecyclecontextdata" to mapOf(
                "lifecyclekey" to "value",
                "installevent" to "InstallEvent"
            )
        )
        updateMockedSharedState("com.adobe.module.lifecycle", lifecycleResponseData)
        val lifecycleResponseEvent = Event.Builder(
            "lifecycle event",
            EventType.LIFECYCLE,
            EventSource.RESPONSE_CONTENT
        ).setEventData(lifecycleResponseData).build()

        analyticsExtension.handleIncomingEvent(lifecycleResponseEvent)

        val acquisitionData = mapOf(
            "contextdata" to mapOf(
                "a.referrerkey" to "value"
            )
        )
        updateMockedSharedState("com.adobe.module.acquisition", acquisitionData)
        val acquisitionEvent = Event.Builder(
            "acquisition event",
            EventType.ACQUISITION,
            EventSource.RESPONSE_CONTENT
        ).setEventData(acquisitionData).build()
        analyticsExtension.handleIncomingEvent(acquisitionEvent)

        countDownLatch.await()
        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pageName" to "testState",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZone.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "a.InstallEvent" to "InstallEvent",
            "a.referrerkey" to "value",
            "k1" to "v1",
            "lifecyclekey" to "value"
        )
        Assert.assertTrue(expectedContextData == contextDataMap)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }

    @Test(timeout = 10000)
    fun `acquisition data sent out on second hit if referrer timer is exceeded`() {
        val countDownLatch = CountDownLatch(2)
        var varMap1: Map<String, Any> = emptyMap()
        var varMap2: Map<String, Any> = emptyMap()
        var contextDataMap1: Map<String, Any> = emptyMap()
        var contextDataMap2: Map<String, Any> = emptyMap()
        monitorNetwork { request ->
            if (request.url.startsWith("https://test.com/b/ss/rsid/0/")) {
                val body = URLDecoder.decode(String(request.body), "UTF-8")
                if (varMap1.isEmpty()) {
                    varMap1 = extractQueryParamsFrom(body)
                    contextDataMap1 = extractContextDataFrom(body)
                } else {
                    varMap2 = extractQueryParamsFrom(body)
                    contextDataMap2 = extractContextDataFrom(body)
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
                "analytics.launchHitDelay" to 1
            ),
            mapOf(
                "mid" to "mid",
                "blob" to "blob",
                "locationhint" to "lochint"
            )
        )
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

        val trackEvent = Event.Builder(
            "track sate",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "start",
                "contextdata" to mapOf(
                    "k1" to "v1"
                )
            )
        ).build()

        analyticsExtension.handleIncomingEvent(trackEvent)

        val lifecycleResponseData = mapOf(
            "lifecyclecontextdata" to mapOf(
                "lifecyclekey" to "value",
                "installevent" to "InstallEvent"
            )
        )
        updateMockedSharedState("com.adobe.module.lifecycle", lifecycleResponseData)
        val lifecycleResponseEvent = Event.Builder(
            "lifecycle event",
            EventType.LIFECYCLE,
            EventSource.RESPONSE_CONTENT
        ).setEventData(lifecycleResponseData).build()

        analyticsExtension.handleIncomingEvent(lifecycleResponseEvent)
        var acquisitionEvent: Event? = null
        Thread {
            Thread.sleep(1500L)
            val acquisitionData = mapOf(
                "contextdata" to mapOf(
                    "test_key_1" to "test_value_1",
                    "a.deeplink.id" to "test_deeplinkId",
                    "test_key_0" to "test_value_0"
                )
            )
            updateMockedSharedState("com.adobe.module.acquisition", acquisitionData)
            val event = Event.Builder(
                "acquisition event",
                EventType.ACQUISITION,
                EventSource.RESPONSE_CONTENT
            ).setEventData(acquisitionData).build()
            acquisitionEvent = event
            analyticsExtension.handleIncomingEvent(event)
        }.start()

        countDownLatch.await()
        val expectedVars1: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "AMACTION:start",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZone.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData1: Map<String, String> = mapOf(
            "k1" to "v1",
            "a.action" to "start",
            "a.InstallEvent" to "InstallEvent",
            "lifecyclekey" to "value"
        )
        Assert.assertTrue(expectedContextData1 == contextDataMap1)
        Assert.assertEquals(expectedVars1.size, varMap1.size)
        Assert.assertEquals(expectedVars1, varMap1)

        val expectedVars2: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "ADBINTERNAL:AdobeLink",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZone.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to acquisitionEvent?.timestampInSeconds.toString()
        )
        val expectedContextData2: Map<String, String> = mapOf(
            "a.internalaction" to "AdobeLink",
            "a.deeplink.id" to "test_deeplinkId",
            "test_key_0" to "test_value_0",
            "test_key_1" to "test_value_1",
        )
        Assert.assertTrue(expectedContextData2 == contextDataMap2)
        Assert.assertEquals(expectedVars2.size, varMap2.size)
        Assert.assertEquals(expectedVars2, varMap2)
    }

    @Test(timeout = 10000)
    fun `lifecycle and acquisition events dispatched after the track event`() {
        val countDownLatch = CountDownLatch(2)
        var varMap1: Map<String, Any> = emptyMap()
        var varMap2: Map<String, Any> = emptyMap()
        var contextDataMap1: Map<String, Any> = emptyMap()
        var contextDataMap2: Map<String, Any> = emptyMap()
        monitorNetwork { request ->
            if (request.url.startsWith("https://test.com/b/ss/rsid/0/")) {
                val body = URLDecoder.decode(String(request.body), "UTF-8")
                if (varMap1.isEmpty()) {
                    varMap1 = extractQueryParamsFrom(body)
                    contextDataMap1 = extractContextDataFrom(body)
                } else {
                    varMap2 = extractQueryParamsFrom(body)
                    contextDataMap2 = extractContextDataFrom(body)
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
                "analytics.launchHitDelay" to 1
            ),
            mapOf(
                "mid" to "mid",
                "blob" to "blob",
                "locationhint" to "lochint"
            )
        )
        val trackEvent = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "start",
                "contextdata" to mapOf(
                    "k1" to "v1"
                )
            )
        ).build()

        analyticsExtension.handleIncomingEvent(trackEvent)

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


        val lifecycleResponseData = mapOf(
            "lifecyclecontextdata" to mapOf(
                "lifecyclekey" to "value",
                "installevent" to "InstallEvent"
            )
        )
        updateMockedSharedState("com.adobe.module.lifecycle", lifecycleResponseData)
        val lifecycleResponseEvent = Event.Builder(
            "lifecycle event",
            EventType.LIFECYCLE,
            EventSource.RESPONSE_CONTENT
        ).setEventData(lifecycleResponseData).build()

        analyticsExtension.handleIncomingEvent(lifecycleResponseEvent)

        val acquisitionData = mapOf(
            "contextdata" to mapOf(
                "test_key_1" to "test_value_1",
                "a.deeplink.id" to "test_deeplinkId",
                "test_key_0" to "test_value_0"
            )
        )
        updateMockedSharedState("com.adobe.module.acquisition", acquisitionData)
        val acquisitionEvent = Event.Builder(
            "acquisition event",
            EventType.ACQUISITION,
            EventSource.RESPONSE_CONTENT
        ).setEventData(acquisitionData).build()
        analyticsExtension.handleIncomingEvent(acquisitionEvent)

        countDownLatch.await()
        val expectedVars1: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "AMACTION:start",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZone.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData1: Map<String, String> = mapOf(
            "k1" to "v1",
            "a.action" to "start",
        )
        Assert.assertTrue(expectedContextData1 == contextDataMap1)
        Assert.assertEquals(expectedVars1.size, varMap1.size)
        Assert.assertEquals(expectedVars1, varMap1)

        val expectedVars2: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "ADBINTERNAL:Lifecycle",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZone.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to lifecycleResponseEvent?.timestampInSeconds.toString()
        )
        val expectedContextData2: Map<String, String> = mapOf(
            "a.InstallEvent" to "InstallEvent",
            "lifecyclekey" to "value",
            "a.internalaction" to "Lifecycle",
            "a.deeplink.id" to "test_deeplinkId",
            "test_key_0" to "test_value_0",
            "test_key_1" to "test_value_1"
        )
        Assert.assertTrue(expectedContextData2 == contextDataMap2)
        Assert.assertEquals(expectedVars2.size, varMap2.size)
        Assert.assertEquals(expectedVars2, varMap2)
    }

    @Test(timeout = 10000)
    fun `track event should wait for processing of lifecycle and acquisition data`() {
        val countDownLatch = CountDownLatch(2)
        var varMap1: Map<String, Any> = emptyMap()
        var varMap2: Map<String, Any> = emptyMap()
        var contextDataMap1: Map<String, Any> = emptyMap()
        var contextDataMap2: Map<String, Any> = emptyMap()
        monitorNetwork { request ->
            if (request.url.startsWith("https://test.com/b/ss/rsid/0/")) {
                val body = URLDecoder.decode(String(request.body), "UTF-8")
                if (varMap1.isEmpty()) {
                    varMap1 = extractQueryParamsFrom(body)
                    contextDataMap1 = extractContextDataFrom(body)
                } else {
                    varMap2 = extractQueryParamsFrom(body)
                    contextDataMap2 = extractContextDataFrom(body)
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
                "analytics.launchHitDelay" to 5
            ),
            mapOf(
                "mid" to "mid",
                "blob" to "blob",
                "locationhint" to "lochint"
            )
        )
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

        val lifecycleResponseData = mapOf(
            "lifecyclecontextdata" to mapOf(
                "osversion" to "mockOSName",
                "locale" to "en-US",
                "resolution" to "0x0",
                "carriername" to "mockMobileCarrier",
                "devicename" to "mockDeviceBuildId",
                "appid" to "mockAppName",
                "runmode" to "Application",
                "installevent" to "InstallEvent",
                "launchevent" to "LaunchEvent",
                "monthlyenguserevent" to "MonthlyEngUserEvent"
            )
        )
        updateMockedSharedState("com.adobe.module.lifecycle", lifecycleResponseData)
        val lifecycleResponseEvent = Event.Builder(
            "lifecycle event",
            EventType.LIFECYCLE,
            EventSource.RESPONSE_CONTENT
        ).setEventData(lifecycleResponseData).build()

        analyticsExtension.handleIncomingEvent(lifecycleResponseEvent)

        val acquisitionData = mapOf(
            "contextdata" to mapOf(
                "test_key_1" to "test_value_1",
                "a.deeplink.id" to "test_deeplinkId",
                "test_key_0" to "test_value_0"
            )
        )
        updateMockedSharedState("com.adobe.module.acquisition", acquisitionData)
        val acquisitionEvent = Event.Builder(
            "acquisition event",
            EventType.ACQUISITION,
            EventSource.RESPONSE_CONTENT
        ).setEventData(acquisitionData).build()
        analyticsExtension.handleIncomingEvent(acquisitionEvent)

        val trackEvent = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "start",
                "contextdata" to mapOf(
                    "k1" to "v1"
                )
            )
        ).build()

        analyticsExtension.handleIncomingEvent(trackEvent)

        countDownLatch.await()
        val expectedVars1: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "ADBINTERNAL:Lifecycle",
            "pageName" to "mockAppName",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZone.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to lifecycleResponseEvent.timestampInSeconds.toString()
        )
        val expectedContextData1: Map<String, String> = mapOf(
            "a.locale" to "en-US",
            "a.AppID" to "mockAppName",
            "a.CarrierName" to "mockMobileCarrier",
            "a.DeviceName" to "mockDeviceBuildId",
            "a.OSVersion" to "mockOSName",
            "a.Resolution" to "0x0",
            "a.RunMode" to "Application",
            "a.internalaction" to "Lifecycle",
            "a.LaunchEvent" to "LaunchEvent",
            "a.InstallEvent" to "InstallEvent",
            "a.MonthlyEngUserEvent" to "MonthlyEngUserEvent",
            "a.deeplink.id" to "test_deeplinkId",
            "test_key_0" to "test_value_0",
            "test_key_1" to "test_value_1"
        )
        Assert.assertTrue(expectedContextData1 == contextDataMap1)
        Assert.assertEquals(expectedVars1.size, varMap1.size)
        Assert.assertEquals(expectedVars1, varMap1)

        val expectedVars2: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "AMACTION:start",
            "pageName" to "mockAppName",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZone.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData2: Map<String, String> = mapOf(
            "k1" to "v1",
            "a.action" to "start",
            "a.AppID" to "mockAppName",
            "a.CarrierName" to "mockMobileCarrier",
            "a.DeviceName" to "mockDeviceBuildId",
            "a.OSVersion" to "mockOSName",
            "a.Resolution" to "0x0",
            "a.RunMode" to "Application"
        )
        Assert.assertTrue(expectedContextData2 == contextDataMap2)
        Assert.assertEquals(expectedVars2.size, varMap2.size)
        Assert.assertEquals(expectedVars2, varMap2)
    }

    @Test(timeout = 10000)
    fun `acquisition sent as a seperate hit`() {
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
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1
            ),
            mapOf(
                "mid" to "mid",
                "blob" to "blob",
                "locationhint" to "lochint"
            )
        )

        val acquisitionData = mapOf(
            "contextdata" to mapOf(
                "test_key_1" to "test_value_1",
                "a.deeplink.id" to "test_deeplinkId",
                "test_key_0" to "test_value_0"
            )
        )
        updateMockedSharedState("com.adobe.module.acquisition", acquisitionData)
        val acquisitionEvent = Event.Builder(
            "acquisition event",
            EventType.ACQUISITION,
            EventSource.RESPONSE_CONTENT
        ).setEventData(acquisitionData).build()
        analyticsExtension.handleIncomingEvent(acquisitionEvent)

        countDownLatch.await()
        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "ADBINTERNAL:AdobeLink",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZone.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to acquisitionEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "a.internalaction" to "AdobeLink",
            "a.deeplink.id" to "test_deeplinkId",
            "test_key_0" to "test_value_0",
            "test_key_1" to "test_value_1"
        )
        Assert.assertTrue(expectedContextData == contextDataMap)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }
}