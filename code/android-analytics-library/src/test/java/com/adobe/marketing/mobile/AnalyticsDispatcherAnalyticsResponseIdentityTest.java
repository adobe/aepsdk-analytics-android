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
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//
//public class AnalyticsDispatcherAnalyticsResponseIdentityTest extends BaseTest {
//	private static final String AID_KEY = "aid";
//	private static final String VID_KEY = "vid";
//	private AnalyticsDispatcherAnalyticsResponseIdentity dispatcher;
//	private MockAnalytics mockAnalyticsModule;
//
//	@Before
//	public void testSetup() throws Exception {
//		super.beforeEach();
//		mockAnalyticsModule = new MockAnalytics(eventHub, platformServices);
//		dispatcher = new AnalyticsDispatcherAnalyticsResponseIdentity(eventHub, mockAnalyticsModule);
//	}
//
//	@Test
//	public void testDispatch_createsValidEvent() throws Exception {
//		// test that aid is dispatched correctly
//		dispatcher.dispatch("someTestAID", "someTestVID", "testPairID");
//		assertTrue(eventHub.isDispatchedCalled);
//		assertEquals("TrackingIdentifierValue", eventHub.dispatchedEvent.getName());
//		assertEquals(EventType.ANALYTICS, eventHub.dispatchedEvent.getEventType());
//		assertEquals(EventSource.RESPONSE_IDENTITY, eventHub.dispatchedEvent.getEventSource());
//		// TODO AMSDK-6907 Need to update mock event hub to handle multiple dispatched events
//		//assertEquals("testPairID", eventHub.dispatchedEvent.getPairID());
//		assertEquals("someTestAID", eventHub.dispatchedEvent.getData().optString(AID_KEY, null));
//		assertEquals("someTestVID", eventHub.dispatchedEvent.getData().optString(VID_KEY, null));
//	}
//
//	@Test
//	public void testDispatch_createsValidEventWithoutPairId() throws Exception {
//		// test that aid is dispatched correctly
//		dispatcher.dispatch("someTestAID", "someTestVID", null);
//		assertTrue(eventHub.isDispatchedCalled);
//		assertEquals("TrackingIdentifierValue", eventHub.dispatchedEvent.getName());
//		assertEquals(EventType.ANALYTICS, eventHub.dispatchedEvent.getEventType());
//		assertEquals(EventSource.RESPONSE_IDENTITY, eventHub.dispatchedEvent.getEventSource());
//		assertNull(eventHub.dispatchedEvent.getPairID());
//		assertEquals("someTestAID", eventHub.dispatchedEvent.getData().optString(AID_KEY, null));
//		assertEquals("someTestVID", eventHub.dispatchedEvent.getData().optString(VID_KEY, null));
//	}
//}
