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
//import java.util.Map;
//
//class MockAnalyticsRequestSerializer extends AnalyticsRequestSerializer {
//	boolean             methodBuildRequestCalled;
//	Map<String, String> paramAnalyticsData;
//	Map<String, String> paramAnalyticsVars;
//	String              buildRequestReturnValue;
//
//	@Override
//	String buildRequest(final AnalyticsState state, final Map<String, String> data, final Map<String, String> vars) {
//		methodBuildRequestCalled = true;
//		paramAnalyticsData = data;
//		paramAnalyticsVars = vars;
//		return buildRequestReturnValue;
//	}
//}
