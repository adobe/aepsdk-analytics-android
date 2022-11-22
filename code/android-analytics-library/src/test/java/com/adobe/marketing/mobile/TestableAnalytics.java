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
//class TestableAnalytics extends AnalyticsExtension {
//	private EventHub hub;
//
//	TestableAnalytics(final EventHub hub, final PlatformServices services) {
//		super(hub, services);
//		this.hub = hub;
//	}
//
//	void setHelpers(final AnalyticsHitsDatabase hitsDatabase, final AnalyticsProperties properties,
//					final AnalyticsRequestSerializer analyticsRequestSerializer) {
//		this.hitDatabase = hitsDatabase;
//		this.analyticsProperties = properties;
//		this.analyticsRequestSerializer = analyticsRequestSerializer;
//	}
//
//	void registerAnalyticsDispatchers() {
//		this.analyticsIdentityResponseDispatcher = new MockAnalyticsDispatcherAnalyticsResponseIdentity(hub, this);
//		this.analyticsResponseDispatcher = new MockAnalyticsDispatcherAnalyticsResponseContent(hub, this);
//	}
//
//	boolean trackCallRealMethod = true;
//	List<TrackMethodParameters> trackMethodParameters = new ArrayList<TrackMethodParameters>();
//
//	static class TrackMethodParameters {
//		AnalyticsState state;
//		EventData      request;
//		long           timestamp;
//
//		TrackMethodParameters(AnalyticsState state, EventData request, long timestamp) {
//			this.state = state;
//			this.request = request;
//			this.timestamp = timestamp;
//		}
//	}
//
//	@Override
//	void track(final AnalyticsState state, final EventData analyticsData, final long timestamp,
//			   final boolean appendToPlaceHolder, final String eventUniqueIdentifier) {
//		if (trackCallRealMethod) {
//			super.track(state, analyticsData, timestamp, appendToPlaceHolder, eventUniqueIdentifier);
//		}
//
//		trackMethodParameters.add(new TrackMethodParameters(state, analyticsData, timestamp));
//	}
//
//	boolean processEventsWasCalled;
//	@Override
//	void processEvents() {
//		super.processEvents();
//		processEventsWasCalled = true;
//	}
//
//	boolean trackLifecycleWasCalled;
//	@Override
//	void trackLifecycle(final AnalyticsState state, final Event event)  {
//		super.trackLifecycle(state, event);
//		trackLifecycleWasCalled = true;
//	}
//
//	boolean clearAllHitsWasCalled;
//	@Override
//	void clearAllHits() {
//		super.clearAllHits();
//		clearAllHitsWasCalled = true;
//	}
//
//	boolean getTrackingQueueSizeCalled;
//	@Override
//	void getTrackingQueueSize(String pairID) {
//		super.getTrackingQueueSize(pairID);
//		getTrackingQueueSizeCalled = true;
//	}
//}
