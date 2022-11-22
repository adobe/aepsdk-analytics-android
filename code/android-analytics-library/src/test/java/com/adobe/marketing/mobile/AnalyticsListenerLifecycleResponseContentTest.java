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
//public class AnalyticsListenerLifecycleResponseContentTest extends BaseTest {
//
//	private AnalyticsListenerLifecycleResponseContent testListener;
//	private MockAnalytics mockAnalyticsModule;
//
//	@Before
//	public void testSetup() throws Exception {
//		super.beforeEach();
//		mockAnalyticsModule = new MockAnalytics(eventHub, platformServices);
//		testListener = new AnalyticsListenerLifecycleResponseContent(mockAnalyticsModule, EventType.LIFECYCLE,
//				EventSource.RESPONSE_CONTENT);
//	}
//
//	@Test
//	public void testListener_CallsHandleLifecycleEventData() throws Exception {
//		EventData testData = new EventData();
//		testData.putString("key1", "val1");
//		testData.putBoolean("key2", true);
//		Event testEvent = new Event.Builder("TEST", EventType.LIFECYCLE,
//											EventSource.RESPONSE_CONTENT).setData(testData).build();
//		testListener.hear(testEvent);
//		waitForExecutor(mockAnalyticsModule.getExecutor());
//		assertTrue(mockAnalyticsModule.handleLifecycleEventWasCalled);
//		assertEquals(testEvent, mockAnalyticsModule.handleLifecycleEventParamEvent);
//	}
//}
