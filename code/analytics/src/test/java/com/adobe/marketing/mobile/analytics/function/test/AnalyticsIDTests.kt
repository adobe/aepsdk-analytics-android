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
import com.adobe.marketing.mobile.analytics.internal.AnalyticsHit
import com.adobe.marketing.mobile.analytics.internal.TimeZoneHelper
import com.adobe.marketing.mobile.analytics.internal.extractContextDataFrom
import com.adobe.marketing.mobile.analytics.internal.extractQueryParamsFrom
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.eq
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.secondValue
import java.net.URLDecoder
import java.util.concurrent.CountDownLatch

internal class AnalyticsIDTests : AnalyticsFunctionalTestBase() {

    @Test(timeout = 10000)
    fun `handleGenericTrackEvent - hit contains vid vars`() {
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(config(MobilePrivacyStatus.OPT_IN), defaultIdentity())

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

        assertEquals(1, mockedMainDataQueue.count())

        val hit = AnalyticsHit.from(mockedMainDataQueue.peek())
        val body = URLDecoder.decode(hit.payload, "UTF-8")
        val varMap: Map<String, Any> = extractQueryParamsFrom(body)
        val contextDataMap: Map<String, Any> = extractContextDataFrom(body)

        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "AMACTION:testActionName",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            // TODO: ?? no timezone??
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "a.action" to "testActionName"
        )
        assertEquals(expectedContextData, contextDataMap)
        assertEquals(expectedVars.size, varMap.size)
        assertEquals(expectedVars, varMap)
    }

    @Test(timeout = 10000)
    fun `handleGenericTrackEvent - hit contains vid and aid`() {
        Mockito.`when`(mockedNameCollection.getString(any(), anyOrNull()))
            .thenAnswer { invocation ->
                when (invocation.arguments[0] as? String) {
                    "ADOBEMOBILE_STOREDDEFAULTS_AID" -> {
                        return@thenAnswer "testaid"
                    }
                    "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER" -> {
                        return@thenAnswer "testvid"
                    }
                    else -> {
                        return@thenAnswer ""
                    }
                }
            }

        val configuration = config(MobilePrivacyStatus.OPT_IN)
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(configuration, defaultIdentity())

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

        assertEquals(1, mockedMainDataQueue.count())

        val hit = AnalyticsHit.from(mockedMainDataQueue.peek())
        val body = URLDecoder.decode(hit.payload, "UTF-8")
        val varMap: Map<String, Any> = extractQueryParamsFrom(body)
        val contextDataMap: Map<String, Any> = extractContextDataFrom(body)

        val expectedVars: Map<String, String> = mapOf(
            "ndh" to "1",
            "ce" to "UTF-8",
            "cp" to "foreground",
            "pev2" to "AMACTION:testActionName",
            "pe" to "lnk_o",
            "mid" to "mid",
            "aid" to "testaid",
            "vid" to "testvid",
            "aamb" to "blob",
            "aamlh" to "lochint",
            // TODO: ?? no timezone??
            "t" to TimeZoneHelper.TIMESTAMP_TIMEZONE_OFFSET,
            "ts" to trackEvent.timestampInSeconds.toString()
        )
        val expectedContextData: Map<String, String> = mapOf(
            "k1" to "v1",
            "k2" to "v2",
            "a.action" to "testActionName"
        )
        assertEquals(expectedContextData, contextDataMap)
        assertEquals(expectedVars.size, varMap.size)
        assertEquals(expectedVars, varMap)
    }

    @Test(timeout = 10000)
    fun `optout - shared state should not contain vid and aid`() {
        val countDownLatch = CountDownLatch(1)
        var analyticsSharedState: Map<String, Any> = emptyMap()

        Mockito.`when`(mockedExtensionApi.createSharedState(any(), anyOrNull()))
            .then { invocation ->
                val data = invocation.arguments[0] as? Map<String, Any>
                data?.let { analyticsSharedState = it }
                countDownLatch.countDown()
            }
        val configuration = config(MobilePrivacyStatus.OPT_OUT)

        val configurationResponseEvent = Event.Builder(
            "configuration event",
            EventType.CONFIGURATION,
            EventSource.RESPONSE_CONTENT
        ).setEventData(configuration).build()
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(configuration, defaultIdentity())

        analyticsExtension.handleIncomingEvent(configurationResponseEvent)

        countDownLatch.await()
        assertTrue(analyticsSharedState.isEmpty())
    }

    @Test(timeout = 10000)
    fun `handleAnalyticsRequestIdentityEvent - shared state should contain vid`() {
        val countDownLatch = CountDownLatch(1)
        var analyticsSharedState: Map<String, Any> = emptyMap()

        var setAid: String? = null
        var setVid: String? = null

        Mockito.`when`(mockedNameCollection.setString(any(), any()))
            .thenAnswer { invocation ->
                when (invocation.arguments[0] as? String) {
                    "ADOBEMOBILE_STOREDDEFAULTS_AID" -> {
                        setAid = invocation.arguments[1] as String
                    }
                    "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER" -> {
                        setVid = invocation.arguments[1] as String
                    }
                }
            }

        Mockito.`when`(mockedNameCollection.getString(any(), anyOrNull()))
            .thenAnswer { invocation ->
                when (invocation.arguments[0] as? String) {
                    "ADOBEMOBILE_STOREDDEFAULTS_AID" -> {
                        return@thenAnswer setAid
                    }
                    "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER" -> {
                        return@thenAnswer setVid
                    }
                    else -> {
                        return@thenAnswer ""
                    }
                }
            }

        Mockito.`when`(mockedExtensionApi.createSharedState(any(), anyOrNull()))
            .then { invocation ->
                val data = invocation.arguments[0] as? Map<String, Any>
                data?.let { analyticsSharedState = it }
                countDownLatch.countDown()
            }

        val analyticsExtension = initializeAnalyticsExtensionWithPreset(config(MobilePrivacyStatus.OPT_IN), defaultIdentity())

        val event = Event.Builder(
            "analytics event",
            EventType.ANALYTICS,
            EventSource.REQUEST_IDENTITY
        ).setEventData(
            mapOf(
                "vid" to "myvid"
            )
        ).build()

        analyticsExtension.handleIncomingEvent(event)

        countDownLatch.await()
        assertEquals(
            mapOf(
                "vid" to "myvid"
            ),
            analyticsSharedState
        )
    }

    @Test(timeout = 10000)
    fun `handleAnalyticsRequestIdentityEvent - shared state should not contain vid if optedout`() {
        val countDownLatch = CountDownLatch(1)
        var analyticsSharedState: Map<String, Any> = emptyMap()

        Mockito.`when`(mockedExtensionApi.createSharedState(any(), anyOrNull()))
            .then { invocation ->
                val data = invocation.arguments[0] as? Map<String, Any>
                data?.let { analyticsSharedState = it }
                countDownLatch.countDown()
            }
        val configuration = config(MobilePrivacyStatus.OPT_OUT)

        val configurationResponseEvent = Event.Builder(
            "configuration event",
            EventType.CONFIGURATION,
            EventSource.RESPONSE_CONTENT
        ).setEventData(configuration).build()
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(
            configuration,
            defaultIdentity()
        )
        analyticsExtension.handleIncomingEvent(configurationResponseEvent)
        val event = Event.Builder(
            "analytics event",
            EventType.ANALYTICS,
            EventSource.REQUEST_IDENTITY
        ).setEventData(
            mapOf(
                "vid" to "myvid"
            )
        ).build()

        analyticsExtension.handleIncomingEvent(event)

        countDownLatch.await()
        assertTrue(analyticsSharedState.isEmpty())
    }

    @Test(timeout = 10000)
    fun `handleAnalyticsRequestIdentityEvent - when vid update with valid value should update persistence, update shared state and dispatch updated ids`() {
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(config(MobilePrivacyStatus.OPT_IN), defaultIdentity())

        // test
        val event = Event.Builder("analytics event", EventType.ANALYTICS, EventSource.REQUEST_IDENTITY)
            .setEventData(mapOf("vid" to "updatedVid")).build()

        analyticsExtension.handleIncomingEvent(event)

        // verify persistence
        verify(mockedNameCollection).setString(eq("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER"), eq("updatedVid"))
        verify(mockedNameCollection, never()).remove(eq("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER"))

        // verify shared state created
        verify(mockedExtensionApi, times(1)).createSharedState(any(), any())

        // verify both paired and generic response events are dispatched
        val eventCaptor = ArgumentCaptor.forClass(Event::class.java)
        verify(mockedExtensionApi, times(2)).dispatch(eventCaptor.capture())
        val responseEventPaired = eventCaptor.firstValue
        assertNotNull(responseEventPaired)
        assertEquals("TrackingIdentifierValue", responseEventPaired.name)
        assertEquals(EventType.ANALYTICS, responseEventPaired.type)
        assertEquals(EventSource.RESPONSE_IDENTITY, responseEventPaired.source)
        assertEquals(event.uniqueIdentifier, responseEventPaired.responseID)

        val responseEventGeneric = eventCaptor.secondValue
        assertNotNull(responseEventGeneric)
        assertEquals("TrackingIdentifierValue", responseEventGeneric.name)
        assertEquals(EventType.ANALYTICS, responseEventGeneric.type)
        assertEquals(EventSource.RESPONSE_IDENTITY, responseEventGeneric.source)
        assertNull(responseEventGeneric.responseID)
    }

    @Test(timeout = 10000)
    fun `handleAnalyticsRequestIdentityEvent - when vid update with null should remove vid, update shared state and dispatch updated ids`() {
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(config(MobilePrivacyStatus.OPT_IN), defaultIdentity())

        // test
        val event = Event.Builder("analytics event", EventType.ANALYTICS, EventSource.REQUEST_IDENTITY)
            .setEventData(mapOf("vid" to null)).build()

        analyticsExtension.handleIncomingEvent(event)

        // verify
        verify(mockedNameCollection, never()).setString(eq("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER"), any())
        verify(mockedNameCollection).remove(eq("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER"))

        // verify shared state created
        verify(mockedExtensionApi, times(1)).createSharedState(any(), any())

        // verify both paired and generic response events are dispatched
        val eventCaptor = ArgumentCaptor.forClass(Event::class.java)
        verify(mockedExtensionApi, times(2)).dispatch(eventCaptor.capture())
        val responseEventPaired = eventCaptor.firstValue
        assertNotNull(responseEventPaired)
        assertEquals("TrackingIdentifierValue", responseEventPaired.name)
        assertEquals(EventType.ANALYTICS, responseEventPaired.type)
        assertEquals(EventSource.RESPONSE_IDENTITY, responseEventPaired.source)
        assertEquals(event.uniqueIdentifier, responseEventPaired.responseID)

        val responseEventGeneric = eventCaptor.secondValue
        assertNotNull(responseEventGeneric)
        assertEquals("TrackingIdentifierValue", responseEventGeneric.name)
        assertEquals(EventType.ANALYTICS, responseEventGeneric.type)
        assertEquals(EventSource.RESPONSE_IDENTITY, responseEventGeneric.source)
        assertNull(responseEventGeneric.responseID)
    }

    @Test(timeout = 10000)
    fun `handleAnalyticsRequestIdentityEvent - when vid update with empty should remove vid, update shared state and dispatch updated ids`() {
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(config(MobilePrivacyStatus.OPT_IN), defaultIdentity())

        // test
        val event = Event.Builder("analytics event", EventType.ANALYTICS, EventSource.REQUEST_IDENTITY)
            .setEventData(mapOf("vid" to "")).build()

        analyticsExtension.handleIncomingEvent(event)

        // verify
        verify(mockedNameCollection, never()).setString(eq("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER"), any())
        verify(mockedNameCollection).remove(eq("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER"))

        // verify shared state created
        verify(mockedExtensionApi, times(1)).createSharedState(any(), any())

        // verify both paired and generic response events are dispatched
        val eventCaptor = ArgumentCaptor.forClass(Event::class.java)
        verify(mockedExtensionApi, times(2)).dispatch(eventCaptor.capture())
        val responseEventPaired = eventCaptor.firstValue
        assertNotNull(responseEventPaired)
        assertEquals("TrackingIdentifierValue", responseEventPaired.name)
        assertEquals(EventType.ANALYTICS, responseEventPaired.type)
        assertEquals(EventSource.RESPONSE_IDENTITY, responseEventPaired.source)
        assertEquals(event.uniqueIdentifier, responseEventPaired.responseID)

        val responseEventGeneric = eventCaptor.secondValue
        assertNotNull(responseEventGeneric)
        assertEquals("TrackingIdentifierValue", responseEventGeneric.name)
        assertEquals(EventType.ANALYTICS, responseEventGeneric.type)
        assertEquals(EventSource.RESPONSE_IDENTITY, responseEventGeneric.source)
        assertNull(responseEventGeneric.responseID)
    }

    @Test(timeout = 10000)
    fun `handleAnalyticsRequestIdentityEvent - when vid with invalid type should ignore request`() {
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(config(MobilePrivacyStatus.OPT_IN), defaultIdentity())

        // test
        val event = Event.Builder("analytics event", EventType.ANALYTICS, EventSource.REQUEST_IDENTITY)
            .setEventData(mapOf("vid" to true)).build()

        analyticsExtension.handleIncomingEvent(event)

        // verify persistence
        verify(mockedNameCollection, never()).setString(eq("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER"), any())
        verify(mockedNameCollection, never()).remove(eq("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER"))

        // verify shared state not created
        verify(mockedExtensionApi, never()).createSharedState(any(), any())

        // verify no response events are dispatched
        verify(mockedExtensionApi, never()).dispatch(any())
    }

    @Test(timeout = 10000)
    fun `handleAnalyticsRequestIdentityEvent - when identity request should not update shared state, should dispatch current ids`() {
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(config(MobilePrivacyStatus.OPT_IN), defaultIdentity())

        // test
        val event = Event.Builder("analytics event", EventType.ANALYTICS, EventSource.REQUEST_IDENTITY)
            .setEventData(mapOf("novid" to "")).build()

        analyticsExtension.handleIncomingEvent(event)

        // verify persistence not updated
        verify(mockedNameCollection, never()).setString(eq("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER"), any())
        verify(mockedNameCollection, never()).setString(eq("ADOBEMOBILE_STOREDDEFAULTS_AID"), any())
        verify(mockedNameCollection, never()).remove(eq("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER"))
        verify(mockedNameCollection, never()).remove(eq("ADOBEMOBILE_STOREDDEFAULTS_AID"))

        // verify no new shared state is created
        verify(mockedExtensionApi, never()).createSharedState(any(), any())

        // verify both paired and generic response events are dispatched
        val eventCaptor = ArgumentCaptor.forClass(Event::class.java)
        verify(mockedExtensionApi, times(2)).dispatch(eventCaptor.capture())
        val responseEventPaired = eventCaptor.firstValue
        assertNotNull(responseEventPaired)
        assertEquals("TrackingIdentifierValue", responseEventPaired.name)
        assertEquals(EventType.ANALYTICS, responseEventPaired.type)
        assertEquals(EventSource.RESPONSE_IDENTITY, responseEventPaired.source)
        assertEquals(event.uniqueIdentifier, responseEventPaired.responseID)

        val responseEventGeneric = eventCaptor.secondValue
        assertNotNull(responseEventGeneric)
        assertEquals("TrackingIdentifierValue", responseEventGeneric.name)
        assertEquals(EventType.ANALYTICS, responseEventGeneric.type)
        assertEquals(EventSource.RESPONSE_IDENTITY, responseEventGeneric.source)
        assertNull(responseEventGeneric.responseID)
    }

    @Test(timeout = 10000)
    fun `vid and aid should not be shared again on config update with privacy optin`() {
        val configuration = config(MobilePrivacyStatus.OPT_IN)
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(configuration, defaultIdentity())

        val configurationResponseEvent = Event.Builder(
            "first configuration event",
            EventType.CONFIGURATION,
            EventSource.RESPONSE_CONTENT
        ).setEventData(configuration).build()

        analyticsExtension.handleIncomingEvent(configurationResponseEvent)

        verify(mockedExtensionApi, never()).createSharedState(any(), any())
    }

    @Test(timeout = 10000)
    fun `vid and aid should be cleared after optedout`() {
        var setAid: String? = "testaid"
        var setVid: String? = "testvid"

        Mockito.`when`(mockedNameCollection.setString(any(), any()))
            .thenAnswer { invocation ->
                when (invocation.arguments[0] as? String) {
                    "ADOBEMOBILE_STOREDDEFAULTS_AID" -> {
                        setAid = invocation.arguments[1] as String
                    }
                    "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER" -> {
                        setVid = invocation.arguments[1] as String
                    }
                }
            }

        Mockito.`when`(mockedNameCollection.getString(any(), anyOrNull()))
            .thenAnswer { invocation ->
                when (invocation.arguments[0] as? String) {
                    "ADOBEMOBILE_STOREDDEFAULTS_AID" -> {
                        return@thenAnswer setAid
                    }
                    "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER" -> {
                        return@thenAnswer setVid
                    }
                    else -> {
                        return@thenAnswer ""
                    }
                }
            }

        Mockito.`when`(mockedNameCollection.remove(any()))
            .thenAnswer { invocation ->
                when (invocation.arguments[0] as? String) {
                    "ADOBEMOBILE_STOREDDEFAULTS_AID" -> {
                        setAid = null
                    }
                    "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER" -> {
                        setVid = null
                    }
                }
            }

        val countDownLatch = CountDownLatch(1)
        var analyticsSharedState1: Map<String, Any> = emptyMap()

        Mockito.`when`(mockedExtensionApi.createSharedState(any(), anyOrNull()))
            .then { invocation ->
                val data = invocation.arguments[0] as? Map<String, Any>
                data?.let {
                    analyticsSharedState1 = it
                }
                countDownLatch.countDown()
            }

        val configuration = config(MobilePrivacyStatus.OPT_IN)
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(configuration, defaultIdentity())

        val configurationResponseEvent = Event.Builder(
            "first configuration event",
            EventType.CONFIGURATION,
            EventSource.RESPONSE_CONTENT
        ).setEventData(configuration).build()

        // test
        analyticsExtension.handleIncomingEvent(configurationResponseEvent)
        updateMockedSharedState(
            "com.adobe.module.configuration",
            config(MobilePrivacyStatus.OPT_OUT)
        )
        analyticsExtension.handleIncomingEvent(
            Event.Builder(
                "second configuration event",
                EventType.CONFIGURATION,
                EventSource.RESPONSE_CONTENT
            ).build()
        )

        countDownLatch.await()
        assertTrue(analyticsSharedState1.isEmpty())
    }

    @Test(timeout = 10000)
    fun `vid and aid should be cleared after request reset event`() {
        var setAid: String? = "testaid"
        var setVid: String? = "testvid"

        Mockito.`when`(mockedNameCollection.setString(any(), any()))
            .thenAnswer { invocation ->
                when (invocation.arguments[0] as? String) {
                    "ADOBEMOBILE_STOREDDEFAULTS_AID" -> {
                        setAid = invocation.arguments[1] as String
                    }
                    "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER" -> {
                        setVid = invocation.arguments[1] as String
                    }
                }
            }

        Mockito.`when`(mockedNameCollection.getString(any(), anyOrNull()))
            .thenAnswer { invocation ->
                when (invocation.arguments[0] as? String) {
                    "ADOBEMOBILE_STOREDDEFAULTS_AID" -> {
                        return@thenAnswer setAid
                    }
                    "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER" -> {
                        return@thenAnswer setVid
                    }
                    else -> {
                        return@thenAnswer ""
                    }
                }
            }

        Mockito.`when`(mockedNameCollection.remove(any()))
            .thenAnswer { invocation ->
                when (invocation.arguments[0] as? String) {
                    "ADOBEMOBILE_STOREDDEFAULTS_AID" -> {
                        setAid = null
                    }
                    "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER" -> {
                        setVid = null
                    }
                }
            }

        val countDownLatch = CountDownLatch(1)
        var analyticsSharedState1: Map<String, Any> = emptyMap()

        Mockito.`when`(mockedExtensionApi.createSharedState(any(), anyOrNull()))
            .then { invocation ->
                val data = invocation.arguments[0] as? Map<String, Any>
                data?.let {
                    analyticsSharedState1 = it
                }
                countDownLatch.countDown()
            }

        val configuration = config(MobilePrivacyStatus.OPT_IN)
        val analyticsExtension = initializeAnalyticsExtensionWithPreset(configuration, defaultIdentity())

        val configurationResponseEvent = Event.Builder(
            "configuration event",
            EventType.CONFIGURATION,
            EventSource.RESPONSE_CONTENT
        ).setEventData(configuration).build()

        analyticsExtension.handleIncomingEvent(configurationResponseEvent)
        analyticsExtension.handleIncomingEvent(
            Event.Builder(
                "reset event",
                EventType.GENERIC_IDENTITY,
                EventSource.REQUEST_RESET
            ).build()
        )

        countDownLatch.await()
        assertTrue(analyticsSharedState1.isEmpty())
    }
}
