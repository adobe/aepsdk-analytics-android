package com.adobe.marketing.mobile.analytics.function.test

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.analytics.extractContextDataFrom
import com.adobe.marketing.mobile.analytics.extractQueryParamsFrom
import org.junit.Test
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch

internal class HitReorderTests : AnalyticsFunctionalTestBase() {

    @Test
    fun `lifecycle and acquisition data should append to the first analytics hit`() {
        val countDownLatch = CountDownLatch(1)
        var varMap: Map<String, Any> = emptyMap()
        var contextDataMap: Map<String, Any> = emptyMap()
        monitorNetwork { request ->
            if (request.url.startsWith("https://test.com")) {
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
            "track sate",
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
        ).setEventData(lifecycleResponseData).build()
        analyticsExtension.handleIncomingEvent(acquisitionEvent)

        countDownLatch.await()

    }
}