///* *************************************************************************
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
// **************************************************************************/
//
//package com.adobe.marketing.mobile;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.File;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.Assert.*;
//
//public class AnalyticsHitsDatabaseTest extends BaseTest {
//	private static final String DATABASE_LOG_TAG = "AnalyticsHitsDatabase";
//	private static final String HITS_TABLE_NAME = "HITS";
//	private AnalyticsHitsDatabase hitsDatabase;
//	private MockHitQueue<AnalyticsHit, AnalyticsHitSchema> hitQueue;
//	private FakeLoggingService mockLoggingService;
//	private MockAnalyticsDispatcherAnalyticsResponseContent mockDispatcherAnalyticsResponseContent;
//	private MockSystemInfoService mockSystemInfoService;
//	private MockNetworkService mockNetworkService;
//	private AnalyticsProperties analyticsProperties;
//
//	@Before
//	public void setupTest() throws Exception {
//		super.beforeEach();
//		analyticsProperties = new AnalyticsProperties();
//		mockSystemInfoService = platformServices.getMockSystemInfoService();
//		mockSystemInfoService.networkConnectionStatus = SystemInfoService.ConnectionStatus.CONNECTED;
//		mockSystemInfoService.applicationCacheDir = new File(this.getClass().getResource("").getPath() + "test");
//		mockNetworkService = platformServices.getMockNetworkService();
//		mockDispatcherAnalyticsResponseContent = new MockAnalyticsDispatcherAnalyticsResponseContent(eventHub, null);
//		mockLoggingService = (FakeLoggingService) platformServices.getLoggingService();
//		Log.setLoggingService(platformServices.getLoggingService());
//		Log.setLogLevel(LoggingMode.VERBOSE);
//		hitQueue = new MockHitQueue<AnalyticsHit, AnalyticsHitSchema>(platformServices);
//		hitsDatabase = new AnalyticsHitsDatabase(platformServices, analyticsProperties,
//				mockDispatcherAnalyticsResponseContent, hitQueue);
//
//		AnalyticsVersionProvider.setVersion("ANDNMockAnalyticsVersionMockCoreVersion");
//	}
//
//	@Test
//	public void testProcess_UpdateTimeStamp_When_lastHitTimestampIsLarger() {
//		long timestamp = TimeUtil.getUnixTimeInSeconds();
//		AnalyticsHit analyticsHit = createHit("id", "serverName2.com", "&ts=" + timestamp, timestamp, true, true, false);
//		hitsDatabase.lastHitTimestampInSeconds = 90000000000L;
//
//		hitsDatabase.process(analyticsHit);
//
//		assertTrue(mockNetworkService.connectUrlWasCalled);
//		assertEquals("&ts=90000000001", new String(mockNetworkService.connectUrlParametersConnectPayload));
//
//	}
//
//	@Test
//	public void testProcess_NotDeleteTheHit_When_OfflineTrackingIsDisabled_TimestampIsWithinThreshold() {
//		long timestamp = TimeUtil.getUnixTimeInSeconds() - 59;
//		AnalyticsHit analyticsHit = createHit("id", "serverName2.com", "&ts=" + timestamp, timestamp, true, false,
//											  false);
//
//		hitsDatabase.process(analyticsHit);
//
//		assertFalse(hitQueue.deleteHitWithIdentifierWasCalled);
//		assertTrue(mockNetworkService.connectUrlWasCalled);
//	}
//
//	@Test
//	public void testProcess_GetPostBodyAfterQuestionMark_When_UrlContainsQuestionMark() {
//		AnalyticsHit analyticsHit = createHit("id", "serverName2.com", "test/test?data", TimeUtil.getUnixTimeInSeconds(),
//											  true,
//											  false, false);
//
//		hitsDatabase.process(analyticsHit);
//
//		assertEquals("data", new String(mockNetworkService.connectUrlParametersConnectPayload));
//	}
//
//	@Test
//	public void testProcess_GetPostBodyFromUrl_When_UrlDoesNotContainsQuestionMark() {
//		AnalyticsHit analyticsHit = createHit("id", "serverName2.com", "data", TimeUtil.getUnixTimeInSeconds(), true,
//											  false, false);
//
//		hitsDatabase.process(analyticsHit);
//
//		assertEquals("data", new String(mockNetworkService.connectUrlParametersConnectPayload));
//	}
//
//	@Test
//	public void testProcess_Headers_When_SSLIsTrue() {
//		AnalyticsHit analyticsHit = createHit("id", "serverName2.com", "data", TimeUtil.getUnixTimeInSeconds(), true,
//											  false, false);
//		hitsDatabase.process(analyticsHit);
//
//		assertNull(mockNetworkService.connectUrlParametersRequestProperty.get("connection"));
//		assertEquals("application/x-www-form-urlencoded",
//					 mockNetworkService.connectUrlParametersRequestProperty.get("Content-Type"));
//	}
//
//	@Test
//	public void testProcess_ReturnRetryYes_When_ConnectionObjectIsNull() {
//		AnalyticsHit analyticsHit = createHit("id", "serverName2.com", "data", TimeUtil.getUnixTimeInSeconds(), true,
//											  false, false);
//		mockNetworkService.connectUrlReturnValue = null;
//
//		HitQueue.RetryType  retryType = hitsDatabase.process(analyticsHit);
//		assertEquals(HitQueue.RetryType.YES, retryType);
//
//	}
//
//	@Test
//	public void testProcess_ReturnRetryYes_When_ResponseCodeIsMinusOne() {
//		AnalyticsHit analyticsHit = createHit("id", "serverName2.com", "data", TimeUtil.getUnixTimeInSeconds(), true,
//											  false, false);
//		MockConnection mockConnection = new MockConnection("error", -1, null,
//				null);
//		mockNetworkService.connectUrlReturnValue = mockConnection;
//
//		HitQueue.RetryType  retryType = hitsDatabase.process(analyticsHit);
//
//		assertEquals(HitQueue.RetryType.YES, retryType);
//		assertTrue(mockConnection.closeWasCalled);
//
//	}
//
//	@Test
//	public void testProcess_ReturnRetryNo_When_ResponseIsNotNull() {
//		AnalyticsHit analyticsHit = createHit("id", "serverName2.com", "data", TimeUtil.getUnixTimeInSeconds(), true,
//											  false, false);
//
//		HashMap<String, String> headers = new HashMap<String, String>();
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.ETAG_HEADER, "test-etag");
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.SERVER_HEADER, "server.com");
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.CONTENT_TYPE_HEADER, "content-type/test");
//
//		MockConnection mockConnection = new MockConnection("", 200, null,
//				headers);
//		mockNetworkService.connectUrlReturnValue = mockConnection;
//
//		HitQueue.RetryType  retryType = hitsDatabase.process(analyticsHit);
//
//		assertEquals(HitQueue.RetryType.NO, retryType);
//		assertTrue(mockConnection.closeWasCalled);
//
//	}
//
//	@Test
//	public void testProcess_ReturnRetryNo_When_ResponseIsNull_ResponseCode200() {
//		AnalyticsHit analyticsHit = createHit("id", "serverName2.com", "data", TimeUtil.getUnixTimeInSeconds(), true,
//											  false, false);
//
//		HashMap<String, String> headers = new HashMap<String, String>();
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.ETAG_HEADER, "test-etag");
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.SERVER_HEADER, "server.com");
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.CONTENT_TYPE_HEADER, "content-type/test");
//
//		MockConnection mockConnection = new MockConnection(null, 200, null,
//				headers);
//		mockNetworkService.connectUrlReturnValue = mockConnection;
//
//		HitQueue.RetryType  retryType = hitsDatabase.process(analyticsHit);
//
//		assertEquals(HitQueue.RetryType.NO, retryType);
//		assertTrue(mockConnection.closeWasCalled);
//
//	}
//
//	@Test
//	public void testProcess_DispatchAnalyticsEvent_When_ResponseIsValid() {
//		String data =
//			"ndh=1&ce=UTF-8&c.&a.&DebugEventIdentifier=e1e06dc8-887e-43af-8535-13032cd30c42&action=testActionName&.a&.c&t=00%2F00%2F0000%2000%3A00%3A00%200%20360&pe=lnk_o&pev2=AMACTION%3AtestActionName&cp=foreground&ts=1564608467";
//		AnalyticsHit analyticsHit = createHit("id", "serverName2.com", data, TimeUtil.getUnixTimeInSeconds(), true,
//											  false, true);
//
//		HashMap<String, String> headers = new HashMap<String, String>();
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.ETAG_HEADER, "test-etag");
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.SERVER_HEADER, "server.com");
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.CONTENT_TYPE_HEADER, "content-type/test");
//
//		MockConnection mockConnection = new MockConnection("valid", 200, null,
//				headers);
//		mockNetworkService.connectUrlReturnValue = mockConnection;
//
//		HitQueue.RetryType  retryType = hitsDatabase.process(analyticsHit);
//
//		assertTrue(mockDispatcherAnalyticsResponseContent.dispatchAnalyticsHitResponseWasCalled);
//		assertEquals(1, mockDispatcherAnalyticsResponseContent.dispatchAnalyticsHitResponseParamServerResponse.size());
//		assertEquals("valid", mockDispatcherAnalyticsResponseContent.dispatchAnalyticsHitResponseParamServerResponse.get(0));
//
//	}
//
//	@Test
//	public void testProcess_DoesDispatchAnalyticsEvent_When_AAMForwardingI_IsDisabled() {
//		AnalyticsHit analyticsHit = createHit("id", "serverName2.com", "data", TimeUtil.getUnixTimeInSeconds(), true,
//											  false, false);
//
//		HashMap<String, String> headers = new HashMap<String, String>();
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.ETAG_HEADER, "test-etag");
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.SERVER_HEADER, "server.com");
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.CONTENT_TYPE_HEADER, "content-type/test");
//
//		MockConnection mockConnection = new MockConnection("valid", 200, null,
//				headers);
//		mockNetworkService.connectUrlReturnValue = mockConnection;
//
//		HitQueue.RetryType  retryType = hitsDatabase.process(analyticsHit);
//
//		assertTrue(mockDispatcherAnalyticsResponseContent.dispatchAnalyticsHitResponseWasCalled);
//	}
//
//
//	@Test
//	public void testQueue_HitIsCorrect() {
//
//		hitsDatabase.queue(configureAnalyticsState(), "data", 1000l, false, false, "test-event-uuid");
//
//		assertEquals("https://myServer.com/b/ss/rsid/0/ANDNMockAnalyticsVersionMockCoreVersion/s",
//					 hitQueue.queueParametersHit.server);
//		assertEquals("data", hitQueue.queueParametersHit.url);
//		assertEquals(1000, hitQueue.queueParametersHit.timestamp);
//		assertEquals(false, hitQueue.queueParametersHit.isWaiting);
//		assertEquals(false, hitQueue.queueParametersHit.offlineTrackingEnabled);
//	}
//
//	@Test
//	public void testQueue_InstallIs1_When_InstallIsTrue() {
//
//		hitsDatabase.queue(configureAnalyticsState(), "data", 1000l, true, false, "test-event-uuid");
//
//		assertEquals(true, hitQueue.queueParametersHit.isWaiting);
//	}
//
//
//	@Test
//	public void testQueue_NotKickTheQueue_When_DatabaseIsWaiting() {
//		analyticsProperties.getLifecycleTimerState().startTimer(100, null);
//		hitsDatabase.queue(configureAnalyticsState(), "data", 1000l, false, false, "test-event-uuid");
//
//		assertFalse(hitQueue.bringOnlineWasCalled);
//	}
//
//	@Test
//	public void testQueue_NotKickTheQueue_When_NoStateToUse() {
//		hitsDatabase.queue(null, "data", 1000l, false, false, "test-event-uuid");
//
//		assertFalse(hitQueue.bringOnlineWasCalled);
//	}
//
//	@Test
//	public void testQueue_KickTheQueue_When_QueueTheHit_OfflineTrackingIsDisabled() {
//
//		hitsDatabase.queue(configureAnalyticsState(), "data", 1000l, false, false, "test-event-uuid");
//
//		assertTrue(hitQueue.bringOnlineWasCalled);
//	}
//
//	@Test
//	public void testQueue_NotKickTheQueue_When_QueueTheHit_OptOut() {
//		AnalyticsState analyticsState = configureAnalyticsState();
//		analyticsState.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
//
//		hitsDatabase.queue(analyticsState, "data", 1000l, false, false, "test-event-uuid");
//
//		assertFalse(hitQueue.bringOnlineWasCalled);
//	}
//
//	@Test
//	public void testQueue_NotKickTheQueue_When_QueueTheHit_OptUnkonw() {
//		AnalyticsState analyticsState = configureAnalyticsState();
//		analyticsState.setPrivacyStatus(MobilePrivacyStatus.UNKNOWN);
//
//		hitsDatabase.queue(analyticsState, "data", 1000l, false, false, "test-event-uuid");
//
//		assertFalse(hitQueue.bringOnlineWasCalled);
//	}
//
//
//	@Test
//	public void testQueue_NotKickTheQueue_When_QueueTheHit_QueueSizeIsSmallerThanBatchLimit() {
//		AnalyticsState analyticsState = configureAnalyticsToQueueMultipleHits();
//		hitQueue.getSizeWithQueryReturnValue = 10;
//
//		hitsDatabase.queue(analyticsState, "data", 1000l, false, false, "test-event-uuid");
//
//		assertFalse(hitQueue.bringOnlineWasCalled);
//	}
//
//	@Test
//	public void testQueue_KickTheQueue_When_QueueTheHit_QueueSizeIsLargeThanBatchLimit() {
//		AnalyticsState analyticsState = configureAnalyticsToQueueMultipleHits();
//		hitQueue.getSizeWithQueryReturnValue = 11;
//
//		hitsDatabase.queue(analyticsState, "data", 1000l, false, false, "test-event-uuid");
//
//		assertTrue(hitQueue.bringOnlineWasCalled);
//	}
//
//	@Test
//	public void testForceKick_KickTheQueue_When_QueueSizeIsSmallerThanBatchLimit() {
//		AnalyticsState analyticsState = configureAnalyticsToQueueMultipleHits();
//		hitQueue.getSizeWithQueryReturnValue = 0;
//		hitsDatabase.forceKick(analyticsState);
//		assertTrue(hitQueue.bringOnlineWasCalled);
//	}
//
//	@Test
//	public void testClearTrackingQueue() {
//		hitsDatabase.clearTrackingQueue();
//		assertTrue(hitQueue.deleteAllHitsWasCalled);
//	}
//
//	@Test
//	public void testGetTrackingQueueSize() {
//		hitQueue.getSizeWithQueryReturnValue = 100;
//		assertEquals(100, hitsDatabase.getTrackingQueueSize());
//	}
//
//	@Test
//	public void testKickWithAdditionalData_UpdatesHitInDB() throws Exception {
//		AnalyticsState state = configureAnalyticsToQueueMultipleHits();
//		hitQueue.queryHitReturnValue = createHit("id", "serverName2.com",
//									   "someotherkey=value&c.&a.&key1=value1&.a&.c&abc=com", TimeUtil.getUnixTimeInSeconds(), true, false, false);
//
//		hitsDatabase.kickWithAdditionalData(state, new HashMap<String, String>() {
//			{
//				put("a.key2", "value2");
//			}
//		});
//		assertEquals("someotherkey=value&c.&a.&key1=value1&key2=value2&.a&.c&abc=com", hitQueue.updateHitParametersHit.url);
//	}
//
//	@Test
//	public void testKickWithAdditionalData_DoesNotUpdateHit_When_DataEmpty() throws Exception {
//		AnalyticsState state = configureAnalyticsToQueueMultipleHits();
//		hitQueue.queryHitReturnValue = createHit("id", "serverName2.com",
//									   "someotherkey=value&c.&a.&key1=value1&.a&.c&abc=com", TimeUtil.getUnixTimeInSeconds(), true, false, false);
//
//		hitsDatabase.kickWithAdditionalData(state, new HashMap<String, String>());
//
//		assertFalse(hitQueue.updateHitWasCalled);
//	}
//
//	@Test
//	public void testKickWithAdditionalData_DoesNotUpdateHit_When_DataNull() throws Exception {
//		AnalyticsState state = configureAnalyticsToQueueMultipleHits();
//		hitQueue.queryHitReturnValue = createHit("id", "serverName2.com",
//									   "someotherkey=value&c.&a.&key1=value1&.a&.c&abc=com", TimeUtil.getUnixTimeInSeconds(), true, false, false);
//
//		hitsDatabase.kickWithAdditionalData(state, null);
//		assertFalse(hitQueue.updateHitWasCalled);
//	}
//
//
//	@Test
//	public void testKickWithAdditionalData_DoesNothing_When_NullHitFragment() throws Exception {
//		AnalyticsState state = configureAnalyticsToQueueMultipleHits();
//		hitsDatabase.queue(state, null, 1000, true, false, "test-event-uuid");
//		hitsDatabase.kickWithAdditionalData(state, new HashMap<String, String>() {
//			{
//				put("test", "value");
//			}
//		});
//		assertFalse(hitQueue.updateHitWasCalled);
//	}
//
//	@Test
//	public void testKickWithBadUrl() throws Exception {
//		hitsDatabase.queue(configureAnalyticsToQueueMultipleBadHits(), null, 1000, true, false, "test-event-uuid");
//		hitsDatabase.queue(configureAnalyticsState(), null, 1000, true, false, "test-event-uuid");
//		hitsDatabase.kick(configureAnalyticsState(), true);
//
//		assertTrue(hitQueue.bringOnlineWasCalled);
//		assertTrue(hitQueue.updateAllHitsWasCalled);
//		assertEquals("https://myServer.com/b/ss/rsid/0/ANDNMockAnalyticsVersionMockCoreVersion/s",
//					 hitQueue.updateAllHitsParameters.get("SERVER"));
//	}
//
//	@Test
//	public void testUpdateBackdatedHit_DoesNothing_When_NullHitFragment() {
//		AnalyticsState state = configureAnalyticsToQueueMultipleHits();
//		hitsDatabase.queue(state, null, 1000, true, true, "test-event-uuid");
//		hitsDatabase.updateBackdatedHit(state, "test", 100, "test-event-uuid");
//		assertFalse(hitQueue.updateHitWasCalled);
//	}
//
//	@Test
//	public void testUpdateBackdatedHit_DoesNothing_When_NohitInDatabase()  {
//		AnalyticsState state = configureAnalyticsToQueueMultipleHits();
//		hitsDatabase.updateBackdatedHit(state, "test", 100, "test-event-uuid");
//		assertFalse(hitQueue.updateHitWasCalled);
//	}
//
//
//	@Test
//	public void testUpdateBackdatedHit_UpdateTheData() {
//		AnalyticsState state = configureAnalyticsToQueueMultipleHits();
//		hitQueue.queryHitReturnValue = createHit("id", "serverName2.com",
//									   "", TimeUtil.getUnixTimeInSeconds(), true, false, false);
//
//		hitsDatabase.queue(state, "", 1000, true, true, "test-event-uuid");
//		hitsDatabase.updateBackdatedHit(state, "test", 100, "test-event-uuid");
//		assertEquals("test", hitQueue.updateHitParametersHit.url);
//		assertEquals(100, hitQueue.updateHitParametersHit.timestamp);
//	}
//
//	@Test
//	public void testResetIsWaitingFlag() {
//
//		hitsDatabase.resetIsWaitingFlag();
//		assertEquals(new AnalyticsHitSchema().generateUpdateValuesForResetIsWaitingFlag(), hitQueue.updateAllHitsParameters);
//	}
//	// ----------------------------------- Helper methods -----------------------------------
//	/**
//	 * Sets the analytics state - server, rsid, marketing cloud id and marketing cloud organization id
//	 */
//	private AnalyticsState configureAnalyticsState() {
//		AnalyticsState state = new AnalyticsState(null);
//		state.setServer("myServer.com");
//		state.setRsids("rsid");
//		state.setMarketingCloudOrganizationID("orgid");
//		return state;
//	}
//
//	private AnalyticsState configureAnalyticsToQueueMultipleHits() {
//		AnalyticsState state = new AnalyticsState(null);
//		state.setServer("myServer.com");
//		state.setRsids("rsid");
//		state.setMarketingCloudOrganizationID("orgid");
//		state.setPrivacyStatus(MobilePrivacyStatus.OPT_IN);
//		state.setBatchLimit(10);
//		state.setOfflineEnabled(true);
//		return state;
//	}
//
//	private AnalyticsState configureAnalyticsToQueueMultipleBadHits() {
//		AnalyticsState state = new AnalyticsState(null);
//		state.setServer("foo");
//		state.setRsids("rsid");
//		state.setMarketingCloudOrganizationID("orgid");
//		state.setPrivacyStatus(MobilePrivacyStatus.OPT_IN);
//		state.setBatchLimit(10);
//		state.setOfflineEnabled(true);
//		return state;
//	}
//
//	private AnalyticsHit createHit(String identifier, String server, String url, long timeStamp,
//								   boolean isWaiting, boolean offlineTrackingEnabled, boolean aamForwardingEnabled) {
//		AnalyticsHit newHit = new AnalyticsHit();
//		newHit.identifier = identifier;
//		newHit.server = server;
//		newHit.url = url;
//		newHit.isWaiting = isWaiting;
//		newHit.timestamp = timeStamp;
//		newHit.offlineTrackingEnabled = offlineTrackingEnabled;
//		newHit.aamForwardingEnabled = aamForwardingEnabled;
//		return newHit;
//	}
//}