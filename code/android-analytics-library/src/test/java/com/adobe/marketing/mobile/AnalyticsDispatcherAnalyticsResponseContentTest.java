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
//import java.util.HashMap;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//public class AnalyticsDispatcherAnalyticsResponseContentTest extends BaseTest {
//	private static final String RESP_KEY = "analyticsserverresponse";
//	private static final String QUEUE_KEY = "queuesize";
//	private AnalyticsDispatcherAnalyticsResponseContent dispatcher;
//	private MockAnalytics mockAnalyticsModule;
//
//	@Before
//	public void testSetup() throws Exception {
//		super.beforeEach();
//		mockAnalyticsModule = new MockAnalytics(eventHub, platformServices);
//		dispatcher = new AnalyticsDispatcherAnalyticsResponseContent(eventHub, mockAnalyticsModule);
//	}
//
//	@Test
//	public void testDispatchAnalyticsHitResponse_createsValidEvent() throws Exception {
//		HashMap<String, String> headers = new HashMap<String, String>();
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.ETAG_HEADER, "test-etag");
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.SERVER_HEADER, "server.com");
//		headers.put(AnalyticsTestConstants.EventDataKeys.Analytics.CONTENT_TYPE_HEADER, "content-type/test");
//
//		String expectedRequestIdentifier = "testRequestId";
//		String expectedHost = "https://test.com/b/ss/rsid/0/OIP-mockAnalyticsVersion-mockCoreVersion/s";
//		String expectedUrl =
//			"ndh=1&c.&a.&AppID=mockAppName%20mockAppVersion%20%28mockAppVersionCode%29&CarrierName=mockMobileCarrier&DeviceName";
//
//		dispatcher.dispatchAnalyticsHitResponse("testServerResponse", headers, expectedRequestIdentifier, expectedHost,
//												expectedUrl);
//		assertTrue(eventHub.isDispatchedCalled);
//		assertEquals("AnalyticsResponse", eventHub.dispatchedEvent.getName());
//		assertEquals(EventType.ANALYTICS, eventHub.dispatchedEvent.getEventType());
//		assertEquals(EventSource.RESPONSE_CONTENT, eventHub.dispatchedEvent.getEventSource());
//		assertEquals("testServerResponse", eventHub.dispatchedEvent.getData().optString(RESP_KEY, null));
//		assertEquals(headers, eventHub.dispatchedEvent.getData().optStringMap(
//						 AnalyticsTestConstants.EventDataKeys.Analytics.HEADERS_RESPONSE, null));
//		assertEquals(expectedRequestIdentifier,
//					 eventHub.dispatchedEvent.getData().optString(AnalyticsTestConstants.EventDataKeys.Analytics.REQUEST_EVENT_IDENTIFIER,
//							 null));
//		assertEquals(expectedHost,
//					 eventHub.dispatchedEvent.getData().optString(AnalyticsTestConstants.EventDataKeys.Analytics.HIT_HOST,
//							 null));
//		assertEquals(expectedUrl,
//					 eventHub.dispatchedEvent.getData().optString(AnalyticsTestConstants.EventDataKeys.Analytics.HIT_URL,
//							 null));
//	}
//
//	@Test
//	public void testDispatchQueueSize_createsValidEvent() throws Exception {
//		dispatcher.dispatchQueueSize(10, "testPairID");
//		assertTrue(eventHub.isDispatchedCalled);
//		assertEquals("QueueSizeValue", eventHub.dispatchedEvent.getName());
//		assertEquals(EventType.ANALYTICS, eventHub.dispatchedEvent.getEventType());
//		assertEquals(EventSource.RESPONSE_CONTENT, eventHub.dispatchedEvent.getEventSource());
//		assertEquals("testPairID", eventHub.dispatchedEvent.getPairID());
//		assertEquals(10L, eventHub.dispatchedEvent.getData().optLong(QUEUE_KEY, 0L));
//	}
//}
