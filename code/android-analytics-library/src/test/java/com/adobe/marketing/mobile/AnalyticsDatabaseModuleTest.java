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
//import java.util.UUID;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
///**
// * Created by dobrin on 7/24/17.
// */
//@Ignore
//public class AnalyticsDatabaseModuleTest extends SystemTest {
//	private static final int TEST_TIMEOUT = 5000;
//	private static int EVENTHUB_WAIT_MS = 1000;
//
//	private CountDownLatch registerLatch;
//	private AnalyticsCoreAPI testableCore;
//	private static final String[]                                  analyticsTableColumnNames = new String[] {"ID", "URL", "TIMESTAMP", "SERVER", "INSTALL", "OFFLINETRACKING"};
//	private static final DatabaseService.Database.ColumnDataType[] analyticsTableColumnTypes = new
//	DatabaseService.Database.ColumnDataType[] {
//		DatabaseService.Database.ColumnDataType.INTEGER,
//		DatabaseService.Database.ColumnDataType.TEXT,
//		DatabaseService.Database.ColumnDataType.INTEGER,
//		DatabaseService.Database.ColumnDataType.TEXT,
//		DatabaseService.Database.ColumnDataType.INTEGER,
//		DatabaseService.Database.ColumnDataType.INTEGER,
//		DatabaseService.Database.ColumnDataType.INTEGER
//	};
//	private static final String                                    analyticsDatabaseFile     = "ADBMobileDataCache.sqlite";
//	private static final String                                    analyticsTableName        = "HITS";
//	private TestableDatabase           testableDatabase;
//	private TestableNetworkService     testableNetworkService;
//	private AnalyticsModuleTestsHelper testHelper;
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
//				registerLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
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
//			platformServices.getMockSystemInfoService().applicationCacheDir = new File(this.getClass().getResource("").getPath() +
//					"test");
//			platformServices.getMockSystemInfoService().networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//			testableNetworkService = platformServices.getTestableNetworkService();
//			testableDatabase = new TestableDatabase(eventHub, analyticsDatabaseFile, analyticsTableName, analyticsTableColumnNames,
//													analyticsTableColumnTypes);
//			testableCore = new AnalyticsCoreAPI(eventHub);
//			testHelper = new AnalyticsModuleTestsHelper(eventHub);
//
//			testableNetworkService.ignoreNetworkRequestURL("https://test.com/id");
//			eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_IDENTITY);
//			eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY);
//			eventHub.clearEvents();
//		} catch (Exception e) {
//		}
//	}
//
//	@After
//	public void afterEach() {
//		eventHub.clearEvents();
//		eventHub.clearIgnoredEventFilters();
//		testableDatabase.deleteAll();
//		testableDatabase.closeDatabase();
//		testableNetworkService.reset();
//	}
//
//	@Test
//	public void testClearQueue_shouldRemoveAllHits() throws Exception {
//		eventHub.setExpectedEventCount(1);
//		insertNHits(10);
//		assertEquals(10, testableDatabase.count());
//		testableCore.clearQueue();
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, eventHub.getEvents().size());
//		assertEquals(0, testableDatabase.count());
//	}
//
//	@Test
//	public void testClearQueue_doesNotCrash_when_emptyDatabase() throws Exception {
//		eventHub.setExpectedEventCount(1);
//		assertEquals(0, testableDatabase.count());
//		testableCore.clearQueue();
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, eventHub.getEvents().size());
//		assertEquals(0, testableDatabase.count());
//	}
//
//	@Test
//	public void testClearQueue_doesNotCrash_when_closedDatabase() throws Exception {
//		testableDatabase.closeDatabase();
//		testableCore.clearQueue();
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, eventHub.getEvents().size());
//	}
//
//	@Test
//	public void testAnalyticsTrackRequest_When_PrivacyOptIn_Then_ShouldNotQueueAnyHits() {
//		// setup
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(1);
//		testableNetworkService.setExpectedCount(1);
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 0, false);
//		testHelper.setIdentitySharedState();
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData().putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE,
//										   "privacyOptIn_ShouldNotQueueHits"));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, eventHub.getEvents().size());
//		assertEquals(0, testableDatabase.count());
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		assertTrue(new String(testableNetworkService.getItem(
//								  0).connectPayload).contains("pageName=privacyOptIn_ShouldNotQueueHits"));
//	}
//
//	@Test
//	public void testAnalyticsTrackRequest_When_PrivacyOptOut_Then_ShouldNotSendAnyHits() {
//		// setup
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(1);
//		testableNetworkService.setExpectedCount(1);
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_OUT, 0, false);
//		testHelper.setIdentitySharedState();
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData().putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
//										   "privacyOptOut_ShouldNotSendAHit"));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, eventHub.getEvents().size());
//		assertEquals(0, testableNetworkService.waitAndGetCount());
//		assertEquals(0, testableDatabase.count());
//	}
//
//	@Test
//	public void testAnalyticsTrackRequest_When_PrivacyOptUnknown_Then_ShouldQueueAllHits() {
//		// setup
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(2);
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.UNKNOWN, 0, false);
//		testHelper.setIdentitySharedState();
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData().putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
//										   "privacyOptUnknown_ShouldQueueHit"));
//		testableCore.trackAnalyticsRequest(new EventData().putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE,
//										   "privacyOptUnknown_ShouldQueueHit"));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(2, eventHub.getEvents().size());
//		assertEquals(0, testableNetworkService.waitAndGetCount());
//		assertEquals(2, testableDatabase.count());
//	}
//
//	@Ignore
//	public void testTrackingSendQueuedHits_When_PrivacyOptIn_Then_ShouldSendAllQueuedHits() throws Exception {
//		// todo: uncomment this - currently, forceKick event is queued and the getQueue size is not queued, in this case,
//		// the second event might be executed before the first event
//		//setup
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(4);
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 4, false);
//		testHelper.setIdentitySharedState();
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE,
//												   "trackingSendQueuedHits_PrivacyOptIn_ShouldSendAllQueuedHits"));
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
//												   "trackingSendQueuedHits_PrivacyOptIn_ShouldSendAllQueuedHits"));
//		testableCore.trackAnalyticsRequest(new EventData().putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
//										   "action"));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(4, eventHub.getEvents().size());
//		assertEquals(0, testableNetworkService.waitAndGetCount());
//		assertEquals(3, testableDatabase.count());
//		eventHub.clearEvents();
//		eventHub.setExpectedEventCount(3);
//
//		//test
//		testableCore.sendQueuedHits();
//		final Long[] size = new Long[1];
//		AdobeCallback<Long> adobeCallback = new AdobeCallback<Long>() {
//			@Override
//			public void call(Long value) {
//				size[0] = value;
//			}
//		};
//
//		testableCore.getQueueSize(adobeCallback);
//
//		//verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(3, eventHub.getEvents().size());
//		assertEquals(3, testableNetworkService.waitAndGetCount());
//		assertEquals(0, testableDatabase.count());
//		assertEquals(new Long(0), size[0]);
//	}
//
//	@Test
//	public void testTrackingSendQueuedHits_When_PrivacyOptOut_Then_ShouldNotSendAnyHits() {
//		// setup
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(4);
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_OUT, 4, false);
//		testHelper.setIdentitySharedState();
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
//												   "trackingSendQueuedHits_PrivacyOptIn_ShouldSendAllQueuedHits"));
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
//												   "trackingSendQueuedHits_PrivacyOptIn_ShouldSendAllQueuedHits"));
//		testableCore.trackAnalyticsRequest(new EventData()
//										   .putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "action"));
//
//		// test
//		testableCore.sendQueuedHits();
//
//		//verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(4, eventHub.getEvents().size());
//		assertEquals(0, testableNetworkService.waitAndGetCount());
//		assertEquals(0, testableDatabase.count());
//	}
//
//	@Test
//	public void testTrackUpdatesHits_When_OutOfOrder_And_OfflineTrackingEnabled() throws Exception {
//		// setup
//		long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
//		long firstHitTime = currentTime;
//		long secondHitTime = currentTime - 20l;
//
//		eventHub.setExpectedEventCount(1);
//		// old hit, but still in 60sec threshold
//		Log.debug("Test", "First hit timestamp %s", firstHitTime);
//		Log.debug("Test", "Second hit timestamp %s", secondHitTime);
//		final Map<String, Object> firstHit = new HashMap<String, Object>();
//		firstHit.put("URL", String.format("&ts=%s&aid=test1", Long.toString(firstHitTime)));
//		firstHit.put("TIMESTAMP", firstHitTime);
//		firstHit.put("SERVER", "abc.com");
//		firstHit.put("OFFLINETRACKING", 1);
//		// ancient hit, should be dropped
//		final Map<String, Object> secondHit = new HashMap<String, Object>();
//		secondHit.put("URL", String.format("&ts=%s&aid=test2", Long.toString(secondHitTime)));
//		secondHit.put("TIMESTAMP", secondHitTime);
//		secondHit.put("SERVER", "def.com");
//		secondHit.put("OFFLINETRACKING", 1);
//		testableDatabase.insert(firstHit);
//		testableDatabase.insert(secondHit);
//		assertEquals(2, testableDatabase.count());
//		eventHub.ignoreAllStateChangeEvents();
//		testableNetworkService.setExpectedCount(3);
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 0, false);
//		testHelper.setIdentitySharedState();
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData().putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
//										   "testHits"));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, eventHub.getEvents().size());
//		assertEquals(0, testableDatabase.count());
//		assertEquals(3, testableNetworkService.waitAndGetCount());
//		assertTrue(new String(testableNetworkService.getItem(0).connectPayload).contains("test1"));
//		assertTrue(new String(testableNetworkService.getItem(0).connectPayload).contains(String.format("&ts=%s",
//				   firstHitTime)));
//		assertTrue(new String(testableNetworkService.getItem(1).connectPayload).contains("test2"));
//		assertTrue(new String(testableNetworkService.getItem(1).connectPayload).contains(String.format("&ts=%s",
//				   firstHitTime + 1)));
//		assertTrue(new String(testableNetworkService.getItem(2).connectPayload).contains("action=testHits"));
//	}
//
//	@Test
//	public void testTrackRemovesHits_When_OlderThan60Sec_And_OfflineTrackingDisabled() throws Exception {
//		// setup
//		EventData analyticsConfig = new EventData();
//		analyticsConfig.putString("analytics.server", "test.com");
//		analyticsConfig.putString("analytics.rsids", "123,132123");
//		analyticsConfig.putInteger("analytics.batchLimit", 0);
//		analyticsConfig.putBoolean("analytics.offlineEnabled", false);
//		analyticsConfig.putInteger("analytics.launchHitDelay", 5);
//
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(1);
//		testableNetworkService.setExpectedCount(2);
//		testHelper.setIdentitySharedState();
//		testHelper.setConfigurationSharedState(analyticsConfig);
//
//		long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
//		long firstHitTime = currentTime;
//		long secondHitTime = currentTime - 70;
//
//
//		// old hit, but still in 60sec threshold
//		Log.debug("Test", "First hit timestamp %s", firstHitTime);
//		Log.debug("Test", "Second hit timestamp %s", secondHitTime);
//		final Map<String, Object> firstHit = new HashMap<String, Object>();
//		firstHit.put("URL", String.format("&ts=%s&aid=test1", Long.toString(firstHitTime)));
//		firstHit.put("TIMESTAMP", firstHitTime);
//		firstHit.put("SERVER", "abc.com");
//		firstHit.put("OFFLINETRACKING", 0);
//		// ancient hit, should be dropped
//		final Map<String, Object> secondHit = new HashMap<String, Object>();
//		secondHit.put("URL", String.format("&ts=%s&aid=test2", Long.toString(secondHitTime)));
//		secondHit.put("TIMESTAMP", secondHitTime);
//		secondHit.put("SERVER", "def.com");
//		firstHit.put("OFFLINETRACKING", 0);
//		testableDatabase.insert(firstHit);
//		testableDatabase.insert(secondHit);
//		assertEquals(2, testableDatabase.count());
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData().putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
//										   "testHits"));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(1, eventHub.getEvents().size());
//		assertEquals(0, testableDatabase.count());
//		assertEquals(2, testableNetworkService.waitAndGetCount());
//		assertTrue(new String(testableNetworkService.getItem(0).connectPayload).contains("test1"));
//		assertTrue(new String(testableNetworkService.getItem(0).connectPayload).contains(String.format("&ts=%s",
//				   firstHitTime)));
//		assertTrue(new String(testableNetworkService.getItem(1).connectPayload).contains("action=testHits"));
//	}
//
//	@Test
//	public void testForceKick_sendsAllQueuedHits_and_BatchLimitIgnored() throws Exception {
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(1);
//		testableNetworkService.setExpectedCount(100);
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 0, false);
//		testHelper.setIdentitySharedState();
//		insertNHits(100);
//		testableCore.sendQueuedHits();
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals("Not all the analytics hits were sent", 100, testableNetworkService.waitAndGetCount());
//	}
//
//	@Test
//	public void testRequestSent_whenOfflineDisabled_and_batchLimitIgnored() throws Exception {
//		// setup
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(1);
//		testableNetworkService.setExpectedCount(1);
//		EventData analyticsConfig = new EventData();
//		analyticsConfig.putString("analytics.server", "test.com");
//		analyticsConfig.putString("analytics.rsids", "123,132123");
//		analyticsConfig.putInteger("analytics.batchLimit", 100);
//		analyticsConfig.putBoolean("analytics.offlineEnabled", false);
//		testHelper.setConfigurationSharedState(analyticsConfig);
//		testHelper.setIdentitySharedState();
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData().putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
//										   "testHits"));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals("Not all the analytics hits were sent", 1, testableNetworkService.waitAndGetCount());
//	}
//
//	@Test
//	public void testRequestsNOTSent_when_NotOverBatchLimit() throws Exception {
//		// setup
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(1);
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 15, false);
//		testHelper.setIdentitySharedState();
//		insertNHits(10);
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData().putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
//										   "testHits"));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals("The hits should not be sent when batch limit not exceeded",
//					 0, testableNetworkService.waitAndGetCount());
//		assertEquals(11, testableDatabase.count());
//	}
//
//	@Test
//	public void testRequestsSent_when_overBatchLimit() throws Exception {
//		// setup
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(1);
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 15, false);
//		testHelper.setIdentitySharedState();
//		testableNetworkService.setExpectedCount(15);
//		insertNHits(13);
//
//		// test
//		testableCore.trackAnalyticsRequest(new EventData().putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
//										   "testHits"));
//		testableCore.trackAnalyticsRequest(new EventData().putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
//										   "testHits"));
//		testableCore.trackAnalyticsRequest(new EventData().putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
//										   "testHits"));
//
//		// verify
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals("Not all the analytics hits were sent", 16, testableNetworkService.waitAndGetCount());
//		assertEquals(0, testableDatabase.count());
//	}
//
//	// --------------------- HELPER METHODS ------------------------
//	private void insertNHits(final int hitsNumber) {
//		Map<String, Object> dummyHit = new HashMap<String, Object>();
//		dummyHit.put("URL", "dummyURL");
//		dummyHit.put("TIMESTAMP", 123);
//		dummyHit.put("SERVER", "def.com");
//		dummyHit.put("INSTALL", 0);
//		dummyHit.put("OFFLINETRACKING", 1);
//		testableDatabase.insertNHits(dummyHit, hitsNumber);
//	}
//
//}
