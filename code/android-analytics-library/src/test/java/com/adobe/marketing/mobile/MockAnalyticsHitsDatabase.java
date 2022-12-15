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
//import java.util.Map;
//
//public class MockAnalyticsHitsDatabase extends AnalyticsHitsDatabase {
//
//	MockAnalyticsHitsDatabase(final FakePlatformServices fakePlatformServices, final AnalyticsProperties properties,
//							  final AnalyticsDispatcherAnalyticsResponseContent dispatcher) throws MissingPlatformServicesException {
//		super(fakePlatformServices, properties, dispatcher);
//	}
//
//	boolean kickWithAdditionalDataWasCalled;
//	AnalyticsState kickWithAdditionalDataParamState;
//	Map<String, String> kickWithAdditionalDataParamData;
//
//	@Override
//	void kickWithAdditionalData(final AnalyticsState state, final Map<String, String> data) {
//		kickWithAdditionalDataWasCalled = true;
//		kickWithAdditionalDataParamState = state;
//		kickWithAdditionalDataParamData = data;
//	}
//
//	boolean kickWasCalled;
//	boolean kickParamIgnoreBatchLimit;
//
//	@Override
//	void kick(final AnalyticsState state, final boolean ignoreBatchLimit) {
//		kickWasCalled = true;
//		kickParamIgnoreBatchLimit = ignoreBatchLimit;
//	}
//
//	boolean queueWasCalled;
//	AnalyticsState queueParamState;
//	String queueParamUrl;
//	boolean queueParamShouldBeWaiting;
//	boolean queueParamIsPlaceholder;
//	String queueParamEventIdentifier;
//	@Override
//	void queue(final AnalyticsState state, final String url, final long timeStamp, final boolean shouldBeWaiting,
//			   final boolean isPlaceholder, final String uniqueEventIdentifier) {
//		queueWasCalled = true;
//		queueParamState = state;
//		queueParamUrl = url;
//		queueParamShouldBeWaiting = shouldBeWaiting;
//		queueParamIsPlaceholder = isPlaceholder;
//		queueParamEventIdentifier = uniqueEventIdentifier;
//	}
//
//	boolean clearWasCalled;
//
//	@Override
//	protected void clearTrackingQueue() {
//		clearWasCalled = true;
//	}
//
//	long getTrackingQueueSizeReturnValue = 0;
//
//	@Override
//	protected long getTrackingQueueSize() {
//		return getTrackingQueueSizeReturnValue;
//	}
//
//	boolean forceKickWasCalled;
//
//	@Override
//	void forceKick(final AnalyticsState state) {
//		forceKickWasCalled = true;
//	}
//
//	boolean isHitWaitingReturnValue = false;
//	@Override
//	boolean isHitWaiting() {
//		return isHitWaitingReturnValue;
//	}
//}
