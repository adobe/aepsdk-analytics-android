/*
  Copyright 2023 Adobe. All rights reserved.
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
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.ExtensionEventListener
import com.adobe.marketing.mobile.ExtensionHelper
import com.adobe.marketing.mobile.SharedStateResolution
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.services.DataStoring
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.util.SQLiteUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull

@RunWith(MockitoJUnitRunner.Silent::class)
class AnalyticsExtensionTests {
    private lateinit var analytics: AnalyticsExtension

    @Mock
    private lateinit var mockApi: ExtensionApi

    @Mock
    private lateinit var mockDataStore: DataStoring

    @Mock
    private lateinit var mockNamedCollection: NamedCollection

    @Mock
    private lateinit var mockDatabase: AnalyticsDatabase

    @Mock
    private lateinit var mockServiceProvider: ServiceProvider

    private val mockedStaticServiceProvider = mockStatic(
        ServiceProvider::class.java
    )

    @Before
    fun setup() {
        mockedStaticServiceProvider.`when`<Any> { ServiceProvider.getInstance() }
            .thenReturn(mockServiceProvider)
        `when`(mockDataStore.getNamedCollection(eq("AnalyticsDataStorage"))).thenReturn(mockNamedCollection)
        `when`(mockServiceProvider.dataStoreService).thenReturn(mockDataStore)
        analytics = AnalyticsExtension(mockApi, mockDatabase)
    }

    @After
    fun tearDown() {
        reset(mockApi)
        reset(mockDataStore)
        reset(mockDatabase)
        reset(mockNamedCollection)
        mockedStaticServiceProvider.close()
        reset(mockServiceProvider)
    }

    @Test
    fun `onRegistered() when aid, vid should createSharedState with ids for version 0`() {
        `when`(mockNamedCollection.getString(any(), any())).thenReturn("test")
        ExtensionHelper.notifyRegistered(analytics)

        val expectedData = mutableMapOf<String, Any>()
        expectedData["aid"] = "test"
        expectedData["vid"] = "test"

        verify(mockApi).createSharedState(eq(expectedData), eq(null))
    }

    @Test
    fun `onRegistered() when no aid, vid should createSharedState with empty data for version 0`() {
        `when`(mockNamedCollection.getString(any(), any())).thenReturn(null)
        ExtensionHelper.notifyRegistered(analytics)

        val expectedData = emptyMap<String, Any>()

        verify(mockApi).createSharedState(eq(expectedData), eq(null))
    }

    @Test
    fun `onRegistered() should register correct listeners`() {
        ExtensionHelper.notifyRegistered(analytics)

        val eventTypeCaptor = ArgumentCaptor.forClass(String::class.java)
        val eventSourceCaptor = ArgumentCaptor.forClass(String::class.java)
        val listenerCaptor = ArgumentCaptor.forClass(ExtensionEventListener::class.java)

        verify(mockApi, times(9)).registerEventListener(
            eventTypeCaptor.capture(),
            eventSourceCaptor.capture(),
            listenerCaptor.capture()
        )

        assertEquals(EventType.RULES_ENGINE, eventTypeCaptor.allValues[0])
        assertEquals(EventSource.RESPONSE_CONTENT, eventSourceCaptor.allValues[0])
        assertNotNull(listenerCaptor.allValues[0])

        assertEquals(EventType.ANALYTICS, eventTypeCaptor.allValues[1])
        assertEquals(EventSource.REQUEST_CONTENT, eventSourceCaptor.allValues[1])
        assertNotNull(listenerCaptor.allValues[1])

        assertEquals(EventType.ANALYTICS, eventTypeCaptor.allValues[2])
        assertEquals(EventSource.REQUEST_IDENTITY, eventSourceCaptor.allValues[2])
        assertNotNull(listenerCaptor.allValues[2])

        assertEquals(EventType.CONFIGURATION, eventTypeCaptor.allValues[3])
        assertEquals(EventSource.RESPONSE_CONTENT, eventSourceCaptor.allValues[3])
        assertNotNull(listenerCaptor.allValues[3])

        assertEquals(EventType.GENERIC_LIFECYCLE, eventTypeCaptor.allValues[4])
        assertEquals(EventSource.REQUEST_CONTENT, eventSourceCaptor.allValues[4])
        assertNotNull(listenerCaptor.allValues[4])

        assertEquals(EventType.LIFECYCLE, eventTypeCaptor.allValues[5])
        assertEquals(EventSource.RESPONSE_CONTENT, eventSourceCaptor.allValues[5])
        assertNotNull(listenerCaptor.allValues[5])

        assertEquals(EventType.ACQUISITION, eventTypeCaptor.allValues[6])
        assertEquals(EventSource.RESPONSE_CONTENT, eventSourceCaptor.allValues[6])
        assertNotNull(listenerCaptor.allValues[6])

        assertEquals(EventType.GENERIC_TRACK, eventTypeCaptor.allValues[7])
        assertEquals(EventSource.REQUEST_CONTENT, eventSourceCaptor.allValues[7])
        assertNotNull(listenerCaptor.allValues[7])

        assertEquals(EventType.GENERIC_IDENTITY, eventTypeCaptor.allValues[8])
        assertEquals(EventSource.REQUEST_RESET, eventSourceCaptor.allValues[8])
        assertNotNull(listenerCaptor.allValues[8])
    }

    @Test
    fun `onRegistered() should delete deprecated hit database file`() {
        mockStatic(SQLiteUtils::class.java).use { sqliteUtilsMockedStatic ->
            val fileNameClassCaptor =
                ArgumentCaptor.forClass(
                    String::class.java
                )
            sqliteUtilsMockedStatic
                .`when`<Any> {
                    SQLiteUtils.deleteDBFromCacheDir(fileNameClassCaptor.capture())
                }
                .thenReturn(true)
            ExtensionHelper.notifyRegistered(analytics)

            assertEquals("ADBMobileDataCache.sqlite", fileNameClassCaptor.value)
        }
    }

    @Test
    fun `getName() should return full extension name`() {
        assertEquals("com.adobe.module.analytics", ExtensionHelper.getName(analytics))
    }

    @Test
    fun `getFriendlyName() should return friendly extension name`() {
        assertEquals("Analytics", ExtensionHelper.getFriendlyName(analytics))
    }

    @Test
    fun `getVersion() should not be null`() {
        assertNotNull(ExtensionHelper.getVersion(analytics))
    }

    @Test
    fun `readyForEvent() should return false when configuration pending`() {
        `when`(mockApi.getSharedState(eq("com.adobe.module.configuration"), any(Event::class.java), eq(false), any())).thenReturn(
            SharedStateResult(SharedStateStatus.PENDING, null)
        )
        `when`(mockApi.getSharedState(eq("com.adobe.module.identity"), any(Event::class.java), eq(false), any())).thenReturn(
            SharedStateResult(SharedStateStatus.SET, mapOf("mid" to "mid"))
        )
        assertFalse(
            analytics.readyForEvent(
                Event.Builder("event-name", "type", "source").build()
            )
        )
    }

    @Test
    fun `readyForEvent() should return false when identity pending`() {
        `when`(mockApi.getSharedState(eq("com.adobe.module.configuration"), any(Event::class.java), eq(false), any())).thenReturn(
            SharedStateResult(SharedStateStatus.SET, mapOf("analytics.server" to "server"))
        )
        `when`(mockApi.getSharedState(eq("com.adobe.module.identity"), any(Event::class.java), eq(false), any())).thenReturn(
            SharedStateResult(SharedStateStatus.PENDING, null)
        )

        ExtensionHelper.notifyRegistered(analytics)
        assertFalse(
            analytics.readyForEvent(
                Event.Builder("event-name", "type", "source").build()
            )
        )
    }

    @Test
    fun `readyForEvent() should return true when config and identity are set`() {
        `when`(mockApi.getSharedState(eq("com.adobe.module.configuration"), any(Event::class.java), eq(false), any())).thenReturn(
            SharedStateResult(SharedStateStatus.SET, mapOf("analytics.server" to "server"))
        )
        `when`(mockApi.getSharedState(eq("com.adobe.module.identity"), any(Event::class.java), eq(false), any())).thenReturn(
            SharedStateResult(SharedStateStatus.SET, mapOf("mid" to "mid"))
        )

        ExtensionHelper.notifyRegistered(analytics)
        assertTrue(
            analytics.readyForEvent(
                Event.Builder("event-name", "type", "source").build()
            )
        )
    }

    @Test
    fun `handleRuleEngineResponse() should ignore events with no event data`() {
        val event = Event.Builder("rules test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).build()
        analytics.handleRuleEngineResponse(event)

        verifyNoInteractions(mockDatabase)
        verifyNoInteractions(mockApi)
    }

    @Test
    fun `handleRuleEngineResponse() should ignore events with missing triggeredconsequence node`() {
        val eventData: Map<String, Any> = mapOf("consequence" to "unknown")
        val event = Event.Builder("rules test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setEventData(eventData).build()
        analytics.handleRuleEngineResponse(event)

        verifyNoInteractions(mockDatabase)
        verifyNoInteractions(mockApi)
    }

    @Test
    fun `handleRuleEngineResponse() should ignore events with triggeredconsequence node null or empty`() {
        val eventData: Map<String, Any?> = mapOf("triggeredconsequence" to null)
        val event = Event.Builder("rules test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setEventData(eventData).build()
        analytics.handleRuleEngineResponse(event)

        val eventDataEmpty: Map<String, Any?> = mapOf("triggeredconsequence" to emptyMap<String, Any>())
        val eventEmpty = Event.Builder("rules test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setEventData(eventDataEmpty).build()
        analytics.handleRuleEngineResponse(eventEmpty)

        verifyNoInteractions(mockDatabase)
        verifyNoInteractions(mockApi)
    }

    @Test
    fun `handleRuleEngineResponse() should ignore events with null or empty consequence type`() {
        val eventData: Map<String, Any> = mapOf(
            "triggeredconsequence" to mapOf(
                "id" to "id",
                "type" to null
            )
        )
        val event = Event.Builder("rules test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setEventData(eventData).build()
        analytics.handleRuleEngineResponse(event)

        val eventDataEmpty: Map<String, Any?> = mapOf(
            "triggeredconsequence" to mapOf(
                "id" to "id",
                "type" to ""
            )
        )
        val eventEmpty = Event.Builder("rules test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setEventData(eventDataEmpty).build()
        analytics.handleRuleEngineResponse(eventEmpty)

        verifyNoInteractions(mockDatabase)
        verifyNoInteractions(mockApi)
    }

    @Test
    fun `handleRuleEngineResponse() should ignore events with consequence type different than an`() {
        val eventData: Map<String, Any> = mapOf(
            "triggeredconsequence" to mapOf(
                "id" to "id",
                "type" to "pb"
            )
        )
        val event = Event.Builder("rules test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setEventData(eventData).build()
        analytics.handleRuleEngineResponse(event)

        verifyNoInteractions(mockDatabase)
        verifyNoInteractions(mockApi)
    }

    @Test
    fun `handleRuleEngineResponse() should ignore events with null or empty consequence id`() {
        val eventData: Map<String, Any> = mapOf(
            "triggeredconsequence" to mapOf(
                "id" to null,
                "type" to "an"
            )
        )
        val event = Event.Builder("rules test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setEventData(eventData).build()
        analytics.handleRuleEngineResponse(event)

        val eventDataEmpty: Map<String, Any?> = mapOf(
            "triggeredconsequence" to mapOf(
                "id" to "",
                "type" to "an"
            )
        )
        val eventEmpty = Event.Builder("rules test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setEventData(eventDataEmpty).build()
        analytics.handleRuleEngineResponse(eventEmpty)

        verifyNoInteractions(mockDatabase)
        verifyNoInteractions(mockApi)
    }

    @Test
    fun `handleRuleEngineResponse() should handle valid analytics consequence`() {
        `when`(mockApi.getSharedState(eq("com.adobe.module.configuration"), any(Event::class.java), eq(true), any())).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    "analytics.server" to "test.com",
                    "analytics.rsids" to "rsid",
                    "global.privacy" to "optedin"
                )
            )
        )
        `when`(mockApi.getSharedState(eq("com.adobe.module.identity"), any(Event::class.java), eq(true), any())).thenReturn(
            SharedStateResult(SharedStateStatus.SET, mapOf("mid" to "mid"))
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
        val event = Event.Builder("rules test", EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT).setEventData(eventData).build()
        analytics.handleRuleEngineResponse(event)

        verify(mockDatabase).queue(anyString(), anyLong(), anyString(), anyBoolean())
        verify(mockApi, times(5)).getSharedState(anyString(), any(Event::class.java), anyBoolean(), any(SharedStateResolution::class.java)) // hard + soft dependencies
    }

    @Test
    fun `handleGenericLifecycleEvents() should ignore non lifecycle request content events`() {
        val event1 = Event.Builder("invalid lifecycle start test 1", "incorrectType", EventSource.REQUEST_CONTENT).build()
        analytics.handleGenericLifecycleEvents(event1)

        val event2 = Event.Builder("invalid lifecycle start test 2", EventType.GENERIC_LIFECYCLE, "incorrectSource").build()
        analytics.handleGenericLifecycleEvents(event2)

        verifyNoInteractions(mockDatabase)
    }

    @Test
    fun `handleGenericLifecycleEvents() should ignore lifecycle request content events that don't contain the start key`() {
        val eventData = mapOf<String, Any>(
            "action" to "unknown"
        )
        val event = Event.Builder("invalid test lifecycle start", EventType.GENERIC_LIFECYCLE, EventSource.REQUEST_CONTENT).setEventData(eventData).build()
        analytics.handleGenericLifecycleEvents(event)

        verifyNoInteractions(mockDatabase)
    }

    @Test
    fun `handleGenericLifecycleEvents() should handle lifecycle request content events for start action`() {
        val eventData = mapOf<String, Any>(
            "action" to "start"
        )
        val event = Event.Builder("invalid test lifecycle start", EventType.GENERIC_LIFECYCLE, EventSource.REQUEST_CONTENT).setEventData(eventData).build()
        analytics.handleGenericLifecycleEvents(event)

        verify(mockDatabase, times(2)).cancelWaitForAdditionalData(anyOrNull())
    }

    @Test
    fun `handleResetIdentitiesEvent() should ignore unknown type or source events`() {
        val event1 = Event.Builder("invalid reset test 1", "incorrectType", EventSource.REQUEST_RESET).build()
        analytics.handleResetIdentitiesEvent(event1)

        val event2 = Event.Builder("invalid reset test 2", EventType.GENERIC_IDENTITY, "incorrectSource").build()
        analytics.handleResetIdentitiesEvent(event2)

        verifyNoInteractions(mockDatabase)
    }

    @Test
    fun `handleResetIdentitiesEvent() should handle identity request reset`() {
        val event1 = Event.Builder("test reset", EventType.GENERIC_IDENTITY, EventSource.REQUEST_RESET).build()
        analytics.handleResetIdentitiesEvent(event1)

        verify(mockDatabase).reset()
    }
}
