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
import com.adobe.marketing.mobile.analytics.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch

internal class ConsequenceRuleTests : AnalyticsFunctionalTestBase() {

    @Test(timeout = 10000)
    fun `handle Analytics consequence event`() {
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(
            mapOf(
                "analytics.server" to "test.com",
                "analytics.rsids" to "rsid",
                "global.privacy" to "optedin",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 0,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 0
            ),
            mapOf(
                "mid" to "mid",
                "blob" to "blob",
                "locationhint" to "lochint"
            )
        )

        val eventData: Map<String, Any> = mapOf(
            "triggeredconsequence" to mapOf(
                "id" to "id",
                "type" to "an",
                "detail" to mapOf(
                    "action" to "testActionName",
                    "contextdata" to mapOf("k1" to "v1", "k2" to "v2")
                )
            )
        )
        val consequenceEvent = Event.Builder(
            "Rule event",
            EventType.RULES_ENGINE,
            EventSource.RESPONSE_CONTENT
        ).setEventData(eventData).build()

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

        analyticsExtension.handleIncomingEvent(consequenceEvent)
        countDownLatch.await()
        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "AMACTION:testActionName",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to consequenceEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "a.action" to "testActionName"
        )
        assertTrue(expectedContextData == contextDataMap)
        assertEquals(expectedVars.size, varMap.size)
        assertEquals(expectedVars, varMap)
    }

}