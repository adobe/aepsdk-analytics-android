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
//class TestableAnalyticsHitsDatabase extends AnalyticsHitsDatabase {
//
//	TestableAnalyticsHitsDatabase(final FakePlatformServices fakePlatformServices, final AnalyticsProperties properties,
//								  final AnalyticsDispatcherAnalyticsResponseContent dispatcher) throws MissingPlatformServicesException {
//		super(fakePlatformServices, properties, dispatcher);
//	}
//
//	boolean kickWasCalled;
//	boolean kickParamIgnoreBatchLimit;
//	@Override
//	void kick(final AnalyticsState state, final boolean ignoreBatchLimit) {
//		super.kick(state, ignoreBatchLimit);
//		kickWasCalled = true;
//		kickParamIgnoreBatchLimit = ignoreBatchLimit;
//	}
//
//	boolean queueWasCalled;
//	@Override
//	void queue(final AnalyticsState state, final String url, final long timeStamp, final boolean shouldBeWaiting,
//			   final boolean isPlaceholder, final String uniqueEventIdentifier) {
//		super.queue(state, url, timeStamp, shouldBeWaiting, isPlaceholder, uniqueEventIdentifier);
//		queueWasCalled = true;
//	}
//
//	boolean clearWasCalled;
//	@Override
//	protected void clearTrackingQueue() {
//		super.clearTrackingQueue();
//		clearWasCalled = true;
//	}
//
//}
