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
//import java.util.ArrayList;
//
//import static org.junit.Assert.assertEquals;
//
//public class AnalyticsEventsQueueTest extends BaseTest {
//	private TestableAnalytics module;
//
//	@Before
//	public void testSetup() throws Exception {
//		super.beforeEach();
//		module = new TestableAnalytics(eventHub, platformServices);
//		module.analyticsProperties = new AnalyticsProperties();
//		module.analyticsIdentityResponseDispatcher = new AnalyticsDispatcherAnalyticsResponseIdentity(eventHub, module);
//	}
//
//	@Test
//	public void testPutEvent_When_EventNull() {
//		module.putEvent(null, null, null);
//		assertEquals(0, module.analyticsUnprocessedEvents.size());
//	}
//
//	@Test
//	public void testPutEvent_When_EventDataNull() {
//		module.putEvent(new Event.Builder("TEST", EventType.CUSTOM, EventSource.NONE).setData(null).build(), null, null);
//		assertEquals(0, module.analyticsUnprocessedEvents.size());
//	}
//
//	@Test
//	public void testPutEvent_With_DefaultDependencies() {
//		module.putEvent(new Event.Builder("TEST", EventType.CUSTOM, EventSource.NONE).build(), null, null);
//		assertEquals(1, module.analyticsUnprocessedEvents.size());
//	}
//
//	@Test
//	public void testPutEvent_With_DependenciesList() {
//		module.putEvent(new Event.Builder("TEST", EventType.CUSTOM, EventSource.NONE).build(), new ArrayList<String>() {
//			{
//				add("TEST_DEPENDENCY");
//			}
//		}, null);
//		assertEquals(1, module.analyticsUnprocessedEvents.size());
//	}
//
//	@Test
//	public void testProcessEvents_InOrder_NoSoftDep() throws Exception {
//		module.putEvent(new Event.Builder("TEST1", EventType.CUSTOM, EventSource.NONE).build(), new ArrayList<String>() {
//			{
//				add("TEST_DEPENDENCY1");
//				add("TEST_DEPENDENCY2");
//			}
//		}, null);
//		module.putEvent(new Event.Builder("TEST2", EventType.CUSTOM, EventSource.NONE).build(), new ArrayList<String>() {
//			{
//				add("TEST_DEPENDENCY1");
//				add("TEST_DEPENDENCY3");
//			}
//		}, null);
//		eventHub.setSharedState("TEST_DEPENDENCY2", new EventData());
//		eventHub.setSharedState("TEST_DEPENDENCY1", new EventData());
//		module.processEvents();
//		assertEquals(1, module.analyticsUnprocessedEvents.size());
//		assertEquals("TEST2", module.analyticsUnprocessedEvents.peek().getEvent().getName());
//		eventHub.setSharedState("TEST_DEPENDENCY3", new EventData());
//		module.processEvents();
//		assertEquals(0, module.analyticsUnprocessedEvents.size());
//	}
//
//	@Test
//	public void testProcessEvents_InOrder_WithSoftDep() throws Exception {
//		module.putEvent(new Event.Builder("TEST1", EventType.CUSTOM, EventSource.NONE).build(), new ArrayList<String>() {
//			{
//				add("TEST_DEPENDENCY1");
//				add("TEST_DEPENDENCY2");
//			}
//		}, new ArrayList<String>() {
//			{
//				add("SOFT_DEPENDENCY");
//			}
//		});
//		module.putEvent(new Event.Builder("TEST2", EventType.CUSTOM, EventSource.NONE).build(), new ArrayList<String>() {
//			{
//				add("TEST_DEPENDENCY1");
//				add("TEST_DEPENDENCY3");
//			}
//		}, new ArrayList<String>() {
//			{
//				add("SOFT_DEPENDENCY");
//			}
//		});
//		eventHub.setSharedState("TEST_DEPENDENCY2", new EventData());
//		eventHub.setSharedState("TEST_DEPENDENCY1", new EventData());
//		module.processEvents();
//		assertEquals(1, module.analyticsUnprocessedEvents.size());
//		assertEquals("TEST2", module.analyticsUnprocessedEvents.peek().getEvent().getName());
//		eventHub.setSharedState("TEST_DEPENDENCY3", new EventData());
//		eventHub.setSharedState("SOFT_DEPENDENCY", new EventData());
//		module.processEvents();
//		assertEquals(0, module.analyticsUnprocessedEvents.size());
//	}
//}
