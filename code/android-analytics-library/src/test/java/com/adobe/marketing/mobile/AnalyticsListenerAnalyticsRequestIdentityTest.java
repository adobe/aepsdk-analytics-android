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
//import static org.junit.Assert.assertTrue;
//
//public class AnalyticsListenerAnalyticsRequestIdentityTest extends BaseTest {
//
//	private AnalyticsListenerAnalyticsRequestIdentity testListener;
//	private MockAnalytics mockAnalyticsModule;
//
//	@Before
//	public void testSetup() throws Exception {
//		super.beforeEach();
//		mockAnalyticsModule = new MockAnalytics(eventHub, platformServices);
//		testListener = new AnalyticsListenerAnalyticsRequestIdentity(mockAnalyticsModule, EventType.ANALYTICS,
//				EventSource.REQUEST_IDENTITY);
//	}
//
//	@Test
//	public void testListener_CallsProcessAnalyticsIDRequest() throws Exception {
//		Event testEvent = new Event.Builder("TEST", EventType.ANALYTICS, EventSource.REQUEST_IDENTITY).build();
//		testListener.hear(testEvent);
//		waitForExecutor(mockAnalyticsModule.getExecutor());
//		assertTrue(mockAnalyticsModule.processAnalyticsIDRequestWasCalled);
//		assertEquals(testEvent, mockAnalyticsModule.processAnalyticsIDRequestParamEvent);
//	}
//}