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

import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.ExtensionEventListener
import com.adobe.marketing.mobile.ExtensionHelper
import com.adobe.marketing.mobile.services.DataStoring
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.util.SQLiteUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.eq
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

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
    fun `onRegistered() should call createSharedState for version 0`() {
        `when`(mockNamedCollection.getString(any(), any())).thenReturn("test")
        ExtensionHelper.notifyRegistered(analytics)

        val expectedData = mutableMapOf<String, Any>()
        expectedData["aid"] = "test"
        expectedData["vid"] = "test"

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
}
