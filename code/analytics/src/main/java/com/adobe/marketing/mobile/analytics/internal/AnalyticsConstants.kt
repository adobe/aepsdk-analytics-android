/*
  Copyright 2022 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.analytics.internal

import com.adobe.marketing.mobile.MobilePrivacyStatus

internal object AnalyticsConstants {
    const val LOG_TAG = "Analytics"

    const val EXTENSION_NAME = "com.adobe.module.analytics"
    const val FRIENDLY_NAME = "Analytics"
    const val EXTENSION_VERSION = "2.0.3"
    const val DATASTORE_NAME = "AnalyticsDataStorage"
    const val DATA_QUEUE_NAME = EXTENSION_NAME
    const val REORDER_QUEUE_NAME = "com.adobe.module.analyticsreorderqueue"
    const val DEPRECATED_1X_HIT_DATABASE_FILENAME = "ADBMobileDataCache.sqlite"

    const val ANALYTICS_REQUEST_VISITOR_ID_KEY = "vid"
    const val ANALYTICS_REQUEST_CHARSET_KEY = "ce"
    const val ANALYTICS_REQUEST_FORMATTED_TIMESTAMP_KEY = "t"
    const val ANALYTICS_REQUEST_STRING_TIMESTAMP_KEY = "ts"
    const val ANALYTICS_REQUEST_CONTEXT_DATA_KEY = "c"
    const val ANALYTICS_REQUEST_PAGE_NAME_KEY = "pageName"
    const val ANALYTICS_REQUEST_IGNORE_PAGE_NAME_KEY = "pe"
    const val ANALYTICS_REQUEST_CUSTOMER_PERSPECTIVE_KEY = "cp"
    const val ANALYTICS_REQUEST_ACTION_NAME_KEY = "pev2"
    const val ANALYTICS_REQUEST_ANALYTICS_ID_KEY = "aid"
    const val ANALYTICS_REQUEST_PRIVACY_MODE_KEY = "a.privacy.mode"
    const val ANALYTICS_REQUEST_DEBUG_API_PAYLOAD = "&p.&debug=true&.p"

    const val ANALYTICS_PARAMETER_KEY_MID = "mid"
    const val ANALYTICS_PARAMETER_KEY_LOCATION_HINT = "aamlh"
    const val ANALYTICS_PARAMETER_KEY_BLOB = "aamb"

    const val SESSION_INFO_INTERNAL_ACTION_NAME = "SessionInfo"
    const val CRASH_INTERNAL_ACTION_NAME = "Crash"
    const val LIFECYCLE_INTERNAL_ACTION_NAME = "Lifecycle"
    const val TRACK_INTERNAL_ADOBE_LINK = "AdobeLink"

    const val APP_STATE_FOREGROUND = "foreground"
    const val APP_STATE_BACKGROUND = "background"

    const val ANALYTICS_REQUEST_PRIVACY_MODE_UNKNOWN = "unknown"

    // ================================================================================
    // Default values
    // ================================================================================
    const val CONNECTION_TIMEOUT_SEC = 5 // default connection timeout in seconds

    const val IGNORE_PAGE_NAME_VALUE = "lnk_o"
    const val ACTION_PREFIX = "AMACTION:"
    const val INTERNAL_ACTION_PREFIX = "ADBINTERNAL:"
    const val VAR_ESCAPE_PREFIX = "&&"

    internal object Default {
        val DEFAULT_PRIVACY_STATUS = MobilePrivacyStatus.OPT_IN
        const val DEFAULT_FORWARDING_ENABLED = false
        const val DEFAULT_OFFLINE_ENABLED = false
        const val DEFAULT_BACKDATE_SESSION_INFO_ENABLED = false
        const val DEFAULT_BATCH_LIMIT = 0
        const val DEFAULT_REFERRER_TIMEOUT = 0
        const val DEFAULT_LIFECYCLE_RESPONSE_WAIT_TIMEOUT = 1000L // ms
        const val DEFAULT_LAUNCH_DEEPLINK_DATA_WAIT_TIMEOUT = 500 // ms
        const val DEFAULT_ASSURANCE_SESSION_ENABLED = false
        const val DEFAULT_LIFECYCLE_SESSION_TIMEOUT = 300000 // ms
    }

    internal object DataStoreKeys {
        const val MOST_RECENT_HIT_TIMESTAMP_SECONDS = "mostRecentHitTimestampSeconds"
        const val AID_KEY = "ADOBEMOBILE_STOREDDEFAULTS_AID"
        const val VISITOR_IDENTIFIER_KEY = "ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER"
    }

    internal object ContextDataKeys {
        const val INSTALL_EVENT_KEY = "a.InstallEvent"
        const val LAUNCH_EVENT_KEY = "a.LaunchEvent"
        const val CRASH_EVENT_KEY = "a.CrashEvent"
        const val UPGRADE_EVENT_KEY = "a.UpgradeEvent"
        const val DAILY_ENGAGED_EVENT_KEY = "a.DailyEngUserEvent"
        const val MONTHLY_ENGAGED_EVENT_KEY = "a.MonthlyEngUserEvent"
        const val INSTALL_DATE = "a.InstallDate"
        const val LAUNCHES = "a.Launches"
        const val PREVIOUS_SESSION_LENGTH = "a.PrevSessionLength"
        const val DAYS_SINCE_FIRST_LAUNCH = "a.DaysSinceFirstUse"
        const val DAYS_SINCE_LAST_LAUNCH = "a.DaysSinceLastUse"
        const val HOUR_OF_DAY = "a.HourOfDay"
        const val DAY_OF_WEEK = "a.DayOfWeek"
        const val OPERATING_SYSTEM = "a.OSVersion"
        const val APPLICATION_IDENTIFIER = "a.AppID"
        const val DAYS_SINCE_LAST_UPGRADE = "a.DaysSinceLastUpgrade"
        const val LAUNCHES_SINCE_UPGRADE = "a.LaunchesSinceUpgrade"
        const val ADVERTISING_IDENTIFIER = "a.adid"
        const val DEVICE_NAME = "a.DeviceName"
        const val DEVICE_RESOLUTION = "a.Resolution"
        const val CARRIER_NAME = "a.CarrierName"
        const val LOCALE = "a.locale"
        const val SYSTEM_LOCALE = "a.systemLocale"
        const val RUN_MODE = "a.RunMode"
        const val IGNORED_SESSION_LENGTH = "a.ignoredSessionLength"
        const val ACTION_KEY = "a.action"
        const val INTERNAL_ACTION_KEY = "a.internalaction"
        const val TIME_SINCE_LAUNCH_KEY = "a.TimeSinceLaunch"
        const val REGION_ID = "a.loc.poi.id"
        const val REGION_NAME = "a.loc.poi"
        const val EVENT_IDENTIFIER_KEY = "a.DebugEventIdentifier"
    }

    internal object EventDataKeys {
        const val STATE_OWNER = "stateowner"

        internal object Analytics {
            const val EXTENSION_NAME = "com.adobe.module.analytics"
            const val FORCE_KICK_HITS = "forcekick"
            const val CLEAR_HITS_QUEUE = "clearhitsqueue"
            const val ANALYTICS_ID = "aid"
            const val GET_QUEUE_SIZE = "getqueuesize"
            const val QUEUE_SIZE = "queuesize"
            const val TRACK_INTERNAL = "trackinternal"
            const val TRACK_ACTION = "action"
            const val TRACK_STATE = "state"
            const val CONTEXT_DATA = "contextdata"
            const val ANALYTICS_SERVER_RESPONSE = "analyticsserverresponse"
            const val VISITOR_IDENTIFIER = "vid"
            const val RULES_CONSEQUENCE_TYPE_TRACK = "an"
            const val HEADERS_RESPONSE = "headers"
            const val ETAG_HEADER = "ETag"
            const val SERVER_HEADER = "Server"
            const val CONTENT_TYPE_HEADER = "Content-Type"
            const val REQUEST_EVENT_IDENTIFIER = "requestEventIdentifier"
            const val HIT_HOST = "hitHost"
            const val HIT_URL = "hitUrl"
        }

        internal object Configuration {
            const val EXTENSION_NAME = "com.adobe.module.configuration"
            const val SHARED_STATE_NAME = "com.adobe.module.configuration"

            // config response keys
            const val GLOBAL_CONFIG_PRIVACY = "global.privacy"
            const val CONFIG_EXPERIENCE_CLOUD_ORGID_KEY = "experienceCloud.org"
            const val ANALYTICS_CONFIG_AAMFORWARDING = "analytics.aamForwardingEnabled"
            const val ANALYTICS_CONFIG_BATCH_LIMIT = "analytics.batchLimit"
            const val ANALYTICS_CONFIG_OFFLINE_TRACKING = "analytics.offlineEnabled"
            const val ANALYTICS_CONFIG_REPORT_SUITES = "analytics.rsids"
            const val ANALYTICS_CONFIG_SERVER = "analytics.server"
            const val ANALYTICS_CONFIG_LAUNCH_HIT_DELAY = "analytics.launchHitDelay"
            const val ANALYTICS_CONFIG_BACKDATE_PREVIOUS_SESSION =
                "analytics.backdatePreviousSessionInfo"
            const val LIFECYCLE_SESSION_TIMEOUT = "lifecycle.sessionTimeout"
        }

        internal object Identity {
            const val SHARED_STATE_NAME = "com.adobe.module.identity"
            const val EXTENSION_NAME = "com.adobe.module.identity"
            const val VISITOR_ID_MID = "mid"
            const val VISITOR_ID_BLOB = "blob"
            const val VISITOR_ID_LOCATION_HINT = "locationhint"
            const val VISITOR_IDS_LIST = "visitoridslist"
            const val ADVERTISING_IDENTIFIER = "advertisingidentifier"

            internal object VisitorID {
                const val ID = "ID"
                const val ID_TYPE = "ID_TYPE"
                const val STATE = "STATE"
            }
        }

        internal object Lifecycle {
            const val SHARED_STATE_NAME = "com.adobe.module.lifecycle"
            const val EXTENSION_NAME = "com.adobe.module.lifecycle"
            const val APP_ID = "appid"
            const val CARRIER_NAME = "carriername"
            const val CRASH_EVENT = "crashevent"
            const val DAILY_ENGAGED_EVENT = "dailyenguserevent"
            const val DAY_OF_WEEK = "dayofweek"
            const val DAYS_SINCE_FIRST_LAUNCH = "dayssincefirstuse"
            const val DAYS_SINCE_LAST_LAUNCH = "dayssincelastuse"
            const val DAYS_SINCE_LAST_UPGRADE = "dayssincelastupgrade"
            const val DEVICE_NAME = "devicename"
            const val DEVICE_RESOLUTION = "resolution"
            const val HOUR_OF_DAY = "hourofday"
            const val IGNORED_SESSION_LENGTH = "ignoredsessionlength"
            const val INSTALL_DATE = "installdate"
            const val INSTALL_EVENT = "installevent"
            const val LAUNCH_EVENT = "launchevent"
            const val LAUNCHES = "launches"
            const val LAUNCHES_SINCE_UPGRADE = "launchessinceupgrade"
            const val LIFECYCLE_ACTION_KEY = "action"
            const val LIFECYCLE_CONTEXT_DATA = "lifecyclecontextdata"
            const val LIFECYCLE_PAUSE = "pause"
            const val LIFECYCLE_START = "start"
            const val LOCALE = "locale"
            const val SYSTEM_LOCALE = "systemlocale"
            const val MAX_SESSION_LENGTH = "maxsessionlength"
            const val MONTHLY_ENGAGED_EVENT = "monthlyenguserevent"
            const val OPERATING_SYSTEM = "osversion"
            const val PREVIOUS_SESSION_LENGTH = "prevsessionlength"
            const val PREVIOUS_SESSION_START_TIMESTAMP = "previoussessionstarttimestampmillis"
            const val PREVIOUS_SESSION_PAUSE_TIMESTAMP = "previoussessionpausetimestampmillis"
            const val RUN_MODE = "runmode"
            const val SESSION_START_TIMESTAMP = "starttimestampmillis"
            const val UPGRADE_EVENT = "upgradeevent"
            const val SESSION_EVENT = "sessionevent"
            const val PREVIOUS_OS_VERSION = "previousosversion"
            const val PREVIOUS_APP_ID = "previousappid"
        }

        internal object Places {
            const val SHARED_STATE_NAME = "com.adobe.module.places"
            const val EXTENSION_NAME = "com.adobe.module.places"
            const val CURRENT_POI = "currentpoi"
            const val REGION_ID = "regionid"
            const val REGION_NAME = "regionname"
        }

        internal object RuleEngine {
            const val RULES_RESPONSE_CONSEQUENCE_KEY_TYPE = "type"
            const val RULES_RESPONSE_CONSEQUENCE_KEY_ID = "id"
            const val RULES_RESPONSE_CONSEQUENCE_KEY_DETAIL = "detail"
            const val CONSEQUENCE_TRIGGERED = "triggeredconsequence"
        }

        internal object Assurance {
            const val EXTENSION_NAME = "com.adobe.assurance"
            const val SHARED_STATE_NAME = "com.adobe.assurance"
            const val SESSION_ID = "sessionid"
        }
    }

    internal object ContextDataValues {
        const val CRASH_EVENT = "CrashEvent"
    }

    internal val MAP_TO_CONTEXT_DATA_KEYS = mapOf(
        EventDataKeys.Identity.ADVERTISING_IDENTIFIER to ContextDataKeys.ADVERTISING_IDENTIFIER,
        EventDataKeys.Lifecycle.APP_ID to ContextDataKeys.APPLICATION_IDENTIFIER,
        EventDataKeys.Lifecycle.CARRIER_NAME to ContextDataKeys.CARRIER_NAME,
        EventDataKeys.Lifecycle.CRASH_EVENT to ContextDataKeys.CRASH_EVENT_KEY,
        EventDataKeys.Lifecycle.DAILY_ENGAGED_EVENT to ContextDataKeys.DAILY_ENGAGED_EVENT_KEY,
        EventDataKeys.Lifecycle.DAY_OF_WEEK to ContextDataKeys.DAY_OF_WEEK,
        EventDataKeys.Lifecycle.DAYS_SINCE_FIRST_LAUNCH to ContextDataKeys.DAYS_SINCE_FIRST_LAUNCH,
        EventDataKeys.Lifecycle.DAYS_SINCE_LAST_LAUNCH to ContextDataKeys.DAYS_SINCE_LAST_LAUNCH,
        EventDataKeys.Lifecycle.DAYS_SINCE_LAST_UPGRADE to ContextDataKeys.DAYS_SINCE_LAST_UPGRADE,
        EventDataKeys.Lifecycle.DEVICE_NAME to ContextDataKeys.DEVICE_NAME,
        EventDataKeys.Lifecycle.DEVICE_RESOLUTION to ContextDataKeys.DEVICE_RESOLUTION,
        EventDataKeys.Lifecycle.HOUR_OF_DAY to ContextDataKeys.HOUR_OF_DAY,
        EventDataKeys.Lifecycle.IGNORED_SESSION_LENGTH to ContextDataKeys.IGNORED_SESSION_LENGTH,
        EventDataKeys.Lifecycle.INSTALL_DATE to ContextDataKeys.INSTALL_DATE,
        EventDataKeys.Lifecycle.INSTALL_EVENT to ContextDataKeys.INSTALL_EVENT_KEY,
        EventDataKeys.Lifecycle.LAUNCH_EVENT to ContextDataKeys.LAUNCH_EVENT_KEY,
        EventDataKeys.Lifecycle.LAUNCHES to ContextDataKeys.LAUNCHES,
        EventDataKeys.Lifecycle.LAUNCHES_SINCE_UPGRADE to ContextDataKeys.LAUNCHES_SINCE_UPGRADE,
        EventDataKeys.Lifecycle.LOCALE to ContextDataKeys.LOCALE,
        EventDataKeys.Lifecycle.SYSTEM_LOCALE to ContextDataKeys.SYSTEM_LOCALE,
        EventDataKeys.Lifecycle.MONTHLY_ENGAGED_EVENT to ContextDataKeys.MONTHLY_ENGAGED_EVENT_KEY,
        EventDataKeys.Lifecycle.OPERATING_SYSTEM to ContextDataKeys.OPERATING_SYSTEM,
        EventDataKeys.Lifecycle.PREVIOUS_SESSION_LENGTH to ContextDataKeys.PREVIOUS_SESSION_LENGTH,
        EventDataKeys.Lifecycle.RUN_MODE to ContextDataKeys.RUN_MODE,
        EventDataKeys.Lifecycle.UPGRADE_EVENT to ContextDataKeys.UPGRADE_EVENT_KEY,
        EventDataKeys.Lifecycle.PREVIOUS_OS_VERSION to ContextDataKeys.OPERATING_SYSTEM,
        EventDataKeys.Lifecycle.PREVIOUS_APP_ID to ContextDataKeys.APPLICATION_IDENTIFIER
    )
}
