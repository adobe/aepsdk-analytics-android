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

package com.adobe.marketing.mobile.analytics.internal

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adobe.marketing.mobile.Analytics
import com.adobe.marketing.mobile.Identity
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.SDKHelper
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

private typealias NetworkMonitor = (request: NetworkRequest) -> Unit

@RunWith(AndroidJUnit4::class)
class AnalyticsIntegrationTests {
    companion object {
        private var networkMonitor: NetworkMonitor? = null

        @BeforeClass
        @JvmStatic
        fun setupClass() {
            overrideNetworkService()
        }

        private fun overrideNetworkService() {
            ServiceProvider.getInstance().networkService = Networking { request, callback ->
                networkMonitor?.let { it(request) }
                callback.call(object : HttpConnecting {
                    override fun getInputStream(): InputStream? {
                        return null
                    }

                    override fun getErrorStream(): InputStream? {
                        return null
                    }

                    override fun getResponseCode(): Int {
                        return HttpURLConnection.HTTP_REQ_TOO_LONG
                    }

                    override fun getResponseMessage(): String {
                        return ""
                    }

                    override fun getResponsePropertyValue(responsePropertyKey: String?): String {
                        return ""
                    }

                    override fun close() {}
                })
            }
        }
    }

    @Before
    fun setup() {
        networkMonitor = null
        SDKHelper.resetSDK()

        MobileCore.setApplication(ApplicationProvider.getApplicationContext())

        MobileCore.setLogLevel(LoggingMode.VERBOSE)
        val countDownLatch = CountDownLatch(1)
        MobileCore.registerExtensions(
            listOf(
                Analytics.EXTENSION,
                Identity.EXTENSION,
                MonitorExtension::class.java
            )
        ) {
            countDownLatch.countDown()
        }
        assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS))
    }

    @Test
    fun testGetQueueSz() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "analytics.server" to "test.com",
                "analytics.rsids" to "rsid",
                "global.privacy" to "optedin",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 5,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1
            )
        )
        val sharedStatesLatch = CountDownLatch(2)
        configurationAwareness { sharedStatesLatch.countDown() }
        identityAwareness { sharedStatesLatch.countDown() }
        sharedStatesLatch.await()
        MobileCore.trackState(
            "homePage",
            mapOf(
                "key1" to "value1"
            )
        )
        MobileCore.trackState(
            "homePage",
            mapOf(
                "key1" to "value1"
            )
        )
        MobileCore.trackState(
            "homePage",
            mapOf(
                "key1" to "value1"
            )
        )
        Analytics.getQueueSize {
            assertEquals(3, it)
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun testTrackAction() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
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
        val sharedStatesLatch = CountDownLatch(2)
        configurationAwareness { sharedStatesLatch.countDown() }
        identityAwareness { sharedStatesLatch.countDown() }
        sharedStatesLatch.await()
        var varMap: Map<String, Any> = emptyMap()
        var contextDataMap: Map<String, Any> = emptyMap()
        networkMonitor = { request ->
            val url = request.url
            if (url.contains("https://test.com/b/ss/rsid/0/")) {
                val body = URLDecoder.decode(String(request.body), "UTF-8")
                varMap = extractQueryParamsFrom(body)
                contextDataMap = extractContextDataFrom(body)
                countDownLatch.countDown()
            }
        }
        MobileCore.trackAction(
            "clickOK",
            mapOf(
                "key1" to "value1"
            )
        )
        countDownLatch.await()
        val expectedContextData: Map<String, String> = mapOf(
            "key1" to "value1",
            "a.action" to "clickOK"
        )
        assertTrue(expectedContextData == contextDataMap)

        assertEquals("1", varMap["ndh"])
        assertEquals("UTF-8", varMap["ce"])
        assertEquals("lnk_o", varMap["pe"])
        assertEquals("AMACTION:clickOK", varMap["pev2"])
        assertEquals("foreground", varMap["cp"])
        assertNotNull(varMap["t"])
        assertNotNull(varMap["mid"])
        assertNotNull(varMap["ts"])
    }

    @Test(timeout = 10000)
    fun testTrackState() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
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
        val sharedStatesLatch = CountDownLatch(2)
        configurationAwareness { sharedStatesLatch.countDown() }
        identityAwareness { sharedStatesLatch.countDown() }
        sharedStatesLatch.await()
        var varMap: Map<String, Any> = emptyMap()
        var contextDataMap: Map<String, Any> = emptyMap()
        networkMonitor = { request ->
            val url = request.url
            if (url.contains("https://test.com/b/ss/rsid/0/")) {
                val body = URLDecoder.decode(String(request.body), "UTF-8")
                varMap = extractQueryParamsFrom(body)
                contextDataMap = extractContextDataFrom(body)
                countDownLatch.countDown()
            }
        }
        MobileCore.trackState(
            "homePage",
            mapOf(
                "key1" to "value1"
            )
        )
        countDownLatch.await()
        val expectedContextData: Map<String, String> = mapOf(
            "key1" to "value1"
        )
        assertTrue(expectedContextData == contextDataMap)
        assertEquals("1", varMap["ndh"])
        assertEquals("UTF-8", varMap["ce"])
        assertEquals("homePage", varMap["pageName"])
        assertEquals("foreground", varMap["cp"])
        assertNotNull(varMap["mid"])
        assertNotNull(varMap["ts"])
    }

    @Test
    fun testClearQueue() {
        val countDownLatch = CountDownLatch(1)
        MobileCore.updateConfiguration(
            mapOf(
                "analytics.server" to "test.com",
                "analytics.rsids" to "rsid",
                "global.privacy" to "optedin",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 5,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1
            )
        )
        val sharedStatesLatch = CountDownLatch(2)
        configurationAwareness { sharedStatesLatch.countDown() }
        identityAwareness { sharedStatesLatch.countDown() }
        sharedStatesLatch.await()
        MobileCore.trackState(
            "homePage",
            mapOf(
                "key1" to "value1"
            )
        )
        MobileCore.trackState(
            "homePage",
            mapOf(
                "key1" to "value1"
            )
        )
        MobileCore.trackState(
            "homePage",
            mapOf(
                "key1" to "value1"
            )
        )
        Analytics.clearQueue()
        Analytics.getQueueSize {
            assertEquals(0, it)
            countDownLatch.countDown()
        }
        countDownLatch.await()
    }

    @Test(timeout = 10000)
    fun testSendQueuedHits() {
        val countDownLatch = CountDownLatch(2)
        MobileCore.updateConfiguration(
            mapOf(
                "analytics.server" to "test.com",
                "analytics.rsids" to "rsid",
                "global.privacy" to "optedin",
                "experienceCloud.org" to "orgid",
                "analytics.batchLimit" to 5,
                "analytics.offlineEnabled" to true,
                "analytics.backdatePreviousSessionInfo" to true,
                "analytics.launchHitDelay" to 1
            )
        )
        val sharedStatesLatch = CountDownLatch(2)
        configurationAwareness { sharedStatesLatch.countDown() }
        identityAwareness { sharedStatesLatch.countDown() }
        sharedStatesLatch.await()
        networkMonitor = { request ->
            val url = request.url
            if (url.contains("https://test.com/b/ss/rsid/0/")) {
                countDownLatch.countDown()
            }
        }
        MobileCore.trackAction(
            "clickOK",
            mapOf(
                "key1" to "value1"
            )
        )
        MobileCore.trackAction(
            "clickOK",
            mapOf(
                "key2" to "value2"
            )
        )
        Analytics.sendQueuedHits()
        countDownLatch.await()
    }

    private fun configurationAwareness(callback: SharedStateMonitor) {
        MonitorExtension.configurationAwareness(callback)
    }

    private fun identityAwareness(callback: SharedStateMonitor) {
        MonitorExtension.identityAwareness(callback)
    }
}

private val CONTEXT_DATA_REGEX = Regex("(&c\\.(.*)&\\.c)")

internal fun extractQueryParamsFrom(url: String): Map<String, String> {
    val contextDataString = extractContextDataStringFrom(url) ?: run {
        return@extractQueryParamsFrom emptyMap()
    }
    val queryString = url.replace(contextDataString, "")
    val map = mutableMapOf<String, String>()
    var preKey = ""
    queryString.split("&").forEach {
        if (it.isNotEmpty()) {
            val kvPair = it.split("=")
            if (kvPair.size == 2) {
                map[kvPair[0]] = kvPair[1]
                preKey = kvPair[0]
            }
            if (kvPair.size == 1 && preKey.isNotEmpty()) {
                map[preKey] = "${map.get(preKey)}&${kvPair[0]}"
            }
        }
    }
    return map
}

private fun extractContextDataStringFrom(url: String): String? {
    return CONTEXT_DATA_REGEX.find(url)?.value
}

internal fun extractContextDataFrom(url: String): Map<String, String> {
    val kvPairs = extractContextDataStringFrom(url)?.replace("&c.", "")?.replace("&.c", "") ?: run {
        return@extractContextDataFrom emptyMap()
    }
    return ContextDataUtil.deserializeContextDataKeyValuePairs(kvPairs)
}
