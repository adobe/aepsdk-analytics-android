///* ***************************************************************************
// *
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2017 Adobe Systems Incorporated
// * All Rights Reserved.
// *
// * NOTICE:  All information contained herein is, and remains
// * the property of Adobe Systems Incorporated and its suppliers,
// * if any.  The intellectual and technical concepts contained
// * herein are proprietary to Adobe Systems Incorporated and its
// * suppliers and are protected by trade secret or copyright law.
// * Dissemination of this information or reproduction of this material
// * is strictly forbidden unless prior written permission is obtained
// * from Adobe Systems Incorporated.
// *
// ***************************************************************************/
//
//package com.adobe.marketing.mobile;
//
//import org.junit.Test;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.Assert.*;
//
//public class AnalyticsStateTest {
//	private static final String CONFIG_SHARED_STATE = "com.adobe.module.configuration";
//	private static final String LIFECYCLE_SHARED_STATE = "com.adobe.module.lifecycle";
//	private static final String PLACES_SHARED_STATE = "com.adobe.module.places";
//	private static final String ACQUISITION_SHARED_STATE = "com.adobe.module.analytics.acquisition";
//	private static final String IDENTITY_SHARED_STATE = "com.adobe.module.identity";
//	private static final String ASSURANCE_SHARED_STATE = "com.adobe.assurance";
//
//	@Test
//	public void testExtractConfigurationInfo_happyFlow() {
//		final EventData configurationData = new EventData();
//
//		configurationData.putString(AnalyticsTestConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_SERVER,
//									"analyticsServer");
//		configurationData.putString(AnalyticsTestConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_REPORT_SUITES,
//									"rsid1,rsid2");
//		configurationData.putBoolean(AnalyticsTestConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_AAMFORWARDING, true);
//		configurationData.putBoolean(AnalyticsTestConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_OFFLINE_TRACKING,
//									 true);
//		configurationData.putInteger(AnalyticsTestConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_LAUNCH_HIT_DELAY, 300);
//		configurationData.putString(AnalyticsTestConstants.EventDataKeys.Configuration.CONFIG_EXPERIENCE_CLOUD_ORGID_KEY,
//									"marketingServer");
//		configurationData.putBoolean(
//			AnalyticsTestConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_BACKDATE_PREVIOUS_SESSION, true);
//		configurationData.putString(AnalyticsTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY, "optedout");
//
//		AnalyticsState state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(CONFIG_SHARED_STATE, configurationData);
//			}
//		});
//
//		assertEquals("analyticsServer", state.getServer());
//		assertEquals("rsid1,rsid2", state.getRsids());
//		assertTrue(state.isAnalyticsForwardingEnabled());
//		assertTrue(state.isOfflineTrackingEnabled());
//		assertEquals(300, state.getReferrerTimeout());
//		assertEquals("marketingServer", state.getMarketingCloudOrganizationID());
//		assertTrue(state.isBackdateSessionInfoEnabled());
//		assertEquals(MobilePrivacyStatus.OPT_OUT, state.getPrivacyStatus());
//	}
//
//	@Test
//	public void testExtractConfigurationInfo_returnsDefaultValues_when_null() {
//		AnalyticsState state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(CONFIG_SHARED_STATE, null);
//			}
//		});
//
//		assertNull(state.getServer());
//		assertNull(state.getRsids());
//		assertFalse(state.isAnalyticsForwardingEnabled());
//		assertFalse(state.isOfflineTrackingEnabled());
//		assertEquals(0, state.getReferrerTimeout());
//		assertNull(state.getMarketingCloudOrganizationID());
//		assertFalse(state.isBackdateSessionInfoEnabled());
//		assertEquals(MobilePrivacyStatus.OPT_IN, state.getPrivacyStatus());
//	}
//
//	@Test
//	public void testExtractLifecycleInfo_happyFlow() {
//		final EventData lifecycleData = new EventData();
//		Map<String, String> deviceInfo = new HashMap<String, String>();
//		deviceInfo.put("osversion", "10.2.1");
//		deviceInfo.put("devicename", "DeviceX");
//		deviceInfo.put("resolution", "400x640");
//		deviceInfo.put("carriername", "T-Mobile");
//		deviceInfo.put("runmode", "Application");
//		deviceInfo.put("appid", "testApp");
//
//		Map<String, String> contextData = new HashMap<String, String>();
//		contextData.putAll(deviceInfo);
//		contextData.put("RandomKey", "Value");
//		contextData.put("RandomKey2", "value2");
//		lifecycleData.putStringMap("lifecyclecontextdata", contextData);
//		lifecycleData.putLong(AnalyticsTestConstants.EventDataKeys.Lifecycle.SESSION_START_TIMESTAMP, 1L);
//		lifecycleData.putLong(AnalyticsTestConstants.EventDataKeys.Lifecycle.MAX_SESSION_LENGTH, 2L);
//
//		AnalyticsState state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(LIFECYCLE_SHARED_STATE, lifecycleData);
//			}
//		});
//
//		assertEquals("10.2.1", state.getDefaultData().get("a.OSVersion"));
//		assertEquals("DeviceX", state.getDefaultData().get("a.DeviceName"));
//		assertEquals("400x640", state.getDefaultData().get("a.Resolution"));
//		assertEquals("T-Mobile", state.getDefaultData().get("a.CarrierName"));
//		assertEquals("Application", state.getDefaultData().get("a.RunMode"));
//		assertEquals("testApp", state.getDefaultData().get("a.AppID"));
//		assertEquals("testApp", state.getApplicationID());
//
//		assertEquals(1, state.getLifecycleSessionStartTimestamp());
//		assertEquals(2, state.getLifecycleMaxSessionLength());
//	}
//
//	@Test
//	public void testExtractLifecycleInfo_returnsDefaultValues_when_null() {
//		AnalyticsState state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(LIFECYCLE_SHARED_STATE, null);
//			}
//		});
//
//		assertTrue(state.getDefaultData().isEmpty());
//		assertNull(state.getApplicationID());
//		assertEquals(0, state.getLifecycleSessionStartTimestamp());
//		assertEquals(0, state.getLifecycleMaxSessionLength());
//	}
//
//	@Test
//	public void testExtractPlacesInfo_happyFlow() {
//		final EventData placesData = new EventData();
//		Map<String, String> currentPOIInfo = new HashMap<String, String>();
//		currentPOIInfo.put(AnalyticsTestConstants.EventDataKeys.Places.REGION_ID, "sampleRegionId");
//		currentPOIInfo.put(AnalyticsTestConstants.EventDataKeys.Places.REGION_NAME, "sampleRegionName");
//
//		placesData.putStringMap(AnalyticsTestConstants.EventDataKeys.Places.CURRENT_POI, currentPOIInfo);
//
//		AnalyticsState state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(PLACES_SHARED_STATE, placesData);
//			}
//		});
//
//		assertEquals("sampleRegionId", state.getDefaultData().get(AnalyticsTestConstants.ContextDataKeys.REGION_ID));
//		assertEquals("sampleRegionName", state.getDefaultData().get(AnalyticsTestConstants.ContextDataKeys.REGION_NAME));
//	}
//
//	@Test
//	public void testExtractPlacesInfo_returnsDefaultValues_when_empty() {
//		AnalyticsState state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(PLACES_SHARED_STATE, null);
//			}
//		});
//
//		assertTrue(state.getDefaultData().isEmpty());
//	}
//
//	@Test
//	public void testExtractIdentityInfo_happyFlow() {
//		final Map<String, Variant> identityData = new HashMap<String, Variant>();
//		List<VisitorID> vids = new ArrayList<VisitorID>();
//		vids.add(new VisitorID("idOrg1", "type1", "id1",
//							   VisitorID.AuthenticationState.AUTHENTICATED));
//		vids.add(new VisitorID("idOrg2", "type2", "id2",
//							   VisitorID.AuthenticationState.LOGGED_OUT));
//		identityData.put("visitoridslist", Variant.fromTypedList(vids, VisitorID.VARIANT_SERIALIZER));
//		identityData.put("mid", Variant.fromString("testMID"));
//		identityData.put("blob", Variant.fromString("testBlob"));
//		identityData.put("locationhint", Variant.fromString("testLocHint"));
//		identityData.put("advertisingidentifier", Variant.fromString("testADID"));
//
//		AnalyticsState state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(IDENTITY_SHARED_STATE, new EventData(identityData));
//			}
//		});
//
//		Map<String, String> vidParams = state.getAnalyticsIdVisitorParameters();
//		assertEquals("testMID", vidParams.get("mid"));
//		assertEquals("testBlob", vidParams.get("aamb"));
//		assertEquals("testLocHint", vidParams.get("aamlh"));
//		assertEquals("testMID", state.getMarketingCloudId());
//		assertEquals("testADID", state.getAdvertisingIdentifier());
//
//		String serializedVids = state.getSerializedVisitorIDsList();
//		assertTrue(serializedVids.contains("id1"));
//		assertTrue(serializedVids.contains("id2"));
//	}
//
//	@Test
//	public void testExtractIdentityInfo_returnsDefaultValues_when_castException() throws Exception {
//		final Map<String, Variant> identityData = new HashMap<String, Variant>();
//		List<String> vids = new ArrayList<String>();
//		vids.add("testingCastException");
//		identityData.put("visitorIDsList", Variant.fromInteger(42));
//
//		AnalyticsState state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(IDENTITY_SHARED_STATE, new EventData(identityData));
//			}
//		});
//
//		assertTrue(state.getAnalyticsIdVisitorParameters().isEmpty());
//		assertNull(state.getMarketingCloudId());
//		assertNull(state.getSerializedVisitorIDsList());
//	}
//
//	@Test
//	public void testExtractIdentityInfo_returnsDefaultValues_when_null() throws Exception {
//		AnalyticsState state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(IDENTITY_SHARED_STATE, null);
//			}
//		});
//
//		assertTrue(state.getAnalyticsIdVisitorParameters().isEmpty());
//		assertNull(state.getMarketingCloudId());
//		assertNull(state.getSerializedVisitorIDsList());
//	}
//
//	@Test
//	public void testExtractAssuranceInfo_returnsDefaultValues_happyFlow() throws Exception {
//		final Map<String, Variant> assuranceData = new HashMap<String, Variant>();
//		assuranceData.put(AnalyticsTestConstants.EventDataKeys.Assurance.SESSION_ID,
//						  Variant.fromString("validsessionid"));
//
//		AnalyticsState state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(ASSURANCE_SHARED_STATE, new EventData(assuranceData));
//			}
//		});
//
//		assertTrue(state.isAssuranceSessionActive());
//	}
//
//	@Test
//	public void testExtractAssuranceInfo_returnsDefaultValues_when_null() throws Exception {
//		AnalyticsState state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(ASSURANCE_SHARED_STATE, null);
//			}
//		});
//
//		assertFalse(state.isAssuranceSessionActive());
//	}
//
//	@Test
//	public void testGetBaseURL_when_sslAndForwarding() {
//		AnalyticsState state = new AnalyticsState(null);
//		state.setAnalyticsForwardingEnabled(true);
//		state.setServer("test.com");
//		state.setRsids("rsid1,rsid2");
//		assertEquals("https://test.com/b/ss/rsid1%2Crsid2/10/JAVA-5.0.0-AN/s", state.getBaseURL("JAVA-5.0.0-AN"));
//	}
//
//	@Test
//	public void testGetBaseURL_NotForwarding() {
//		AnalyticsState state = new AnalyticsState(null);
//		state.setAnalyticsForwardingEnabled(false);
//		state.setServer("test.com");
//		state.setRsids("rsid1,rsid2");
//		assertEquals("https://test.com/b/ss/rsid1%2Crsid2/0/JAVA-5.0.0-AN/s", state.getBaseURL("JAVA-5.0.0-AN"));
//	}
//
//	@Test
//	public void testIsAnalyticsConfigured_happyFlow() {
//		AnalyticsState state = new AnalyticsState(null);
//		state.setServer("test.com");
//		state.setRsids("rsid1,rsid2");
//		assertTrue(state.isAnalyticsConfigured());
//	}
//
//	@Test
//	public void testIsAnalyticsConfigured_returnsFalse_whenNoServerOrRsid() {
//		AnalyticsState state = new AnalyticsState(null);
//		// server null, rsids null
//		assertFalse(state.isAnalyticsConfigured());
//
//		// server null, rsids not null
//		state.setRsids("rsid1,rsid2");
//		assertFalse(state.isAnalyticsConfigured());
//
//		// server not null, rsids null
//		state.setRsids(null);
//		state.setServer("test");
//		assertFalse(state.isAnalyticsConfigured());
//	}
//}
