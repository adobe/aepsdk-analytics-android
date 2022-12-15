///***************************************************************************
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
//import org.junit.Before;
//import org.junit.Test;
//
//import static org.junit.Assert.*;
//
//public class AnalyticsListenerAnalyticsRequestContentTests extends BaseTest {
//	private AnalyticsListenerAnalyticsRequestContent testListener;
//	private MockAnalytics mockAnalyticsModule;
//
//	@Before
//	public void testSetup() throws Exception {
//		super.beforeEach();
//		mockAnalyticsModule = new MockAnalytics(eventHub, platformServices);
//		testListener = new AnalyticsListenerAnalyticsRequestContent(mockAnalyticsModule, EventType.ANALYTICS,
//				EventSource.REQUEST_CONTENT);
//	}
//
//	@Test
//	public void testListener_HappyFlow() throws Exception {
//		testListener.hear(new Event.Builder("TEST", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).setData(new EventData() {
//			{
//				putNull("trackRequest");
//			}
//		}).build());
//
//		waitForExecutor(mockAnalyticsModule.getExecutor());
//		assertTrue(mockAnalyticsModule.handleAnalyticsRequestWasCalled);
//	}
//
//	@Test
//	public void testListener_IgnoresEventsWithNullOrEmptyEventData() throws Exception {
//		testListener.hear(new Event.Builder("TEST", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).setData(null).build());
//		waitForExecutor(mockAnalyticsModule.getExecutor());
//		assertFalse(mockAnalyticsModule.handleAnalyticsRequestWasCalled);
//		testListener.hear(new Event.Builder("TEST", EventType.ANALYTICS, EventSource.REQUEST_CONTENT).build());
//		waitForExecutor(mockAnalyticsModule.getExecutor());
//		assertFalse(mockAnalyticsModule.handleAnalyticsRequestWasCalled);
//	}
//}
