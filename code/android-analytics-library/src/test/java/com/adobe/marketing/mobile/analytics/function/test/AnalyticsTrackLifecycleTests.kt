package com.adobe.marketing.mobile.analytics.function.test

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.analytics.TimeZone
import com.adobe.marketing.mobile.analytics.extractContextDataFrom
import com.adobe.marketing.mobile.analytics.extractQueryParamsFrom
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch

internal class AnalyticsTrackLifecycleTests : AnalyticsFunctionalTestBase() {
    @Test(timeout = 10000)
    fun `hits should contain lifecycle vars`() {
        val countDownLatch = CountDownLatch(2)
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
                "runmode" to "Application"
            )
        )
        updateMockedSharedState("com.adobe.module.lifecycle", lifecycleResponseData)
        val lifecycleResponseEvent = Event.Builder(
            "lifecycle event",
            EventType.LIFECYCLE,
            EventSource.RESPONSE_CONTENT
        ).setEventData(lifecycleResponseData).build()

        analyticsExtension.handleIncomingEvent(lifecycleResponseEvent)

        val trackEvent = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName",
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
            "pev2" to "AMACTION:testActionName",
            "pageName" to "mockAppName",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZone.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to lifecycleResponseEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "a.action" to "testActionName",
            "a.AppID" to "mockAppName",
            "a.CarrierName" to "mockMobileCarrier",
            "a.DeviceName" to "mockDeviceBuildId",
            "a.OSVersion" to "mockOSName",
            "a.Resolution" to "0x0",
            "a.RunMode" to "Application"
        )
        Assert.assertTrue(expectedContextData == contextDataMap)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }

    @Test
    fun `lifecycle backdated crash hit`() {
        Mockito.`when`(mockedNameCollection.getLong(any(), anyOrNull()))
            .thenAnswer { invocation ->
                when (invocation.arguments[0] as? String) {
                    "mostRecentHitTimestampSeconds" -> {
                        return@thenAnswer 1234567899999L
                    }
                    else -> {
                        return@thenAnswer 0L
                    }
                }

            }

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
                "crashevent" to "CrashEvent",
                "previousosversion" to "previousOSVersion",
                "previousappid" to "previousAppId"
            )
        )
        updateMockedSharedState("com.adobe.module.lifecycle", lifecycleResponseData)
        val lifecycleResponseEvent = Event.Builder(
            "lifecycle event",
            EventType.LIFECYCLE,
            EventSource.RESPONSE_CONTENT
        ).setEventData(lifecycleResponseData).build()

        analyticsExtension.handleIncomingEvent(lifecycleResponseEvent)

        countDownLatch.await()
        val expectedVars1: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "ADBINTERNAL:Crash",
            "pageName" to "mockAppName",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZone.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to "1234567900000"
        )
        val expectedContextData1: Map<String, String> = mapOf(
            "a.CrashEvent" to "CrashEvent",
            "a.internalaction" to "Crash",
            "a.AppID" to "previousAppId",
            "a.CarrierName" to "mockMobileCarrier",
            "a.DeviceName" to "mockDeviceBuildId",
            "a.OSVersion" to "previousOSVersion",
            "a.Resolution" to "0x0",
            "a.RunMode" to "Application"
        )
        Assert.assertTrue(expectedContextData1 == contextDataMap1)
        Assert.assertEquals(expectedVars1.size, varMap1.size)
        Assert.assertEquals(expectedVars1, varMap1)

        val expectedVars2: Map<String, String> = mapOf(
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
        val expectedContextData2: Map<String, String> = mapOf(
            "a.internalaction" to "Lifecycle",
            "a.AppID" to "mockAppName",
            "a.CarrierName" to "mockMobileCarrier",
            "a.DeviceName" to "mockDeviceBuildId",
            "a.OSVersion" to "mockOSName",
            "a.Resolution" to "0x0",
            "a.RunMode" to "Application",
            "a.locale" to "en-US"
        )
        Assert.assertTrue(expectedContextData2 == contextDataMap2)
        Assert.assertEquals(expectedVars2.size, varMap2.size)
        Assert.assertEquals(expectedVars2, varMap2)
    }

    @Test
    fun `lifecycle backdated session info hit`() {
        Mockito.`when`(mockedNameCollection.getLong(any(), anyOrNull()))
            .thenAnswer { invocation ->
                when (invocation.arguments[0] as? String) {
                    "mostRecentHitTimestampSeconds" -> {
                        return@thenAnswer 1234567899999L
                    }
                    else -> {
                        return@thenAnswer 0L
                    }
                }

            }
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
        val currentTs = System.currentTimeMillis()
        val sessionStartTs = currentTs - 10
        val previousSessionPauseTs = currentTs - 20
        val lifecycleResponseData = mapOf(
            "lifecyclecontextdata" to mapOf(
                "osversion" to "mockOSName",
                "locale" to "en-US",
                "resolution" to "0x0",
                "carriername" to "mockMobileCarrier",
                "devicename" to "mockDeviceBuildId",
                "appid" to "mockAppName",
                "runmode" to "Application",
                "prevsessionlength" to "100",
                "previousosversion" to "previousOSVersion",
                "previousappid" to "previousAppId"
            ),
            "starttimestampmillis" to sessionStartTs,
            "previoussessionpausetimestampmillis" to previousSessionPauseTs
        )
        updateMockedSharedState("com.adobe.module.lifecycle", lifecycleResponseData)
        val lifecycleResponseEvent = Event.Builder(
            "lifecycle event",
            EventType.LIFECYCLE,
            EventSource.RESPONSE_CONTENT
        ).setEventData(lifecycleResponseData).build()

        analyticsExtension.handleIncomingEvent(lifecycleResponseEvent)

        countDownLatch.await()
        val expectedVars1: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "ADBINTERNAL:SessionInfo",
            "pageName" to "mockAppName",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZone.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to "1234567900000"
        )
        val expectedContextData1: Map<String, String> = mapOf(
            "a.internalaction" to "SessionInfo",
            "a.AppID" to "previousAppId",
            "a.CarrierName" to "mockMobileCarrier",
            "a.DeviceName" to "mockDeviceBuildId",
            "a.OSVersion" to "previousOSVersion",
            "a.Resolution" to "0x0",
            "a.RunMode" to "Application",
            "a.PrevSessionLength" to "100"
        )
        Assert.assertTrue(expectedContextData1 == contextDataMap1)
        Assert.assertEquals(expectedVars1.size, varMap1.size)
        Assert.assertEquals(expectedVars1, varMap1)

        val expectedVars2: Map<String, String> = mapOf(
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
        val expectedContextData2: Map<String, String> = mapOf(
            "a.internalaction" to "Lifecycle",
            "a.AppID" to "mockAppName",
            "a.CarrierName" to "mockMobileCarrier",
            "a.DeviceName" to "mockDeviceBuildId",
            "a.OSVersion" to "mockOSName",
            "a.Resolution" to "0x0",
            "a.RunMode" to "Application",
            "a.locale" to "en-US"
        )
        Assert.assertTrue(expectedContextData2 == contextDataMap2)
        Assert.assertEquals(expectedVars2.size, varMap2.size)
        Assert.assertEquals(expectedVars2, varMap2)
    }

    @Test
    fun `hit contains time since launch`() {
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

        updateMockedSharedState(
            "com.adobe.module.lifecycle", mapOf(
                "lifecyclecontextdata" to mapOf(
                    "osversion" to "mockOSName",
                    "locale" to "en-US",
                    "resolution" to "0x0",
                    "carriername" to "mockMobileCarrier",
                    "devicename" to "mockDeviceBuildId",
                    "appid" to "mockAppName",
                    "runmode" to "Application"
                ),
                "starttimestampmillis" to System.currentTimeMillis(),
                "maxsessionlength" to 300.0
            )
        )

        val trackEvent = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName",
                "contextdata" to mapOf(
                    "k1" to "v1",
                    "k2" to "v2"
                )
            )
        ).build()

        val eventTimeStampAfter10Sec = trackEvent.timestamp + 10 * 1000
        val timestampField =
            trackEvent.javaClass.getDeclaredField("timestamp")
        timestampField.isAccessible = true
        timestampField.set(trackEvent, eventTimeStampAfter10Sec)
        analyticsExtension.handleIncomingEvent(trackEvent)

        countDownLatch.await()
        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "AMACTION:testActionName",
            "pageName" to "mockAppName",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZone.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "a.action" to "testActionName",
            "a.AppID" to "mockAppName",
            "a.CarrierName" to "mockMobileCarrier",
            "a.DeviceName" to "mockDeviceBuildId",
            "a.OSVersion" to "mockOSName",
            "a.Resolution" to "0x0",
            "a.RunMode" to "Application",
            "a.TimeSinceLaunch" to "10"
        )
        Assert.assertTrue(expectedContextData == contextDataMap)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }

}