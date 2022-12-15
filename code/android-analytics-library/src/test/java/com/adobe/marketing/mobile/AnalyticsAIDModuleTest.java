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
//import java.io.File;
//import java.util.HashMap;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.Assert.*;
//
//public class AnalyticsAIDModuleTest extends SystemTest {
//	private static final int TEST_TIMEOUT = 1000;
//	private static int EVENTHUB_WAIT_MS = 1000;
//
//	private static final String AID_KEY = "aid";
//	private static final String VID_KEY = "vid";
//
//	private TestableNetworkService     testableNetworkService;
//	private AnalyticsCoreAPI           testableCore;
//	private AnalyticsModuleTestsHelper testHelper;
//	private CountDownLatch			   registrationLatch;
//
//	@Before
//	public void beforeEach() {
//		platformServices.getMockSystemInfoService().applicationCacheDir = new File(this.getClass().getResource("").getPath() +
//				"test");
//		platformServices.getMockSystemInfoService().networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//		testableNetworkService = platformServices.getTestableNetworkService();
//		testableCore = new AnalyticsCoreAPI(eventHub);
//		testHelper = new AnalyticsModuleTestsHelper(eventHub);
//		testHelper.setupForTracking();
//	}
//
//	@Test
//	public void testGetTrackingIdentifier_returnsAIDValue() throws Exception {
//		// setup
//		final String expectedAid = "1A61B875A8D64420-0E969B4EEFD2EFB0";
//		setAidAndVidInLocalStorage(expectedAid, null);
//
//		registrationLatch = new CountDownLatch(1);
//		eventHub.registerModuleWithCallback(AnalyticsExtension.class, new EventHub.RegisterModuleCallback() {
//			@Override
//			public void registered(Module module) {
//				registrationLatch.countDown();
//			}
//		});
//		registrationLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
//		eventHub.finishModulesRegistration(new AdobeCallback() {
//			@Override
//			public void call(Object value) {
//
//			}
//		});
//
//		eventHub.ignoreAllEvents();
//
//		final String[] aidValue = new String[1];
//		AdobeCallback<String> adobeCallback = new AdobeCallback<String>() {
//			@Override
//			public void call(String value) {
//				aidValue[0] = value;
//			}
//		};
//
//		testableCore.getTrackingIdentifier(adobeCallback);
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(expectedAid, aidValue[0]);
//	}
//
//	@Test
//	public void testGetTrackingIdentifier_returnsNull_WhenNoAID() throws Exception {
//		// setup
//		registrationLatch = new CountDownLatch(1);
//		eventHub.registerModuleWithCallback(AnalyticsExtension.class, new EventHub.RegisterModuleCallback() {
//			@Override
//			public void registered(Module module) {
//				registrationLatch.countDown();
//			}
//		});
//		registrationLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
//		eventHub.finishModulesRegistration(new AdobeCallback() {
//			@Override
//			public void call(Object value) {
//
//			}
//		});
//
//		eventHub.ignoreAllEvents();
//
//		final String[] aidValue = new String[1];
//		AdobeCallback<String> adobeCallback = new AdobeCallback<String>() {
//			@Override
//			public void call(String value) {
//				aidValue[0] = value;
//			}
//		};
//
//		testableCore.getTrackingIdentifier(adobeCallback);
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertNull(aidValue[0]);
//	}
//
//	@Test
//	public void testGetTrackingIdentifier_updatesAIDValueInSharedState() throws Exception {
//		// setup
//		final String expectedAid = "1A61B875A8D64420-0E969B4EEFD2EFB0";
//		setAidAndVidInLocalStorage(expectedAid, null);
//
//		registrationLatch = new CountDownLatch(1);
//		eventHub.registerModuleWithCallback(AnalyticsExtension.class, new EventHub.RegisterModuleCallback() {
//			@Override
//			public void registered(Module module) {
//				registrationLatch.countDown();
//			}
//		});
//		registrationLatch.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
//		eventHub.finishModulesRegistration(new AdobeCallback() {
//			@Override
//			public void call(Object value) {
//
//			}
//		});
//
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		eventHub.clearEvents();
//		eventHub.clearIgnoredEventFilters();
//		eventHub.ignoreEvents(EventType.ANALYTICS, EventSource.REQUEST_IDENTITY);
//		eventHub.setExpectedEventCount(2);
//		final String[] aidValue = new String[1];
//		AdobeCallback<String> adobeCallback = new AdobeCallback<String>() {
//			@Override
//			public void call(String value) {
//				aidValue[0] = value;
//			}
//		};
//
//		// test
//		testableCore.getTrackingIdentifier(adobeCallback);
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//
//		// verify shared state update event and aid value
//		assertEquals(3, eventHub.getEvents().size());
//		Event event1 = eventHub.getEvents().get(0);
//		EventAssertions.assertEventType(event1, EventType.HUB);
//		EventAssertions.assertEventSource(event1, EventSource.SHARED_STATE);
//		EventAssertions.assertEventDataContains(event1, "stateowner", "com.adobe.module.analytics");
//		Event event2 = eventHub.getEvents().get(1);
//		EventAssertions.assertEventType(event2, EventType.ANALYTICS);
//		EventAssertions.assertEventSource(event2, EventSource.RESPONSE_IDENTITY);
//		EventAssertions.assertEventDataContains(event2, AID_KEY);
//
//		EventData analyticsShared = getAnalyticsSharedState();
//		assertEquals(2, analyticsShared.size());
//		assertEquals(expectedAid, analyticsShared.optString(AID_KEY, null));
//		assertEquals(expectedAid, aidValue[0]);
//	}
//
//	@Test
//	public void testAnalyticsModule_putsAIDinSharedState_when_boot() throws Exception {
//		// setup - set AID in persistence
//		final String expectedAid = "1A61B875A8D64420-0E969B4EEFD2EFB0";
//		setAidAndVidInLocalStorage(expectedAid, null);
//
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(2);
//
//		// test - this will create the boot get AID event
//		registrationLatch = new CountDownLatch(1);
//		eventHub.registerModuleWithCallback(AnalyticsExtension.class, new EventHub.RegisterModuleCallback() {
//			@Override
//			public void registered(Module module) {
//				registrationLatch.countDown();
//			}
//		});
//		registrationLatch.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
//		eventHub.finishModulesRegistration(new AdobeCallback() {
//			@Override
//			public void call(Object value) {
//
//			}
//		});
//
//		// verify shared state update event and aid value
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(2, eventHub.getEvents().size());
//		EventData analyticsShared = getAnalyticsSharedState();
//		assertNotNull("Analytics shared state should be updated at boot", analyticsShared);
//		assertEquals(2, analyticsShared.size());
//		assertEquals(expectedAid, analyticsShared.optString(AID_KEY, null));
//	}
//
//	@Test
//	public void testAnalyticsModule_hadNoAID_when_boot() throws Exception {
//		// setup - set AID in persistence
//		eventHub.ignoreAllStateChangeEvents();
//		eventHub.setExpectedEventCount(2);
//
//		// test - this will create the boot get AID event
//		registrationLatch = new CountDownLatch(1);
//		eventHub.registerModuleWithCallback(AnalyticsExtension.class, new EventHub.RegisterModuleCallback() {
//			@Override
//			public void registered(Module module) {
//				registrationLatch.countDown();
//			}
//		});
//		registrationLatch.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
//		eventHub.finishModulesRegistration(new AdobeCallback() {
//			@Override
//			public void call(Object value) {
//
//			}
//		});
//
//		// verify shared state update event and aid value
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		assertEquals(2, eventHub.getEvents().size());
//		EventData analyticsShared = getAnalyticsSharedState();
//		assertNotNull("Analytics shared state should be updated at boot", analyticsShared);
//		assertEquals(2, analyticsShared.size());
//		assertNull(analyticsShared.optString(AID_KEY, null));
//	}
//
//	@Test
//	public void testAnalyticsModule_when_boot_and_LocalStorageService_NotInitialized_ShouldNotCrash() throws Exception {
//		// recreate eventHub with null LocalStorageService.
//		eventHub.shutdown();
//
//		// recreate eventHub and platformServices with null LocalStorageService.
//		platformServices = new CucumberTestPlatformServices();
//		platformServices.setFakeLocalStorageService(null);
//		FakeLoggingService loggingService = (FakeLoggingService) platformServices.getLoggingService();
//		Log.setLoggingService(loggingService);
//		Log.setLogLevel(LoggingMode.VERBOSE);
//		testableNetworkService = (TestableNetworkService) platformServices.getNetworkService();
//		platformServices.getMockSystemInfoService().applicationCacheDir = new File(this.getClass().getResource("").getPath() +
//				"test");
//		platformServices.getMockSystemInfoService().networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//		eventHub = new MockEventHubCucumberTest("eventhub", platformServices);
//
//		// setup the fake core implementation
//		testableCore = new AnalyticsCoreAPI(eventHub);
//		testHelper = new AnalyticsModuleTestsHelper(eventHub);
//		testHelper.setupForTracking();
//
//		// test - this will create the boot get AID event
//		registrationLatch = new CountDownLatch(1);
//		eventHub.registerModuleWithCallback(AnalyticsExtension.class, new EventHub.RegisterModuleCallback() {
//			@Override
//			public void registered(Module module) {
//				registrationLatch.countDown();
//			}
//		});
//		registrationLatch.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
//		eventHub.finishModulesRegistration(new AdobeCallback() {
//			@Override
//			public void call(Object value) {
//
//			}
//		});
//
//		// verify shared state update event and aid value
//		waitForThreadsWithFailIfTimedOut(TEST_TIMEOUT);
//		EventData analyticsShared = getAnalyticsSharedState();
//		assertNotNull("Analytics shared state should be updated at boot", analyticsShared);
//		assertEquals(2, analyticsShared.size());
//		assertNull(analyticsShared.optString(AID_KEY, null));
//		assertNull(analyticsShared.optString(VID_KEY, null));
//		// verify error logs
//		assertTrue(loggingService.containsWarningLog("AnalyticsExtension",
//				   "handleAnalyticsIdentityRequest - Unable to get AID from persistence. LocalStorage Service not initialized."));
//	}
//
//	@Test
//	public void testGenericResetIdentifier_shouldClearAIDVID() throws Exception {
//		// setup
//		final String expectedAid = "AID-123";
//		final String expectedVid = "VID-123";
//		setAidAndVidInLocalStorage(expectedAid, expectedVid);
//
//		registrationLatch = new CountDownLatch(1);
//		eventHub.registerModuleWithCallback(AnalyticsExtension.class, new EventHub.RegisterModuleCallback() {
//			@Override
//			public void registered(Module module) {
//				registrationLatch.countDown();
//			}
//		});
//		registrationLatch.await(EVENTHUB_WAIT_MS, TimeUnit.MILLISECONDS);
//		eventHub.finishModulesRegistration(new AdobeCallback() {
//			@Override
//			public void call(Object value) {
//
//			}
//		});
//
//		eventHub.ignoreAllEvents();
//
//		final String[] idValue = new String[2];
//		final CountDownLatch latch1 = new CountDownLatch(1);
//		final CountDownLatch latch2 = new CountDownLatch(1);
//		AdobeCallback<String> adobeAIDCallback = new AdobeCallback<String>() {
//			@Override
//			public void call(String value) {
//				idValue[0] = value;
//				latch1.countDown();
//			}
//		};
//
//		AdobeCallback<String> adobeVIDCallback = new AdobeCallback<String>() {
//			@Override
//			public void call(String value) {
//				idValue[1] = value;
//				latch2.countDown();
//			}
//		};
//
//		testableCore.getTrackingIdentifier(adobeAIDCallback);
//		latch1.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
//		assertEquals(expectedAid, idValue[0]);
//
//		testableCore.getVisitorIdentifier(adobeVIDCallback);
//		latch2.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
//		assertEquals(expectedVid, idValue[1]);
//
//
//		Event genericResetEvent = new Event.Builder(null, EventType.GENERIC_IDENTITY, EventSource.REQUEST_RESET)
//		.build();
//		eventHub.dispatch(genericResetEvent);
//		waitForThreadsWithFailIfTimedOut(5000);
//
//		final String[] resetIdValue = new String[2];
//		final CountDownLatch latch3 = new CountDownLatch(1);
//		final CountDownLatch latch4 = new CountDownLatch(1);
//		AdobeCallback<String> aidCallbackAfterReset = new AdobeCallback<String>() {
//			@Override
//			public void call(String value) {
//				resetIdValue[0] = value;
//				latch3.countDown();
//			}
//		};
//
//		AdobeCallback<String> vidCallbackAfterReset = new AdobeCallback<String>() {
//			@Override
//			public void call(String value) {
//				resetIdValue[1] = value;
//				latch4.countDown();
//			}
//		};
//
//		testableCore.getTrackingIdentifier(aidCallbackAfterReset);
//		latch3.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
//		assertNull(resetIdValue[0]);
//
//		testableCore.getVisitorIdentifier(vidCallbackAfterReset);
//		latch4.await(TEST_TIMEOUT, TimeUnit.MILLISECONDS);
//		assertNull(resetIdValue[1]);
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
//	private void setAidAndVidInLocalStorage(final String aid, final String vid) {
//		final FakeDataStore dataStore = new FakeDataStore();
//		dataStore.setString("ADOBEMOBILE_STOREDDEFAULTS_AID", aid);
//		dataStore.setString("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER", vid);
//		((FakeLocalStorageService)platformServices.getLocalStorageService()).mapping = new
//		HashMap<String, LocalStorageService.DataStore>() {
//			{
//				put("AnalyticsDataStorage", dataStore);
//			}
//		};
//	}
//}
