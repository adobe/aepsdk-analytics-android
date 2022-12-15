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
//import static org.junit.Assert.*;
//
//public class AnalyticsListenerHubBootedTest extends BaseTest {
//	private AnalyticsListenerHubBooted listener;
//	private MockAnalytics mockAnalyticsModule;
//
//	@Before
//	public void beforeEach() {
//		super.beforeEach();
//		mockAnalyticsModule = new MockAnalytics(eventHub, platformServices);
//		listener = new AnalyticsListenerHubBooted(mockAnalyticsModule, EventType.HUB, EventSource.BOOTED);
//	}
//
//	@Test
//	public void testHear_Happy() throws Exception {
//		Event hubBootEvent = new Event.Builder("TEST", EventType.HUB, EventSource.BOOTED).build();
//		listener.hear(hubBootEvent);
//		waitForExecutor(mockAnalyticsModule.getExecutor());
//		assertTrue(mockAnalyticsModule.handleHubBootedEventWasCalled);
//		assertEquals(hubBootEvent, mockAnalyticsModule.handleHubBootedEventParamEvent);
//	}
//}
