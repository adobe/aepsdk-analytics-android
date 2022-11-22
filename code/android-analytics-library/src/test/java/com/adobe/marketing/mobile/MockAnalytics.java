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
//import java.util.List;
//import java.util.Map;
//
//@SuppressWarnings("all")
//class MockAnalytics extends AnalyticsExtension {
//	boolean                   methodUpdateVisitorIDCalled;
//	String                    configVisitorID;
//	Boolean                   configAnalyticsForwardingEnabled;
//	Boolean                   configBackdateSessionInfo;
//	Boolean                   configOfflineTracking;
//	String                    configServer;
//	String                    configRsids;
//	Integer                   configBatchLimit;
//	String                    configCharset;
//	boolean                   methodHandleTrackActionInformationCalled;
//	boolean                   methodUpdatePrivacyStatusCalled;
//	boolean                   methodHandleTrackStateCalled;
//	Map<String, String>       paramTrackContextData;
//	String                    paramTrackActionName;
//	boolean                   paramTrackInternal;
//	Integer                   configReferrerTimeout;
//	String                    paramTrackStateName;
//	long                      eventTimestamp;
//	List<Map<String, String>> visitorIDsList;
//
//	boolean                   updateVisitorIDsListAndHashWasCalled;
//	int                       updateVisitorIDsListAndHashParamVisitorIDsHash;
//	List<Map<String, String>> updateVisitorIDsListAndHashParamVisitorIDs;
//	EventHub hub;
//
//	MockAnalytics(final EventHub hub, final PlatformServices services) {
//		super(hub, services);
//		this.hub = hub;
//	}
//
//	MockAnalytics(final EventHub hub, final PlatformServices services, final AnalyticsState state) {
//		super(hub, services);
//	}
//
//	@Override
//	void registerAnalyticsDispatchers() {
//	}
//
//	@Override
//	void updatePrivacyStatus(final int eventNumber, final AnalyticsState state) {
//		methodUpdatePrivacyStatusCalled = true;
//	}
//
//	boolean   methodTrackDataCalled;
//	EventData paramAnalyticsData;
//	long      paramTimestamp;
//	boolean   paramAppendToPlaceHolder;
//	String    paramEventUniqueIdentifier;
//
//	@Override
//	void track(AnalyticsState state, EventData request, long timestamp,
//			   final boolean appendToPlaceHolder, final String eventUniqueIdentifier) {
//		methodTrackDataCalled = true;
//		paramAnalyticsData = request;
//		paramTimestamp = timestamp;
//		paramAppendToPlaceHolder = appendToPlaceHolder;
//		paramEventUniqueIdentifier = eventUniqueIdentifier;
//	}
//
//	boolean clearAllHitsWasCalled;
//
//	@Override
//	void clearAllHits() {
//		clearAllHitsWasCalled = true;
//	}
//
//	boolean getTrackingQueueSizeWasCalled;
//	String  getTrackingQueueSizeParameterPairID;
//	@Override
//	void getTrackingQueueSize(final String pairID) {
//		getTrackingQueueSizeWasCalled = true;
//		getTrackingQueueSizeParameterPairID = pairID;
//	}
//
//	boolean handleAnalyticsRequestWasCalled;
//	@Override
//	void handleAnalyticsRequestEvent(final Event event) {
//		handleAnalyticsRequestWasCalled = true;
//	}
//
//	boolean processWasCalled;
//	@Override
//	void process(final Event event, final Map<String, EventData> sharedData) {
//		processWasCalled = true;
//		super.process(event, sharedData);
//	}
//
//	boolean handleLifecycleEventWasCalled;
//	Event handleLifecycleEventParamEvent;
//	@Override
//	void handleLifecycleResponseEvent(final Event event) {
//		handleLifecycleEventWasCalled = true;
//		handleLifecycleEventParamEvent = event;
//	}
//
//	boolean handleSharedStateUpdateEventWasCalled;
//	String handleSharedStateUpdateEventParamStateOwner;
//	@Override
//	void handleSharedStateUpdateEvent(final String stateOwner) {
//		handleSharedStateUpdateEventWasCalled = true;
//		handleSharedStateUpdateEventParamStateOwner = stateOwner;
//	}
//
//	boolean handleHubBootedEventWasCalled;
//	Event handleHubBootedEventParamEvent;
//	@Override
//	void handleHubBootedEvent(final Event event) {
//		handleHubBootedEventWasCalled = true;
//		handleHubBootedEventParamEvent = event;
//	}
//
//	boolean processAnalyticsIDRequestWasCalled;
//	Event processAnalyticsIDRequestParamEvent;
//	@Override
//	void processAnalyticsIDRequest(final Event event) {
//		processAnalyticsIDRequestWasCalled = true;
//		processAnalyticsIDRequestParamEvent = event;
//	}
//
//	boolean handleAcquisitionResponseEventWasCalled;
//	Event handleAcquisitionResponseEventParamEvent;
//	@Override
//	void handleAcquisitionResponseEvent(final Event event) {
//		handleAcquisitionResponseEventWasCalled = true;
//		handleAcquisitionResponseEventParamEvent = event;
//	}
//
//	boolean handleAnalyticsConsequenceEventCalled;
//	Event handleAnalyticsConsequenceEventEvent;
//	@Override
//	void handleAnalyticsConsequenceEvent(final Event event) {
//		handleAnalyticsConsequenceEventCalled = true;
//		this.handleAnalyticsConsequenceEventEvent = event;
//	}
//
//	boolean handleConfigurationUpdateEventCalled;
//	Event handleConfigurationUpdateEventEvent;
//	@Override
//	void handleConfigurationUpdateEvent(final Event event) {
//		handleConfigurationUpdateEventCalled = true;
//		this.handleConfigurationUpdateEventEvent = event;
//	}
//}
