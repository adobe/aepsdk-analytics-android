///* ***************************************************************************
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
//import org.junit.Test;
//
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import static org.junit.Assert.*;
//
//public class AnalyticsRequestSerializerTest {
//	private static final String IDENTITY_SHARED_STATE = "com.adobe.module.identity";
//	private static final String CONFIG_SHARED_STATE = "com.adobe.module.configuration";
//	private AnalyticsRequestSerializer requestSerializer = new AnalyticsRequestSerializer();
//	private AnalyticsState mockState = new AnalyticsState(null);
//
//	@Test
//	public void testGenerateAnalyticsCustomerIdString_happyFlow() throws Exception {
//		List<VisitorID> visitorIDList = new ArrayList<VisitorID>();
//		visitorIDList.add(new VisitorID("d_cid_ic", "loginidhash", "97717",
//										VisitorID.AuthenticationState.UNKNOWN));
//		visitorIDList.add(new VisitorID("d_cid_ic", "xboxlivehash", "1629158955",
//										VisitorID.AuthenticationState.AUTHENTICATED));
//		visitorIDList.add(new VisitorID("d_cid_ic", "psnidhash", "1144032295",
//										VisitorID.AuthenticationState.LOGGED_OUT));
//		visitorIDList.add(new VisitorID("d_cid", "pushid", "testPushId",
//										VisitorID.AuthenticationState.AUTHENTICATED));
//		String expectedString =
//			"&cid.&loginidhash.&id=97717&as=0&.loginidhash&xboxlivehash.&id=1629158955&as=1&.xboxlivehash&psnidhash.&id=1144032295&as=2&.psnidhash&pushid.&id=testPushId&as=1&.pushid&.cid";
//		List<String> expectedList = Arrays.asList(expectedString.split("&"));
//		Collections.sort(expectedList);
//
//		String analyticsIdString = AnalyticsRequestSerializer.generateAnalyticsCustomerIdString(visitorIDList);
//		List<String> testList = Arrays.asList(analyticsIdString.split("&"));
//		Collections.sort(testList);
//
//		assertEquals(expectedList.size(), testList.size());
//		assertTrue(String.format("Lists don't match. \nExpected: %s\nActual  : %s",
//								 expectedList.toString(), testList.toString()), expectedList.equals(testList));
//	}
//
//	@Test
//	public void testGenerateAnalyticsCustomerIdString_returnsNull_when_nullVIDList() throws Exception {
//		assertNull(AnalyticsRequestSerializer.generateAnalyticsCustomerIdString(null));
//	}
//
//	@Test
//	public void testBuildRequest_happyFlow_ValidDataAndValidVars() throws Exception {
//		Map<String, String> vars = new HashMap<String, String>();
//		vars.put("v1", "evar1Value");
//		vars.put("v2", "evar2Value");
//		Map<String, String> data = new HashMap<String, String>();
//		data.put("testKey1", "val1");
//		data.put("testKey2", "val2");
//		String result = requestSerializer.buildRequest(mockState, data, vars);
//		assertEquals("&c.&testKey2=val2&testKey1=val1&.c", getContextData(result));
//		assertEquals("ndh=1&v2=evar2Value&v1=evar1Value", getAdditionalData(result));
//		assertTrue(getCidData(result).isEmpty());
//	}
//
//	@Test
//	public void testBuildRequest_when_NullDataAndValidVars() throws Exception {
//		Map<String, String> vars = new HashMap<String, String>();
//		vars.put("v1", "evar1Value");
//		vars.put("v2", "evar2Value");
//		String result = requestSerializer.buildRequest(mockState, null, vars);
//		assertEquals("ndh=1&v2=evar2Value&v1=evar1Value", getAdditionalData(result));
//		assertTrue(getCidData(result).isEmpty());
//		assertTrue(getContextData(result).isEmpty());
//	}
//
//	@Test
//	public void testBuildRequest_when_ValidDataAndNullVars() throws Exception {
//		Map<String, String> data = new HashMap<String, String>();
//		data.put("testKey1", "val1");
//		data.put("testKey2", "val2");
//		String result = requestSerializer.buildRequest(null, data, null);
//		assertEquals("&c.&testKey2=val2&testKey1=val1&.c", getContextData(result));
//		assertEquals("ndh=1", getAdditionalData(result));
//		assertTrue(getCidData(result).isEmpty());
//	}
//
//	@Test
//	public void testBuildRequest_when_NullDataAndNullVars() throws Exception {
//		String result = requestSerializer.buildRequest(null, null, null);
//		assertEquals("ndh=1", result);
//	}
//
//	@Test
//	public void testBuildRequest_when_NullVisitorIDList() throws Exception {
//		Map<String, String> data = new HashMap<String, String>();
//		data.put("testKey1", "val1");
//		data.put("testKey2", "val2");
//
//		final Map<String, Variant> identityData = new HashMap<String, Variant>();
//		identityData.put("mid", Variant.fromString("testMID"));
//
//		final EventData configurationData = new EventData();
//		configurationData.putString("analytics.server", "analyticsServer");
//		configurationData.putString("experienceCloud.org", "marketingServer");
//
//		mockState = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(IDENTITY_SHARED_STATE, new EventData(identityData));
//				put(CONFIG_SHARED_STATE, configurationData);
//			}
//		});
//		String result = requestSerializer.buildRequest(mockState, data, null);
//
//		assertEquals("ndh=1&c.&testKey2=val2&testKey1=val1&.c", result);
//	}
//
//	@Test
//	public void testBuildRequest_removesNullDataKeys() throws Exception {
//		// the null data keys will be skipped
//		Map<String, String> vars = new HashMap<String, String>();
//		vars.put("v1", "evar1Value");
//		Map<String, String> data = new HashMap<String, String>();
//		data.put(null, "val1");
//		data.put("key2", "val2");
//		data.put(null, "val2");
//		String result = requestSerializer.buildRequest(mockState, data, vars);
//		assertEquals("&c.&key2=val2&.c", getContextData(result));
//		assertEquals("ndh=1&v1=evar1Value", getAdditionalData(result));
//		assertTrue(getCidData(result).isEmpty());
//	}
//
//	@Test
//	public void testBuildRequest_movesToVars_when_dataKeysPrefixed() throws Exception {
//		// the data keys which are prefixed with "&&" will be moved to analytics vars
//		Map<String, String> vars = new HashMap<String, String>();
//		vars.put("v1", "evar1Value");
//		Map<String, String> data = new HashMap<String, String>();
//		data.put("&&key1", "val1");
//		data.put("key2", "val2");
//		String result = requestSerializer.buildRequest(mockState, data, vars);
//		assertEquals("ndh=1&key1=val1&v1=evar1Value", getAdditionalData(result));
//		assertEquals("&c.&key2=val2&.c", getContextData(result));
//		assertTrue(getCidData(result).isEmpty());
//	}
//
//	@Test
//	public void testBuildRequest_withVisitorIDsList() throws Exception {
//		Map<String, String> vars = new HashMap<String, String>();
//		vars.put("v1", "evar1Value");
//		Map<String, String> data = new HashMap<String, String>();
//		data.put("key1", "val1");
//		final List<VisitorID> visitorIDList = new ArrayList<VisitorID>();
//		visitorIDList.add(new VisitorID("orig1", "type1", "97717",
//										VisitorID.AuthenticationState.AUTHENTICATED));
//		AnalyticsState state = new AnalyticsState(new HashMap<String, EventData>() {
//			{
//				put(IDENTITY_SHARED_STATE, new EventData() {
//					{
//						putTypedList("visitoridslist", visitorIDList, VisitorID.VARIANT_SERIALIZER);
//					}
//				});
//			}
//		});
//		state.setMarketingCloudOrganizationID("orgID");
//		String result = requestSerializer.buildRequest(state, data, vars);
//		assertEquals("ndh=1&v1=evar1Value", getAdditionalData(result));
//		assertEquals("&c.&key1=val1&.c", getContextData(result));
//		assertEquals("&cid.&type1.&as=1&id=97717&.type1&.cid", getCidData(result));
//	}
//
//	// --------------------- helper methods ----------------------
//	private String getCidData(final String source) {
//		Pattern pattern = Pattern.compile(".*(&cid\\.(.*)&\\.cid).*");
//		Matcher matcher = pattern.matcher(source);
//		return matcher.matches() ? matcher.group(1) : "";
//	}
//
//	private String getContextData(final String source) {
//		Pattern pattern = Pattern.compile(".*(&c\\.(.*)&\\.c).*");
//		Matcher matcher = pattern.matcher(source);
//		return matcher.matches() ? matcher.group(1) : "";
//	}
//
//	private String getAdditionalData(final String source) {
//		return new String(source).replace(getCidData(source), "").replace(getContextData(source), "");
//	}
//}
