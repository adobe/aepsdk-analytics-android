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
//public class AnalyticsListenerAcquisitionResponseContentTest extends BaseTest {
//	private AnalyticsListenerAcquisitionResponseContent testListener;
//	private MockAnalytics mockAnalyticsModule;
//
//	@Before
//	public void testSetup() throws Exception {
//		super.beforeEach();
//		mockAnalyticsModule = new MockAnalytics(eventHub, platformServices);
//		testListener = new AnalyticsListenerAcquisitionResponseContent(mockAnalyticsModule, EventType.ACQUISITION,
//				EventSource.RESPONSE_CONTENT);
//	}
//
//	@Test
//	public void testListener_CallsHandleAcquisitionResponseEvent() throws Exception {
//		Event testEvent = new Event.Builder("TEST", EventType.ACQUISITION, EventSource.RESPONSE_CONTENT).build();
//		testListener.hear(testEvent);
//		waitForExecutor(mockAnalyticsModule.getExecutor());
//		assertTrue(mockAnalyticsModule.handleAcquisitionResponseEventWasCalled);
//		assertEquals(testEvent, mockAnalyticsModule.handleAcquisitionResponseEventParamEvent);
//	}
//}
