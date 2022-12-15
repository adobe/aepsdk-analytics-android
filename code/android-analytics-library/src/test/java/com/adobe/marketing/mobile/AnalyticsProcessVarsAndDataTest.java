///****************************************************************************
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
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//public class AnalyticsProcessVarsAndDataTest extends BaseTest {
//
//	private static final String IDENTITY_SHARED_STATE    = "com.adobe.module.identity";
//	private static final String LIFECYCLE_SHARED_STATE   = "com.adobe.module.lifecycle";
//	private static final String ACQUISITION_SHARED_STATE = "com.adobe.module.analytics.acquisition";
//
//	static final String TIMESTAMP_TIMEZONE_OFFSET;
//
//	static {
//		Calendar cal = Calendar.getInstance();
//		TIMESTAMP_TIMEZONE_OFFSET = "00/00/0000 00:00:00 0 " + TimeUnit.MILLISECONDS.toMinutes((long)(cal.get(
//										Calendar.ZONE_OFFSET) * -1) - cal.get(Calendar.DST_OFFSET));
//	}
//
//	private TestableAnalytics   module;
//	private AnalyticsState      state;
//	private AnalyticsProperties properties;
//
//	@Before
//	public void testSetup() throws Exception {
//		super.beforeEach();
//		properties = new AnalyticsProperties();
//		state = new AnalyticsState(null);
//		module = new TestableAnalytics(eventHub, platformServices);
//		module.analyticsProperties = properties;
//		module.setHelpers(null, properties, null);
//
//		// logging settings
//		FakeLoggingService fakeLoggingService = (FakeLoggingService) platformServices.getLoggingService();
//		fakeLoggingService.setIgnoreRegisteringErrors();
//		Log.setLoggingService(fakeLoggingService);
//		Log.setLogLevel(LoggingMode.VERBOSE);
//	}
//
//	// ------------- TEST PROCESS WITH CONTEXT DATA -----------
//	@Test
//	public void testProcessAnalyticsData_When_WithContextData_Happy() throws Exception {
//		Map<String, String> contextData = new HashMap<String, String>() {
//			{
//				put("key1", "value1");
//			}
//		};
//		EventData request = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE, "testingHere")
//		.putStringMap(AnalyticsTestConstants.EventDataKeys.Analytics.CONTEXT_DATA, contextData);
//		Map<String, String> result = module.processAnalyticsContextData(state, request);
//		assertEquals(contextData, result);
//	}
//
//	@Test
//	public void testProcessAnalyticsData_When_NullContextData() throws Exception {
//		EventData request = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE, "testingHere")
//		.putStringMap(AnalyticsTestConstants.EventDataKeys.Analytics.CONTEXT_DATA, null);
//		Map<String, String> result = module.processAnalyticsContextData(state, request);
//		assertTrue(result.isEmpty());
//	}
//
//	@Test
//	public void testProcessAnalyticsData_doesNOTAppendReferrerData_when_installEvent_and_referrerDataNull() throws
//		Exception {
//		EventData request = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE, "testingHere")
//		.putStringMap(AnalyticsTestConstants.EventDataKeys.Analytics.CONTEXT_DATA, new HashMap<String, String>() {
//			{
//				put("a.InstallEvent", "InstallEvent");
//			}
//		});
//		Map<String, String> result = module.processAnalyticsContextData(state, request);
//		assertEquals(1, result.size());
//		assertEquals("InstallEvent", result.get("a.InstallEvent"));
//		assertFalse(properties.getReferrerTimerState().isTimerRunning());
//	}
//
//	@Test
//	public void testProcessAnalyticsData_appendsTimeSinceLaunch_when_NotOverMaxSessionLength() throws Exception {
//		EventData lifecycleData = new EventData();
//		lifecycleData.putLong(AnalyticsTestConstants.EventDataKeys.Lifecycle.SESSION_START_TIMESTAMP,
//							  System.currentTimeMillis() / 1000 - 10);
//		lifecycleData.putLong(AnalyticsTestConstants.EventDataKeys.Lifecycle.MAX_SESSION_LENGTH, 150);
//
//		Map<String, EventData> sharedData = new HashMap<String, EventData>();
//		sharedData.put(LIFECYCLE_SHARED_STATE, lifecycleData);
//		state = new AnalyticsState(sharedData);
//
//		EventData request = new EventData().putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE,
//				"testingHere");
//		Map<String, String> result = module.processAnalyticsContextData(state, request);
//		assertEquals(1, result.size());
//		assertTrue(String.format("Time Since launch is negative: %s", result.get("a.TimeSinceLaunch")),
//				   Long.valueOf(result.get("a.TimeSinceLaunch")) > 0);
//	}
//
//	@Test
//	public void testProcessAnalyticsData_doesNOTAppendTimeSinceLaunch_when_overMaxSessionLength() throws Exception {
//		EventData lifecycleData = new EventData();
//		lifecycleData.putLong(AnalyticsTestConstants.EventDataKeys.Lifecycle.SESSION_START_TIMESTAMP,
//							  System.currentTimeMillis() / 1000 - 100);
//		lifecycleData.putLong(AnalyticsTestConstants.EventDataKeys.Lifecycle.MAX_SESSION_LENGTH, 1);
//
//		Map<String, EventData> sharedData = new HashMap<String, EventData>();
//		sharedData.put(LIFECYCLE_SHARED_STATE, lifecycleData);
//		state = new AnalyticsState(sharedData);
//
//		EventData request = new EventData().putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE,
//				"testingHere");
//		Map<String, String> result = module.processAnalyticsContextData(state, request);
//		assertEquals(0, result.size());
//	}
//
//	@Test
//	public void testProcessAnalyticsData_doesNOTAppendTimeSinceLaunch_when_lifecycleStartIsZero() throws Exception {
//		EventData lifecycleData = new EventData();
//		lifecycleData.putLong(AnalyticsTestConstants.EventDataKeys.Lifecycle.SESSION_START_TIMESTAMP, 0L);
//		lifecycleData.putLong(AnalyticsTestConstants.EventDataKeys.Lifecycle.MAX_SESSION_LENGTH, 150);
//
//		Map<String, EventData> sharedData = new HashMap<String, EventData>();
//		sharedData.put(LIFECYCLE_SHARED_STATE, lifecycleData);
//		state = new AnalyticsState(sharedData);
//
//		EventData request = new EventData().putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE,
//				"testingHere");
//		Map<String, String> result = module.processAnalyticsContextData(state, request);
//		assertEquals(0, result.size());
//	}
//
//	@Test
//	public void testProcessAnalyticsData_appendsUnknow_when_privacyStatusUnknown() throws Exception {
//		state.setPrivacyStatus(MobilePrivacyStatus.UNKNOWN);
//		EventData request = new EventData().putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE,
//				"testingHere");
//		Map<String, String> result = module.processAnalyticsContextData(state, request);
//		assertEquals(1, result.size());
//		assertEquals("unknown", result.get("a.privacy.mode"));
//	}
//
//	@Test
//	public void testProcessAnalyticsData_doesNotAppendPrivacy_when_privacyStatusOptIn() throws Exception {
//		state.setPrivacyStatus(MobilePrivacyStatus.OPT_IN);
//		EventData request = new EventData().putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE,
//				"testingHere");
//		Map<String, String> result = module.processAnalyticsContextData(state, request);
//		assertEquals(0, result.size());
//	}
//
//	// ------------- TEST PROCESS ANALYTICS VARS -----------
//	@Test
//	public void testProcessAnalyticsVars_When_ValidExternalTrackActionEvent() {
//		state.setApplicationID("appName 1.0 (1)");
//		state.setOfflineEnabled(true);
//		Map<String, String> contextData = new HashMap<String, String>();
//		contextData.put("key1", "value1");
//		contextData.put("key2", "value2");
//		EventData testRequest = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION, "clickOK")
//		.putStringMap(AnalyticsTestConstants.EventDataKeys.Analytics.CONTEXT_DATA, contextData);
//		Map<String, String> expectedAnalyticsData = new HashMap<String, String>();
//		expectedAnalyticsData.put("pe", "lnk_o");
//		expectedAnalyticsData.put("pageName", "appName 1.0 (1)");
//		expectedAnalyticsData.put("pev2", "AMACTION:clickOK");
//		expectedAnalyticsData.put("ce", "UTF-8");
//		expectedAnalyticsData.put("t", TIMESTAMP_TIMEZONE_OFFSET);
//		expectedAnalyticsData.put("cp", "foreground");
//		expectedAnalyticsData.put("ts", "123");
//		Map<String, String> resultedAnalyticsData = module.processAnalyticsVars(state, testRequest, 123);
//		assertEquals(expectedAnalyticsData, resultedAnalyticsData);
//	}
//
//	@Test
//	public void testProcessAnalyticsVars_When_ValidExternalTrackActionEventWithAIDAndVisitorID() {
//		properties.setAid("testAID");
//		properties.setVid("testVID");
//
//		Map<String, EventData> sharedData = new HashMap<String, EventData>();
//		sharedData.put(IDENTITY_SHARED_STATE, new EventData());
//		sharedData.put(LIFECYCLE_SHARED_STATE, new EventData());
//		state = new AnalyticsState(sharedData);
//		state.setApplicationID("appName 1.0 (1)");
//		Map<String, String> contextData = new HashMap<String, String>();
//		contextData.put("key1", "value1");
//		contextData.put("key2", "value2");
//		EventData analyticsData = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION, "clickOK")
//		.putStringMap(AnalyticsTestConstants.EventDataKeys.Analytics.CONTEXT_DATA, contextData);
//		Map<String, String> expectedAnalyticsData = new HashMap<String, String>();
//		expectedAnalyticsData.put("pe", "lnk_o");
//		expectedAnalyticsData.put("pageName", "appName 1.0 (1)");
//		expectedAnalyticsData.put("pev2", "AMACTION:clickOK");
//		expectedAnalyticsData.put("ce", "UTF-8");
//		expectedAnalyticsData.put("aid", "testAID");
//		expectedAnalyticsData.put("vid", "testVID");
//		expectedAnalyticsData.put("t", TIMESTAMP_TIMEZONE_OFFSET);
//		expectedAnalyticsData.put("cp", "foreground");
//		Map<String, String> resultedAnalyticsData = module.processAnalyticsVars(state, analyticsData,
//				System.currentTimeMillis());
//		assertEquals(expectedAnalyticsData, resultedAnalyticsData);
//	}
//
//	@Test
//	public void testProcessAnalyticsVars_When_ValidExternalTrackStateEvent() {
//		state.setApplicationID("appName 1.0 (1)");
//		EventData analyticsData = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE, "HomePage");
//		Map<String, String> expectedAnalyticsData = new HashMap<String, String>();
//		expectedAnalyticsData.put("pageName", "HomePage");
//		expectedAnalyticsData.put("ce", "UTF-8");
//		expectedAnalyticsData.put("t", TIMESTAMP_TIMEZONE_OFFSET);
//		expectedAnalyticsData.put("cp", "foreground");
//		Map<String, String> resultedAnalyticsData = module.processAnalyticsVars(state, analyticsData,
//				System.currentTimeMillis());
//		assertEquals(expectedAnalyticsData, resultedAnalyticsData);
//	}
//
//	@Test
//	public void testProcessAnalyticsVars_When_ValidExternalTrackStateAndActionEvent() {
//		state.setApplicationID("appName 1.0 (1)");
//
//		Map<String, String> contextData = new HashMap<String, String>();
//		contextData.put("key1", "value1");
//		contextData.put("key2", "value2");
//		EventData analyticsData = new EventData();
//		analyticsData.putStringMap(AnalyticsTestConstants.EventDataKeys.Analytics.CONTEXT_DATA, contextData);
//		analyticsData.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE, "HomePage");
//		analyticsData.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION, "clickOK");
//		Map<String, String> expectedAnalyticsData = new HashMap<String, String>();
//		expectedAnalyticsData.put("pageName", "HomePage");
//		expectedAnalyticsData.put("ce", "UTF-8");
//		expectedAnalyticsData.put("t", TIMESTAMP_TIMEZONE_OFFSET);
//		expectedAnalyticsData.put("cp", "foreground");
//		expectedAnalyticsData.put("pe", "lnk_o");
//		expectedAnalyticsData.put("pev2", "AMACTION:clickOK");
//
//		Map<String, String> resultedAnalyticsData = module.processAnalyticsVars(state, analyticsData,
//				System.currentTimeMillis());
//		assertEquals(expectedAnalyticsData, resultedAnalyticsData);
//	}
//
//	@Test
//	public void testProcessAnalyticsVars_When_UIServiceIsNull() {
//		EventData testRequest = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION, "clickOK")
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE, "HomePage");
//
//		platformServices.mockUIService = null;
//
//		Map<String, String> resultedAnalyticsData = module.processAnalyticsVars(state, testRequest, System.currentTimeMillis());
//
//		assertEquals("foreground", resultedAnalyticsData.get("cp"));
//	}
//
//	@Test
//	public void testProcessAnalyticsVars_When_AppStateForeground() {
//		EventData testRequest = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION, "clickOK")
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE, "HomePage");
//
//		MockUIService mockUIService = (MockUIService) platformServices.getUIService();
//		mockUIService.appState = UIService.AppState.FOREGROUND;
//		Map<String, String> resultedAnalyticsData = module.processAnalyticsVars(state, testRequest, System.currentTimeMillis());
//
//		assertEquals("foreground", resultedAnalyticsData.get("cp"));
//	}
//
//	@Test
//	public void testProcessAnalyticsVars_When_AppStateUnknown() {
//		EventData testRequest = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION, "clickOK")
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE, "HomePage");
//		MockUIService mockUIService = (MockUIService) platformServices.getUIService();
//		mockUIService.appState = UIService.AppState.UNKNOWN;
//
//		Map<String, String> analyticsData = module.processAnalyticsVars(state, testRequest, System.currentTimeMillis());
//
//		assertEquals("foreground", analyticsData.get("cp"));
//	}
//
//	@Test
//	public void testProcessAnalyticsVars_When_AppStateBackground() {
//		EventData testRequest = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION, "testAction")
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE, "testState");
//		MockUIService mockUIService = (MockUIService) platformServices.getUIService();
//		mockUIService.appState = UIService.AppState.BACKGROUND;
//
//		Map<String, String> analyticsData = module.processAnalyticsVars(state, testRequest, System.currentTimeMillis());
//
//		assertEquals("background", analyticsData.get("cp"));
//	}
//
//	@Test
//	public void testProcessAnalyticsVars_When_ValidExternalTrackActionWithAnalyticsIDVisitorParams() {
//		Map<String, EventData> sharedData = new HashMap<String, EventData>();
//		sharedData.put(IDENTITY_SHARED_STATE, new EventData() {
//			{
//				putString("blob", "testBlob");
//				putString("locationhint", "testLocationHint");
//				putString("mid", "testMID");
//			}
//		});
//		state = new AnalyticsState(sharedData);
//		state.setApplicationID("appName 1.0 (1)");
//		state.setMarketingCloudOrganizationID("orgID");
//		EventData testRequest = new EventData().putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION,
//				"clickOK");
//		Map<String, String> expectedAnalyticsData = new HashMap<String, String>();
//		expectedAnalyticsData.put("pageName", "appName 1.0 (1)");
//		expectedAnalyticsData.put("pe", "lnk_o");
//		expectedAnalyticsData.put("pev2", "AMACTION:clickOK");
//		expectedAnalyticsData.put("ce", "UTF-8");
//		expectedAnalyticsData.put("mid", "testMID");
//		expectedAnalyticsData.put("aamb", "testBlob");
//		expectedAnalyticsData.put("aamlh", "testLocationHint");
//		expectedAnalyticsData.put("t", TIMESTAMP_TIMEZONE_OFFSET);
//		expectedAnalyticsData.put("cp", "foreground");
//		Map<String, String> resultedAnalyticsData = module.processAnalyticsVars(state, testRequest, System.currentTimeMillis());
//		assertEquals(expectedAnalyticsData, resultedAnalyticsData);
//	}
//
//	@Test
//	public void testProcessAnalyticsVars_When_ActionNullStateValid() throws Exception {
//		EventData request = new EventData().putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE,
//				"testingHere");
//		Map<String, String> result = module.processAnalyticsVars(state, request, TimeUtil.getUnixTimeInSeconds());
//		assertEquals("testingHere", result.get("pageName"));
//	}
//}
