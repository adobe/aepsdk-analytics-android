///* **************************************************************************
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
//import java.util.HashMap;
//
//final class AnalyticsTestConstants {
//
//	static final class Default {
//		static final MobilePrivacyStatus DEFAULT_PRIVACY_STATUS     = MobilePrivacyStatus.OPT_IN;
//		static final boolean    DEFAULT_FORWARDING_ENABLED             = false;
//		static final boolean    DEFAULT_OFFLINE_ENABLED                = false;
//		static final boolean    DEFAULT_BACKDATE_SESSION_INFO_ENABLED  = false;
//		static final int        DEFAULT_BATCH_LIMIT                    = 0;
//		static final int        DEFAULT_REFERRER_TIMEOUT               = 0;
//		static final int 		DEFAULT_LIFECYCLE_RESPONSE_WAIT_TIMEOUT   = 1000; //ms
//		static final int 		DEFAULT_LAUNCH_DEEPLINK_DATA_WAIT_TIMEOUT = 500; //ms
//
//		private Default() {}
//	}
//
//	// ================================================================================
//	// String value constants - Data store names
//	// ================================================================================
//	static final String ANALYTICS_DATA_STORAGE = "AnalyticsDataStorage";
//
//	static final String ANALYTICS_REQUEST_VISITOR_ID_KEY            = "vid";
//	static final String ANALYTICS_REQUEST_CHARSET_KEY               = "ce";
//	static final String ANALYTICS_REQUEST_FORMATTED_TIMESTAMP_KEY   = "t";
//	static final String ANALYTICS_REQUEST_STRING_TIMESTAMP_KEY      = "ts";
//	static final String ANALYTICS_REQUEST_CONTEXT_DATA_KEY          = "c";
//	static final String ANALYTICS_REQUEST_PAGE_NAME_KEY             = "pageName";
//	static final String ANALYTICS_REQUEST_IGNORE_PAGE_NAME_KEY      = "pe";
//	static final String ANALYTICS_REQUEST_CUSTOMER_PERSPECTIVE_KEY  = "cp";
//	static final String ANALYTICS_REQUEST_ACTION_NAME_KEY           = "pev2";
//	static final String ANALYTICS_REQUEST_ANALYTICS_ID_KEY          = "aid";
//	static final String ANALYTICS_REQUEST_PRIVACY_MODE_KEY          = "a.privacy.mode";
//
//	static final String ANALYTICS_PARAMETER_KEY_MID           = "mid";
//	static final String ANALYTICS_PARAMETER_KEY_LOCATION_HINT = "aamlh";
//	static final String ANALYTICS_PARAMETER_KEY_BLOB          = "aamb";
//
//	static final String SESSION_INFO_INTERNAL_ACTION_NAME         = "SessionInfo";
//	static final String CRASH_INTERNAL_ACTION_NAME = "Crash";
//	static final String LIFECYCLE_INTERNAL_ACTION_NAME            = "Lifecycle";
//
//	static final String APP_STATE_FOREGROUND = "foreground";
//	static final String APP_STATE_BACKGROUND = "background";
//
//	static final String ANALYTICS_REQUEST_PRIVACY_MODE_UNKNOWN          = "unknown";
//
//	// ================================================================================
//	// Default values
//	// ================================================================================
//	static final int                 CONNECTION_TIMEOUT_SEC     = 5; // default connection timeout in seconds
//	static final String              IGNORE_PAGE_NAME_VALUE     = "lnk_o";
//	static final String              ACTION_PREFIX              = "AMACTION:";
//	static final String              INTERNAL_ACTION_PREFIX     = "ADBINTERNAL:";
//	static final String              VAR_ESCAPE_PREFIX          = "&&";
//
//	private AnalyticsTestConstants() {}
//
//	static final HashMap<String, String> MAP_TO_CONTEXT_DATA_KEYS = createMap();
//	static HashMap<String, String> createMap() {
//		final HashMap<String, String> map = new HashMap<String, String>();
//		map.put(EventDataKeys.Identity.ADVERTISING_IDENTIFIER, ContextDataKeys.ADVERTISING_IDENTIFIER);
//		map.put(EventDataKeys.Lifecycle.APP_ID, ContextDataKeys.APPLICATION_IDENTIFIER);
//		map.put(EventDataKeys.Lifecycle.CARRIER_NAME, ContextDataKeys.CARRIER_NAME);
//		map.put(EventDataKeys.Lifecycle.CRASH_EVENT, ContextDataKeys.CRASH_EVENT_KEY);
//		map.put(EventDataKeys.Lifecycle.DAILY_ENGAGED_EVENT, ContextDataKeys.DAILY_ENGAGED_EVENT_KEY);
//		map.put(EventDataKeys.Lifecycle.DAY_OF_WEEK, ContextDataKeys.DAY_OF_WEEK);
//		map.put(EventDataKeys.Lifecycle.DAYS_SINCE_FIRST_LAUNCH, ContextDataKeys.DAYS_SINCE_FIRST_LAUNCH);
//		map.put(EventDataKeys.Lifecycle.DAYS_SINCE_LAST_LAUNCH, ContextDataKeys.DAYS_SINCE_LAST_LAUNCH);
//		map.put(EventDataKeys.Lifecycle.DAYS_SINCE_LAST_UPGRADE, ContextDataKeys.DAYS_SINCE_LAST_UPGRADE);
//		map.put(EventDataKeys.Lifecycle.DEVICE_NAME, ContextDataKeys.DEVICE_NAME);
//		map.put(EventDataKeys.Lifecycle.DEVICE_RESOLUTION, ContextDataKeys.DEVICE_RESOLUTION);
//		map.put(EventDataKeys.Lifecycle.HOUR_OF_DAY, ContextDataKeys.HOUR_OF_DAY);
//		map.put(EventDataKeys.Lifecycle.IGNORED_SESSION_LENGTH, ContextDataKeys.IGNORED_SESSION_LENGTH);
//		map.put(EventDataKeys.Lifecycle.INSTALL_DATE, ContextDataKeys.INSTALL_DATE);
//		map.put(EventDataKeys.Lifecycle.INSTALL_EVENT, ContextDataKeys.INSTALL_EVENT_KEY);
//		map.put(EventDataKeys.Lifecycle.LAUNCH_EVENT, ContextDataKeys.LAUNCH_EVENT_KEY);
//		map.put(EventDataKeys.Lifecycle.LAUNCHES, ContextDataKeys.LAUNCHES);
//		map.put(EventDataKeys.Lifecycle.LAUNCHES_SINCE_UPGRADE, ContextDataKeys.LAUNCHES_SINCE_UPGRADE);
//		map.put(EventDataKeys.Lifecycle.LOCALE, ContextDataKeys.LOCALE);
//		map.put(EventDataKeys.Lifecycle.MONTHLY_ENGAGED_EVENT, ContextDataKeys.MONTHLY_ENGAGED_EVENT_KEY);
//		map.put(EventDataKeys.Lifecycle.OPERATING_SYSTEM, ContextDataKeys.OPERATING_SYSTEM);
//		map.put(EventDataKeys.Lifecycle.PREVIOUS_SESSION_LENGTH, ContextDataKeys.PREVIOUS_SESSION_LENGTH);
//		map.put(EventDataKeys.Lifecycle.RUN_MODE, ContextDataKeys.RUN_MODE);
//		map.put(EventDataKeys.Lifecycle.UPGRADE_EVENT, ContextDataKeys.UPGRADE_EVENT_KEY);
//
//		return map;
//	}
//
//	static final class ContextDataKeys {
//		static final String INSTALL_EVENT_KEY         = "a.InstallEvent";
//		static final String LAUNCH_EVENT_KEY          = "a.LaunchEvent";
//		static final String CRASH_EVENT_KEY           = "a.CrashEvent";
//		static final String UPGRADE_EVENT_KEY         = "a.UpgradeEvent";
//		static final String DAILY_ENGAGED_EVENT_KEY   = "a.DailyEngUserEvent";
//		static final String MONTHLY_ENGAGED_EVENT_KEY = "a.MonthlyEngUserEvent";
//		static final String INSTALL_DATE              = "a.InstallDate";
//		static final String LAUNCHES                  = "a.Launches";
//		static final String PREVIOUS_SESSION_LENGTH   = "a.PrevSessionLength";
//		static final String DAYS_SINCE_FIRST_LAUNCH   = "a.DaysSinceFirstUse";
//		static final String DAYS_SINCE_LAST_LAUNCH    = "a.DaysSinceLastUse";
//		static final String HOUR_OF_DAY               = "a.HourOfDay";
//		static final String DAY_OF_WEEK               = "a.DayOfWeek";
//		static final String OPERATING_SYSTEM          = "a.OSVersion";
//		static final String APPLICATION_IDENTIFIER    = "a.AppID";
//		static final String DAYS_SINCE_LAST_UPGRADE   = "a.DaysSinceLastUpgrade";
//		static final String LAUNCHES_SINCE_UPGRADE    = "a.LaunchesSinceUpgrade";
//		static final String ADVERTISING_IDENTIFIER    = "a.adid";
//		static final String DEVICE_NAME               = "a.DeviceName";
//		static final String DEVICE_RESOLUTION         = "a.Resolution";
//		static final String CARRIER_NAME              = "a.CarrierName";
//		static final String LOCALE                    = "a.locale";
//		static final String RUN_MODE                  = "a.RunMode";
//		static final String IGNORED_SESSION_LENGTH    = "a.ignoredSessionLength";
//		static final String ACTION_KEY                = "a.action";
//		static final String INTERNAL_ACTION_KEY       = "a.internalaction";
//		static final String TIME_SINCE_LAUNCH_KEY     = "a.TimeSinceLaunch";
//		static final String REGION_ID       		  = "a.loc.poi.id";
//		static final String REGION_NAME     		  = "a.loc.poi";
//		static final String EVENT_IDENTIFIER_KEY      = "a.DebugEventIdentifier";
//
//		private ContextDataKeys() {}
//	}
//
//	static final class ContextDataValues {
//		static final String CRASH_EVENT = "CrashEvent";
//
//		private ContextDataValues() {}
//	}
//
//	static final class DataStoreKeys {
//		static final String MOST_RECENT_HIT_TIMESTAMP_SECONDS = "mostRecentHitTimestampSeconds";
//		static final String AID_KEY                = "ADOBEMOBILE_STOREDDEFAULTS_AID";
//
//		private DataStoreKeys() {}
//	}
//
//	static final class EventDataKeys {
//		static final String STATE_OWNER = "stateowner";
//
//		private EventDataKeys() {}
//
//		static final class Analytics {
//			static final String EXTENSION_NAME = "com.adobe.module.analytics";
//			static final String FORCE_KICK_HITS  = "forcekick";
//			static final String CLEAR_HITS_QUEUE = "clearhitsqueue";
//			static final String ANALYTICS_ID     = "aid";
//			static final String GET_QUEUE_SIZE   = "getqueuesize";
//			static final String QUEUE_SIZE       = "queuesize";
//			static final String TRACK_INTERNAL   = "trackinternal";
//			static final String TRACK_ACTION     = "action";
//			static final String TRACK_STATE      = "state";
//			static final String CONTEXT_DATA = "contextdata";
//			static final String ANALYTICS_SERVER_RESPONSE = "analyticsserverresponse";
//			static final String VISITOR_IDENTIFIER = "vid";
//			static final String RULES_CONSEQUENCE_TYPE_TRACK = "an";
//			static final String HEADERS_RESPONSE = "headers";
//			static final String ETAG_HEADER = "ETag";
//			static final String SERVER_HEADER = "Server";
//			static final String CONTENT_TYPE_HEADER = "Content-Type";
//			static final String REQUEST_EVENT_IDENTIFIER = "requestEventIdentifier";
//			static final String HIT_HOST = "hitHost";
//			static final String HIT_URL = "hitUrl";
//
//			private Analytics() {}
//		}
//
//		static final class Configuration {
//			static final String EXTENSION_NAME = "com.adobe.module.configuration";
//
//			// config response keys
//			static final String GLOBAL_CONFIG_PRIVACY            = "global.privacy";
//
//			static final String CONFIG_EXPERIENCE_CLOUD_ORGID_KEY = "experienceCloud.org";
//
//			static final String ANALYTICS_CONFIG_AAMFORWARDING    = "analytics.aamForwardingEnabled";
//			static final String ANALYTICS_CONFIG_BATCH_LIMIT      = "analytics.batchLimit";
//			static final String ANALYTICS_CONFIG_OFFLINE_TRACKING = "analytics.offlineEnabled";
//			static final String ANALYTICS_CONFIG_REPORT_SUITES    = "analytics.rsids";
//			static final String ANALYTICS_CONFIG_SERVER           = "analytics.server";
//			static final String ANALYTICS_CONFIG_LAUNCH_HIT_DELAY = "analytics.launchHitDelay";
//			static final String ANALYTICS_CONFIG_BACKDATE_PREVIOUS_SESSION = "analytics.backdatePreviousSessionInfo";
//
//			private Configuration() {}
//		}
//
//		static final class Identity {
//			static final String EXTENSION_NAME = "com.adobe.module.identity";
//			static final String VISITOR_ID_MID = "mid";
//			static final String VISITOR_ID_BLOB = "blob";
//			static final String VISITOR_ID_LOCATION_HINT = "locationhint";
//			static final String VISITOR_IDS_LIST = "visitoridslist";
//			static final String USER_IDENTIFIER = "vid";
//			static final String ADVERTISING_IDENTIFIER = "advertisingidentifier";
//
//			private Identity() {}
//		}
//
//		static final class Places {
//			static final String EXTENSION_NAME = "com.adobe.module.places";
//			static final String CURRENT_POI = "currentpoi";
//			static final String REGION_ID = "regionid";
//			static final String REGION_NAME = "regionname";
//
//			private Places() {}
//		}
//
//		static final class Lifecycle {
//			static final String EXTENSION_NAME = "com.adobe.module.lifecycle";
//
//			static final String APP_ID                  = "appid";
//			static final String CARRIER_NAME            = "carriername";
//			static final String CRASH_EVENT             = "crashevent";
//			static final String DAILY_ENGAGED_EVENT     = "dailyenguserevent";
//			static final String DAY_OF_WEEK             = "dayofweek";
//			static final String DAYS_SINCE_FIRST_LAUNCH = "dayssincefirstuse";
//			static final String DAYS_SINCE_LAST_LAUNCH  = "dayssincelastuse";
//			static final String DAYS_SINCE_LAST_UPGRADE = "dayssincelastupgrade";
//			static final String DEVICE_NAME             = "devicename";
//			static final String DEVICE_RESOLUTION       = "resolution";
//			static final String HOUR_OF_DAY             = "hourofday";
//			static final String IGNORED_SESSION_LENGTH  = "ignoredsessionlength";
//			static final String INSTALL_DATE            = "installdate";
//			static final String INSTALL_EVENT           = "installevent";
//			static final String LAUNCH_EVENT            = "launchevent";
//			static final String LAUNCHES                = "launches";
//			static final String LAUNCHES_SINCE_UPGRADE  = "launchessinceupgrade";
//			static final String LIFECYCLE_ACTION_KEY    = "action";
//			static final String LIFECYCLE_CONTEXT_DATA  = "lifecyclecontextdata";
//			static final String LIFECYCLE_PAUSE         = "pause";
//			static final String LIFECYCLE_START         = "start";
//			static final String LOCALE                  = "locale";
//			static final String MAX_SESSION_LENGTH      = "maxsessionlength";
//			static final String MONTHLY_ENGAGED_EVENT   = "monthlyenguserevent";
//			static final String OPERATING_SYSTEM        = "osversion";
//			static final String PREVIOUS_SESSION_LENGTH = "prevsessionlength";
//			static final String PREVIOUS_SESSION_START_TIMESTAMP = "previoussessionstarttimestampmillis";
//			static final String PREVIOUS_SESSION_PAUSE_TIMESTAMP = "previoussessionpausetimestampmillis";
//			static final String RUN_MODE                = "runmode";
//			static final String SESSION_START_TIMESTAMP = "starttimestampmillis";
//			static final String UPGRADE_EVENT           = "upgradeevent";
//			static final String SESSION_EVENT 			= "sessionevent";
//			static final String PREVIOUS_OS_VERSION     = "previousosversion";
//			static final String PREVIOUS_APP_ID         = "previousappid";
//
//			private Lifecycle() {}
//		}
//
//		static final class RuleEngine {
//			static final String RULES_RESPONSE_CONSEQUENCE_KEY_TYPE = "type";
//			static final String RULES_RESPONSE_CONSEQUENCE_KEY_ID = "id";
//			static final String RULES_RESPONSE_CONSEQUENCE_KEY_DETAIL = "detail";
//			static final String CONSEQUENCE_TRIGGERED = "triggeredconsequence";
//
//			private RuleEngine() {}
//		}
//
//		static final class Assurance {
//			static final String EXTENSION_NAME = "com.adobe.assurance";
//			static final String SESSION_ID = "sessionid";
//
//			private Assurance() {}
//		}
//	}
//}
