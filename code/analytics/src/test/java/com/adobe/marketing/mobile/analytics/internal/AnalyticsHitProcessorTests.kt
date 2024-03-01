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

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.services.DataEntity
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.util.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.concurrent.CountDownLatch

@RunWith(MockitoJUnitRunner.Silent::class)
class AnalyticsHitProcessorTests {

    private var networkMonitor: NetworkMonitor? = null

    private var mockedHttpConnecting = MockedHttpConnecting()

    @Mock
    private lateinit var mockedExtensionApi: ExtensionApi

    @Mock
    private lateinit var mockedAnalyticsState: AnalyticsState

    @Before
    fun setup() {
        mockedHttpConnecting = MockedHttpConnecting()
        ServiceProvider.getInstance().networkService = Networking { request, callback ->
            networkMonitor?.let { it(request) }
            callback.call(mockedHttpConnecting)
        }

        Mockito.reset(mockedExtensionApi)
        Mockito.reset(mockedAnalyticsState)
        Mockito.`when`(mockedAnalyticsState.host).thenReturn("test.com")
        Mockito.`when`(mockedAnalyticsState.rsids).thenReturn("rsid")
        Mockito.`when`(mockedAnalyticsState.isAnalyticsConfigured).thenReturn(true)
//        Mockito.`when`(mockedAnalyticsState.isAssuranceSessionActive).thenReturn(false)
    }

    private fun initAnalyticsHitProcessor(): AnalyticsHitProcessor {
        return AnalyticsHitProcessor(mockedAnalyticsState, mockedExtensionApi)
    }

    @Test
    fun `retryInterval is 30`() {
        val analyticsHitProcessor = initAnalyticsHitProcessor()
        assertEquals(30, analyticsHitProcessor.retryInterval(DataEntity(null)))
    }

    @Test
    fun `Bad DataEntity should be dropped`() {
        val countDownLatch = CountDownLatch(1)
        val analyticsHitProcessor = initAnalyticsHitProcessor()
        val badDataEntity = DataEntity(null)
        var networkRequest: NetworkRequest? = null
        networkMonitor = { request ->
            networkRequest = request
        }
        analyticsHitProcessor.processHit(badDataEntity) { processingComplete ->
            assertTrue(processingComplete)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        Thread.sleep(50)
        assertNull(networkRequest)
    }

    @Test
    fun `network failure - recoverable error`() {
        val countDownLatch = CountDownLatch(2)
        val analyticsHitProcessor = initAnalyticsHitProcessor()
        val dataEntity =
            AnalyticsHit("payload1", TimeUtils.getUnixTimeInSeconds(), "id1").toDataEntity()
        var networkRequest: NetworkRequest? = null
        // 408, 504, 503, -1
        mockedHttpConnecting.responseCode = 408
        networkMonitor = { request ->
            networkRequest = request
            countDownLatch.countDown()
        }
        analyticsHitProcessor.processHit(dataEntity) { processingComplete ->
            assertFalse(processingComplete)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        assertNotNull(networkRequest)
    }

    @Test
    fun `network failure - 404 error`() {
        val countDownLatch = CountDownLatch(2)
        val analyticsHitProcessor = initAnalyticsHitProcessor()
        val dataEntity =
            AnalyticsHit("payload1", TimeUtils.getUnixTimeInSeconds(), "id1").toDataEntity()
        var networkRequest: NetworkRequest? = null
        // 408, 504, 503, -1
        mockedHttpConnecting.responseCode = 404
        networkMonitor = { request ->
            networkRequest = request
            countDownLatch.countDown()
        }
        analyticsHitProcessor.processHit(dataEntity) { processingComplete ->
            assertTrue(processingComplete)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        assertNotNull(networkRequest)
    }

    @Test
    fun `network success`() {
        val countDownLatch = CountDownLatch(2)
        val analyticsHitProcessor = initAnalyticsHitProcessor()
        val payload =
            "ndh=1&ce=UTF-8&c.&a.&action=testAction&.a&k1=v1&k2=v2&.c&t=00%2F00%2F0000%2000%3A00%3A00%200%20420&pe=lnk_o&pev2=AMACTION%3AtestAction&aamb=blob&mid=mid&aamlh=lochint&cp=foreground&ts=1669845066"
        val timestamp = TimeUtils.getUnixTimeInSeconds()
        val dataEntity =
            AnalyticsHit(payload, timestamp, "id1").toDataEntity()
        mockedHttpConnecting.responseCode = 200
        mockedHttpConnecting.responseProperties = mapOf(
            "ETag" to "eTag",
            "Server" to "abc.com",
            "Content-Type" to "xyz"
        )
        mockedHttpConnecting.inputStream = ("testAnalyticsResponse").byteInputStream()
        networkMonitor = { request ->
            assertTrue(request.url.contains("test.com"))
            assertTrue(String(request.body).contains(payload))
            countDownLatch.countDown()
        }

        assertEquals(0, analyticsHitProcessor.getLastHitTimestamp())

        analyticsHitProcessor.processHit(dataEntity) { processingComplete ->
            assertTrue(processingComplete)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        val eventCaptor = ArgumentCaptor.forClass(
            Event::class.java
        )
        verify(mockedExtensionApi, times(1)).dispatch(eventCaptor.capture())
        val event = eventCaptor.value
        assertNotNull(event)
        assertEquals("AnalyticsResponse", event.name)
        assertEquals("com.adobe.eventType.analytics", event.type)
        assertEquals("com.adobe.eventSource.responseContent", event.source)
        assertEquals(
            mapOf(
                "ETag" to "eTag",
                "Server" to "abc.com",
                "Content-Type" to "xyz"
            ),
            event.eventData["headers"]
        )
        assertEquals("testAnalyticsResponse", event.eventData["analyticsserverresponse"])
        assertEquals("id1", event.eventData["requestEventIdentifier"])
        assertTrue((event.eventData["hitHost"] as? String)?.startsWith("https://test.com/b/ss/rsid/0") == true)
        assertTrue((event.eventData["hitUrl"] as? String)?.startsWith(payload) == true)

        assertEquals(timestamp, analyticsHitProcessor.getLastHitTimestamp())
    }

    @Test
    fun `network success - hit is out of order with offline enabled`() {
        Mockito.`when`(mockedAnalyticsState.isOfflineTrackingEnabled).thenReturn(true)

        val countDownLatch = CountDownLatch(2)
        val analyticsHitProcessor = initAnalyticsHitProcessor()
        val payload =
            "ndh=1&ce=UTF-8&c.&a.&action=testAction&.a&k1=v1&k2=v2&.c&t=00%2F00%2F0000%2000%3A00%3A00%200%20420&pe=lnk_o&pev2=AMACTION%3AtestAction&aamb=blob&mid=mid&aamlh=lochint&cp=foreground&ts=1669845066"
        val timestamp = TimeUtils.getUnixTimeInSeconds()

        analyticsHitProcessor.setLastHitTimestamp(
            timestamp + 10
        )
        val dataEntity =
            AnalyticsHit(payload, timestamp, "id1").toDataEntity()
        mockedHttpConnecting.responseCode = 200
        mockedHttpConnecting.responseProperties = mapOf(
            "ETag" to "eTag",
            "Server" to "abc.com",
            "Content-Type" to "xyz"
        )
        mockedHttpConnecting.inputStream = ("testAnalyticsResponse").byteInputStream()
        networkMonitor = { request ->
            assertTrue(request.url.contains("test.com"))
            assertTrue(String(request.body).contains(payload))
            countDownLatch.countDown()
        }

        assertEquals(timestamp + 10, analyticsHitProcessor.getLastHitTimestamp())

        analyticsHitProcessor.processHit(dataEntity) { processingComplete ->
            assertTrue(processingComplete)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        val eventCaptor = ArgumentCaptor.forClass(
            Event::class.java
        )
        verify(mockedExtensionApi, times(1)).dispatch(eventCaptor.capture())
        val event = eventCaptor.value
        assertNotNull(event)
        assertEquals("AnalyticsResponse", event.name)
        assertEquals("com.adobe.eventType.analytics", event.type)
        assertEquals("com.adobe.eventSource.responseContent", event.source)
        assertEquals(
            mapOf(
                "ETag" to "eTag",
                "Server" to "abc.com",
                "Content-Type" to "xyz"
            ),
            event.eventData["headers"]
        )
        assertEquals("testAnalyticsResponse", event.eventData["analyticsserverresponse"])
        assertEquals("id1", event.eventData["requestEventIdentifier"])
        assertTrue((event.eventData["hitHost"] as? String)?.startsWith("https://test.com/b/ss/rsid/0") == true)
        assertTrue((event.eventData["hitUrl"] as? String)?.startsWith(payload) == true)
        // For offline enabled :- if hitTimestamp is less than lastHitTimestamp, hitTimestamp is corrected to lastHitTimestamp + 1
        assertEquals(timestamp + 10 + 1, analyticsHitProcessor.getLastHitTimestamp())
    }

    @Test
    fun `hit should be dropped if timestamp is exceeded when offline disabled`() {
        Mockito.`when`(mockedAnalyticsState.isOfflineTrackingEnabled).thenReturn(false)

        val countDownLatch = CountDownLatch(1)
        val analyticsHitProcessor = initAnalyticsHitProcessor()
        val currentTimestamp = TimeUtils.getUnixTimeInSeconds()
        val dataEntity =
            AnalyticsHit("payload", currentTimestamp - 65, "id1").toDataEntity()
        var networkRequest: NetworkRequest? = null
        networkMonitor = { request ->
            networkRequest = request
        }

        analyticsHitProcessor.processHit(dataEntity) { processingComplete ->
            assertTrue(processingComplete)
            countDownLatch.countDown()
        }
        countDownLatch.await()

        Thread.sleep(50)
        assertNull(networkRequest)
    }

    @Test
    fun `hit should be retried later if analytics configuration is not ready`() {
        Mockito.`when`(mockedAnalyticsState.isOfflineTrackingEnabled).thenReturn(true)
        Mockito.`when`(mockedAnalyticsState.isAnalyticsConfigured).thenReturn(false)

        val countDownLatch = CountDownLatch(1)
        val analyticsHitProcessor = initAnalyticsHitProcessor()
        val currentTimestamp = TimeUtils.getUnixTimeInSeconds()
        val dataEntity =
            AnalyticsHit("payload", currentTimestamp - 65, "id1").toDataEntity()
        var networkRequest: NetworkRequest? = null
        networkMonitor = { request ->
            networkRequest = request
        }

        analyticsHitProcessor.processHit(dataEntity) { processingComplete ->
            assertFalse(processingComplete)
            countDownLatch.countDown()
        }
        countDownLatch.await()

        Thread.sleep(50)
        assertNull(networkRequest)
    }

    @Test
    fun `hit should be retried later if network connection is null`() {
        ServiceProvider.getInstance().networkService = Networking { request, callback ->
            networkMonitor?.let { it(request) }
            callback.call(null)
        }
        val countDownLatch = CountDownLatch(2)
        val analyticsHitProcessor = initAnalyticsHitProcessor()
        val payload =
            "ndh=1&ce=UTF-8&c.&a.&action=testAction&.a&k1=v1&k2=v2&.c&t=00%2F00%2F0000%2000%3A00%3A00%200%20420&pe=lnk_o&pev2=AMACTION%3AtestAction&aamb=blob&mid=mid&aamlh=lochint&cp=foreground&ts=1669845066"
        val timestamp = TimeUtils.getUnixTimeInSeconds()
        val dataEntity =
            AnalyticsHit(payload, timestamp, "id1").toDataEntity()
        mockedHttpConnecting.responseCode = 200
        mockedHttpConnecting.responseProperties = mapOf(
            "ETag" to "eTag",
            "Server" to "abc.com",
            "Content-Type" to "xyz"
        )

        networkMonitor = { request ->
            assertTrue(request.url.contains("test.com"))
            assertTrue(String(request.body).contains(payload))
            countDownLatch.countDown()
        }

        assertEquals(0, analyticsHitProcessor.getLastHitTimestamp())

        analyticsHitProcessor.processHit(dataEntity) { processingComplete ->
            assertFalse(processingComplete)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        Thread.sleep(50)
    }

    @Test
    fun `hit should be dropped with no retry when URL is malformed`() {
        // malformed URL
        Mockito.`when`(mockedAnalyticsState.host).thenReturn("adobe.com:_80")

        val countDownLatch = CountDownLatch(1)
        val analyticsHitProcessor = initAnalyticsHitProcessor()
        val payload =
            "ndh=1&ce=UTF-8&c.&a.&action=testAction&.a&k1=v1&k2=v2&.c&t=00%2F00%2F0000%2000%3A00%3A00%200%20420&pe=lnk_o&pev2=AMACTION%3AtestAction&aamb=blob&mid=mid&aamlh=lochint&cp=foreground&ts=1669845066"
        val timestamp = TimeUtils.getUnixTimeInSeconds()
        val dataEntity =
            AnalyticsHit(payload, timestamp, "id1").toDataEntity()
        var networkRequest: NetworkRequest? = null
        networkMonitor = { request ->
            networkRequest = request
        }
        analyticsHitProcessor.processHit(dataEntity) { processingComplete ->
            assertTrue(processingComplete)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        Thread.sleep(50)
        assertNull(networkRequest)
    }

    @Test
    fun `network success - assurance enabled`() {
        Mockito.`when`(mockedAnalyticsState.isAssuranceSessionActive).thenReturn(true)

        val countDownLatch = CountDownLatch(2)
        val analyticsHitProcessor = initAnalyticsHitProcessor()
        val payload =
            "ndh=1&ce=UTF-8&c.&a.&action=testAction&.a&k1=v1&k2=v2&.c&t=00%2F00%2F0000%2000%3A00%3A00%200%20420&pe=lnk_o&pev2=AMACTION%3AtestAction&aamb=blob&mid=mid&aamlh=lochint&cp=foreground&ts=1669845066"
        val timestamp = TimeUtils.getUnixTimeInSeconds()

        val dataEntity =
            AnalyticsHit(payload, timestamp, "id1").toDataEntity()
        mockedHttpConnecting.responseCode = 200
        mockedHttpConnecting.responseProperties = mapOf(
            "ETag" to "eTag",
            "Server" to "abc.com",
            "Content-Type" to "xyz"
        )
        mockedHttpConnecting.inputStream = ("testAnalyticsResponse").byteInputStream()
        networkMonitor = { request ->
            assertTrue(request.url.contains("test.com"))
            assertTrue(String(request.body).contains(payload))
            countDownLatch.countDown()
        }

        analyticsHitProcessor.processHit(dataEntity) { processingComplete ->
            assertTrue(processingComplete)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        val eventCaptor = ArgumentCaptor.forClass(
            Event::class.java
        )
        verify(mockedExtensionApi, times(1)).dispatch(eventCaptor.capture())
        val event = eventCaptor.value
        assertNotNull(event)
        assertEquals("AnalyticsResponse", event.name)
        assertEquals("com.adobe.eventType.analytics", event.type)
        assertEquals("com.adobe.eventSource.responseContent", event.source)
        assertEquals(
            mapOf(
                "ETag" to "eTag",
                "Server" to "abc.com",
                "Content-Type" to "xyz"
            ),
            event.eventData["headers"]
        )

        assertEquals("testAnalyticsResponse", event.eventData["analyticsserverresponse"])
        assertEquals("id1", event.eventData["requestEventIdentifier"])
        assertTrue((event.eventData["hitHost"] as? String)?.startsWith("https://test.com/b/ss/rsid/0") == true)
        assertTrue((event.eventData["hitUrl"] as? String)?.startsWith(payload) == true)
        assertTrue((event.eventData["hitUrl"] as? String)?.endsWith("&p.&debug=true&.p") == true)
    }

    @Test
    fun `network success for event processed during resetIdentities should ignore response`() {
        val countDownLatch = CountDownLatch(2)
        Mockito.`when`(mockedAnalyticsState.lastResetIdentitiesTimestampSec).thenReturn(TimeUtils.getUnixTimeInSeconds())
        val analyticsHitProcessor = initAnalyticsHitProcessor()
        val payload =
            "ndh=1&ce=UTF-8&c.&a.&action=testAction&.a&k1=v1&k2=v2&.c&t=00%2F00%2F0000%2000%3A00%3A00%200%20420&pe=lnk_o&pev2=AMACTION%3AtestAction&aamb=blob&mid=mid&aamlh=lochint&cp=foreground&ts=1669845066"
        val timestamp = TimeUtils.getUnixTimeInSeconds()
        val dataEntity =
            AnalyticsHit(payload, timestamp, "id1").toDataEntity()
        mockedHttpConnecting.responseCode = 200
        mockedHttpConnecting.responseProperties = mapOf(
            "ETag" to "eTag",
            "Server" to "abc.com",
            "Content-Type" to "xyz"
        )
        mockedHttpConnecting.inputStream = ("testAnalyticsResponse").byteInputStream()
        networkMonitor = { request ->
            assertTrue(request.url.contains("test.com"))
            assertTrue(String(request.body).contains(payload))
            countDownLatch.countDown()
        }

        assertEquals(0, analyticsHitProcessor.getLastHitTimestamp())

        analyticsHitProcessor.processHit(dataEntity) { processingComplete ->
            assertTrue(processingComplete)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        verify(mockedExtensionApi, never()).dispatch(any(Event::class.java))
        assertEquals(timestamp, analyticsHitProcessor.getLastHitTimestamp())
    }

    @Test
    fun `network success for event processed after resetIdentities should dispatch response`() {
        val countDownLatch = CountDownLatch(2)
        Mockito.`when`(mockedAnalyticsState.lastResetIdentitiesTimestampSec).thenReturn(TimeUtils.getUnixTimeInSeconds() - 10) // simulate previous reset
        val analyticsHitProcessor = initAnalyticsHitProcessor()
        val payload =
            "ndh=1&ce=UTF-8&c.&a.&action=testAction&.a&k1=v1&k2=v2&.c&t=00%2F00%2F0000%2000%3A00%3A00%200%20420&pe=lnk_o&pev2=AMACTION%3AtestAction&aamb=blob&mid=mid&aamlh=lochint&cp=foreground&ts=1669845066"
        val timestamp = TimeUtils.getUnixTimeInSeconds()
        val dataEntity =
            AnalyticsHit(payload, timestamp, "id1").toDataEntity()
        mockedHttpConnecting.responseCode = 200
        mockedHttpConnecting.responseProperties = mapOf(
            "ETag" to "eTag",
            "Server" to "abc.com",
            "Content-Type" to "xyz"
        )
        mockedHttpConnecting.inputStream = ("testAnalyticsResponse").byteInputStream()
        networkMonitor = { request ->
            assertTrue(request.url.contains("test.com"))
            assertTrue(String(request.body).contains(payload))
            countDownLatch.countDown()
        }

        assertEquals(0, analyticsHitProcessor.getLastHitTimestamp())

        analyticsHitProcessor.processHit(dataEntity) { processingComplete ->
            assertTrue(processingComplete)
            countDownLatch.countDown()
        }
        countDownLatch.await()
        val eventCaptor = ArgumentCaptor.forClass(
            Event::class.java
        )
        verify(mockedExtensionApi, times(1)).dispatch(eventCaptor.capture())
        val event = eventCaptor.value
        assertNotNull(event)
        assertEquals("AnalyticsResponse", event.name)
        assertEquals("com.adobe.eventType.analytics", event.type)
        assertEquals("com.adobe.eventSource.responseContent", event.source)
    }
}
