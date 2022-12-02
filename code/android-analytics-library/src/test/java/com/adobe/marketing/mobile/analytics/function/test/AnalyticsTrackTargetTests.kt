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
import com.adobe.marketing.mobile.analytics.TimeZoneHelper
import com.adobe.marketing.mobile.analytics.extractContextDataFrom
import com.adobe.marketing.mobile.analytics.extractQueryParamsFrom
import org.junit.Assert
import org.junit.Test
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch

internal class AnalyticsTrackTargetTests : AnalyticsFunctionalTestBase() {
    @Test(timeout = 10000)
    fun `analytics hit for A4TTrackAction`() {
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

        val trackEvent = Event.Builder(
            "A4T track action event",
            EventType.ANALYTICS,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "AnalyticsForTarget",
                "trackinternal" to true,
                "contextdata" to mapOf(
                    "&&tnta" to "285408:0:0|2",
                    "&&pe" to "tnt",
                    "a.target.sessionId" to "8E0988F2-57C7-42CA-B5A6-6458D370F315"
                )
            )
        ).build()

        analyticsExtension.handleIncomingEvent(trackEvent)

        countDownLatch.await()
        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "ADBINTERNAL:AnalyticsForTarget",
            "pe" to "tnt",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "tnta" to "285408:0:0|2",
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "a.internalaction" to "AnalyticsForTarget",
            "a.target.sessionId" to "8E0988F2-57C7-42CA-B5A6-6458D370F315"
        )
        Assert.assertTrue(expectedContextData == contextDataMap)
        Assert.assertEquals(expectedVars.size, varMap.size)
        Assert.assertEquals(expectedVars, varMap)
    }
}