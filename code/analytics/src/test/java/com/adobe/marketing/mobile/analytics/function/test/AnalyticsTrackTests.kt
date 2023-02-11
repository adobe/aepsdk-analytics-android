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
import com.adobe.marketing.mobile.MobilePrivacyStatus
import com.adobe.marketing.mobile.analytics.internal.TimeZoneHelper
import com.adobe.marketing.mobile.analytics.internal.extractContextDataFrom
import com.adobe.marketing.mobile.analytics.internal.extractContextDataKVPairFrom
import com.adobe.marketing.mobile.analytics.internal.extractQueryParamsFrom
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch

internal class AnalyticsTrackTests : AnalyticsFunctionalTestBase() {
    @Test(timeout = 10000)
    fun `track state`() {
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
            config(MobilePrivacyStatus.OPT_IN),
            defaultIdentity()
        )

        val trackEvent = trackEventWithData(
            mapOf(
                "state" to "testState",
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2"
                )
            )
        )

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
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
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
    fun `track action`() {
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
            config(MobilePrivacyStatus.OPT_IN),
            defaultIdentity()
        )

        val trackEvent = trackEventWithData(
            mapOf(
                "action" to "testAction",
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2"
                )
            )
        )

        analyticsExtension.handleIncomingEvent(trackEvent)

        countDownLatch.await()
        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "AMACTION:testAction",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "a.action" to "testAction"
        )
        Assert.assertTrue(expectedContextData == contextDataMap)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }

    @Test(timeout = 10000)
    fun `track internal action`() {
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
            config(MobilePrivacyStatus.OPT_IN),
            defaultIdentity()
        )

        val trackEvent = trackEventWithData(
            mapOf(
                "action" to "testAction",
                "trackinternal" to true,
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2"
                )
            )
        )

        analyticsExtension.handleIncomingEvent(trackEvent)

        countDownLatch.await()
        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "ADBINTERNAL:testAction",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "a.internalaction" to "testAction"
        )
        Assert.assertTrue(expectedContextData == contextDataMap)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }

    @Test(timeout = 10000)
    fun `track context data only`() {
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
            config(MobilePrivacyStatus.OPT_IN),
            defaultIdentity()
        )

        val trackEvent = trackEventWithData(
            mapOf(
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2"
                )
            )
        )

        analyticsExtension.handleIncomingEvent(trackEvent)

        countDownLatch.await()
        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
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
    fun `track context data overriding`() {
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
            config(MobilePrivacyStatus.OPT_IN),
            defaultIdentity()
        )
        updateMockedSharedState(
            "com.adobe.module.lifecycle",
            mapOf(
                "lifecyclecontextdata" to mapOf(
                    "osversion" to "originalOS",
                    "devicename" to "originalDeviceName",
                    "appid" to "originalAppID"
                )
            )
        )
        val trackEvent = trackEventWithData(
            mapOf(
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2",
                    "a.AppID" to "overwrittenApp",
                    "a.DeviceName" to "overwrittenDevice",
                    "a.OSVersion" to "overwrittenOS"
                )
            )
        )

        analyticsExtension.handleIncomingEvent(trackEvent)

        countDownLatch.await()
        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            // TODO:???
            "pageName" to "originalAppID",
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "a.AppID" to "overwrittenApp",
            "a.DeviceName" to "overwrittenDevice",
            "a.OSVersion" to "overwrittenOS"
        )
        Assert.assertTrue(expectedContextData == contextDataMap)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }

    @Test(timeout = 10000)
    fun `track state and action in one hit`() {
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
            config(MobilePrivacyStatus.OPT_IN),
            defaultIdentity()
        )

        val trackEvent = trackEventWithData(
            mapOf(
                "state" to "testState",
                "action" to "testAction",
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2"
                )
            )
        )

        analyticsExtension.handleIncomingEvent(trackEvent)

        countDownLatch.await()
        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pageName" to "testState",
            "pev2" to "AMACTION:testAction",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "a.action" to "testAction"
        )
        Assert.assertTrue(expectedContextData == contextDataMap)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }

    @Test(timeout = 10000)
    fun `track state and action with special characters`() {
        val countDownLatch = CountDownLatch(1)
        var varMap: Map<String, Any> = emptyMap()
        var contextDataKVPair: String = ""
        monitorNetwork { request ->
            if (request.url.startsWith("https://test.com/b/ss/rsid/0/")) {
                val body = URLDecoder.decode(String(request.body), "UTF-8")
                varMap = extractQueryParamsFrom(body)
                contextDataKVPair = extractContextDataKVPairFrom(body)
                countDownLatch.countDown()
            }
        }

        val analyticsExtension = initializeAnalyticsExtensionWithPreset(
            config(MobilePrivacyStatus.OPT_IN),
            defaultIdentity()
        )

        val trackEvent = trackEventWithData(
            mapOf(
                "state" to "~!@#\$%^&*()_.-+",
                "action" to "网页",
                "contextdata" to mapOf(
                    "~!@#$%^&*()_.-+" to "~!@#$%^&*()_.-+", // Characters other than _ are ignored
                    "网页" to "网页", // This key is ignored
                    "k1" to "网页"
                )
            )
        )

        analyticsExtension.handleIncomingEvent(trackEvent)

        countDownLatch.await()
        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pageName" to "~!@#$%^&*()_.-+",
            "pev2" to "AMACTION:网页",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        Assert.assertTrue("&a.&action=网页&.a&k1=网页&_=~!@#\$%^&*()_.-+" == contextDataKVPair)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }

    @Ignore
    @Test(timeout = 10000)
    fun `track context data with non string values`() {
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
            config(MobilePrivacyStatus.OPT_IN),
            defaultIdentity()
        )

        val trackEvent = trackEventWithData(
            mapOf(
                "contextdata" to mapOf(
                    "StringValue" to "v1",
                    "IntValue" to 1,
                    "FloatValue" to 3.3,
                    "BoolValue" to true,
                    "CharValue" to 'c',
                    // Keys whose values are not String, Number or Character are dropped
                    "Null" to null,
                    "ArrayValue" to emptyArray<Any>(),
                    "ObjValue" to Any(),
                    "DictValue" to emptyMap<Any, Any>()
                )
            )
        )
        // TODO: the event data was set to null because of the above unsupported value types, like emptyArray.

        analyticsExtension.handleIncomingEvent(trackEvent)

        countDownLatch.await()
        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "k1" to "v1",
            "k2" to "v2"
        )
        Assert.assertTrue(expectedContextData == contextDataMap)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }

    @Test
    fun `track request should be ignored when privacy opted out`() {
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(
            config(MobilePrivacyStatus.OPT_OUT),
            defaultIdentity()
        )

        val trackEvent = trackEventWithData(
            mapOf(
                "state" to "testState",
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2"
                )
            )
        )

        analyticsExtension.handleIncomingEvent(trackEvent)

        assertEquals(0, mockedMainDataQueue.count())
    }

    @Test
    fun `track request should be queued when privacy unknown`() {
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(
            config(MobilePrivacyStatus.UNKNOWN),
            defaultIdentity()
        )

        val trackEvent = trackEventWithData(
            mapOf(
                "state" to "testState",
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2"
                )
            )
        )

        analyticsExtension.handleIncomingEvent(trackEvent)

        assertEquals(1, mockedMainDataQueue.count())
    }

    @Test
    fun `track request should be ignored when no data`() {
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(
            config(MobilePrivacyStatus.OPT_IN),
            defaultIdentity()
        )

        val trackEvent = trackEventWithData(emptyMap())

        analyticsExtension.handleIncomingEvent(trackEvent)

        assertEquals(0, mockedMainDataQueue.count())
    }

    private fun trackEventWithData(data: Map<String, Any?>): Event {
        return Event.Builder(
            "Generic track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(data).build()
    }
}
