//package com.adobe.marketing.mobile;
//
//import org.junit.Test;
//
//import java.util.Map;
//
//import static org.junit.Assert.*;
//
//public class AnalyticsHitSchemaTest {
//	private static final String HIT_ID_COL_NAME = "ID";
//	private static final int HIT_ID_COL_INDEX = 0;
//	private static final String HIT_URL_COL_NAME = "URL";
//	private static final int HIT_URL_COL_INDEX = 1;
//	private static final String HIT_TIMESTAMP_COL_NAME = "TIMESTAMP";
//	private static final int HIT_TIMESTAMP_COL_INDEX = 2;
//	private static final String HIT_SERVER_COL_NAME = "SERVER";
//	private static final int HIT_SERVER_COL_INDEX = 3;
//	private static final String HIT_OFFLINE_TRACKING_COL_NAME = "OFFLINETRACKING";
//	private static final int HIT_OFFLINE_TRACKING_COL_INDEX = 4;
//	private static final String HIT_IS_WAITING_COL_NAME = "ISWAITING";
//	private static final int HIT_IS_WAITING_COL_INDEX = 5;
//	private static final String HIT_IS_PLACEHOLDER_COL_NAME = "ISPLACEHOLDER";
//	private static final int HIT_IS_PLACEHOLDER_COL_INDEX = 6;
//	private static final String HIT_AAM_FORWARDING_COL_NAME = "AAMFORWARDING";
//	private static final int HIT_AAM_FORWARDING_COL_INDEX = 7;
//
//	@Test
//	public void testGenerateDataMap() {
//		AnalyticsHitSchema schema = new AnalyticsHitSchema();
//		AnalyticsHit newHit = new AnalyticsHit();
//		newHit.identifier = "id";
//		newHit.server = "server";
//		newHit.url = "url";
//		newHit.timestamp = 123;
//		newHit.offlineTrackingEnabled = true;
//		newHit.isWaiting = true;
//		newHit.isBackdatePlaceHolder = true;
//		newHit.aamForwardingEnabled = true;
//		Map<String, Object> values = schema.generateDataMap(newHit);
//		assertFalse(values.containsKey(HIT_ID_COL_NAME));
//		assertEquals("server", values.get(HIT_SERVER_COL_NAME));
//		assertEquals(123L, values.get(HIT_TIMESTAMP_COL_NAME));
//		assertEquals(true, values.get(HIT_IS_WAITING_COL_NAME));
//		assertEquals(true, values.get(HIT_IS_PLACEHOLDER_COL_NAME));
//		assertEquals(true, values.get(HIT_OFFLINE_TRACKING_COL_NAME));
//		assertEquals("url", values.get(HIT_URL_COL_NAME));
//		assertEquals(true, values.get(HIT_AAM_FORWARDING_COL_NAME));
//	}
//
//	@Test
//	public void testGenerateHit() {
//		DatabaseService.QueryResult queryResult = new MockQueryResult(new Object[][] {
//					new Object[]{"id", "url", 123L, "server", 1, 1, 1, 1, "test-event-uuid"}
//				});
//		AnalyticsHit newHit = new AnalyticsHitSchema().generateHit(queryResult);
//		assertEquals("server", newHit.server);
//		assertEquals(123L, newHit.timestamp);
//		assertEquals("url", newHit.url);
//		assertTrue(newHit.isBackdatePlaceHolder);
//		assertTrue(newHit.isWaiting);
//		assertTrue(newHit.offlineTrackingEnabled);
//		assertTrue(newHit.aamForwardingEnabled);
//		assertEquals("test-event-uuid", newHit.uniqueEventIdentifier);
//	}
//
//	@Test
//	public void testGenerateHit_FalseVariables() {
//		DatabaseService.QueryResult queryResult = new MockQueryResult(new Object[][] {
//					new Object[]{"id", "url", 123L, "server", 0, 0, 0, 0, "test-event-uuid"}
//				});
//		AnalyticsHit newHit = new AnalyticsHitSchema().generateHit(queryResult);
//		assertEquals("server", newHit.server);
//		assertEquals(123L, newHit.timestamp);
//		assertEquals("url", newHit.url);
//		assertFalse(newHit.isBackdatePlaceHolder);
//		assertFalse(newHit.isWaiting);
//		assertFalse(newHit.offlineTrackingEnabled);
//		assertFalse(newHit.aamForwardingEnabled);
//		assertEquals("test-event-uuid", newHit.uniqueEventIdentifier);
//	}
//
//	@Test
//	public void testGetFirstWaitingHitQuery() {
//		Query query = new AnalyticsHitSchema().getFirstWaitingHitQuery("table");
//		assertEquals("1", query.getLimit());
//		assertEquals("ID ASC", query.getOrderBy());
//		assertEquals("ISWAITING = ?", query.getSelection());
//		assertEquals("1", query.getSelectionArgs()[0]);
//	}
//
//	@Test
//	public void testGetLatestPlaceHolderHitQuery() {
//		Query query = new AnalyticsHitSchema().getLatestPlaceHolderHitQuery("table");
//		assertEquals("1", query.getLimit());
//		assertEquals("ID DESC", query.getOrderBy());
//		assertEquals("ISPLACEHOLDER = ?", query.getSelection());
//		assertEquals("1", query.getSelectionArgs()[0]);
//	}
//
//
//	@Test
//	public void testGetRegularHitQuery() {
//		Query query = new AnalyticsHitSchema().getRegularHitQuery("table");
//		assertNull(query.getLimit());
//		assertEquals("ID DESC", query.getOrderBy());
//		assertEquals("ISPLACEHOLDER = ?", query.getSelection());
//		assertEquals("0", query.getSelectionArgs()[0]);
//	}
//
//	@Test
//	public void testGenerateUpdateValuesForResetIsWaitingFlag() {
//		Map<String, Object> map = new AnalyticsHitSchema().generateUpdateValuesForResetIsWaitingFlag();
//		assertEquals(0, map.get("ISWAITING"));
//	}
//}
