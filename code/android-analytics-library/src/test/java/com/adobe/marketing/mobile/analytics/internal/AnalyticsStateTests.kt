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

import com.adobe.marketing.mobile.MobilePrivacyStatus
import org.junit.Assert.*
import org.junit.Test

class AnalyticsStateTests {

    companion object {
        private const val CONFIG_SHARED_STATE = "com.adobe.module.configuration"
        private const val LIFECYCLE_SHARED_STATE = "com.adobe.module.lifecycle"
        private const val PLACES_SHARED_STATE = "com.adobe.module.places"
        private const val ACQUISITION_SHARED_STATE = "com.adobe.module.analytics.acquisition"
        private const val IDENTITY_SHARED_STATE = "com.adobe.module.identity"
        private const val ASSURANCE_SHARED_STATE = "com.adobe.assurance"
    }

    @Test
    fun testExtractConfigurationInfo_happyFlow() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                CONFIG_SHARED_STATE to mapOf(
                    "analytics.server" to "analyticsServer",
                    "analytics.rsids" to "rsid1,rsid2",
                    "analytics.aamForwardingEnabled" to true,
                    "analytics.offlineEnabled" to true,
                    "analytics.launchHitDelay" to 300,
                    "experienceCloud.org" to "marketingServer",
                    "analytics.backdatePreviousSessionInfo" to true,
                    "global.privacy" to "optedout"
                )
            )
        )
        assertEquals("analyticsServer", state.host)
        assertEquals("rsid1,rsid2", state.rsids)
        assertTrue(state.isAnalyticsForwardingEnabled)
        assertTrue(state.isOfflineTrackingEnabled)
        assertEquals(300, state.referrerTimeout)
        assertEquals("marketingServer", state.marketingCloudOrganizationID)
        assertTrue(state.isBackdateSessionInfoEnabled)
        assertEquals(MobilePrivacyStatus.OPT_OUT, state.privacyStatus)
    }

    @Test
    fun testExtractConfigurationInfo_returnsDefaultValues_when_null() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                CONFIG_SHARED_STATE to null
            )
        )
        assertNull(state.host)
        assertNull(state.rsids)
        assertFalse(state.isAnalyticsForwardingEnabled)
        assertFalse(state.isOfflineTrackingEnabled)
        assertEquals(0, state.referrerTimeout)
        assertNull(state.marketingCloudOrganizationID)
        assertFalse(state.isBackdateSessionInfoEnabled)
        assertEquals(MobilePrivacyStatus.OPT_IN, state.privacyStatus)
    }

    @Test
    fun testExtractLifecycleInfo_happyFlow() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                LIFECYCLE_SHARED_STATE to mapOf(
                    "lifecyclecontextdata" to mapOf(
                        "osversion" to "10.2.1",
                        "devicename" to "DeviceX",
                        "resolution" to "400x640",
                        "carriername" to "T-Mobile",
                        "runmode" to "Application",
                        "appid" to "testApp",
                        "RandomKey" to "Value",
                        "RandomKey2" to "value2"
                    ),
                    "starttimestampmillis" to 1L,
                    "maxsessionlength" to 2L
                )
            )
        )
        assertEquals("10.2.1", state.defaultData["a.OSVersion"])
        assertEquals("DeviceX", state.defaultData["a.DeviceName"])
        assertEquals("400x640", state.defaultData["a.Resolution"])
        assertEquals("T-Mobile", state.defaultData["a.CarrierName"])
        assertEquals("Application", state.defaultData["a.RunMode"])
        assertEquals("testApp", state.defaultData["a.AppID"])
        assertEquals("testApp", state.applicationID)
        assertEquals(1, state.lifecycleSessionStartTimestamp)
        assertEquals(2, state.lifecycleMaxSessionLength)
    }

    @Test
    fun testExtractLifecycleInfo_returnsDefaultValues_when_null() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                LIFECYCLE_SHARED_STATE to null
            )
        )
        assertTrue(state.defaultData.isEmpty())
        assertNull(state.applicationID)
        assertEquals(0, state.lifecycleSessionStartTimestamp)
        assertEquals(0, state.lifecycleMaxSessionLength)
    }

    @Test
    fun testExtractPlacesInfo_happyFlow() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                PLACES_SHARED_STATE to mapOf(
                    "currentpoi" to mapOf(
                        "regionid" to "sampleRegionId",
                        "regionname" to "sampleRegionName"
                    )

                )
            )
        )
        assertEquals(
            "sampleRegionId",
            state.defaultData[AnalyticsConstants.ContextDataKeys.REGION_ID]
        )
        assertEquals(
            "sampleRegionName",
            state.defaultData[AnalyticsConstants.ContextDataKeys.REGION_NAME]
        )
    }

    @Test
    fun testExtractPlacesInfo_returnsDefaultValues_when_empty() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                PLACES_SHARED_STATE to null
            )
        )
        assertTrue(state.defaultData.isEmpty())
    }

    @Test
    fun testExtractIdentityInfo_happyFlow() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                IDENTITY_SHARED_STATE to mapOf(
                    "mid" to "testMID",
                    "blob" to "testBlob",
                    "locationhint" to "testLocHint",
                    "advertisingidentifier" to "testADID"
                )
            )
        )
        assertEquals("testBlob", state.blob)
        assertEquals("testLocHint", state.locationHint)
        assertEquals("testMID", state.marketingCloudId)
        assertEquals("testADID", state.advertisingIdentifier)
    }

    @Test
    fun testExtractIdentityInfo_returnsDefaultValues_when_null() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                IDENTITY_SHARED_STATE to null
            )
        )
        assertNull(state.marketingCloudId)
        assertNull(state.blob)
        assertNull(state.locationHint)
        assertNull(state.serializedVisitorIDsList)
    }

    @Test
    fun testExtractAssuranceInfo_returnsDefaultValues_happyFlow() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                ASSURANCE_SHARED_STATE to mapOf(
                    "sessionid" to "validsessionid"
                )
            )
        )
        assertTrue(state.isAssuranceSessionActive)
    }

    @Test
    fun testExtractAssuranceInfo_returnsDefaultValues_when_null() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                ASSURANCE_SHARED_STATE to null
            )
        )
        assertFalse(state.isAssuranceSessionActive)
    }

    @Test
    fun testIsAnalyticsConfiguredHappyFlow() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                CONFIG_SHARED_STATE to mapOf(
                    "analytics.server" to "analyticsServer",
                    "analytics.rsids" to "rsid1,rsid2"
                )
            )
        )
        assertTrue(state.isAnalyticsConfigured)
    }

    @Test
    fun testIsAnalyticsConfiguredReturnsFalseWhenNoServerIds() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                CONFIG_SHARED_STATE to mapOf(
                    "analytics.server" to "",
                    "analytics.rsids" to "rsid1,rsid2"
                )
            )
        )
        assertFalse(state.isAnalyticsConfigured)
    }

    @Test
    fun testIsAnalyticsConfiguredReturnsFalseWhenNoRsids() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                CONFIG_SHARED_STATE to mapOf(
                    "analytics.server" to "analyticsServer",
                    "analytics.rsids" to ""
                )
            )
        )
        assertFalse(state.isAnalyticsConfigured)
    }

    @Test
    fun testGetAnalyticsIdVisitorParameters() {
        val state = AnalyticsState()
        state.update(
            mapOf(
                IDENTITY_SHARED_STATE to mapOf(
                    "mid" to "testMID",
                    "blob" to "testBlob",
                    "locationhint" to "testLocHint"
                )
            )
        )
        assertEquals(
            mapOf(
                "mid" to "testMID",
                "aamb" to "testBlob",
                "aamlh" to "testLocHint"
            ), state.analyticsIdVisitorParameters
        )
    }

    @Test
    fun testGetAnalyticsIdVisitorParametersWhenVisitorDataIsAbsent() {
        val state = AnalyticsState()
        assertTrue(state.analyticsIdVisitorParameters.isEmpty())
    }

}