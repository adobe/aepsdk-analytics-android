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
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//
//import java.io.File;
//import java.util.HashMap;
//import java.util.Map;
//
//import static com.adobe.marketing.mobile.AnalyticsContextDataHelper.containsAll;
//import static com.adobe.marketing.mobile.AnalyticsContextDataHelper.getAdditionalData;
//import static com.adobe.marketing.mobile.AnalyticsContextDataHelper.getContextData;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
///**
// * Created by dobrin on 7/26/17.
// */
//
//@Ignore
//public class AnalyticsTrackStateModuleTest extends SystemTest {
//	private static final int TEST_TIMEOUT = 10000;
//
//	private TestableNetworkService     testableNetworkService;
//	private AnalyticsCoreAPI           testableCore;
//	private AnalyticsModuleTestsHelper testHelper;
//
//	private Map<String, String> expectedAnalyticsData = new HashMap<String, String>();
//	{
//		expectedAnalyticsData.put("ce", "UTF-8");
//		expectedAnalyticsData.put("cp", "foreground");
//		expectedAnalyticsData.put("ndh", "1");
//	}
//
//	@Before
//	public void beforeEach() {
//		try {
//			eventHub.registerModule(AnalyticsExtension.class);
//			platformServices.getMockSystemInfoService().applicationCacheDir =
//				new File(this.getClass().getResource("").getPath() + "test");
//			platformServices.getMockSystemInfoService().networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//			testableNetworkService = platformServices.getTestableNetworkService();
//			testableCore = new AnalyticsCoreAPI(eventHub);
//			testHelper = new AnalyticsModuleTestsHelper(eventHub);
//			testHelper.setupForTracking();
//			eventHub.clearEvents();
//			eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_IDENTITY);
//			eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY);
//			testableNetworkService.ignoreNetworkRequestURL("https://test.com/id");
//		} catch (Exception e) {
//
//		}
//	}
//
//	@After
//	public void afterEach() {
//		eventHub.clearEvents();
//		eventHub.clearIgnoredEventFilters();
//	}
//
//	@Test
//	public void testTrackState_when_nullStateName_withContextData_should_sendNetworkRequestTrack() {
//		// setup
//		testHelper.setLifecycleSharedState(new HashMap<String, String>() {
//			{
//				put("appid", "testAppID");
//			}
//		});
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("test.key1", "val1");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap<String, String>();
//		expectedContextData.putAll(testContextData);
//		expectedContextData.put("a.AppID", "testAppID");
//		expectedAnalyticsData.put("pageName", "testAppID");
//
//		Map<String, String> resultAdditionalData = getAdditionalData(requestPayload);
//		Map<String, Object> resultContextData = getContextData(requestPayload);
//
//		assertEquals(String.format("\nExpected: %s \nActual: %s", expectedContextData.toString(),
//								   resultContextData.toString()),
//					 expectedContextData, resultContextData);
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(), resultAdditionalData),
//				   containsAll(expectedAnalyticsData, resultAdditionalData));
//		assertFalse(resultAdditionalData.containsKey("pe"));
//		assertFalse(resultAdditionalData.containsKey("pev2"));
//	}
//
//	@Test
//	public void testTrackState_should_NOTPopulateLinkVars() {
//		// setup
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("test.key1", "val1");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "testState")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		expectedAnalyticsData.put("pageName", "testState");
//
//		assertEquals(String.format("\nExpected: %s \nActual:   %s", testContextData.toString(),
//								   getContextData(requestPayload).toString()),
//					 testContextData, getContextData(requestPayload));
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//		assertFalse("pe key should not be present for track state", getAdditionalData(requestPayload).containsKey("pe"));
//		assertFalse("pev2 key should not be present for track state", getAdditionalData(requestPayload).containsKey("pev2"));
//	}
//
//	@Test
//	public void testTrackState_when_validStateName_noContextData_should_sendNetworkRequestTrack() {
//		testableNetworkService.setExpectedCount(1);
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "testTrackState"));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//
//		expectedAnalyticsData.put("pageName", "testTrackState");
//
//		assertTrue(String.format("\nExpected empty context data \nActual:   %s", getContextData(requestPayload).toString()),
//				   getContextData(requestPayload).isEmpty());
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//	}
//
//	@Test
//	public void testTrackState_when_validStateName_withContextData_should_sendNetworkRequestTrack() {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("test.key1", "val1");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "testTrackState")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//
//		expectedAnalyticsData.put("pageName", "testTrackState");
//
//		assertEquals(String.format("\nExpected: %s \nActual:   %s", testContextData.toString(),
//								   getContextData(requestPayload).toString()),
//					 testContextData, getContextData(requestPayload));
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//	}
//
//	@Test
//	public void testTrackState_when_validStateName_WithContextData1000Keys_should_sendNetworkRequestTrack() {
//		//setup
//		testableNetworkService.setExpectedCount(1);
//		HashMap<String, String> testContextData = new HashMap<String, String>();
//
//		for (int entryNum = 0; entryNum < 1000; entryNum++) {
//			testContextData.put("context_data_key" + entryNum, "context_data_val_" + entryNum);
//		}
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "trackStateWithContextDataMapOfSize1000")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//
//		expectedAnalyticsData.put("pageName", "trackStateWithContextDataMapOfSize1000");
//
//		assertEquals(String.format("\nExpected: %s \nActual:   %s", testContextData.toString(),
//								   getContextData(requestPayload).toString()),
//					 testContextData, getContextData(requestPayload));
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//	}
//
//	@Test
//	public void testTrackState_when_emptyState_should_sendNetworkRequestTrack() {
//		// setup
//		testableNetworkService.setExpectedCount(1);
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, ""));
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> resultAdditionalData = getAdditionalData(requestPayload);
//
//		assertTrue(String.format("\nExpected empty, actual: %s", getContextData(requestPayload).toString()),
//				   getContextData(requestPayload).isEmpty());
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(), resultAdditionalData),
//				   containsAll(expectedAnalyticsData, resultAdditionalData));
//		assertFalse(resultAdditionalData.containsKey("pe"));
//		assertFalse(resultAdditionalData.containsKey("pev2"));
//	}
//
//	@Test
//	public void testTrackState_when_notUsingAnalytics_should_NOTSendRequest() {
//		// setup
//		EventData configMissingAnalytics = new EventData();
//		configMissingAnalytics.putString("analytics.rsids", null);
//		configMissingAnalytics.putString("analytics.server", "server.com");
//		testHelper.setConfigurationSharedState(configMissingAnalytics);
//		eventHub.setExpectedEventCount(1);
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "trackNotUsingAnalytics"));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(0, testableNetworkService.waitAndGetCount());
//	}
//
//	@Test
//	public void testTrackState_when_stateWithSpecialChars_should_sendNetworkRequestTrack() {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("key", "value");
//		testContextData.put("&&ADB_TestName", "testTrackState_State_Escapes");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "~!@#$%^&*()_+")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap<String, String>();
//		expectedContextData.put("key", "value");
//
//		expectedAnalyticsData.put("pageName", "~%21%40%23%24%25%5E%26%2A%28%29_%2B");
//		expectedAnalyticsData.put("ADB_TestName", "testTrackState_State_Escapes");
//
//		assertEquals(String.format("\nExpected: %s \nActual:   %s", expectedContextData.toString(),
//								   getContextData(requestPayload).toString()),
//					 expectedContextData, getContextData(requestPayload));
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//	}
//
//	@Test
//	public void testTrackState_when_stateUTF8_should_sendNetworkRequestTrack() {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("key", "value");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "网页")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap<String, String>();
//		expectedContextData.put("key", "value");
//
//		expectedAnalyticsData.put("pageName", "%E7%BD%91%E9%A1%B5");
//
//		assertEquals(String.format("\nExpected: %s \nActual:   %s", expectedContextData.toString(),
//								   getContextData(requestPayload).toString()),
//					 expectedContextData, getContextData(requestPayload));
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//	}
//}
