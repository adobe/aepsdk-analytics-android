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
//public class AnalyticsListenerHubSharedStateTest extends BaseTest {
//	private static final String STATE_OWNER = "stateowner";
//	private AnalyticsListenerHubSharedState listener;
//	private MockAnalytics mockAnalyticsModule;
//
//	@Before
//	public void testSetup() throws Exception {
//		super.beforeEach();
//		mockAnalyticsModule = new MockAnalytics(eventHub, platformServices);
//		listener = new AnalyticsListenerHubSharedState(mockAnalyticsModule, EventType.HUB, EventSource.SHARED_STATE);
//	}
//
//	@Test
//	public void testListener_CallsHandleSharedStateUpdate_When_ValidStateOwner() throws Exception {
//		listener.hear(new Event.Builder("TEST", EventType.HUB, EventSource.SHARED_STATE)
//					  .setData(new EventData().putString(STATE_OWNER, "testDependency")).build());
//		waitForExecutor(mockAnalyticsModule.getExecutor());
//		assertTrue(mockAnalyticsModule.handleSharedStateUpdateEventWasCalled);
//		assertEquals("testDependency", mockAnalyticsModule.handleSharedStateUpdateEventParamStateOwner);
//	}
//
//	@Test
//	public void testListener_DoesNotCallHandleSharedStateUpdate_When_EventDataNull() throws Exception {
//		listener.hear(new Event.Builder("TEST", EventType.HUB, EventSource.SHARED_STATE).setData(null).build());
//		waitForExecutor(mockAnalyticsModule.getExecutor());
//		assertFalse(mockAnalyticsModule.handleSharedStateUpdateEventWasCalled);
//	}
//
//	@Test
//	public void testListener_DoesNotCallHandleSharedStateUpdate_When_EventDataEmpty() throws Exception {
//		listener.hear(new Event.Builder("TEST", EventType.HUB, EventSource.SHARED_STATE).build());
//		waitForExecutor(mockAnalyticsModule.getExecutor());
//		assertFalse(mockAnalyticsModule.handleSharedStateUpdateEventWasCalled);
//	}
//
//	@Test
//	public void testListener_DoesNotCallHandleSharedStateUpdate_When_StateOwnerNull() throws Exception {
//		listener.hear(new Event.Builder("TEST", EventType.HUB, EventSource.SHARED_STATE)
//					  .setData(new EventData().putString(STATE_OWNER, null)).build());
//		waitForExecutor(mockAnalyticsModule.getExecutor());
//		assertFalse(mockAnalyticsModule.handleSharedStateUpdateEventWasCalled);
//	}
//}
