//package com.adobe.marketing.mobile;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//
//@SuppressWarnings("all")
//class AnalyticsAssertions {
//
//	static void assertAnalyticsTrackDataEquals(final EventData expected,
//			final EventData actual) throws Exception {
//		if (expected == null && actual == null) {
//			return;
//		}
//
//		assertTrue(expected != null && actual != null);
//		assertEquals(expected.getString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION),
//					 actual.getString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_ACTION));
//		assertEquals(expected.getString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE),
//					 actual.getString(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE));
//		assertEquals(expected.getStringMap(AnalyticsTestConstants.EventDataKeys.Analytics.CONTEXT_DATA),
//					 actual.getMap(AnalyticsTestConstants.EventDataKeys.Analytics.CONTEXT_DATA));
//	}
//
//}
