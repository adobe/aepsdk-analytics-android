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
//import static junit.framework.TestCase.assertFalse;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
///**
// * Created by dobrin on 7/25/17.
// */
//@Ignore
//public class AnalyticsTrackActionModuleTest extends SystemTest {
//	private static final int TEST_TIMEOUT = 1000;
//
//	private TestableNetworkService     testableNetworkService;
//	private AnalyticsCoreAPI           testableCore;
//	private AnalyticsModuleTestsHelper testHelper;
//
//	private Map<String, String> expectedAnalyticsData = new HashMap<String, String>();
//
//	{
//		expectedAnalyticsData.put("ce", "UTF-8");
//		expectedAnalyticsData.put("cp", "foreground");
//		expectedAnalyticsData.put("ndh", "1");
//	}
//
//	@Before
//	public void before() throws Exception {
//		eventHub.registerModule(AnalyticsExtension.class);
//		platformServices.getMockSystemInfoService().applicationCacheDir = new File(this.getClass().getResource("").getPath() +
//				"test");
//		platformServices.getMockSystemInfoService().networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//		testableNetworkService = platformServices.getTestableNetworkService();
//		testableCore = new AnalyticsCoreAPI(eventHub);
//		testHelper = new AnalyticsModuleTestsHelper(eventHub);
//		testHelper.setupForTracking();
//		eventHub.clearEvents();
//		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_IDENTITY);
//		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY);
//		testableNetworkService.ignoreNetworkRequestURL("https://test.com/id");
//	}
//
//	@After
//	public void afterEach() {
//		eventHub.clearEvents();
//		eventHub.clearIgnoredEventFilters();
//		testableNetworkService.reset();
//	}
//
//	@Test
//	public void testTrackAction_should_sendNetworkRequestTrack() throws Exception {
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
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "testActionName")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap(testContextData);
//		expectedContextData.put("test.key1", "val1");
//		expectedContextData.put("a.action", "testActionName");
//		expectedContextData.put("a.AppID", "testAppID");
//
//		expectedAnalyticsData.put("pev2", "AMACTION%3AtestActionName");
//		expectedAnalyticsData.put("pageName", "testAppID");
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
//	public void testTrackAction_withNullAction_should_sendNetworkRequestTrack() throws Exception {
//		// setup
//		testHelper.setLifecycleSharedState(new HashMap<String, String>() {
//			{
//				put("appid", "testAppID");
//			}
//		});
//		testableNetworkService.setExpectedCount(1);
//		// test
//		testableCore.trackAnalyticsRequest(new EventData());
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap<String, String>();
//		expectedContextData.put("a.AppID", "testAppID");
//		expectedAnalyticsData.put("pageName", "testAppID");
//
//		Map<String, String> additionalData = getAdditionalData(requestPayload);
//		Map<String, Object> resultContextData = getContextData(requestPayload);
//
//		assertEquals(String.format("\nExpected: %s \nActual: %s", expectedContextData.toString(), resultContextData.toString()),
//					 expectedContextData, resultContextData);
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, additionalData));
//		assertFalse(additionalData.containsKey("pev2"));
//		assertFalse(additionalData.containsKey("pe"));
//	}
//
//	@Test
//	public void testTrackAction_whenContextDataWith1000Pairs_should_sendNetworkRequestTrack() {
//		//setup
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//
//		for (int i = 0; i < 1000; i++) {
//			testContextData.put("context_data_key_" + i, "context_data_val_" + i);
//		}
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "test1000")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap<String, String>(testContextData);
//		expectedContextData.put("a.action", "test1000");
//		expectedAnalyticsData.put("pev2", "AMACTION%3Atest1000");
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
//	public void testTrackAction_withEmptyAction_should_sendNetworkRequestTrack() {
//		// setup
//		testableNetworkService.setExpectedCount(1);
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, ""));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> additionalData = getAdditionalData(requestPayload);
//
//		assertTrue(String.format("\nExpected empty, actual: %s", getContextData(requestPayload).toString()),
//				   getContextData(requestPayload).isEmpty());
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, additionalData));
//		assertFalse(additionalData.containsKey("pev2"));
//		assertFalse(additionalData.containsKey("pe"));
//	}
//
//	@Test
//	public void testTrackAction_doesNOTSendRequest_When_notUsingAnalytics() {
//		//setup
//		EventData configMissingAnalytics = new EventData();
//		configMissingAnalytics.putString("analytics.rsids", null);
//		configMissingAnalytics.putString("analytics.server", "server.com");
//		testHelper.setConfigurationSharedState(configMissingAnalytics);
//		eventHub.setExpectedEventCount(1);
//
//		//test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "notUsingAnalytics"));
//
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, eventHub.getEvents().size());
//		assertEquals(0, testableNetworkService.waitAndGetCount());
//	}
//
//	@Test
//	public void testTrackAction_whenCustomCDataContainSDKKeys_should_overwriteSDKVars() {
//		//setup
//		testHelper.setLifecycleSharedState(new HashMap<String, String>() {
//			{
//				put("devicename", "devName");
//				put("carriername", "mobile");
//				put("appid", "testAppID");
//				put("osversion", "testVersion 1.2");
//				put("resolution", "11x22");
//			}
//		});
//		testableNetworkService.setExpectedCount(1);
//
//		HashMap<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("a.DeviceName", "overwrittenDeviceName");
//		testContextData.put("a.CarrierName", "overwrittenCarrierName");
//		testContextData.put("a.AppID", "overwrittenAppID");
//		testContextData.put("a.OSVersion", "overwrittenOSVersion");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData)
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "testAction"));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap<String, String>();
//		expectedContextData.putAll(testContextData);
//		expectedContextData.put("a.Resolution", "11x22");
//		expectedContextData.put("a.action", "testAction");
//
//		expectedAnalyticsData.put("pev2", "AMACTION%3AtestAction");
//		expectedAnalyticsData.put("pe", "lnk_o");
//
//		assertEquals("Hit context data mismatch",
//					 expectedContextData, getContextData(requestPayload));
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//	}
//
//	@Test
//	public void testTrackAction_whenActionNameContainsSpecialCharacters_should_EscapeCharsExceptDashDotUnderScore() {
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "~!@#$%^&*()_.-+"));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap();
//		expectedContextData.put("a.action", "~!@#$%^&*()_.-+");
//		expectedAnalyticsData.put("pev2", "AMACTION%3A~%21%40%23%24%25%5E%26%2A%28%29_.-%2B");
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
//	public void testTrackAction_when_LocalStorageNotInitialized_should_NotCrash() throws Exception {
//		waitForThreadsWithFailIfTimedOut(1000);
//		// Shutdown old event hub
//		eventHub.shutdown();
//
//		// Recreate Setup
//		platformServices = new CucumberTestPlatformServices();
//		platformServices.setFakeLocalStorageService(null);
//		eventHub = new MockEventHubCucumberTest("eventhub", platformServices);
//		FakeLoggingService loggingService = (FakeLoggingService) platformServices.getLoggingService();
//		Log.setLoggingService(platformServices.getLoggingService());
//		Log.setLogLevel(LoggingMode.DEBUG);
//		eventHub.registerModule(AnalyticsExtension.class);
//		platformServices.getMockSystemInfoService().applicationCacheDir = new File(this.getClass().getResource("").getPath() +
//				"test");
//		platformServices.getMockSystemInfoService().networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//		testableNetworkService = platformServices.getTestableNetworkService();
//		testableCore = new AnalyticsCoreAPI(eventHub);
//		testHelper = new AnalyticsModuleTestsHelper(eventHub);
//		testHelper.setupForTracking();
//		eventHub.clearEvents();
//		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_IDENTITY);
//		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY);
//		testableNetworkService.ignoreNetworkRequestURL("https://test.com/id");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "testActionName"));
//
//
//		// verify the network call is still made with correct data
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap();
//		expectedContextData.put("a.action", "testActionName");
//		expectedAnalyticsData.put("pev2", "AMACTION%3AtestActionName");
//		assertEquals(String.format("\nExpected: %s \nActual:   %s", expectedContextData.toString(),
//								   getContextData(requestPayload).toString()),
//					 expectedContextData, getContextData(requestPayload));
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//
//		// Error logs are created
//		assertTrue(loggingService.containsWarningLog("Analytics",
//				   "LocalStorage Service not initialized. Unable to set most recent hit timestamp in persistence"));
//		assertTrue(loggingService.containsWarningLog("Analytics",
//				   "LocalStorage Service not initialized. Unable to get most recent hit timestamp from persistence"));
//	}
//
//
//}
