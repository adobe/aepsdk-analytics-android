///* ***********************************************************************
// * ADOBE CONFIDENTIAL
// * ___________________
// *
// * Copyright 2019 Adobe Systems Incorporated
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
// **************************************************************************/
//
//package com.adobe.marketing.mobile;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import java.util.HashMap;
//
//public class AnalyticsListenerRulesEngineResponseContentTests  extends BaseTest {
//	MockAnalytics module;
//	AnalyticsListenerRulesEngineResponseContent listener;
//	FakePlatformServices fakePlatformServices;
//
//	private Event createEmptyDataAnalyticsEvent() {
//		EventData data = new EventData();
//		return new Event.Builder("AnalyticsConsequenceTest", EventType.RULES_ENGINE,
//								 EventSource.RESPONSE_CONTENT).setData(data).build();
//	}
//
//	private Event createValidAnalyticsEvent() {
//		HashMap<String, Variant> triggeredConsequence = new HashMap<String, Variant>();
//		HashMap<String, Variant> consequenceDetail = new HashMap<String, Variant>();
//
//		consequenceDetail.put(AnalyticsTestConstants.EventDataKeys.Analytics.TRACK_STATE, Variant.fromString("myState"));
//
//		triggeredConsequence.put(AnalyticsTestConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_ID,
//								 Variant.fromString("my-id"));
//		triggeredConsequence.put(AnalyticsTestConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_TYPE,
//								 Variant.fromString(AnalyticsTestConstants.EventDataKeys.Analytics.RULES_CONSEQUENCE_TYPE_TRACK));
//		triggeredConsequence.put(AnalyticsTestConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_DETAIL,
//								 Variant.fromVariantMap(consequenceDetail));
//
//		EventData data = new EventData();
//		data.putVariantMap(AnalyticsTestConstants.EventDataKeys.RuleEngine.CONSEQUENCE_TRIGGERED, triggeredConsequence);
//
//		return new Event.Builder("AnalyticsConsequenceTest", EventType.RULES_ENGINE,
//								 EventSource.RESPONSE_CONTENT).setData(data).build();
//	}
//
//	@Before()
//	public void beforeEach() {
//		fakePlatformServices = new FakePlatformServices();
//		module = new MockAnalytics(new MockEventHubUnitTest("UnitTest", fakePlatformServices), fakePlatformServices);
//		listener = new AnalyticsListenerRulesEngineResponseContent(module, null, null);
//	}
//
//	@Test
//	public void notCallOnReceiveAnalyticsEvent_When_EventDoesNotContainData() throws Exception {
//		Event e = createEmptyDataAnalyticsEvent();
//		listener.hear(e);
//		waitForExecutor(module.getExecutor());
//		Assert.assertNull(module.handleAnalyticsConsequenceEventEvent);
//	}
//
//	@Test
//	public void callOnReceiveAnalyticsEvent_When_EventContainsData() throws Exception {
//		Event e = createValidAnalyticsEvent();
//		listener.hear(e);
//		waitForExecutor(module.getExecutor());
//		Assert.assertEquals(e, module.handleAnalyticsConsequenceEventEvent);
//	}
//
//}