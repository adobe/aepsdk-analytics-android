///***************************************************************************
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
// **************************************************************************/
//
//package com.adobe.marketing.mobile;
//
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MockAnalyticsDispatcherAnalyticsResponseIdentity extends AnalyticsDispatcherAnalyticsResponseIdentity {
//	boolean methodDispatchCalled;
//	List<String> dispatchParamsAid;
//
//	MockAnalyticsDispatcherAnalyticsResponseIdentity(EventHub hub, AnalyticsExtension module) {
//		super(hub, module);
//		dispatchParamsAid = new ArrayList<String>();
//	}
//
//	@Override
//	void dispatch(final String aid, final String vid, final String pairID) {
//		methodDispatchCalled = true;
//		dispatchParamsAid.add(aid);
//	}
//}
