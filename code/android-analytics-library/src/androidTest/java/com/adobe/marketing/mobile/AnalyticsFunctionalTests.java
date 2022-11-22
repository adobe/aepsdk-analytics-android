///* **************************************************************************
// *
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2018 Adobe Systems Incorporated
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
// **************************************************************************/
//
//package com.adobe.marketing.mobile;
//
//
//import android.support.test.runner.AndroidJUnit4;
//
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.TimeZone;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//import static com.adobe.marketing.mobile.E2ETestableNetworkService.NetworkRequest;
//import static com.adobe.marketing.mobile.PlatformAssertions.getAdditionalData;
//import static com.adobe.marketing.mobile.PlatformAssertions.getContextData;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//
//@RunWith(AndroidJUnit4.class)
//public class AnalyticsFunctionalTests extends AbstractE2ETest {
//	TestHelper testHelper = new TestHelper();
//	private AndroidLocalStorageService localStorageService;
//
//	@Before
//	public void setUp() {
//		super.setUp();
//		localStorageService = new AndroidLocalStorageService();
//		MobileCore.setApplication(defaultApplication);
//
//		try {
//			Analytics.registerExtension();
//			Identity.registerExtension();
//		} catch (InvalidInitException e) {
//			e.printStackTrace();
//		}
//
//		MobileCore.start(null);
//
//
//		HashMap<String, Object> data = new HashMap<String, Object>();
//		data.put("analytics.server", "analytics.com");
//		data.put("analytics.rsids", "rsid1,rsid2");
//		data.put("analytics.referrerTimeout", 0);
//		data.put("analytics.offlineEnabled", true);
//		data.put("analytics.batchLimit", 0);
//		data.put("analytics.backdatePreviousSessionInfo", true);
//		data.put("lifecycle.sessionTimeout", 1);
//		data.put("global.privacy", "optedin");
//		data.put("experienceCloud.org", "972C898555E9F7BC7F000101@AdobeOrg");
//		data.put("experienceCloud.server", "identity.com");
//		MobileCore.updateConfiguration(data);
//		MobileCore.setLogLevel(LoggingMode.VERBOSE);
//
//		waitForConfigChange();
//		waitForIdentitySync();
//
//		testableNetworkService.resetTestableNetworkService();
//	}
//
//	@After
//	public void tearDown() {
//		super.tearDown();
//	}
//
//	// ---------------------------------------------------//
//	// Happy Tests
//	// ---------------------------------------------------//
//
//	@Test
//	public void test_Functional_Happy_Analytics_trackAction_VerifyRequestHasAllNecessaryDefaultParameters() {
//		//setup
//		TimeZone timeZone = Calendar.getInstance().getTimeZone();
//		int timezoneOffsetInMinutes = (timeZone.getOffset(Calendar.getInstance().getTimeInMillis())) / 60000 * -1;
//		String timezoneOffset = Integer.toString(timezoneOffsetInMinutes);
//
//		MobileCore.updateConfiguration(new HashMap<String, Object>() {
//			{
//				put("analytics.batchLimit", 0);
//			}
//		});
//		waitForConfigChange();
//		Map<String, String> additionalData = new HashMap<String, String>();
//		additionalData.put("key1", "value1");
//		//test
//		MobileCore.trackAction("clickOK", additionalData);
//		//verify
//		assertEquals(1, testableNetworkService.waitAndGetCount(1));
//		NetworkRequest analyticsHit = testableNetworkService.getItem(0);
//		assertTrue(analyticsHit.url.contains("https://analytics.com/b/ss/rsid1%2Crsid2/0/ANDN"));
//		Map<String, Object> contextData = getContextData(new String(analyticsHit.connectPayload));
//		String payload = new String(analyticsHit.connectPayload);
//		assertEquals("value1", contextData.get("key1"));
//		assertEquals("clickOK", contextData.get("a.action"));
//		assertTrue("Assert failed, ndh=1 not found", payload.contains("ndh=1"));
//		assertTrue("Assert failed, ce=UTF8 not found", payload.contains("ce=UTF-8"));
//		assertTrue("Assert failed, cp=foreground not found", payload.contains("cp=foreground"));
//		assertTrue("Assert failed, e=lnk_o not found", payload.contains("e=lnk_o"));
//		assertTrue("Assert failed, timezone offset not found",
//				   payload.contains("t=00%2F00%2F0000%2000%3A00%3A00%200%20" + timezoneOffset));
//		assertTrue("Assert failed, ts not found", payload.contains("ts="));
//	}
//
//
//	// Test Case No : 2
//	@Test
//	public void test_Functional_Happy_Analytics_sendQueuedHits_VerifyQueuedHitsAreSentCorrectly() throws
//		InterruptedException {
//		//setup
//		final CountDownLatch latch = new CountDownLatch(1);
//		final Long queueSize[] = new Long[1];
//		long temp = 0;
//		MobileCore.updateConfiguration(new HashMap<String, Object>() {
//			{
//				put("analytics.batchLimit", 4);
//			}
//		});
//		waitForConfigChange();
//		//test
//		MobileCore.trackAction("clickButton", null);
//		MobileCore.trackState("SecondPage", null);
//		MobileCore.trackAction(null, null);
//
//		Analytics.sendQueuedHits();
//		//verify
//		assertEquals(3, testableNetworkService.waitAndGetCount(3, 3000));
//		testHelper.waitForThreadsWithFailIfTimedOut(500); // allow hit processor to update the queue
//		Analytics.getQueueSize(new AdobeCallback<Long>() {
//			@Override
//			public void call(Long value) {
//				queueSize[0] = value;
//				latch.countDown();
//			}
//		});
//		latch.await(5, TimeUnit.SECONDS);
//		//verify queue is empty
//		temp = queueSize[0];
//		assertEquals(0, temp);
//	}
//
//	// Test Case No : 3
//	@Test
//	public void
//	test_Functional_Happy_Analytics_analyticsTrack_VerifyMultipleAnalyticsTrackCallsCanBeSentAccordingToSetBatchLimit() {
//		//setup
//		MobileCore.updateConfiguration(new HashMap<String, Object>() {
//			{
//				put("analytics.batchLimit", 2);
//			}
//		});
//		waitForConfigChange();
//		//test
//		MobileCore.trackAction("testAction1", null);
//		MobileCore.trackAction("testAction2", null);
//		//verify that no hits are sent due to batch limit not being passed
//		assertEquals(0, testableNetworkService.waitAndGetCount(1));
//		//test
//		MobileCore.trackAction("testAction3", null);
//		//verify that 3 hits are sent due to batch limit being passed
//		assertEquals(3, testableNetworkService.waitAndGetCount(3, 3000));
//	}
//
//	// Test Case No : 4
//	@Test
//	public void
//	test_Functional_Happy_Analytics_clearQueue_getQueueSize_VerifyQueuedAnalyticsHitsAreCleared() throws
//		InterruptedException {
//		//setup
//		final CountDownLatch latch = new CountDownLatch(1);
//		final CountDownLatch latch2 = new CountDownLatch(1);
//		final Long queueSize[] = new Long[2];
//		long temp = 0;
//		MobileCore.updateConfiguration(new HashMap<String, Object>() {
//			{
//				put("analytics.batchLimit", 4);
//			}
//		});
//		waitForConfigChange();
//		//test
//		MobileCore.trackAction("testAction1", null);
//		MobileCore.trackAction("testAction2", null);
//		MobileCore.trackAction("testAction3", null);
//		MobileCore.trackAction("testAction4", null);
//
//
//		//verify that 4 hits are queued
//		Analytics.getQueueSize(new AdobeCallback<Long>() {
//			@Override
//			public void call(Long value) {
//				queueSize[0] = value;
//				latch.countDown();
//			}
//		});
//		latch.await(2, TimeUnit.SECONDS);
//		temp = queueSize[0];
//		assertEquals(4, temp);
//		//clear queue and verify that all hits are cleared
//		Analytics.clearQueue();
//		Analytics.getQueueSize(new AdobeCallback<Long>() {
//			@Override
//			public void call(Long value) {
//				queueSize[1] = value;
//				latch2.countDown();
//			}
//		});
//		latch2.await(2, TimeUnit.SECONDS);
//		temp = queueSize[1];
//		assertEquals(0, temp);
//	}
//
//	// Test Case No : 5
//	@Test
//	public void test_Functional_Happy_Analytics_getQueueSize_CheckingDefaultQueueSize() throws
//		InterruptedException {
//		//setup
//		final CountDownLatch latch = new CountDownLatch(1);
//		final Long queueSize[] = new Long[1];
//		long temp = 0;
//		//test
//		Analytics.getQueueSize(new AdobeCallback<Long>() {
//			@Override
//			public void call(Long value) {
//				queueSize[0] = value;
//				latch.countDown();
//			}
//		});
//		latch.await(2, TimeUnit.SECONDS);
//		//verify default queue size
//		temp = queueSize[0];
//		assertEquals(0, temp);
//	}
//
//	// Test Case No : 6
//	@Test
//	@Ignore
//	public void
//	test_Functional_Happy_Analytics_analyticsGetTrackingIdentifier_CheckingTrackingIdReceivedFromResponseIsTheSameInRequest()
//	throws Exception {
//		//setup
//
//		final CountDownLatch latch = new CountDownLatch(1);
//		final String[] trackingIdentifier = new String[1];
//		String identifierResponse = "{ id : 1A61B875A8D64420-0E969B4EEFD2EFB4 }";
//		Map<String, String> headers = new HashMap<>();
//		SimpleDateFormat simpleDateFormat = TestHelper.createRFC2822Formatter();
//		headers.put("Last-Modified", simpleDateFormat.format(new Date()));
//		E2ETestableNetworkService.NetworkResponse networkResponse = new E2ETestableNetworkService.NetworkResponse(
//			identifierResponse, 200, headers);
//		testableNetworkService.setDefaultResponse(networkResponse);
//		//test
//		Analytics.getTrackingIdentifier(new AdobeCallback<String>() {
//			@Override
//			public void call(String data) {
//				trackingIdentifier[0] = data;
//				latch.countDown();
//			}
//		});
//		latch.await(5, TimeUnit.SECONDS);
//		//verify
//		assertEquals(1, testableNetworkService.waitAndGetCount(1));
//		NetworkRequest analyticsHit = testableNetworkService.getItem(0);
//		assertTrue(analyticsHit.url.contains("https://analytics.com/id?mcorgid=972C898555E9F7BC7F000101%40AdobeOrg&mid="));
//		assertEquals("1A61B875A8D64420-0E969B4EEFD2EFB4", trackingIdentifier[0]);
//	}
//
//	// Test Case No : 7
//	@Test
//	public void test_Functional_Happy_Analytics_analyticsTrack_CheckPingGoesEvenWhenStateActionContextDataNotPresent() {
//		//setup
//		TimeZone timeZone = Calendar.getInstance().getTimeZone();
//		int timezoneOffsetInMinutes = (timeZone.getOffset(Calendar.getInstance().getTimeInMillis())) / 60000 * -1;
//		String timezoneOffset = Integer.toString(timezoneOffsetInMinutes);
//
//		MobileCore.updateConfiguration(new HashMap<String, Object>() {
//			{
//				put("analytics.batchLimit", 0);
//			}
//		});
//		waitForConfigChange();
//
//		//test
//		MobileCore.trackAction(null, null);
//		//verify
//		assertEquals(1, testableNetworkService.waitAndGetCount(1, 3000));
//		NetworkRequest analyticsHit = testableNetworkService.getItem(0);
//		assertTrue(analyticsHit.url.contains("https://analytics.com/b/ss/rsid1%2Crsid2/0/ANDN"));
//		Map<String, Object> contextData = getContextData(new String(analyticsHit.connectPayload));
//		String payload = new String(analyticsHit.connectPayload);
//		assertTrue("Assert failed, ndh=1 not found", payload.contains("ndh=1"));
//		assertTrue("Assert failed, ce=UTF8 not found", payload.contains("ce=UTF-8"));
//		assertTrue("Assert failed, cp=foreground not found", payload.contains("cp=foreground"));
//		assertTrue("Assert failed, timezone offset not found",
//				   payload.contains("t=00%2F00%2F0000%2000%3A00%3A00%200%20" + timezoneOffset));
//		assertTrue("Assert failed, ts not found", payload.contains("ts="));
//	}
//
//	// Test Case No : 8
//	@Test
//	public void
//	test_Functional_Happy_Analytics_analyticsTrack_CheckingNoAnalyticsHitIsSentWhenAnalyticsServerIsNotDefined() {
//		//setup
//		MobileCore.updateConfiguration(new HashMap<String, Object>() {
//			{
//				put("analytics.server", null);
//			}
//		});
//		waitForConfigChange();
//
//		//test
//		MobileCore.trackAction(null, null);
//		//verify
//		assertEquals(0, testableNetworkService.waitAndGetCount(1));
//	}
//
//	// Helper methods
//	private void waitForConfigChange() {
//		final CountDownLatch latch = new CountDownLatch(1);
//		MobileCore.getPrivacyStatus(new AdobeCallback<MobilePrivacyStatus>() {
//			@Override
//			public void call(MobilePrivacyStatus mobilePrivacyStatus) {
//				latch.countDown();
//			}
//		});
//
//		try {
//			latch.await(2, TimeUnit.SECONDS);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//			Assert.fail("Timed out waiting for config change");
//		}
//	}
//
//	private void waitForIdentitySync() {
//		assertEquals(1, testableNetworkService.waitAndGetCount(1, 2000));
//		NetworkRequest identitySync = testableNetworkService.getItem(0);
//		assertTrue(String.format("Failed waiting for identity sync call, found url: (%s)", identitySync.url),
//				   identitySync.url.contains("https://identity.com"));
//	}
//}
