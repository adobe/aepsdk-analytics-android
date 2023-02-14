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
import org.junit.Test
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch

internal class AnalyticsTrackAssuranceTests : AnalyticsFunctionalTestBase() {

    @Test(timeout = 10000)
    fun `append debug param in hit`() {
        val countDownLatch = CountDownLatch(1)
        monitorNetwork { request ->
            if (request.url.startsWith("https://test.com/b/ss/rsid/0/")) {
                val body = URLDecoder.decode(String(request.body), "UTF-8")
                if (body?.contains("&p.&debug=true&.p") == true) {
                    countDownLatch.countDown()
                }
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
            defaultIdentity()
        )

        updateMockedSharedState("com.adobe.assurance", mapOf("sessionid" to "session_id"))

        val trackEvent = Event.Builder(
            "track event",
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT
        ).setEventData(
            mapOf(
                "action" to "testActionName"
            )
        ).build()

        analyticsExtension.handleIncomingEvent(trackEvent)

        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun `append debug param to queued hits`() {
        val countDownLatch = CountDownLatch(2)
        monitorNetwork { request ->
            if (request.url.startsWith("https://test.com/b/ss/rsid/0/")) {
                val body = URLDecoder.decode(String(request.body), "UTF-8")
                if (body?.contains("&p.&debug=true&.p") == true) {
                    countDownLatch.countDown()
                }
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
                "analytics.batchLimit" to 1
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

        analyticsExtension.handleIncomingEvent(trackEvent1)
        updateMockedSharedState("com.adobe.assurance", mapOf("sessionid" to "session_id"))
        analyticsExtension.handleIncomingEvent(trackEvent2)
        countDownLatch.await()
    }
}
