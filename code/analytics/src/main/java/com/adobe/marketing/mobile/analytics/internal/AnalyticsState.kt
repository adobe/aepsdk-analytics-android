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
import com.adobe.marketing.mobile.internal.util.VisitorIDSerializer
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.util.DataReader
import com.adobe.marketing.mobile.util.DataReaderException
import com.adobe.marketing.mobile.util.StringUtils
import com.adobe.marketing.mobile.util.TimeUtils
import java.util.HashMap

/**
 * The [AnalyticsState] class will encapsulate the analytics config properties used across the analytics handlers,
 * which are retrieved from SharedState.
 */
internal class AnalyticsState {
    internal var host: String? = null
        private set

    internal var lastResetIdentitiesTimestampSec: Long = TimeUtils.getUnixTimeInSeconds()

    // ----------- Configuration properties -----------
    internal var isAnalyticsForwardingEnabled: Boolean =
        AnalyticsConstants.Default.DEFAULT_FORWARDING_ENABLED
        private set
    internal var isOfflineTrackingEnabled: Boolean =
        AnalyticsConstants.Default.DEFAULT_OFFLINE_ENABLED
        private set
    internal var batchLimit: Int = AnalyticsConstants.Default.DEFAULT_BATCH_LIMIT
        private set
    internal var privacyStatus: MobilePrivacyStatus =
        AnalyticsConstants.Default.DEFAULT_PRIVACY_STATUS
        private set
    internal var referrerTimeout: Int = AnalyticsConstants.Default.DEFAULT_REFERRER_TIMEOUT
        private set
    internal var isAssuranceSessionActive: Boolean =
        AnalyticsConstants.Default.DEFAULT_ASSURANCE_SESSION_ENABLED
        private set
    internal var isBackdateSessionInfoEnabled: Boolean =
        AnalyticsConstants.Default.DEFAULT_BACKDATE_SESSION_INFO_ENABLED
    internal var marketingCloudOrganizationID: String? = null
        private set
    internal var rsids: String? = null
        private set

    // ----------- Identity properties -----------
    internal var marketingCloudId: String? = null
        private set
    internal var locationHint: String? = null
        private set
    internal var advertisingIdentifier: String? = null
        private set
    internal var blob: String? = null
        private set
    internal var serializedVisitorIDsList: String? = null
        private set

    // ----------- Lifecycle properties ----------
    internal var applicationID: String? = null
        private set
    internal val defaultData: MutableMap<String, String> = HashMap()
    private var sessionTimeout: Int = AnalyticsConstants.Default.DEFAULT_LIFECYCLE_SESSION_TIMEOUT
    internal var lifecycleMaxSessionLength: Long = 0
        private set
    internal var lifecycleSessionStartTimestamp: Long = 0
        private set

    internal fun update(dataMap: Map<String, Map<String, Any>?>) {
        for ((key, value) in dataMap) {
            if (value == null) {
                Log.trace(
                    AnalyticsConstants.LOG_TAG,
                    LOG_TAG,
                    "update - Unable to extract data for %s, it was null.",
                    key
                )
                continue
            }
            when (key) {
                AnalyticsConstants.EventDataKeys.Configuration.EXTENSION_NAME -> {
                    extractConfigurationInfo(value)
                }
                AnalyticsConstants.EventDataKeys.Lifecycle.EXTENSION_NAME -> {
                    extractLifecycleInfo(value)
                }
                AnalyticsConstants.EventDataKeys.Identity.EXTENSION_NAME -> {
                    extractIdentityInfo(value)
                }
                AnalyticsConstants.EventDataKeys.Places.EXTENSION_NAME -> {
                    extractPlacesInfo(value)
                }
                AnalyticsConstants.EventDataKeys.Assurance.EXTENSION_NAME -> {
                    extractAssuranceInfo(value)
                }
            }
        }
    }

    /**
     * Extracts the configuration data from the provided EventData object
     *
     * @param configuration the eventData map from Config's shared state
     */
    private fun extractConfigurationInfo(configuration: Map<String, Any?>) {
        host = DataReader.optString(
            configuration,
            AnalyticsConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_SERVER,
            null
        )
        rsids = DataReader.optString(
            configuration,
            AnalyticsConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_REPORT_SUITES,
            null
        )
        isAnalyticsForwardingEnabled = DataReader.optBoolean(
            configuration,
            AnalyticsConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_AAMFORWARDING,
            AnalyticsConstants.Default.DEFAULT_FORWARDING_ENABLED
        )
        isOfflineTrackingEnabled = DataReader.optBoolean(
            configuration,
            AnalyticsConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_OFFLINE_TRACKING,
            AnalyticsConstants.Default.DEFAULT_OFFLINE_ENABLED
        )
        batchLimit = DataReader.optInt(
            configuration,
            AnalyticsConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_BATCH_LIMIT,
            AnalyticsConstants.Default.DEFAULT_BATCH_LIMIT
        )
        val referrerTimeoutFromConfig = DataReader.optInt(
            configuration,
            AnalyticsConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_LAUNCH_HIT_DELAY,
            AnalyticsConstants.Default.DEFAULT_REFERRER_TIMEOUT
        )
        if (referrerTimeoutFromConfig >= 0) {
            referrerTimeout = referrerTimeoutFromConfig
        }
        marketingCloudOrganizationID = DataReader.optString(
            configuration,
            AnalyticsConstants.EventDataKeys.Configuration.CONFIG_EXPERIENCE_CLOUD_ORGID_KEY,
            null
        )
        isBackdateSessionInfoEnabled = DataReader.optBoolean(
            configuration,
            AnalyticsConstants.EventDataKeys.Configuration.ANALYTICS_CONFIG_BACKDATE_PREVIOUS_SESSION,
            AnalyticsConstants.Default.DEFAULT_BACKDATE_SESSION_INFO_ENABLED
        )
        privacyStatus = MobilePrivacyStatus.fromString(
            DataReader.optString(
                configuration,
                AnalyticsConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY,
                AnalyticsConstants.Default.DEFAULT_PRIVACY_STATUS.getValue()
            )
        )
        sessionTimeout = DataReader.optInt(
            configuration,
            AnalyticsConstants.EventDataKeys.Configuration.LIFECYCLE_SESSION_TIMEOUT,
            AnalyticsConstants.Default.DEFAULT_LIFECYCLE_SESSION_TIMEOUT
        )
    }

    /**
     * Extracts the identity data from the provided EventData object
     *
     * @param identityInfo the eventData map from Identity's shared state
     */
    private fun extractIdentityInfo(identityInfo: Map<String, Any?>) {
        marketingCloudId = DataReader.optString(
            identityInfo,
            AnalyticsConstants.EventDataKeys.Identity.VISITOR_ID_MID,
            null
        )
        blob = DataReader.optString(
            identityInfo,
            AnalyticsConstants.EventDataKeys.Identity.VISITOR_ID_BLOB,
            null
        )
        locationHint = DataReader.optString(
            identityInfo,
            AnalyticsConstants.EventDataKeys.Identity.VISITOR_ID_LOCATION_HINT,
            null
        )
        advertisingIdentifier = DataReader.optString(
            identityInfo,
            AnalyticsConstants.EventDataKeys.Identity.ADVERTISING_IDENTIFIER,
            null
        )
        if (identityInfo.containsKey(AnalyticsConstants.EventDataKeys.Identity.VISITOR_IDS_LIST)) {
            try {
                val list = DataReader.getTypedList(
                    Map::class.java,
                    identityInfo,
                    AnalyticsConstants.EventDataKeys.Identity.VISITOR_IDS_LIST
                )
                val visitorIdsList = VisitorIDSerializer.convertToVisitorIds(list)
                serializedVisitorIDsList =
                    AnalyticsRequestSerializer.generateAnalyticsCustomerIdString(visitorIdsList)
            } catch (ex: DataReaderException) {
                Log.debug(
                    AnalyticsConstants.LOG_TAG,
                    LOG_TAG,
                    "extractIdentityInfo - The format of the serializedVisitorIDsList list is invalid: %s",
                    ex
                )
            }
        }
    }

    /**
     * Extracts the places data from the provided EventData object
     *
     * @param placesInfo the eventData map from Places shared state
     */
    private fun extractPlacesInfo(placesInfo: Map<String, Any?>) {
        val placesContextData = DataReader.optTypedMap(
            String::class.java,
            placesInfo,
            AnalyticsConstants.EventDataKeys.Places.CURRENT_POI,
            null
        ) ?: return
        val regionId = placesContextData[AnalyticsConstants.EventDataKeys.Places.REGION_ID]
        if (!StringUtils.isNullOrEmpty(regionId)) {
            defaultData[AnalyticsConstants.ContextDataKeys.REGION_ID] =
                regionId ?: ""
        }
        val regionName = placesContextData[AnalyticsConstants.EventDataKeys.Places.REGION_NAME]
        if (!StringUtils.isNullOrEmpty(regionName)) {
            defaultData[AnalyticsConstants.ContextDataKeys.REGION_NAME] =
                regionName ?: ""
        }
    }

    /**
     * Extracts the lifecycle data from the provided EventData object
     *
     * @param lifecycleData the eventData map from Lifecycle's shared state
     */
    private fun extractLifecycleInfo(lifecycleData: Map<String, Any?>) {
        lifecycleSessionStartTimestamp = DataReader.optLong(
            lifecycleData,
            AnalyticsConstants.EventDataKeys.Lifecycle.SESSION_START_TIMESTAMP,
            0L
        )
        lifecycleMaxSessionLength = DataReader.optLong(
            lifecycleData,
            AnalyticsConstants.EventDataKeys.Lifecycle.MAX_SESSION_LENGTH,
            0L
        )
        val lifecycleContextData = DataReader.optTypedMap(
            String::class.java,
            lifecycleData,
            AnalyticsConstants.EventDataKeys.Lifecycle.LIFECYCLE_CONTEXT_DATA,
            null
        )
        if (lifecycleContextData == null || lifecycleContextData.isEmpty()) {
            return
        }
        val osVersion =
            lifecycleContextData[AnalyticsConstants.EventDataKeys.Lifecycle.OPERATING_SYSTEM]
        if (!StringUtils.isNullOrEmpty(osVersion)) {
            defaultData[AnalyticsConstants.ContextDataKeys.OPERATING_SYSTEM] =
                osVersion ?: ""
        }
        val deviceName =
            lifecycleContextData[AnalyticsConstants.EventDataKeys.Lifecycle.DEVICE_NAME]
        if (!StringUtils.isNullOrEmpty(deviceName)) {
            defaultData[AnalyticsConstants.ContextDataKeys.DEVICE_NAME] = deviceName ?: ""
        }
        val deviceResolution =
            lifecycleContextData[AnalyticsConstants.EventDataKeys.Lifecycle.DEVICE_RESOLUTION]
        if (!StringUtils.isNullOrEmpty(deviceResolution)) {
            defaultData[AnalyticsConstants.ContextDataKeys.DEVICE_RESOLUTION] =
                deviceResolution ?: ""
        }
        val carrier = lifecycleContextData[AnalyticsConstants.EventDataKeys.Lifecycle.CARRIER_NAME]
        if (!StringUtils.isNullOrEmpty(carrier)) {
            defaultData[AnalyticsConstants.ContextDataKeys.CARRIER_NAME] =
                carrier ?: ""
        }
        val runMode = lifecycleContextData[AnalyticsConstants.EventDataKeys.Lifecycle.RUN_MODE]
        if (!StringUtils.isNullOrEmpty(runMode)) {
            defaultData[AnalyticsConstants.ContextDataKeys.RUN_MODE] = runMode ?: ""
        }
        val appId = lifecycleContextData[AnalyticsConstants.EventDataKeys.Lifecycle.APP_ID]
        if (!StringUtils.isNullOrEmpty(appId)) {
            defaultData[AnalyticsConstants.ContextDataKeys.APPLICATION_IDENTIFIER] =
                appId ?: ""
            applicationID = appId
        }
    }

    /**
     * Extracts the Assurance data from the provided EventData object
     *
     * @param assuranceInfo the eventData map from Assurance's shared state
     */
    private fun extractAssuranceInfo(assuranceInfo: Map<String, Any?>) {
        val assuranceSessionId = DataReader.optString(
            assuranceInfo,
            AnalyticsConstants.EventDataKeys.Assurance.SESSION_ID,
            null
        )

        // assurance sessionId non empty non null means session is active
        isAssuranceSessionActive = !StringUtils.isNullOrEmpty(assuranceSessionId)
    }

    /**
     * Extracts the visitor id blob, locationHint and marketing could id (mid) in a map mid is not null
     *
     * @return the resulted map or an empty map if mid is null
     */
    val analyticsIdVisitorParameters: Map<String, String>
        get() {
            val analyticsIdVisitorParameters: MutableMap<String, String> = HashMap()
            if (StringUtils.isNullOrEmpty(marketingCloudId)) {
                return analyticsIdVisitorParameters
            }
            analyticsIdVisitorParameters[AnalyticsConstants.ANALYTICS_PARAMETER_KEY_MID] =
                marketingCloudId ?: ""
            if (!StringUtils.isNullOrEmpty(blob)) {
                analyticsIdVisitorParameters[AnalyticsConstants.ANALYTICS_PARAMETER_KEY_BLOB] =
                    blob ?: ""
            }
            if (!StringUtils.isNullOrEmpty(locationHint)) {
                analyticsIdVisitorParameters[AnalyticsConstants.ANALYTICS_PARAMETER_KEY_LOCATION_HINT] =
                    locationHint ?: ""
            }
            return analyticsIdVisitorParameters
        }

    /**
     * Checks if rsids and tracking server are configured for the analytics module
     *
     * @return true if both conditions are met, false otherwise
     */
    val isAnalyticsConfigured: Boolean
        get() = !StringUtils.isNullOrEmpty(rsids) && !StringUtils.isNullOrEmpty(host)

    val isVisitorIDServiceEnabled: Boolean
        get() = !StringUtils.isNullOrEmpty(marketingCloudOrganizationID)

    fun resetIdentities() {
        clearPlacesData()
        marketingCloudId = null
        locationHint = null
        blob = null
        serializedVisitorIDsList = null
        applicationID = null
        advertisingIdentifier = null
    }

    private fun clearPlacesData() {
        defaultData.remove(AnalyticsConstants.ContextDataKeys.REGION_ID)
        defaultData.remove(AnalyticsConstants.ContextDataKeys.REGION_NAME)
    }

    val isOptIn: Boolean
        get() = privacyStatus == MobilePrivacyStatus.OPT_IN

    companion object {
        private const val LOG_TAG = "AnalyticsState"
    }
}
