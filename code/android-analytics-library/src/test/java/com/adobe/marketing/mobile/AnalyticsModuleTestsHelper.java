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
//class AnalyticsModuleTestsHelper {
//	private static final String CONFIG_SHARED_STATE      = "com.adobe.module.configuration";
//	private static final String IDENTITY_SHARED_STATE    = "com.adobe.module.identity";
//	private static final String ACQUISITION_SHARED_STATE = "com.adobe.module.acquisition";
//	private static final String LIFECYCLE_SHARED_STATE   = "com.adobe.module.lifecycle";
//	private MockEventHubCucumberTest eventHub;
//
//	AnalyticsModuleTestsHelper(final MockEventHubCucumberTest eventHub) {
//		this.eventHub = eventHub;
//	}
//
//	void setConfigurationSharedState(final MobilePrivacyStatus status, final int batchLimit,
//									 final boolean aamForwardingEnabled) {
//		EventData analyticsConfig = new EventData();
//		analyticsConfig.putString("analytics.server", "test.com");
//		analyticsConfig.putString("analytics.rsids", "123,132123");
//		analyticsConfig.putString("global.privacy", status.getValue());
//		analyticsConfig.putInteger("analytics.batchLimit", batchLimit);
//		analyticsConfig.putBoolean("analytics.offlineEnabled", true);
//		analyticsConfig.putBoolean("analytics.backdatePreviousSessionInfo", true);
//		analyticsConfig.putInteger("analytics.launchHitDelay", 5);
//		analyticsConfig.putBoolean("analytics.aamForwardingEnabled", aamForwardingEnabled);
//		eventHub.createSharedState(CONFIG_SHARED_STATE, eventHub.getAllEventsCount(), analyticsConfig);
//	}
//
//	void setConfigurationSharedState(final EventData data) {
//		eventHub.createSharedState(CONFIG_SHARED_STATE, eventHub.getAllEventsCount(), data);
//	}
//
//	void setIdentitySharedState() {
//		eventHub.createSharedState(IDENTITY_SHARED_STATE, eventHub.getAllEventsCount(), new EventData().putString("adid",
//								   "adid"));
//	}
//
//	void setAcquisitionSharedState(final Map<String, String> referrerData) {
//		eventHub.createSharedState(ACQUISITION_SHARED_STATE, eventHub.getAllEventsCount(), new EventData() {
//			{
//				putStringMap("referrerdata", referrerData);
//			}
//		});
//	}
//
//	void setLifecycleSharedState(final Map<String, String> contextData) {
//		eventHub.createSharedState(LIFECYCLE_SHARED_STATE, eventHub.getAllEventsCount(), new EventData() {
//			{
//				putStringMap("lifecyclecontextdata", contextData);
//			}
//		});
//	}
//
//	void setupForTracking() {
//		eventHub.ignoreAllStateChangeEvents();
//		setConfigurationSharedState(MobilePrivacyStatus.OPT_IN, 0, false);
//		setIdentitySharedState();
//	}
//
//	void dispatchConfigurationResponseEvent(final MobilePrivacyStatus status, final int batchLimit) {
//		EventData analyticsConfig = new EventData();
//		analyticsConfig.putString("analytics.server", "test.com");
//		analyticsConfig.putString("analytics.rsids", "123,132123");
//		analyticsConfig.putString("global.privacy", status.getValue());
//		analyticsConfig.putInteger("analytics.batchLimit", batchLimit);
//		analyticsConfig.putBoolean("analytics.offlineEnabled", true);
//		analyticsConfig.putBoolean("analytics.backdatePreviousSessionInfo", true);
//		analyticsConfig.putInteger("analytics.launchHitDelay", 5);
//		final Event event = new Event.Builder("Configuration Response Event", EventType.CONFIGURATION,
//											  EventSource.RESPONSE_CONTENT).setData(analyticsConfig).build();
//		eventHub.createSharedState(CONFIG_SHARED_STATE, eventHub.getAllEventsCount(), analyticsConfig);
//		eventHub.dispatch(event);
//	}
//}
