//package com.adobe.marketing.mobile;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.Map;
//
//import static org.junit.Assert.*;
//
//public class AnalyticsCoreTests {
//	private AnalyticsCore core;
//	private MockEventHubUnitTest eventHub;
//	private final static String ANALYTICS_TEST_VERSION = "ANDN010101020202";
//
//	@Before
//	public void setup() {
//		PlatformServices fakePlatformServices = new FakePlatformServices();
//
//		eventHub = new MockEventHubUnitTest("MockEventHubUnitTest", fakePlatformServices);
//		core = new AnalyticsCore(eventHub, new ModuleDetails() {
//			@Override
//			public String getName() {
//				return "AnalyticsExtension";
//			}
//
//			@Override
//			public String getVersion() {
//				return "AnalyticsExtensionVersion";
//			}
//
//			@Override
//			public Map<String, String> getAdditionalInfo() {
//				return null;
//			}
//		}, ANALYTICS_TEST_VERSION);
//	}
//
//	@Test
//	public void getQueueSize_should_dispatchAnalyticsContentRequest() throws Exception {
//		core.getQueueSize(new AdobeCallback<Long>() {
//			@Override
//			public void call(Long aLong) {
//
//			}
//		});
//
//		EventData data = eventHub.dispatchedEvent.getData();
//		assertEquals(1, data.size());
//		assertTrue(data.optBoolean("getqueuesize", false));
//		assertTrue("event is dispatched", eventHub.isDispatchedCalled);
//		assertEquals("event has correct name", "GetQueueSize", eventHub.dispatchedEvent.getName());
//		assertEquals("event has correct event type", EventType.ANALYTICS, eventHub.dispatchedEvent.getEventType());
//		assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
//		assertTrue("one-time listener is registered for callback", eventHub.registerOneTimeListenerWithErrorCalled);
//		assertNotNull("one-time listener has a callback block", eventHub.registerOneTimeListenerWithErrorParamBlock);
//	}
//
//	@Test
//	public void getTrackingIdentifier_should_dispatchAnalyticsIdentityRequest() throws Exception {
//		core.getTrackingIdentifier(new AdobeCallback<String>() {
//			@Override
//			public void call(String s) {
//
//			}
//		});
//
//		EventData data = eventHub.dispatchedEvent.getData();
//		assertEquals(0, data.size());
//		assertTrue("event is dispatched", eventHub.isDispatchedCalled);
//		assertEquals("event has correct name", "GetTrackingIdentifier", eventHub.dispatchedEvent.getName());
//		assertEquals("event has correct event type", EventType.ANALYTICS, eventHub.dispatchedEvent.getEventType());
//		assertEquals("event has correct event source", EventSource.REQUEST_IDENTITY, eventHub.dispatchedEvent.getEventSource());
//		assertTrue("one-time listener is registered for callback", eventHub.registerOneTimeListenerWithErrorCalled);
//		assertNotNull("one-time listener has a callback block", eventHub.registerOneTimeListenerWithErrorParamBlock);
//	}
//
//	@Test
//	public void getVisitorIdentifier_should_dispatchAnalyticsIdentityRequest() throws Exception {
//		core.getVisitorIdentifier(new AdobeCallback<String>() {
//			@Override
//			public void call(String s) {
//
//			}
//		});
//
//		EventData data = eventHub.dispatchedEvent.getData();
//		assertEquals(0, data.size());
//		assertTrue("event is dispatched", eventHub.isDispatchedCalled);
//		assertEquals("event has correct name", "GetVisitorIdentifier", eventHub.dispatchedEvent.getName());
//		assertEquals("event has correct event type", EventType.ANALYTICS, eventHub.dispatchedEvent.getEventType());
//		assertEquals("event has correct event source", EventSource.REQUEST_IDENTITY, eventHub.dispatchedEvent.getEventSource());
//		assertTrue("one-time listener is registered for callback", eventHub.registerOneTimeListenerWithErrorCalled);
//		assertNotNull("one-time listener has a callback block", eventHub.registerOneTimeListenerWithErrorParamBlock);
//	}
//
//	@Test
//	public void clearQueue_should_dispatchAnalyticsContentRequest() throws Exception {
//		core.clearQueue();
//
//		EventData data = eventHub.dispatchedEvent.getData();
//		assertEquals(1, data.size());
//		assertTrue(data.optBoolean("clearhitsqueue", false));
//		assertTrue("event is dispatched", eventHub.isDispatchedCalled);
//		assertEquals("event has correct name", "ClearHitsQueue", eventHub.dispatchedEvent.getName());
//		assertEquals("event has correct event type", EventType.ANALYTICS, eventHub.dispatchedEvent.getEventType());
//		assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
//	}
//
//	@Test
//	public void sendQueuedHits_should_dispatchAnalyticsContentRequest() throws Exception {
//		core.sendQueuedHits();
//
//		EventData data = eventHub.dispatchedEvent.getData();
//		assertEquals(1, data.size());
//		assertTrue(data.optBoolean("forcekick", false));
//		assertTrue("event is dispatched", eventHub.isDispatchedCalled);
//		assertEquals("event has correct name", "ForceKickHits", eventHub.dispatchedEvent.getName());
//		assertEquals("event has correct event type", EventType.ANALYTICS, eventHub.dispatchedEvent.getEventType());
//		assertEquals("event has correct event source", EventSource.REQUEST_CONTENT, eventHub.dispatchedEvent.getEventSource());
//	}
//
//	@Test
//	public void updateVisitorIdentifier_should_dispatchAnalyticsIdentityRequest() throws Exception {
//		core.updateVisitorIdentifier("vid-sample");
//
//		EventData data = eventHub.dispatchedEvent.getData();
//		assertEquals(1, data.size());
//		assertEquals("vid-sample", data.optString("vid", ""));
//		assertTrue("event is dispatched", eventHub.isDispatchedCalled);
//		assertEquals("event has correct name", "UpdateVisitorIdentifier", eventHub.dispatchedEvent.getName());
//		assertEquals("event has correct event type", EventType.ANALYTICS, eventHub.dispatchedEvent.getEventType());
//		assertEquals("event has correct event source", EventSource.REQUEST_IDENTITY, eventHub.dispatchedEvent.getEventSource());
//	}
//}
