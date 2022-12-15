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
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//class MockAnalyticsDispatcherAnalyticsResponseContent extends AnalyticsDispatcherAnalyticsResponseContent {
//
//	MockAnalyticsDispatcherAnalyticsResponseContent(EventHub hub, AnalyticsExtension module) {
//		super(hub, module);
//		dispatchAnalyticsHitResponseParamServerResponse = new ArrayList<String>();
//		dispatchAnalyticsHitResponseParamHeaderResponse = new ArrayList<HashMap<String, String>>();
//		dispatchAnalyticsHitResponseParamRequestIdentifier = new ArrayList<String>();
//		dispatchAnalyticsHitResponseParamHitHost = new ArrayList<String>();
//		dispatchAnalyticsHitResponseParamHitUrl = new ArrayList<String>();
//	}
//
//	boolean      dispatchAnalyticsHitResponseWasCalled;
//	List<String> dispatchAnalyticsHitResponseParamServerResponse;
//	List<HashMap<String, String>> dispatchAnalyticsHitResponseParamHeaderResponse;
//	List<String> dispatchAnalyticsHitResponseParamRequestIdentifier;
//	List<String> dispatchAnalyticsHitResponseParamHitHost;
//	List<String> dispatchAnalyticsHitResponseParamHitUrl;
//
//	@Override
//	void dispatchAnalyticsHitResponse(final String serverResponse, final HashMap<String, String> headers,
//									  final String requestEventIdentifier, final String hitHost, final String hitUrl) {
//		dispatchAnalyticsHitResponseWasCalled = true;
//		dispatchAnalyticsHitResponseParamServerResponse.add(serverResponse);
//		dispatchAnalyticsHitResponseParamHeaderResponse.add(headers);
//		dispatchAnalyticsHitResponseParamRequestIdentifier.add(requestEventIdentifier);
//		dispatchAnalyticsHitResponseParamHitHost.add(hitHost);
//		dispatchAnalyticsHitResponseParamHitUrl.add(hitUrl);
//		super.dispatchAnalyticsHitResponse(serverResponse, headers, requestEventIdentifier, hitHost, hitUrl);
//	}
//
//	boolean dispatchQueueSizeWasCalled;
//	long    dispatchQueueSizeParamSize;
//	String  dispatchQueueSizeParamPairID;
//
//	@Override
//	void dispatchQueueSize(final long size, final String pairID) {
//		dispatchQueueSizeWasCalled = true;
//		dispatchQueueSizeParamSize = size;
//		dispatchQueueSizeParamPairID = pairID;
//	}
//}
