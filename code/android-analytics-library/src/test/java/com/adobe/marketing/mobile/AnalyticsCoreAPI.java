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
//class AnalyticsCoreAPI {
//	private static final String TRACK_REQUEST_EVENT_KEY  = "trackRequest";
//	private static final String GET_QUEUE_SIZE_EVENT_KEY = "getqueuesize";
//	private static final String CLEAR_QUEUE_EVENT_KEY    = "clearhitsqueue";
//	private static final String FORCE_KICK_EVENT_KEY     = "forcekick";
//	private static final String QUEUE_SIZE_RESPONSE_KEY  = "queuesize";
//	private static final String AID_RESPONSE_KEY         = "aid";
//	private static final String VID_REQUEST_KEY         = "vid";
//	private MockEventHubCucumberTest eventHub;
//
//	AnalyticsCoreAPI(final MockEventHubCucumberTest eventHub) {
//		this.eventHub = eventHub;
//	}
//
//	void trackAnalyticsRequest(final EventData eventData) {
//		if (eventData == null) {
//			return;
//		}
//
//		// Analytics module will not process a track event unless the event
//		// contains either "state", "action", or "contextData" keys
//		if (!eventData.containsKey(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE)) {
//			eventData.putString(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE, null);
//		}
//
//		final Event analyticsReqEvent = new Event.Builder("AnalyticsRequest", EventType.GENERIC_TRACK,
//				EventSource.REQUEST_CONTENT).setData(eventData).build();
//		eventHub.dispatch(analyticsReqEvent);
//	}
//
//	void getQueueSize(final AdobeCallback<Long> callback) {
//		final Event analyticsReqEvent = new Event.Builder("GetQueueSize", EventType.ANALYTICS,
//				EventSource.REQUEST_CONTENT).setData(new EventData().putBoolean(GET_QUEUE_SIZE_EVENT_KEY, true)).build();
//
//
//		eventHub.registerOneTimeListener(analyticsReqEvent.getResponsePairID(), new Module.OneTimeListenerBlock() {
//			@Override
//			public void call(final Event e) {
//				EventData eventData = e.getData();
//				long queueSize = 0L;
//
//				if (eventData != null) {
//					queueSize = eventData.optLong(QUEUE_SIZE_RESPONSE_KEY, 0L);
//				}
//
//				callback.call(queueSize);
//			}
//		});
//
//
//		eventHub.dispatch(analyticsReqEvent);
//	}
//
//
//	void clearQueue() {
//		eventHub.dispatch(new Event.Builder("ClearHitsQueue", EventType.ANALYTICS,
//											EventSource.REQUEST_CONTENT).setData(new EventData().putBoolean(CLEAR_QUEUE_EVENT_KEY, true)).build());
//	}
//
//	void getTrackingIdentifier(final AdobeCallback<String> callback) {
//		final Event analyticsReqEvent = new Event.Builder("GetTrackingIdentifier", EventType.ANALYTICS,
//				EventSource.REQUEST_IDENTITY).build();
//
//		eventHub.registerOneTimeListener(analyticsReqEvent.getResponsePairID(), new Module.OneTimeListenerBlock() {
//			@Override
//			public void call(final Event e) {
//				EventData eventData = e.getData();
//				callback.call(eventData != null ? eventData.optString(AID_RESPONSE_KEY, null) : null);
//			}
//		});
//
//		eventHub.dispatch(analyticsReqEvent);
//	}
//
//	void sendQueuedHits() {
//		eventHub.dispatch(new Event.Builder("ForceKickHits", EventType.ANALYTICS,
//											EventSource.REQUEST_CONTENT).setData(new EventData().putBoolean(FORCE_KICK_EVENT_KEY, true)).build());
//	}
//
//	void updateVisitorIdentifier(final String vid) {
//		eventHub.dispatch(new Event.Builder("UpdateVisitorIdentifier", EventType.ANALYTICS,
//											EventSource.REQUEST_IDENTITY).setData(new EventData().putString(VID_REQUEST_KEY, vid)).build());
//	}
//
//	void getVisitorIdentifier(final AdobeCallback<String> callback) {
//		final Event analyticsReqEvent = new Event.Builder("GetVisitorIdentifier", EventType.ANALYTICS,
//				EventSource.REQUEST_IDENTITY).build();
//
//		eventHub.registerOneTimeListener(analyticsReqEvent.getResponsePairID(), new Module.OneTimeListenerBlock() {
//			@Override
//			public void call(final Event e) {
//				EventData eventData = e.getData();
//				callback.call(eventData != null ? eventData.optString(VID_REQUEST_KEY, null) : null);
//			}
//		});
//
//		eventHub.dispatch(analyticsReqEvent);
//	}
//}
