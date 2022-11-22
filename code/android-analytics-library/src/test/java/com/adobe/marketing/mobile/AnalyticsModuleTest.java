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
//import org.junit.runner.RunWith;
//import org.junit.runners.JUnit4;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.Assert.*;
//import static org.junit.Assert.assertNull;
//
//
//@RunWith(JUnit4.class)
//public class AnalyticsModuleTest extends SystemTest {
//	private static int EVENTHUB_WAIT_MS = 1000;
//
//	private TestableNetworkService testableNetworkService;
//	private AnalyticsCoreAPI testableCore;
//	private AnalyticsModuleTestsHelper testHelper;
//	private CountDownLatch registerLatch;
//
//	private final Map<String, String> referrerData = new HashMap<String, String>();
//
//	{
//		referrerData.put("a.referrer.campaign.trackingcode", "1234567890");
//		referrerData.put("a.launch.campaign.trackingcode", "1234567890");
//		referrerData.put("a.referrer.campaign.name", "myLink");
//		referrerData.put("a.referrer.campaign.source", "mycompany");
//		referrerData.put("a.launch.campaign.source", "mycompany");
//		referrerData.put("a.acquisition.custom.amo1.key1", "amo1.value1");
//		referrerData.put("a.acquisition.custom.amo1.key2", "amo1.value2");
//	}
//
//	private static final String expectedPayloadStarting =
//		"ndh=1&ce=UTF-8&c.&a.&referrer.&campaign.&trackingcode=1234567890&name=myLink"
//		+ "&source=mycompany&.campaign&.referrer&InstallEvent=InstallEvent&launch.&campaign.&trackingcode=1234567890"
//		+ "&source=mycompany&.campaign&.launch&acquisition.&custom.&amo1.&key1=amo1.value1&key2=amo1.value2&.amo1&.custom&.acquisition"
//		+ "&internalaction=Lifecycle&.a&.c&t=";
//	private static final String expectedPayloadEnding = "&pe=lnk_o&pev2=ADBINTERNAL%3ALifecycle&cp=foreground";
//
//	@Before
//	public void beforeEach() {
//		try {
//			testableNetworkService = platformServices.getTestableNetworkService();
//
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
//			testableCore = new AnalyticsCoreAPI(eventHub);
//			testHelper = new AnalyticsModuleTestsHelper(eventHub);
//			eventHub.clearEvents();
//
//			AnalyticsVersionProvider.setVersion("ANDNMockAnalyticsVersionMockCoreVersion");
//		} catch (Exception e) {
//		}
//	}
//
//	@After
//	public void afterEach() {
//		eventHub.clearEvents();
//	}
//
//	@Ignore
//	@Test
//	public void testReferrerProcessed_When_LifecycleInstallEvent_And_ReferrerDataSharedState() {
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.ignoreEvents(EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT);
//		eventHub.setExpectedEventCount(2);
//		testableNetworkService.setExpectedCount(1);
//
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 0, false);
//		testHelper.setIdentitySharedState();
//		testHelper.setAcquisitionSharedState(referrerData);
//		platformServices.getMockSystemInfoService().networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//		testableNetworkService.setDefaultResponse("serverResponse");
//		eventHub.dispatch(createLifecycleInstallEvent());
//
//		waitForThreadsWithFailIfTimedOut(5000);
//		assertEquals(1, testableNetworkService.waitAndGetCount());
//		assertTrue(testableNetworkService.getItem(0).url.startsWith("https://test.com/b/ss/123%2C132123/0/mockSdkVersion/s"));
//		assertTrue(new String(testableNetworkService.getItem(0).connectPayload).startsWith(expectedPayloadStarting));
//		assertTrue("expected " + new String(testableNetworkService.getItem(0).connectPayload) + "\n to end with \"" +
//				   expectedPayloadEnding + "\"", new String(testableNetworkService.getItem(0).connectPayload).endsWith(
//					   expectedPayloadEnding));
//
//		List<Event> events =  eventHub.getEvents(500);
//		assertEquals(2, events.size());
//		assertTrue(events.get(1).getData().containsKey("analyticsServerResponse"));
//	}
//
//	@Ignore
//	@Test
//	public void testReferrerProcessed_When_LifecycleInstallEvent_And_AcquisitionEvent() {
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.ignoreEvents(EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT);
//		eventHub.setExpectedEventCount(3);
//		testableNetworkService.setExpectedCount(1);
//		testableNetworkService.setDefaultResponse("serverResponse");
//		platformServices.getMockSystemInfoService().networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 1, false);
//		testHelper.setIdentitySharedState();
//
//		eventHub.dispatch(createLifecycleInstallEvent());
//		// TODO: revisit this after AMSDK-4206 is done
//		waitForThreadsWithFailIfTimedOut(5000);
//		eventHub.dispatch(new Event.Builder("Acquisition", EventType.ACQUISITION, EventSource.RESPONSE_CONTENT)
//						  .setData(new EventData().putStringMap("contextData", referrerData)).build());
//
//		waitForThreadsWithFailIfTimedOut(5000);
//		assertEquals(1, testableNetworkService.waitAndGetCount(500));
//		assertTrue(testableNetworkService.getItem(0).url.startsWith("https://test.com/b/ss/123%2C132123/0/mockSdkVersion/s"));
//		assertTrue(new String(testableNetworkService.getItem(0).connectPayload).startsWith(expectedPayloadStarting));
//		assertTrue(new String(testableNetworkService.getItem(0).connectPayload).endsWith(expectedPayloadEnding));
//		List<Event> events = eventHub.getEvents(500);
//		assertEquals(EventType.ACQUISITION, events.get(1).getEventType());
//		assertTrue(events.get(2).getData().containsKey("analyticsServerResponse"));
//	}
//
//	@Test
//	public void testResponseEventSent_When_OverBatchLimit_And_ValidServerResponse() {
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.ignoreEvents(EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT);
//		eventHub.ignoreEvents(EventType.GENERIC_TRACK, EventSource.REQUEST_CONTENT);
//		eventHub.setExpectedEventCount(3);
//		testableNetworkService.setExpectedCount(3);
//		testableNetworkService.setDefaultResponse("serverResponse");
//		platformServices.getMockSystemInfoService().networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 2, true);
//		testHelper.setIdentitySharedState();
//
//		testableCore.clearQueue();
//		waitForThreadsWithFailIfTimedOut(5000);
//		eventHub.clearEvents();
//		testableNetworkService.clearNetworkRequests();
//		EventData request = new EventData()
//		.putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "action1")
//		.putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, new HashMap<String, String>() {
//			{
//				put("key1", "val1");
//			}
//		});
//		testableCore.trackAnalyticsRequest(request);
//		request = new EventData()
//		.putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "action2")
//		.putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, new HashMap<String, String>() {
//			{
//				put("key2", "val2");
//			}
//		});
//		testableCore.trackAnalyticsRequest(request);
//		request = new EventData()
//		.putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "action3")
//		.putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, new HashMap<String, String>() {
//			{
//				put("key3", "val3");
//			}
//		});
//		testableCore.trackAnalyticsRequest(request);
//
//		waitForThreadsWithFailIfTimedOut(5000);
//		assertEquals(3, testableNetworkService.waitAndGetCount(500));
//		assertTrue(testableNetworkService.getItem(
//					   0).url.startsWith("https://test.com/b/ss/123%2C132123/10/ANDNMockAnalyticsVersionMockCoreVersion/s"));
//		List<Event> events = eventHub.getEvents(500);
//		assertEquals(3, events.size());
//		assertEquals(EventType.ANALYTICS, events.get(0).getEventType());
//		assertEquals(EventSource.RESPONSE_CONTENT, events.get(0).getEventSource());
//		assertEquals("serverResponse", events.get(0).getData().optString("analyticsserverresponse", null));
//	}
//
//	// We have dropped the check for empty server response so we should still see hit response event
//	@Test
//	public void testResponseEventSent_When_OverBatchLimit_And_EmptyServerResponse() {
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.ignoreEvents(EventType.RULES_ENGINE, EventSource.RESPONSE_CONTENT);
//		eventHub.ignoreEvents(EventType.GENERIC_TRACK, EventSource.REQUEST_CONTENT);
//		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_CONTENT);
//		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_IDENTITY);
//		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.RESPONSE_IDENTITY);
//		testableNetworkService.setExpectedCount(3);
//		testableNetworkService.ignoreNetworkRequestURL("https://test.com/id");
//		testableNetworkService.setDefaultResponse("");
//		platformServices.getMockSystemInfoService().networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 2, false);
//		testHelper.setIdentitySharedState();
//
//		testableCore.clearQueue();
//		eventHub.clearEvents();
//		waitForThreadsWithFailIfTimedOut(5000);
//		EventData request = new EventData()
//		.putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "action1")
//		.putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, new HashMap<String, String>() {
//			{
//				put("key1", "val1");
//			}
//		});
//		testableCore.trackAnalyticsRequest(request);
//		request = new EventData()
//		.putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "action2")
//		.putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, new HashMap<String, String>() {
//			{
//				put("key2", "val2");
//			}
//		});
//		testableCore.trackAnalyticsRequest(request);
//		request = new EventData()
//		.putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "action3")
//		.putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, new HashMap<String, String>() {
//			{
//				put("key3", "val3");
//			}
//		});
//		testableCore.trackAnalyticsRequest(request);
//
//		waitForThreadsWithFailIfTimedOut(5000);
//		assertEquals(3, testableNetworkService.waitAndGetCount(1000));
//		assertTrue(testableNetworkService.getItem(
//					   0).url.startsWith("https://test.com/b/ss/123%2C132123/0/ANDNMockAnalyticsVersionMockCoreVersion/s"));
//		List<Event> events = eventHub.getEvents(500);
//		assertEquals(3, events.size());
//	}
//
//	@Test
//	public void testGetVisitorIdentifier_returnsVIDValue() throws Exception {
//		// setup
//		testHelper.setupForTracking();
//
//		final String expectedVid = "1A61B875A8D64420-0E969B4EEFD2EFB0";
//		setVidInLocalStorage(expectedVid);
//
//		final CountDownLatch latch = new CountDownLatch(1);
//		final String[] vidValue = new String[1];
//		AdobeCallback<String> adobeCallback = new AdobeCallback<String>() {
//			@Override
//			public void call(String value) {
//				vidValue[0] = value;
//				latch.countDown();
//			}
//		};
//
//		testableCore.getVisitorIdentifier(adobeCallback);
//		latch.await(5000, TimeUnit.MILLISECONDS);
//		assertEquals(expectedVid, vidValue[0]);
//	}
//
//	@Test
//	public void testGetVisitorIdentifier_returnsNull_WhenNoVID() throws Exception {
//		testHelper.setupForTracking();
//		final String[] vidValue = new String[1];
//
//		final CountDownLatch latch = new CountDownLatch(1);
//		AdobeCallback<String> adobeCallback = new AdobeCallback<String>() {
//			@Override
//			public void call(String value) {
//				vidValue[0] = value;
//				latch.countDown();
//			}
//		};
//
//		testableCore.getTrackingIdentifier(adobeCallback);
//		latch.await(5000, TimeUnit.MILLISECONDS);
//		assertNull(vidValue[0]);
//	}
//
//	@Test
//	public void testGetVisitorIdentifier_returnsNull_WhenPrivacyOptout() throws Exception {
//		eventHub.ignoreAllStateChangeEvents();
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 0, false);
//		testHelper.setIdentitySharedState();
//
//		final String expectedVid = "1A61B875A8D64420-0E969B4EEFD2EFB0";
//		testableCore.updateVisitorIdentifier(expectedVid);
//
//
//		final CountDownLatch latch = new CountDownLatch(1);
//		final String[] vidValue = new String[1];
//		AdobeCallback<String> adobeCallback = new AdobeCallback<String>() {
//			@Override
//			public void call(String value) {
//				vidValue[0] = value;
//				latch.countDown();
//			}
//		};
//
//		testableCore.getVisitorIdentifier(adobeCallback);
//		latch.await(5000, TimeUnit.MILLISECONDS);
//		assertEquals(expectedVid, vidValue[0]);
//
//		testHelper.dispatchConfigurationResponseEvent(MobilePrivacyStatus.OPT_OUT, 0);
//		waitForThreadsWithFailIfTimedOut(5000); // wait for configuration change
//
//
//		final CountDownLatch latch2 = new CountDownLatch(1);
//		final String[] vidValue2 = new String[1];
//		AdobeCallback<String> adobeCallback2 = new AdobeCallback<String>() {
//			@Override
//			public void call(String value) {
//				vidValue2[0] = value;
//				latch2.countDown();
//			}
//		};
//
//		testableCore.getVisitorIdentifier(adobeCallback2);
//		latch2.await(5000, TimeUnit.MILLISECONDS);
//		assertNull(vidValue2[0]);
//	}
//
//	@Test
//	public void testUpdateVisitorIdentifier_updatesSharedState_whenPrivacyOptin() throws Exception {
//		// setup
//		eventHub.ignoreAllStateChangeEvents();
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 0, false);
//		testHelper.setIdentitySharedState();
//
//		final String expectedVid = "1A61B875A8D64420-0E969B4EEFD2EFB0";
//
//		testableCore.updateVisitorIdentifier(expectedVid);
//		waitForThreadsWithFailIfTimedOut(5000);
//
//		final String sharedVid = getAnalyticsSharedState().optString("vid", null);
//		assertEquals(expectedVid, sharedVid);
//		final String actualVid = getVidInLocalStorage();
//		assertEquals(expectedVid, actualVid);
//	}
//
//	@Test
//	public void testUpdateVisitorIdentifier_updatesSharedState_whenPrivacyUnknown() throws Exception {
//		// setup
//		eventHub.ignoreAllStateChangeEvents();
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.UNKNOWN, 0, false);
//		testHelper.setIdentitySharedState();
//
//		final String expectedVid = "1A61B875A8D64420-0E969B4EEFD2EFB0";
//
//		testableCore.updateVisitorIdentifier(expectedVid);
//		waitForThreadsWithFailIfTimedOut(5000);
//
//		final String sharedVid = getAnalyticsSharedState().optString("vid", null);
//		assertEquals(expectedVid, sharedVid);
//		final String actualVid = getVidInLocalStorage();
//		assertEquals(expectedVid, actualVid);
//	}
//
//	@Test
//	public void testUpdateVisitorIdentifier_doesNotUpdateSharedState_whenPrivacyOptout() throws Exception {
//		// setup
//		eventHub.ignoreAllStateChangeEvents();
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_OUT, 0, false);
//		testHelper.setIdentitySharedState();
//
//		final String expectedVid = "1A61B875A8D64420-0E969B4EEFD2EFB0";
//
//		testableCore.updateVisitorIdentifier(expectedVid);
//		waitForThreadsWithFailIfTimedOut(5000);
//
//		final String sharedVid = getAnalyticsSharedState().optString("vid", null);
//		assertNull(sharedVid);
//		final String actualVid = getVidInLocalStorage();
//		assertNull(actualVid);
//	}
//
//	@Test
//	public void testGenericResetIdentifier_ClearsQueue() throws InterruptedException {
//		// setup
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(1);
//
//		testableNetworkService.setDefaultResponse("serverResponse");
//		platformServices.getMockSystemInfoService().networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 3, true);
//		testHelper.setIdentitySharedState();
//
//		testableCore.clearQueue();
//		waitForThreadsWithFailIfTimedOut(5000);
//		eventHub.clearEvents();
//		testableNetworkService.clearNetworkRequests();
//
//		final CountDownLatch latch = new CountDownLatch(1);
//		final long[] queueSize = new long[1];
//		AdobeCallback<Long> adobeCallback = new AdobeCallback<Long>() {
//			@Override
//			public void call(Long value) {
//				queueSize[0] = value;
//				latch.countDown();
//			}
//		};
//
//		EventData request = new EventData()
//		.putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "action1")
//		.putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, new HashMap<String, String>() {
//			{
//				put("key1", "val1");
//			}
//		});
//		testableCore.trackAnalyticsRequest(request);
//		request = new EventData()
//		.putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "action2")
//		.putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, new HashMap<String, String>() {
//			{
//				put("key2", "val2");
//			}
//		});
//		testableCore.trackAnalyticsRequest(request);
//		waitForThreadsWithFailIfTimedOut(5000);
//
//		testableCore.getQueueSize(adobeCallback);
//		latch.await(5000, TimeUnit.MILLISECONDS);
//		assertEquals(2, queueSize[0]);
//
//		// test
//		Event genericResetEvent = new Event.Builder("Test Reset", EventType.GENERIC_IDENTITY, EventSource.REQUEST_RESET)
//		.build();
//		eventHub.dispatch(genericResetEvent);
//
//		//verify
//		final CountDownLatch latch2 = new CountDownLatch(1);
//		final long[] queueSizeAfterReset = new long[1];
//		AdobeCallback<Long> adobeCallback2 = new AdobeCallback<Long>() {
//			@Override
//			public void call(Long value) {
//				queueSizeAfterReset[0] = value;
//				latch2.countDown();
//			}
//		};
//		testableCore.getQueueSize(adobeCallback2);
//		latch2.await(5000, TimeUnit.MILLISECONDS);
//		assertEquals(0, queueSizeAfterReset[0]);
//		assertEquals(0, testableNetworkService.waitAndGetCount());
//	}
//
//	@Test
//	public void
//	testGenericResetIdentifier_ShouldNotAllowHitInProcess_AndIfHitAlreadyProcessedShouldNotAllowToDispatchResponse() {
//		// setup
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(1);
//
//		testableNetworkService.setDefaultResponse("serverResponse");
//		platformServices.getMockSystemInfoService().networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//		testHelper.setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 0, true);
//		testHelper.setIdentitySharedState();
//
//		testableCore.clearQueue();
//		waitForThreadsWithFailIfTimedOut(5000);
//		eventHub.clearEvents();
//		testableNetworkService.clearNetworkRequests();
//		EventData request = new EventData()
//		.putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION, "action1")
//		.putStringMap(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA, new HashMap<String, String>() {
//			{
//				put("key1", "val1");
//			}
//		});
//
//		testableCore.trackAnalyticsRequest(request);
//
//		// test
//		Event genericResetEvent = new Event.Builder("Test Reset", EventType.GENERIC_IDENTITY, EventSource.REQUEST_RESET)
//		.build();
//		eventHub.dispatch(genericResetEvent);
//
//		//verify
//		final CountDownLatch latch = new CountDownLatch(1);
//		final long[] queueSize = new long[1];
//		AdobeCallback<Long> adobeCallback = new AdobeCallback<Long>() {
//			@Override
//			public void call(Long value) {
//				queueSize[0] = value;
//				latch.countDown();
//			}
//		};
//		testableCore.getQueueSize(adobeCallback);
//		assertEquals(0, testableNetworkService.waitAndGetCount());
//		assertEquals(0, queueSize[0]);
//		List<Event> events =  eventHub.getEvents(500);
//		// Assert that the analytics response is not being dispatched
//		assertEquals(4,
//					 events.size()); // request content/generic track, request reset/generic track, request content/analytics - getQueueSize, responseContent - queueSizeValue
//	}
//
//
//	private Event createLifecycleInstallEvent() {
//		EventData eventData = new EventData();
//		eventData.putString(AnalyticsConstants.EventDataKeys.Lifecycle.SESSION_EVENT,
//							AnalyticsConstants.EventDataKeys.Lifecycle.LIFECYCLE_START);
//		eventData.putLong(AnalyticsConstants.EventDataKeys.Lifecycle.SESSION_START_TIMESTAMP, 12314124134L);
//		eventData.putStringMap(AnalyticsConstants.EventDataKeys.Lifecycle.LIFECYCLE_CONTEXT_DATA,
//		new HashMap<String, String>() {
//			{
//				put("a.InstallEvent", "InstallEvent");
//			}
//		});
//		eventData.putLong(AnalyticsConstants.EventDataKeys.Lifecycle.PREVIOUS_SESSION_START_TIMESTAMP, 12314L);
//		eventData.putLong(AnalyticsConstants.EventDataKeys.Lifecycle.PREVIOUS_SESSION_PAUSE_TIMESTAMP, 123451L);
//		return new Event.Builder("LifecycleInstall", EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//			   .setData(eventData)
//			   .build();
//	}
//
//	private class TestModule extends Module {
//		TestModule(final EventHub hub) {
//			super("TestModule", hub);
//		}
//	}
//
//	private EventData getAnalyticsSharedState() {
//		Event identityTestEvent = new Event.Builder("TEST", EventType.IDENTITY, EventSource.REQUEST_CONTENT).build();
//		identityTestEvent.setEventNumber(eventHub.getAllEventsCount());
//		return eventHub.getSharedEventState("com.adobe.module.analytics", identityTestEvent,
//											new TestModule(eventHub));
//	}
//
//	private void setVidInLocalStorage(final String vid) {
//		final FakeDataStore dataStore = new FakeDataStore();
//		dataStore.setString("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER", vid);
//		((FakeLocalStorageService)platformServices.getLocalStorageService()).mapping = new
//		HashMap<String, LocalStorageService.DataStore>() {
//			{
//				put("AnalyticsDataStorage", dataStore);
//			}
//		};
//	}
//
//	private String getVidInLocalStorage() {
//		LocalStorageService.DataStore dataStore =
//			platformServices.getLocalStorageService().getDataStore("AnalyticsDataStorage");
//		return dataStore.getString("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER", null);
//	}
//}
