///* **************************************************************************
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
// **************************************************************************/
//
//package com.adobe.marketing.mobile;
//
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.File;
//import java.util.*;
//
//import static org.junit.Assert.*;
//
//public class AnalyticsTests extends BaseTest {
//
//	private static final String IDENTITY_SHARED_STATE                          = "com.adobe.module.identity";
//	private static final String CONFIGURATION_SHARED_STATE                     = "com.adobe.module.configuration";
//	private static final String LIFECYCLE_SHARED_STATE                         = "com.adobe.module.lifecycle";
//	private static final String PLACES_SHARED_STATE							   = "com.adobe.module.places";
//	private static final String ASSURANCE_SHARED_STATE                         = "com.adobe.assurance";
//	private static final String ACQUISITION_SHARED_STATE                       = "com.adobe.module.analytics.acquisition";
//	private static final String CONTEXT_DATA_KEY_PREV_SESSION_LENGTH           = "a.PrevSessionLength";
//	private static final String CONTEXT_DATA_KEY_CRASH_EVENT                   = "a.CrashEvent";
//	private static final String CONTEXT_DATA_VALUE_CRASH_EVENT                 = "CrashEvent";
//	private static final String CONTEXT_DATA_KEY_IGNORED_SESSION_LENGTH        = "a.ignoredSessionLength";
//	private static final String LIFECYCLE_EVENT_CONTEXT_DATA_KEY               = "lifecyclecontextdata";
//	private static final String DATASTORE_KEY_MOST_RECENT_HIT_TIMESTAMP_MILLIS = "mostRecentHitTimestampMillis";
//	private static final String INTERNAL_REQUEST_KEY                           = "trackinternal";
//	private static final String CONTEXT_DATA_KEY                           = "contextdata";
//	private static final String TRACK_ACTION                           = "action";
//	private static final String LIFECYCLE_INTERNAL_ACTION_NAME            = "Lifecycle";
//	static final String LIFECYCLE_ACTION_KEY = "action";
//	static final String LIFECYCLE_START = "start";
//	static final String LIFECYCLE_PAUSE = "pause";
//	static final String INSTALL_EVENT_KEY         = "a.InstallEvent";
//	static final String LAUNCH_EVENT_KEY          = "a.LaunchEvent";
//
//
//	private MockAnalyticsDispatcherAnalyticsResponseContent  mockDispatcherAnalyticsResponseContent;
//	private MockAnalyticsDispatcherAnalyticsResponseIdentity mockDispatcherAnalyticsResponseIdentity;
//	private TestableAnalytics                                module;
//	private MockAnalyticsRequestSerializer                   mockRequestBuilder;
//	private AnalyticsState                                   state;
//	private FakeLoggingService                               fakeLoggingService;
//	private MockAnalyticsHitsDatabase                        mockHitsDB;
//	private FakeDataStore                                    fakeDataStore;
//	private MockNetworkService                               mockNetworkService;
//	private MockSystemInfoService                            fakeSystemInfo;
//	private AnalyticsProperties                              properties;
//	private List<String> expectedHardDep = new ArrayList<String>();
//	private List<String> expectedSoftDep = new ArrayList<String>();
//
//	@Before
//	public void testSetup() throws Exception {
//		super.beforeEach();
//		module = new TestableAnalytics(eventHub, platformServices);
//		properties = new AnalyticsProperties();
//		module.analyticsProperties = properties;
//		setupSharedState();
//		setupAnalyticsConfig();
//		fakeSystemInfo = platformServices.getMockSystemInfoService();
//		fakeSystemInfo.applicationCacheDir = new File(this.getClass().getResource("").getPath() + "test");
//		fakeDataStore = (FakeDataStore) platformServices.getLocalStorageService().getDataStore("AnalyticsDataStorage");
//		mockNetworkService = platformServices.getMockNetworkService();
//		fakeLoggingService = (FakeLoggingService) platformServices.getLoggingService();
//		fakeLoggingService.setIgnoreRegisteringErrors();
//		Log.setLoggingService(fakeLoggingService);
//		Log.setLogLevel(LoggingMode.VERBOSE);
//		mockHitsDB = new MockAnalyticsHitsDatabase(platformServices, properties, null);
//		mockRequestBuilder = new MockAnalyticsRequestSerializer();
//		mockDispatcherAnalyticsResponseContent = (MockAnalyticsDispatcherAnalyticsResponseContent)
//				module.analyticsResponseDispatcher;
//		mockDispatcherAnalyticsResponseIdentity = (MockAnalyticsDispatcherAnalyticsResponseIdentity)
//				module.analyticsIdentityResponseDispatcher;
//
//		module.setHelpers(mockHitsDB, properties, mockRequestBuilder);
//		expectedHardDep.add(CONFIGURATION_SHARED_STATE);
//		expectedHardDep.add(IDENTITY_SHARED_STATE);
//		expectedSoftDep.add(PLACES_SHARED_STATE);
//		expectedSoftDep.add(ASSURANCE_SHARED_STATE);
//		expectedSoftDep.add(LIFECYCLE_SHARED_STATE);
//	}
//
//	@Test
//	public void testUpdatePrivacyStatus_kicksAnalyticsHitsIsCalled_When_PrivacyStatusIsOptIn() throws Exception {
//		module.analyticsProperties.setAid("testAid");
//		module.analyticsProperties.setVid("testVid");
//		module.analyticsUnprocessedEvents.add(
//			new AnalyticsUnprocessedEvent(
//				new Event.Builder("AnalyticsTest", EventType.ANALYTICS, EventSource.REQUEST_IDENTITY).build(),
//				null, null));
//
//		AnalyticsState state = new AnalyticsState(null);
//		state.setPrivacyStatus(MobilePrivacyStatus.OPT_IN);
//		module.updatePrivacyStatus(0, state);
//		assertTrue(mockHitsDB.kickWasCalled);
//		assertFalse(module.clearAllHitsWasCalled);
//		assertFalse(module.hasSharedEventState(AnalyticsTestConstants.EventDataKeys.Analytics.EXTENSION_NAME));
//		assertEquals("testAid", module.analyticsProperties.getAid());
//		assertEquals("testVid", module.analyticsProperties.getVid());
//		assertEquals(1, module.analyticsUnprocessedEvents.size());
//	}
//
//	@Test
//	public void testUpdatePrivacyStatus_clearAllAnalyticsHitsIsCalled_When_PrivacyStatusIsOptOut() throws Exception {
//		module.analyticsProperties.setAid("testAid");
//		module.analyticsProperties.setVid("testVid");
//		module.analyticsUnprocessedEvents.add(
//			new AnalyticsUnprocessedEvent(
//				new Event.Builder("AnalyticsTest", EventType.ANALYTICS, EventSource.REQUEST_IDENTITY).build(),
//				null, null));
//
//		AnalyticsState state = new AnalyticsState(null);
//		state.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
//		module.updatePrivacyStatus(0, state);
//		assertTrue(module.clearAllHitsWasCalled);
//		assertFalse(mockHitsDB.kickWasCalled);
//		assertEquals(null, module.analyticsProperties.getAid());
//		assertEquals(null, module.analyticsProperties.getVid());
//		assertEquals(0, module.analyticsUnprocessedEvents.size());
//		EventData sharedState = module.getSharedEventState(AnalyticsTestConstants.EventDataKeys.Analytics.EXTENSION_NAME,
//								Event.SHARED_STATE_NEWEST);
//		assertTrue(sharedState.isEmpty());
//	}
//
//	@Test
//	public void testUpdatePrivacyStatus_nothingHappens_When_PrivacyStatusIsUnknown() throws Exception {
//		module.analyticsProperties.setAid("testAid");
//		module.analyticsProperties.setVid("testVid");
//		module.analyticsUnprocessedEvents.add(
//			new AnalyticsUnprocessedEvent(
//				new Event.Builder("AnalyticsTest", EventType.ANALYTICS, EventSource.REQUEST_IDENTITY).build(),
//				null, null));
//
//		AnalyticsState state = new AnalyticsState(null);
//		state.setPrivacyStatus(MobilePrivacyStatus.UNKNOWN);
//		module.updatePrivacyStatus(0, state);
//		assertFalse(module.clearAllHitsWasCalled);
//		assertFalse(mockHitsDB.kickWasCalled);
//		assertFalse(module.hasSharedEventState(AnalyticsTestConstants.EventDataKeys.Analytics.EXTENSION_NAME));
//		assertEquals("testAid", module.analyticsProperties.getAid());
//		assertEquals("testVid", module.analyticsProperties.getVid());
//		assertEquals(1, module.analyticsUnprocessedEvents.size());
//	}
//
//	@Test
//	public void purgeAid() {
//		module.analyticsProperties.setAid("testAid");
//		LocalStorageService.DataStore store = platformServices.getLocalStorageService().getDataStore(
//				AnalyticsTestConstants.ANALYTICS_DATA_STORAGE);
//		store.setString(AnalyticsTestConstants.DataStoreKeys.AID_KEY, "testAid");
//
//		module.purgeAid();
//
//		assertNull(module.analyticsProperties.getAid());
//		assertFalse(store.contains(AnalyticsTestConstants.DataStoreKeys.AID_KEY));
//	}
//
//
//	@Test
//	public void testTrack_NullAnalyticsRequest() throws Exception {
//		module.track(state, null, TimeUtil.getUnixTimeInSeconds(), false, "test-unique-id");
//
//		assertTrue(fakeLoggingService.containsDebugLog("AnalyticsExtension",
//				   "track - Dropping the Analytics track request, request was null."));
//	}
//
//	@Test
//	public void testTrack_DoesNothing_When_AnalyticsNotConfigured() throws Exception {
//		state.setServer(null);
//		EventData request = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION, "action");
//		module.track(state, request, TimeUtil.getUnixTimeInSeconds(), false, "test-unique-id");
//		FakeLoggingService fakeLoggingService = (FakeLoggingService) platformServices.getLoggingService();
//		assertTrue(fakeLoggingService.containsWarningLog("AnalyticsExtension",
//				   "track - Dropping the Analytics track request, Analytics is not configured."));
//		assertFalse(mockRequestBuilder.methodBuildRequestCalled);
//	}
//
//	@Test
//	public void testTrack_DoesNothing_When_PrivacyOptOut() throws Exception {
//		state.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
//		EventData request = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION, "action");
//		module.track(state, request, TimeUtil.getUnixTimeInSeconds(), false, "test-unique-id");
//		FakeLoggingService fakeLoggingService = (FakeLoggingService) platformServices.getLoggingService();
//		assertTrue(fakeLoggingService.containsWarningLog("AnalyticsExtension",
//				   "track - Dropping the Analytics track request, privacy status is opted out."));
//		assertFalse(mockRequestBuilder.methodBuildRequestCalled);
//	}
//
//	@Test
//	public void testGetAID_ReturnsNull_When_NoValueInDataStore() {
//		module.handleAnalyticsIdentityRequest("pairID", 0);
//		assertFalse(mockNetworkService.connectUrlWasCalled);
//		assertTrue(mockDispatcherAnalyticsResponseIdentity.methodDispatchCalled);
//		assertEquals(null, mockDispatcherAnalyticsResponseIdentity.dispatchParamsAid.get(0));
//	}
//
//	@Test
//	public void testGetAIDReturnsValueFromDataStore_WhenPresent() {
//		fakeDataStore.setString("ADOBEMOBILE_STOREDDEFAULTS_AID", "567F7BBCB65A4686-27C7E1B5B6C8C9E6");
//		module.handleAnalyticsIdentityRequest("pairID", 0);
//		assertFalse(mockNetworkService.connectUrlWasCalled);
//		assertEquals("567F7BBCB65A4686-27C7E1B5B6C8C9E6", mockDispatcherAnalyticsResponseIdentity.dispatchParamsAid.get(0));
//	}
//
//	@Test
//	public void testProcessAnalyticsIDRequest_AddsEventInQueue() {
//		// It should add the correct event in the unprocessed queued events
//		Event testEvent = new Event.Builder("TEST", EventType.ANALYTICS, EventSource.REQUEST_IDENTITY).build();
//		module.processAnalyticsIDRequest(testEvent);
//		assertEquals(1, module.analyticsUnprocessedEvents.size());
//	}
//
//	@Test
//	public void testHandleAnalyticsRequest_DoesNotQueue_When_ClearAllHitsEvent() {
//		Event testEvent = new Event.Builder("TEST", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).setData(new EventData() {
//			{
//				putBoolean("clearhitsqueue", true);
//			}
//		}).build();
//		module.handleAnalyticsRequestEvent(testEvent);
//		assertEquals(0, module.analyticsUnprocessedEvents.size());
//		assertTrue(module.clearAllHitsWasCalled);
//	}
//
//	@Test
//	public void testHandleAnalyticsRequest_DoesNotQueue_When_GetQueueSizeEvent() {
//		Event testEvent = new Event.Builder("TEST", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).setData(new EventData() {
//			{
//				putBoolean("getqueuesize", true);
//			}
//		}).build();
//		module.handleAnalyticsRequestEvent(testEvent);
//		assertEquals(0, module.analyticsUnprocessedEvents.size());
//		assertTrue(module.getTrackingQueueSizeCalled);
//	}
//
//	@Test
//	public void testHandleAnalyticsRequest_QueuesUp_When_ForceKick() {
//		Event testEvent = new Event.Builder("TEST", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).setData(new EventData() {
//			{
//				putBoolean("forcekick", true);
//			}
//		}).build();
//		module.handleAnalyticsRequestEvent(testEvent);
//		assertEquals(1, module.analyticsUnprocessedEvents.size());
//	}
//
//	@Test
//	public void testHandleAnalyticsRequest_AddsEventInQueue_When_TrackRequestEvent() {
//		Event testEvent = new Event.Builder("TEST", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).setData(new EventData() {
//			{
//				putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE, "state");
//			}
//		}).build();
//		module.handleAnalyticsRequestEvent(testEvent);
//		assertEquals(1, module.analyticsUnprocessedEvents.size());
//	}
//
//	@Test
//	public void testHandleAnalyticsRequest_AddsEventInQueue_When_TrackInternalRequestEvent() {
//		Event testEvent = new Event.Builder("TEST", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).setData(new EventData() {
//			{
//				putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE, "state");
//				putBoolean(INTERNAL_REQUEST_KEY, true);
//			}
//		}).build();
//		module.handleAnalyticsRequestEvent(testEvent);
//		assertEquals(1, module.analyticsUnprocessedEvents.size());
//	}
//
//	@Test
//	public void testGetTrackingQueueSize_DispatchesAnalyticsResponseEvent() {
//		module.putEvent(new Event.Builder("TEST", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build(), null, null);
//		module.getTrackingQueueSize("testPairID");
//		assertTrue(mockDispatcherAnalyticsResponseContent.dispatchQueueSizeWasCalled);
//		assertEquals("testPairID", mockDispatcherAnalyticsResponseContent.dispatchQueueSizeParamPairID);
//		assertEquals(1, mockDispatcherAnalyticsResponseContent.dispatchQueueSizeParamSize);
//	}
//
//	@Test
//	public void testGetTrackingQueueSize_ReturnsDatabaseHitsNoPlusQueuedEventsNo() {
//		mockHitsDB.getTrackingQueueSizeReturnValue = 2;
//		module.putEvent(new Event.Builder("TEST", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build(), null, null);
//		module.getTrackingQueueSize("testPairID");
//		assertEquals(3, mockDispatcherAnalyticsResponseContent.dispatchQueueSizeParamSize);
//	}
//
//	@Test
//	public void testClearAllHits_ClearsBothDatabaseHitsAndQueuedEvents() {
//		module.putEvent(new Event.Builder("TEST", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build(), null, null);
//		module.clearAllHits();
//		assertEquals(0, module.analyticsUnprocessedEvents.size());
//		assertTrue(mockHitsDB.clearWasCalled);
//	}
//
//	@Test
//	public void testProcess_LogsError_when_NullEvent() throws Exception {
//		module.process(null, null);
//		assertTrue(fakeLoggingService.containsDebugLog("AnalyticsExtension",
//				   "process - Failed to process this event; invalid event or null data"));
//	}
//
//	@Test
//	public void testProcess_LogsError_when_NullEventData() throws Exception {
//		module.process(new Event.Builder("test", EventType.ANALYTICS, EventSource.REQUEST_IDENTITY).setData(null).build(),
//					   null);
//		assertTrue(fakeLoggingService.containsDebugLog("AnalyticsExtension",
//				   "process - Failed to process this event; invalid event or null data"));
//	}
//
//	@Test
//	public void testProcess_CallsTrack_When_TrackRequestEvent() throws Exception {
//		EventData config = new EventData();
//		config.putString("analytics.server", "server");
//		config.putString("analytics.rsids", "132312");
//		config.putInteger("analytics.batchLimit", 3);
//		Map<String, EventData> sharedData = new HashMap<String, EventData>();
//		sharedData.put(CONFIGURATION_SHARED_STATE, config);
//		EventData request = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION, "action");
//		Event testEvent = new Event.Builder("test", EventType.GENERIC_TRACK,
//											EventSource.REQUEST_CONTENT).setData(request).build();
//		module.process(testEvent, sharedData);
//		assertTrue(mockHitsDB.queueWasCalled);
//	}
//
//	@Test
//	public void testProcess_CallsTrack_When_TrackInternalEvent() throws Exception {
//		EventData config = new EventData();
//		config.putString("analytics.server", "server");
//		config.putString("analytics.rsids", "132312");
//		config.putInteger("analytics.batchLimit", 3);
//		Map<String, EventData> sharedData = new HashMap<String, EventData>();
//		sharedData.put(CONFIGURATION_SHARED_STATE, config);
//		EventData request = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION, "action")
//		.putBoolean(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_INTERNAL, true);
//		Event testEvent = new Event.Builder("test", EventType.ANALYTICS,
//											EventSource.REQUEST_CONTENT).setData(request).build();
//
//		module.process(testEvent, sharedData);
//		assertTrue(mockHitsDB.queueWasCalled);
//	}
//
//	@Test
//	public void testProcess_CallsHandleAIDRequest_When_AIDRequest() throws Exception {
//		state.setServer(null);
//		Event testEvent = new Event.Builder("test", EventType.ANALYTICS, EventSource.REQUEST_IDENTITY).build();
//		module.process(testEvent, new HashMap<String, EventData>());
//		assertTrue(mockDispatcherAnalyticsResponseIdentity.methodDispatchCalled);
//	}
//
//	@Test
//	public void testProcess_CallsForceKick_When_ForceKickRequest() throws Exception {
//		Event testEvent = new Event.Builder("test", EventType.ANALYTICS, EventSource.REQUEST_CONTENT)
//		.setData(new EventData().putBoolean("forcekick", true)).build();
//		module.process(testEvent, new HashMap<String, EventData>());
//		assertTrue(mockHitsDB.forceKickWasCalled);
//	}
//
//	@Test
//	public void trackLifecycle_SessionInfoAndCrashEvents_BackdateDisabled() throws Exception {
//		module.trackCallRealMethod = false;
//		state.setBackdateSessionInfoEnabled(false);
//		long mostRecentHitTimestamp = TimeUtil.getUnixTimeInSeconds();
//		fakeDataStore.setLong(DATASTORE_KEY_MOST_RECENT_HIT_TIMESTAMP_MILLIS, mostRecentHitTimestamp);
//
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData()
//		.putStringMap(LIFECYCLE_EVENT_CONTEXT_DATA_KEY, new HashMap<String, String>() {
//			{
//				put("prevsessionlength", "3000");
//				put("crashevent", CONTEXT_DATA_VALUE_CRASH_EVENT);
//				put("key", "value");
//			}
//		}))
//		.build();
//
//		module.trackLifecycle(state, lifecycleEvent);
//
//		assertEquals(1, module.trackMethodParameters.size());
//		TestableAnalytics.TrackMethodParameters trackLifecycle = module.trackMethodParameters.get(0);
//		assertTrue(trackLifecycle.request.optBoolean(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_INTERNAL, false));
//		assertEquals(state, trackLifecycle.state);
//
//		EventData expectedLifecycleRequest = new EventData()
//		.putString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION, "Lifecycle")
//		.putStringMap(AnalyticsTestConstants.EventDataKeys.Analytics.CONTEXT_DATA, new HashMap<String, String>() {
//			{
//				put("key", "value");
//				put(CONTEXT_DATA_KEY_PREV_SESSION_LENGTH, "3000");
//				put(CONTEXT_DATA_KEY_CRASH_EVENT, CONTEXT_DATA_VALUE_CRASH_EVENT);
//			}
//		});
//		AnalyticsAssertions.assertAnalyticsTrackDataEquals(expectedLifecycleRequest, trackLifecycle.request);
//		assertEquals(lifecycleEvent.getTimestampInSeconds(), trackLifecycle.timestamp);
//	}
//
//	@Test
//	public void trackLifecycle_SessionInfoEvent() throws Exception {
//		module.trackCallRealMethod = false;
//		state.setBackdateSessionInfoEnabled(true);
//		state.setOfflineEnabled(true);
//
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData()
//		.putStringMap(LIFECYCLE_EVENT_CONTEXT_DATA_KEY, new HashMap<String, String>() {
//			{
//				put("prevsessionlength", "3000");
//			}
//		}))
//		.build();
//
//		module.trackLifecycle(state, lifecycleEvent);
//
//		assertEquals(2, module.trackMethodParameters.size());
//	}
//
//	@Test
//	public void trackLifecycle_SessionInfoEvent_IgnoredSessionLength() throws Exception {
//		module.trackCallRealMethod = false;
//		state.setBackdateSessionInfoEnabled(true);
//		state.setOfflineEnabled(true);
//
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData()
//		.putStringMap(LIFECYCLE_EVENT_CONTEXT_DATA_KEY, new HashMap<String, String>() {
//			{
//				put("ignoredsessionlength", "3000");
//			}
//		}))
//		.build();
//
//		module.trackLifecycle(state, lifecycleEvent);
//
//		assertEquals(1, module.trackMethodParameters.size());
//		assertEquals("Lifecycle", module.trackMethodParameters.get(0).request.optString("action", null));
//	}
//
//	@Test
//	public void trackLifecycle_CrashEvent() throws Exception {
//		module.trackCallRealMethod = false;
//		state.setBackdateSessionInfoEnabled(true);
//		state .setOfflineEnabled(true);
//
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData()
//		.putStringMap(LIFECYCLE_EVENT_CONTEXT_DATA_KEY, new HashMap<String, String>() {
//			{
//				put("key", "value");
//				put("crashevent", CONTEXT_DATA_VALUE_CRASH_EVENT);
//			}
//		}))
//		.build();
//
//		module.trackLifecycle(state, lifecycleEvent);
//
//		assertEquals(2, module.trackMethodParameters.size());
//	}
//
//	@Test
//	public void trackLifecycle_BackdateSessionInfoDisabled() throws Exception {
//		module.trackCallRealMethod = false;
//		state.setBackdateSessionInfoEnabled(false);
//
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData()
//		.putStringMap(LIFECYCLE_EVENT_CONTEXT_DATA_KEY, new HashMap<String, String>() {
//			{
//				put("prevsessionlength", "3000");
//			}
//		}))
//		.build();
//
//		module.trackLifecycle(state, lifecycleEvent);
//
//		assertEquals(1, module.trackMethodParameters.size());
//	}
//
//	@Test
//	public void trackLifecycle_NoCrashEvent_NoSessionInfoEvent() throws Exception {
//		module.trackCallRealMethod = false;
//		state.setBackdateSessionInfoEnabled(true);
//
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData()
//		.putStringMap(LIFECYCLE_EVENT_CONTEXT_DATA_KEY, new HashMap<String, String>() {
//			{
//				put("key", "value");
//			}
//		}))
//		.build();
//
//		module.trackLifecycle(state, lifecycleEvent);
//
//		assertEquals(1, module.trackMethodParameters.size());
//	}
//
//	@Test
//	public void trackLifecycle_LifecycleContextDataNull() throws Exception {
//		module.trackCallRealMethod = false;
//		state.setBackdateSessionInfoEnabled(true);
//
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT).build();
//
//		module.trackLifecycle(state, lifecycleEvent);
//
//		assertEquals(0, module.trackMethodParameters.size());
//	}
//
//	@Test
//	public void trackLifecycle_LifecycleContextDataEmpty() throws Exception {
//		module.trackCallRealMethod = false;
//		state.setBackdateSessionInfoEnabled(true);
//
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData()
//				 .putStringMap(LIFECYCLE_EVENT_CONTEXT_DATA_KEY, new HashMap<String, String>()))
//		.build();
//
//		module.trackLifecycle(state, lifecycleEvent);
//
//		assertEquals(0, module.trackMethodParameters.size());
//	}
//
//	@Test
//	public void trackLifecycle_StateIsNull() throws Exception {
//		module.trackCallRealMethod = false;
//		state.setBackdateSessionInfoEnabled(true);
//
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData().putStringMap(LIFECYCLE_EVENT_CONTEXT_DATA_KEY, new HashMap<String, String>() {
//			{
//				put(CONTEXT_DATA_KEY_PREV_SESSION_LENGTH, "3000");
//			}
//		})).build();
//
//		module.trackLifecycle(null, lifecycleEvent);
//
//		assertEquals(0, module.trackMethodParameters.size());
//	}
//
//	@Test
//	public void trackLifecycle_InstallEventTriggerReferrerTimer() {
//		module.trackCallRealMethod = false;
//
//		state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(CONFIGURATION_SHARED_STATE, new EventData().putInteger("analytics.launchHitDelay", 1));
//			}
//		});
//
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData().putStringMap(LIFECYCLE_EVENT_CONTEXT_DATA_KEY, new HashMap<String, String>() {
//			{
//				put("installevent", INSTALL_EVENT_KEY);
//			}
//		})).build();
//
//		module.trackLifecycle(state, lifecycleEvent);
//
//		assertTrue(properties.getReferrerTimerState().isTimerRunning());
//	}
//
//	@Test
//	public void trackLifecycle_LaunchEventTriggerReferrerTimer() {
//		module.trackCallRealMethod = false;
//
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData().putStringMap(LIFECYCLE_EVENT_CONTEXT_DATA_KEY, new HashMap<String, String>() {
//			{
//				put("launchevent", LAUNCH_EVENT_KEY);
//			}
//		})).build();
//
//		module.trackLifecycle(state, lifecycleEvent);
//
//		assertTrue(properties.getReferrerTimerState().isTimerRunning());
//	}
//
//	@Test
//	public void trackLifecycle_OtherEventDoNothing() {
//		module.trackCallRealMethod = false;
//
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData().putStringMap(LIFECYCLE_EVENT_CONTEXT_DATA_KEY, new HashMap<String, String>() {
//			{
//				put("Test", "Test");
//			}
//		})).build();
//
//		module.trackLifecycle(state, lifecycleEvent);
//
//		assertFalse(properties.getReferrerTimerState().isTimerRunning());
//	}
//
//	@Test
//	public void trackLifecycle_CancelTheLifecycleTimerIfItIsRunning() {
//		module.trackCallRealMethod = false;
//		mockHitsDB.isHitWaitingReturnValue = true;
//		properties.getLifecycleTimerState().startTimer(1000, null);
//
//
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData().putStringMap(LIFECYCLE_EVENT_CONTEXT_DATA_KEY, new HashMap<String, String>() {
//			{
//				put("launchevent", LAUNCH_EVENT_KEY);
//			}
//		})).build();
//
//		module.trackLifecycle(state, lifecycleEvent);
//
//		assertTrue(properties.getReferrerTimerState().isTimerRunning());
//		assertFalse(properties.getLifecycleTimerState().isTimerRunning());
//		assertTrue(mockHitsDB.kickWithAdditionalDataWasCalled);
//	}
//
//	@Test
//	public void testHandleLifecycleEvent_putsEventInQueueWithRightDependencies() throws Exception {
//		// Test
//		module.handleLifecycleResponseEvent(new Event.Builder("Test", EventType.LIFECYCLE,
//											EventSource.RESPONSE_CONTENT).build());
//		assertEquals(1, module.analyticsUnprocessedEvents.size());
//		AnalyticsUnprocessedEvent event = module.analyticsUnprocessedEvents.peek();
//		assertEquals(expectedHardDep, event.getHardStateDependencies());
//		assertEquals(expectedSoftDep, event.getSoftStateDependencies());
//	}
//
//	@Test
//	public void testHandleAcquisitionResponseEvent_callsKickWithReferrer_happy() throws Exception {
//		properties.getReferrerTimerState().startTimer(1000, null);
//		Map<String, String> referrerData = new HashMap<String, String>();
//		referrerData.put("key", "value");
//		Event acquisitionEvent = new Event.Builder("test", EventType.ACQUISITION, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData().putStringMap("contextdata", referrerData)).build();
//		// Test
//		module.handleAcquisitionResponseEvent(acquisitionEvent);
//		assertTrue(mockHitsDB.kickWithAdditionalDataWasCalled);
//		assertNull(mockHitsDB.kickWithAdditionalDataParamState);
//		assertEquals(referrerData, mockHitsDB.kickWithAdditionalDataParamData);
//	}
//
//	@Test
//	public void testHandleAcquisitionResponseEvent_callsKickWithReferrer_when_NullData() throws Exception {
//		properties.getReferrerTimerState().startTimer(1000, null);
//
//
//		Event acquisitionEvent = new Event.Builder("test", EventType.ACQUISITION, EventSource.RESPONSE_CONTENT)
//		.setData(null).build();
//		// Test
//		module.handleAcquisitionResponseEvent(acquisitionEvent);
//		assertTrue(mockHitsDB.kickWithAdditionalDataWasCalled);
//		assertNull(mockHitsDB.kickWithAdditionalDataParamData);
//	}
//
//	@Test
//	public void testHandleAcquisitionResponseEvent_addEventToTheQueue_when_timerIsNotRunning() throws Exception {
//		properties.getReferrerTimerState().cancel();
//
//		Event acquisitionEvent = new Event.Builder("test", EventType.ACQUISITION, EventSource.RESPONSE_CONTENT)
//		.setData(null).build();
//		// Test
//		module.handleAcquisitionResponseEvent(acquisitionEvent);
//
//		assertTrue(module.processEventsWasCalled);
//	}
//
//
//	@Test
//	public void testHandleConfigurationUpdateEvent_queuesEventWithDependencies_whenValidEvent() throws Exception {
//		EventData data = new EventData().putString("global.privacy", "optedin");
//		Event event = new Event.Builder("AnalyticsTest", EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT)
//		.setData(data)
//		.build();
//		module.handleConfigurationUpdateEvent(event);
//		assertTrue(module.processEventsWasCalled);
//		assertEquals(1, module.analyticsUnprocessedEvents.size());
//		AnalyticsUnprocessedEvent unprocessedEvent = module.analyticsUnprocessedEvents.peek();
//		assertEquals(expectedHardDep, unprocessedEvent.getHardStateDependencies());
//		assertEquals(expectedSoftDep, unprocessedEvent.getSoftStateDependencies());
//	}
//
//	@Test
//	public void testHandleConfigurationUpdateEvent_ignoresEvent_whenNullEvent() throws Exception {
//		module.handleConfigurationUpdateEvent(null);
//		assertFalse(module.processEventsWasCalled);
//		assertEquals(0, module.analyticsUnprocessedEvents.size());
//	}
//
//	@Test
//	public void testHandleConfigurationUpdateEvent_ignoresEvent_whenNullData() throws Exception {
//		Event event = new Event.Builder("AnalyticsTest", EventType.CONFIGURATION, EventSource.RESPONSE_CONTENT)
//		.setData(null)
//		.build();
//		module.handleConfigurationUpdateEvent(event);
//		assertFalse(module.processEventsWasCalled);
//		assertEquals(0, module.analyticsUnprocessedEvents.size());
//	}
//
//	@Test
//	public void testHandleGenericResetEvent_queuesEventWithNoDependencies_processedImmediately() {
//		Event event = new Event.Builder("AnalyticsTest", EventType.GENERIC_IDENTITY, EventSource.REQUEST_RESET)
//		.build();
//		module.handleGenericResetEvent(event);
//		assertTrue(module.processEventsWasCalled);
//		assertEquals(0, module.analyticsUnprocessedEvents.size());
//		assertTrue(module.clearAllHitsWasCalled);
//	}
//
//	@Test
//	public void testHandleSharedStateUpdateEvent_callsProcessEvents_when_hardDependency() {
//		module.handleSharedStateUpdateEvent(CONFIGURATION_SHARED_STATE);
//		assertTrue(module.processEventsWasCalled);
//	}
//
//	@Test
//	public void testHandleSharedStateUpdateEvent_doesNOTCallProcessEvents_when_NOHardDependency() {
//		module.handleSharedStateUpdateEvent("testingModuleSharedState");
//		assertFalse(module.processEventsWasCalled);
//	}
//
//	@Test
//	public void testTrackAcquisition_stateIsNull() {
//		module.trackAcquisition(null, null);
//		assertFalse(mockHitsDB.kickWithAdditionalDataWasCalled);
//		assertEquals(0, module.trackMethodParameters.size());
//	}
//
//	@Test
//	public void testTrackAcquisition_timerIsRunning()  {
//		properties.getReferrerTimerState().startTimer(100, null);
//		state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(IDENTITY_SHARED_STATE, new EventData().putString("mid", "MID"));
//			}
//		});
//
//		Event acquisitionEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData()
//		.putStringMap(CONTEXT_DATA_KEY, new HashMap<String, String>() {
//			{
//				put("key", "value");
//			}
//		}))
//		.build();
//		module.trackAcquisition(state, acquisitionEvent);
//
//		assertFalse(properties.getReferrerTimerState().isTimerRunning());
//		assertTrue(mockHitsDB.kickWithAdditionalDataWasCalled);
//		assertEquals(state, mockHitsDB.kickWithAdditionalDataParamState);
//		assertEquals(1, mockHitsDB.kickWithAdditionalDataParamData.size());
//
//	}
//
//	@Test
//	public void testTrackAcquisition_timerIsNotRunning()  {
//		module.trackCallRealMethod = false;
//		state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(IDENTITY_SHARED_STATE, new EventData().putString("mid", "MID"));
//			}
//		});
//
//		Event acquisitionEvent = new Event.Builder(null, EventType.ACQUISITION, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData()
//		.putStringMap(CONTEXT_DATA_KEY, new HashMap<String, String>() {
//			{
//				put("key", "value");
//			}
//		}))
//		.build();
//		module.trackAcquisition(state, acquisitionEvent);
//
//		assertFalse(properties.getReferrerTimerState().isTimerRunning());
//		assertEquals(1, module.trackMethodParameters.size());
//		assertTrue(module.trackMethodParameters.get(0).request.optBoolean(INTERNAL_REQUEST_KEY, false));
//		assertEquals(1, module.trackMethodParameters.get(0).request.optStringMap(CONTEXT_DATA_KEY, null).size());
//		assertEquals("AdobeLink", module.trackMethodParameters.get(0).request.optString(TRACK_ACTION, null));
//
//	}
//
//	@Test
//	public void processLifecycleRequest_startEventWillStartTimerAndInsertPlaceholderHit()  {
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData()
//				 .putString(LIFECYCLE_ACTION_KEY, LIFECYCLE_START))
//		.build();
//		module.processLifecycleRequest(state, lifecycleEvent);
//
//		assertTrue(properties.getLifecycleTimerState().isTimerRunning());
//		assertTrue(mockHitsDB.queueWasCalled);
//		assertFalse(mockHitsDB.queueParamShouldBeWaiting);
//		assertTrue(mockHitsDB.queueParamIsPlaceholder);
//		assertEquals("", mockHitsDB.queueParamUrl);
//
//
//	}
//
//	@Test
//	public void processLifecycleRequest_startEventWhenTimerIsRunning()  {
//		properties.getLifecycleTimerState().startTimer(100, null);
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData()
//				 .putString(LIFECYCLE_ACTION_KEY, LIFECYCLE_START))
//		.build();
//		module.processLifecycleRequest(state, lifecycleEvent);
//
//		assertTrue(properties.getLifecycleTimerState().isTimerRunning());
//		assertFalse(mockHitsDB.queueWasCalled);
//
//	}
//
//	@Test
//	public void processLifecycleRequest_endEvent()  {
//		properties.getLifecycleTimerState().startTimer(100, null);
//		properties.getReferrerTimerState().startTimer(100, null);
//		Event lifecycleEvent = new Event.Builder(null, EventType.LIFECYCLE, EventSource.RESPONSE_CONTENT)
//		.setData(new EventData()
//				 .putString(LIFECYCLE_ACTION_KEY, LIFECYCLE_PAUSE))
//		.build();
//		module.processLifecycleRequest(state, lifecycleEvent);
//
//		assertFalse(properties.getLifecycleTimerState().isTimerRunning());
//		assertFalse(properties.getReferrerTimerState().isTimerRunning());
//
//	}
//
//	@Test
//	public void test_track_lifecycle_generated_event_contains_previous_osversion_and_previous_appid() {
//		final String prevOsVersion = "prevOsVersion";
//		final String prevAppId = "prevAppId";
//
//		state.setBackdateSessionInfoEnabled(true);
//		state.setOfflineEnabled(true);
//
//		EventData testData = new EventData();
//		testData.putString("previoussessionpausetimestampmillis", "0");
//		testData.putString("maxsessionlength", "604800");
//		testData.putString("sessionevent", "start");
//
//		Map<String, String> prevSessionInfoMap = new HashMap<String, String>() {
//			{
//				put("runmode", "application");
//				put("ignoredsessionlength", "-1600367248");
//				put("carriername", "Android");
//				put("locale", "en-US");
//				put("resolution", "1440x2392");
//				put("dayssincefirstuse", "2");
//				put("prevsessionlength", "prevsessionlength");
//				put("dayssincelastuse", "0");
//				put("dayofweek", "5");
//				put("launchevent", "LaunchEvent");
//				put("devicename", "sdk_gphone_x86_arm");
//				put("hourofday", "12");
//				put("osversion", "Android 11");
//				put(AnalyticsTestConstants.EventDataKeys.Lifecycle.PREVIOUS_APP_ID, prevAppId);
//				put("launches", "11");
//				put(AnalyticsTestConstants.EventDataKeys.Lifecycle.PREVIOUS_OS_VERSION, prevOsVersion);
//			}
//		};
//
//		testData.putStringMap("lifecyclecontextdata", prevSessionInfoMap);
//
//		testData.putString("previoussessionstarttimestampmillis", "1600367248");
//		testData.putString("starttimestampmillis", "1600371801");
//
//		Event testEvent = new Event.Builder("TEST", EventType.LIFECYCLE,
//											EventSource.RESPONSE_CONTENT).setData(testData).build();
//
//		module.trackLifecycle(state, testEvent);
//
//		try {
//			Map<String, String> mapContextData = module.trackMethodParameters.get(0).request.getStringMap(
//					AnalyticsTestConstants.EventDataKeys.Analytics.CONTEXT_DATA);
//			Assert.assertEquals(mapContextData.get(AnalyticsTestConstants.ContextDataKeys.OPERATING_SYSTEM), prevOsVersion);
//			Assert.assertEquals(mapContextData.get(AnalyticsTestConstants.ContextDataKeys.APPLICATION_IDENTIFIER), prevAppId);
//		} catch (VariantException e) {
//			Assert.fail();
//		}
//	}
//
//	@Test
//	public void testProcessGenericResetEvent() throws Exception {
//		module.analyticsProperties.setAid("testAid");
//		module.analyticsProperties.setVid("testVid");
//		module.analyticsUnprocessedEvents.add(
//			new AnalyticsUnprocessedEvent(
//				new Event.Builder("AnalyticsTest", EventType.ANALYTICS, EventSource.REQUEST_IDENTITY).build(),
//				null, null));
//
//		//Test
//		Event genericResetEvent = new Event.Builder(null, EventType.GENERIC_IDENTITY, EventSource.REQUEST_RESET)
//		.build();
//		module.processGenericResetEvent(genericResetEvent);
//
//		assertTrue(module.clearAllHitsWasCalled);
//		assertFalse(mockHitsDB.kickWasCalled);
//		assertEquals(null, module.analyticsProperties.getAid());
//		assertEquals(null, module.analyticsProperties.getVid());
//		assertEquals(0, module.analyticsUnprocessedEvents.size());
//		EventData sharedState = module.getSharedEventState(AnalyticsTestConstants.EventDataKeys.Analytics.EXTENSION_NAME,
//								Event.SHARED_STATE_NEWEST);
//		assertTrue(sharedState.isEmpty());
//	}
//
//	private void setupAnalyticsConfig() {
//		state.setServer("analyticsServer");
//		state.setRsids("12312414341");
//		state.setMarketingCloudOrganizationID("mkcloudID");
//	}
//
//	private void setupSharedState() {
//		Map<String, EventData> sharedData = new HashMap<String, EventData>();
//		sharedData.put(IDENTITY_SHARED_STATE, new EventData());
//		sharedData.put(CONFIGURATION_SHARED_STATE, new EventData());
//		state = new AnalyticsState(sharedData);
//	}
//}
