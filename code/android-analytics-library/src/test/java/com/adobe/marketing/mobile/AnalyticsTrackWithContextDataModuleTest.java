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
//import org.junit.Test;
//
//import java.io.File;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
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
//public class AnalyticsTrackWithContextDataModuleTest extends SystemTest {
//	private static final int TEST_TIMEOUT = 1000;
//	private static final String CONFIG_SHARED_STATE = "com.adobe.marketing.mobile.configuration";
//	private static final String IDENTITY_SHARED_STATE = "com.adobe.marketing.mobile.identity";
//	private static final int TEST_REFERRER_TIMEOUT = 5;
//
//	private TestableNetworkService     testableNetworkService;
//	private AnalyticsCoreAPI           testableCore;
//	private AnalyticsModuleTestsHelper testHelper;
//	private CountDownLatch registerLatch;
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
//	public void beforeEach() {
//		try {
//			try {
//				registerLatch = new CountDownLatch(1);
//				eventHub.registerModuleWithCallback(AnalyticsExtension.class, new EventHub.RegisterModuleCallback() {
//					@Override
//					public void registered(Module module) {
//						registerLatch.countDown();
//					}
//				});
//				registerLatch.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
//				eventHub.finishModulesRegistration(new AdobeCallback() {
//					@Override
//					public void call(Object value) {
//
//					}
//				});
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//			platformServices.getMockSystemInfoService().applicationCacheDir =
//				new File(this.getClass().getResource("").getPath() + "test");
//			platformServices.getMockSystemInfoService().networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//			testableNetworkService = platformServices.getTestableNetworkService();
//			testableNetworkService.ignoreNetworkRequestURL("https://test.com/id");
//			testableCore = new AnalyticsCoreAPI(eventHub);
//			testHelper = new AnalyticsModuleTestsHelper(eventHub);
//			testHelper.setupForTracking();
//			eventHub.clearEvents();
//			eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_IDENTITY);
//			eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY);
//
//			setupForTracking();
//		} catch (Exception e) {
//		}
//	}
//
//	@After
//	public void afterEach() {
//		eventHub.clearEvents();
//		eventHub.clearIgnoredEventFilters();
//	}
//
//	public void setConfigurationSharedState(final MobilePrivacyStatus status, final int batchLimit) {
//		EventData analyticsConfig = new EventData();
//		analyticsConfig.putString("analytics.server", "test.com");
//		analyticsConfig.putString("analytics.rsids", "123,132123");
//		analyticsConfig.putString("global.privacy", status.getValue());
//		analyticsConfig.putInteger("analytics.batchLimit", batchLimit);
//		analyticsConfig.putBoolean("analytics.offlineEnabled", true);
//		analyticsConfig.putBoolean("analytics.backdatePreviousSessionInfo", true);
//		analyticsConfig.putInteger("analytics.launchHitDelay", TEST_REFERRER_TIMEOUT);
//		eventHub.createSharedState(CONFIG_SHARED_STATE, eventHub.getAllEventsCount(), analyticsConfig);
//	}
//
//	void setIdentitySharedState() {
//		eventHub.createSharedState(IDENTITY_SHARED_STATE,
//								   eventHub.getAllEventsCount(),
//								   new EventData().putString("adid", "adid"));
//	}
//
//	private void setupForTracking() {
//		eventHub.ignoreAllStateChangeEvents();
//		setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 0);
//		setIdentitySharedState();
//	}
//
//	@Test
//	public void testTrackState_when_contextDataKeyContainsDot_should_sendNetworkRequestTrack() {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("key.key1.key2", "value");
//		testContextData.put("&&ADB_TestName", "testVal");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "testTrackState_ContextData_Key_ContainsDot")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap<String, String>();
//		expectedContextData.put("key.key1.key2", "value");
//
//		expectedAnalyticsData.put("pageName", "testTrackState_ContextData_Key_ContainsDot");
//		expectedAnalyticsData.put("ADB_TestName", "testVal");
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
//	public void testTrackState_when_contextDataKeyNull_should_sendNetworkRequestTrack() {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put(null, "value");
//		testContextData.put("&&ADB_TestName", "testVal");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "testTrackState_ContextData_Key_IsNull")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//
//		expectedAnalyticsData.put("pageName", "testTrackState_ContextData_Key_IsNull");
//		expectedAnalyticsData.put("ADB_TestName", "testVal");
//
//		assertTrue(String.format("\nExpected context data empty \nActual:   %s", getContextData(requestPayload).toString()),
//				   getContextData(requestPayload).isEmpty());
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//	}
//
//	@Test
//	public void testTrackState_when_contextDataValueNull_should_sendNetworkRequestTrack() {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("key", null);
//		testContextData.put("&&ADB_TestName", "testVal");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "testTrackState_ContextData_Value_IsNull")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//
//		expectedAnalyticsData.put("pageName", "testTrackState_ContextData_Value_IsNull");
//		expectedAnalyticsData.put("ADB_TestName", "testVal");
//
//		assertTrue(String.format("\nExpected context data empty \nActual:   %s", getContextData(requestPayload).toString()),
//				   getContextData(requestPayload).isEmpty());
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//	}
//
//	@Test
//	public void testTrackState_when_contextDataKeyContainsDotNested_should_sendNetworkRequestTrack() {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("key.key1.key2", "value");
//		testContextData.put("key.key1", "value");
//		testContextData.put("key", "value");
//		testContextData.put("&&ADB_TestName", "testVal");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "testTrackState_ContextData_Key_ContainsDot_Nested")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap<String, String>();
//		expectedContextData.put("key.key1.key2", "value");
//		expectedContextData.put("key.key1", "value");
//		expectedContextData.put("key", "value");
//
//		expectedAnalyticsData.put("pageName", "testTrackState_ContextData_Key_ContainsDot_Nested");
//		expectedAnalyticsData.put("ADB_TestName", "testVal");
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
//	public void testTrackState_when_contextDataKeyEscapes_should_sendNetworkRequestTrack() {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("~!@#$%^&*()_+", "value");
//		testContextData.put("&&ADB_TestName", "testVal");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "testTrackState_ContextData_Key_Escapes")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap<String, String>();
//		expectedContextData.put("_", "value");
//
//		expectedAnalyticsData.put("pageName", "testTrackState_ContextData_Key_Escapes");
//		expectedAnalyticsData.put("ADB_TestName", "testVal");
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
//	public void testTrackState_when_contextDataKeyValueEscapes_should_sendNetworkRequestTrack() {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("~!@#$%^&*()_+", "~!@#$%^&*()_+");
//		testContextData.put("&&ADB_TestName", "testVal");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "testTrackState_ContextData_KeyValue_Escapes")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap<String, String>();
//		expectedContextData.put("_", "~!@#$%^&*()_+");
//
//		expectedAnalyticsData.put("pageName", "testTrackState_ContextData_KeyValue_Escapes");
//		expectedAnalyticsData.put("ADB_TestName", "testVal");
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
//	public void testTrackState_when_contextDataContainsUTF8_should_sendNetworkRequestTrack() {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("a网页b", "a网页b");
//		testContextData.put("&&ADB_TestName", "testVal");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "testTrackState_ContextData_ContainsUTF8")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//		Map<String, String> expectedContextData = new HashMap<String, String>();
//		expectedContextData.put("ab", "a网页b");
//
//		expectedAnalyticsData.put("pageName", "testTrackState_ContextData_ContainsUTF8");
//		expectedAnalyticsData.put("ADB_TestName", "testVal");
//
//		assertEquals(expectedContextData, getContextData(requestPayload));
//		assertTrue(containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//	}
//
//	@Test
//	public void testTrackState_when_contextDataKeyIsEmpty_should_sendNetworkRequestTrack() {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("", "value");
//		testContextData.put("&&ADB_TestName", "testVal");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "testTrackState_ContextData_Key_IsEmpty")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//
//		expectedAnalyticsData.put("pageName", "testTrackState_ContextData_Key_IsEmpty");
//		expectedAnalyticsData.put("ADB_TestName", "testVal");
//
//		assertTrue(String.format("\nExpected context data is empty \nActual:   %s", getContextData(requestPayload).toString()),
//				   getContextData(requestPayload).isEmpty());
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//	}
//
//	@Test
//	public void testTrackState_when_contextDataValueIsEmpty_should_sendNetworkRequestTrack() {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("key", "");
//		testContextData.put("&&ADB_TestName", "testVal");
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, "testTrackState_ContextData_Key_IsEmpty")
//										   .putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, testContextData));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		String requestPayload = new String(testableNetworkService.getItem(0).connectPayload);
//
//		expectedAnalyticsData.put("pageName", "testTrackState_ContextData_Key_IsEmpty");
//		expectedAnalyticsData.put("ADB_TestName", "testVal");
//
//		assertTrue(String.format("\nExpected context data is empty \nActual:   %s", getContextData(requestPayload).toString()),
//				   getContextData(requestPayload).isEmpty());
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//	}
//
//	@Test
//	public void testTrackState_when_contextDataHack_should_properlyEncodeValuesForVars() throws Exception {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("&&tnt", "tnt value");
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
//
//		expectedAnalyticsData.put("pageName", "testState");
//		expectedAnalyticsData.put("tnt", "tnt%20value");
//
//		assertTrue(String.format("\nExpected context data is empty \nActual:   %s", getContextData(requestPayload).toString()),
//				   getContextData(requestPayload).isEmpty());
//		assertTrue(String.format("\nExpected: %s \nActual:   %s", expectedAnalyticsData.toString(),
//								 getAdditionalData(requestPayload)),
//				   containsAll(expectedAnalyticsData, getAdditionalData(requestPayload)));
//	}
//
//	@Test
//	public void testTrackState_with_ContextDataHack_should_putStrippedNonConformingVarsIntoContextData() throws Exception {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("&tnt", "tnt value");
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
//		Map<String, String> expectedContextData = new HashMap<String, String>();
//		expectedContextData.put("tnt", "tnt value");
//
//		expectedAnalyticsData.put("pageName", "testState");
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
//	public void testTrackState_with_ContextDataHack_should_NotEncodeBlankKeys() throws Exception {
//		testableNetworkService.setExpectedCount(1);
//		Map<String, String> testContextData = new HashMap<String, String>();
//		testContextData.put("&&", "tnt value");
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
//
//		assertFalse(String.format("Request payload should not contain \"&=tntvalue\", but it was %s", requestPayload),
//					requestPayload.contains("&=tntvalue"));
//	}
//}
