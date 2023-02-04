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

import androidx.annotation.VisibleForTesting
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.Extension
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.ExtensionEventListener
import com.adobe.marketing.mobile.MobilePrivacyStatus
import com.adobe.marketing.mobile.SharedStateResolution
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.services.AppState
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.util.DataReader
import com.adobe.marketing.mobile.util.DataReaderException
import com.adobe.marketing.mobile.util.SQLiteUtils
import com.adobe.marketing.mobile.util.StringUtils
import java.util.HashMap
import java.util.concurrent.TimeUnit

/**
 * Class [AnalyticsExtension] is an implementation of [Extension] and is responsible
 * for registering event listeners and processing events
 * heard by those listeners. The extension is registered to the Mobile SDK by calling
 * [MobileCore.registerExtensions].
 */
internal class AnalyticsExtension(extensionApi: ExtensionApi) : Extension(extensionApi) {

    companion object {
        private const val CLASS_NAME = "AnalyticsExtension"
        private val ANALYTICS_HARD_DEPENDENCIES = listOf(
            AnalyticsConstants.EventDataKeys.Configuration.SHARED_STATE_NAME,
            AnalyticsConstants.EventDataKeys.Identity.SHARED_STATE_NAME
        )
        private val ANALYTICS_SOFT_DEPENDENCIES = listOf(
            AnalyticsConstants.EventDataKeys.Lifecycle.SHARED_STATE_NAME,
            AnalyticsConstants.EventDataKeys.Assurance.SHARED_STATE_NAME,
            AnalyticsConstants.EventDataKeys.Places.SHARED_STATE_NAME
        )
    }

    private val dataStore: NamedCollection =
        ServiceProvider.getInstance().dataStoreService.getNamedCollection(AnalyticsConstants.DATASTORE_NAME)
    private val analyticsProperties = AnalyticsProperties(dataStore)
    private val analyticsState = AnalyticsState()
    private val analyticsDatabase =
        AnalyticsDatabase(AnalyticsHitProcessor(analyticsState, extensionApi), analyticsState)
    private val eventHandler = ExtensionEventListener { handleIncomingEvent(it) }
    private val analyticsTimer = AnalyticsTimer()

    private var sdkBootUpCompleted = false

    override fun getName(): String {
        return AnalyticsConstants.EXTENSION_NAME
    }

    override fun getFriendlyName(): String {
        return AnalyticsConstants.FRIENDLY_NAME
    }

    override fun getVersion(): String {
        return AnalyticsConstants.EXTENSION_VERSION
    }

    /**
     * {@inheritDoc}
     * <p>
     * The following listeners are registered during this extension's registration:
     * <ul>
     *     <li> EventType [EventType.RULES_ENGINE] and [EventSource EventSource.RESPONSE_CONTENT]</li>
     *     <li> EventType [EventType.ANALYTICS] and [EventSource EventSource.REQUEST_CONTENT]</li>
     *     <li> EventType [EventType.ANALYTICS] and [EventSource EventSource.REQUEST_IDENTITY]</li>
     *     <li> EventType [EventType.CONFIGURATION] and [EventSource EventSource.RESPONSE_CONTENT]</li>
     *     <li> EventType [EventType.GENERIC_LIFECYCLE] and [EventSource EventSource.REQUEST_CONTENT]</li>
     *     <li> EventType [EventType.LIFECYCLE] and EventSource [EventSource.RESPONSE_CONTENT]</li>
     *     <li> EventType [EventType.ACQUISITION] and EventSource [EventSource.RESPONSE_CONTENT]</li>
     *     <li> EventType [EventType.GENERIC_TRACK] and EventSource [EventSource.REQUEST_CONTENT]</li>
     *     <li> EventType [EventType.GENERIC_IDENTITY] and EventSource [EventSource.REQUEST_RESET]</li>
     * </ul>
     * </p>
     */
    override fun onRegistered() {
        api.registerEventListener(
            EventType.RULES_ENGINE,
            EventSource.RESPONSE_CONTENT,
            eventHandler
        )
        api.registerEventListener(
            EventType.ANALYTICS,
            EventSource.REQUEST_CONTENT,
            eventHandler
        )
        api.registerEventListener(
            EventType.ANALYTICS,
            EventSource.REQUEST_IDENTITY,
            eventHandler
        )
        api.registerEventListener(
            EventType.CONFIGURATION,
            EventSource.RESPONSE_CONTENT,
            eventHandler
        )
        api.registerEventListener(
            EventType.GENERIC_LIFECYCLE,
            EventSource.REQUEST_CONTENT,
            eventHandler
        )
        api.registerEventListener(
            EventType.LIFECYCLE,
            EventSource.RESPONSE_CONTENT,
            eventHandler
        )
        api.registerEventListener(
            EventType.ACQUISITION,
            EventSource.RESPONSE_CONTENT,
            eventHandler
        )
        api.registerEventListener(
            EventType.GENERIC_TRACK,
            EventSource.REQUEST_CONTENT,
            eventHandler
        )
        api.registerEventListener(
            EventType.GENERIC_IDENTITY,
            EventSource.REQUEST_RESET,
            eventHandler
        )
        deleteDeprecatedV5HitDatabase()
    }

    override fun readyForEvent(event: Event): Boolean {
        val configurationStatus = api.getSharedState(
            AnalyticsConstants.EventDataKeys.Configuration.SHARED_STATE_NAME,
            event,
            false,
            SharedStateResolution.ANY
        )
        val identityStatus = api.getSharedState(
            AnalyticsConstants.EventDataKeys.Identity.SHARED_STATE_NAME,
            event,
            false,
            SharedStateResolution.ANY
        )
        return configurationStatus?.status == SharedStateStatus.SET && identityStatus?.status == SharedStateStatus.SET
    }

    /**
     * Generic handler for for all incoming events which routes each event to specific handlers.
     *
     * @param event the received event
     */
    @VisibleForTesting
    internal fun handleIncomingEvent(event: Event) {
        when (event.type) {
            EventType.GENERIC_TRACK -> {
                handleGenericTrackEvent(event)
            }
            EventType.RULES_ENGINE -> {
                handleRuleEngineResponse(event)
            }
            EventType.CONFIGURATION -> {
                handleConfigurationResponseEvent(event)
            }
            EventType.LIFECYCLE -> {
                handleLifecycleEvents(event)
            }
            EventType.GENERIC_LIFECYCLE -> {
                handleGenericLifecycleEvents(event)
            }
            EventType.ACQUISITION -> {
                handleAcquisitionEvent(event)
            }
            EventType.ANALYTICS -> {
                when (event.source) {
                    EventSource.REQUEST_IDENTITY -> {
                        handleAnalyticsRequestIdentityEvent(event)
                    }
                    EventSource.REQUEST_CONTENT -> {
                        handleAnalyticsRequestContentEvent(event)
                    }
                }
            }
            EventType.GENERIC_IDENTITY -> {
                handleResetIdentitiesEvent(event)
            }
        }
    }

    /**
     * Handler for Identity Request Reset events.
     * Clears the identities held by this extension and shares a new state with the cleared properties.
     *
     * @param event the identity request reset event
     *
     * @see AnalyticsProperties.reset
     * @see AnalyticsState.resetIdentities
     * @see AnalyticsDatabase.reset
     */
    private fun handleResetIdentitiesEvent(event: Event) {
        if (event.type != EventType.GENERIC_IDENTITY || event.source != EventSource.REQUEST_RESET) {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "handleResetIdentitiesEvent - Ignoring reset event (event is of unexpected type or source)."
            )
            return
        }
        Log.debug(
            AnalyticsConstants.LOG_TAG,
            CLASS_NAME,
            "handleResetIdentitiesEvent - Resetting all identifiers."
        )
        analyticsDatabase.reset()
        analyticsProperties.reset()
        analyticsState.resetIdentities()
        analyticsState.lastResetIdentitiesTimestampSec = event.timestampInSeconds
        api.createSharedState(getSharedState(), event)
    }

    /**
     * Handler for Generic Track Request Content events, dispatched by the public APIs.
     *
     * @param event the generic track request content event
     */
    private fun handleGenericTrackEvent(event: Event) {
        if (event.type != EventType.GENERIC_TRACK || event.source != EventSource.REQUEST_CONTENT) {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "handleAnalyticsTrackEvent - Ignoring track event (event is of unexpected type or source)."
            )
            return
        }
        updateAnalyticsState(event, ANALYTICS_HARD_DEPENDENCIES + ANALYTICS_SOFT_DEPENDENCIES)
        val eventData = event.eventData ?: run {
            Log.debug(AnalyticsConstants.LOG_TAG, CLASS_NAME, "handleGenericTrackEvent - event data is null or empty.")
            return@handleGenericTrackEvent
        }
        handleTrackRequest(event, eventData)
    }

    /**
     * Handler for Rules Engine Response Content events.
     * Handles track consequences of type Analytics ("an") dispatched from the Rules Engine. All other
     * Rules Engine events are ignored.
     *
     * @param event the rules engine response content event
     */
    private fun handleRuleEngineResponse(event: Event) {
        val eventData = event.eventData ?: run {
            Log.trace(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "handleRuleEngineResponse - Event with id %s contained no data, ignoring.",
                event.uniqueIdentifier
            )
            return@handleRuleEngineResponse
        }

        val triggeredConsequence = DataReader.optTypedMap(
            Any::class.java,
            eventData,
            AnalyticsConstants.EventDataKeys.RuleEngine.CONSEQUENCE_TRIGGERED,
            null
        )

        if (triggeredConsequence == null || triggeredConsequence.isEmpty()) {
            Log.trace(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "handleRuleEngineResponse - Missing consequence data, ignoring event %s.",
                event.uniqueIdentifier
            )
            return
        }

        val consequenceType = DataReader.optString(
            triggeredConsequence,
            AnalyticsConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_TYPE,
            null
        )

        if (StringUtils.isNullOrEmpty(consequenceType)) {
            Log.trace(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "handleRuleEngineResponse - No consequence type received, ignoring event %s.",
                event.uniqueIdentifier
            )
            return
        }

        if (AnalyticsConstants.EventDataKeys.Analytics.RULES_CONSEQUENCE_TYPE_TRACK != consequenceType) {
            Log.trace(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "handleRuleEngineResponse - Consequence type is not Analytics, ignoring event %s.",
                event.uniqueIdentifier
            )
            return
        }

        val consequenceId = DataReader.optString(
            triggeredConsequence,
            AnalyticsConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_ID,
            null
        )

        if (StringUtils.isNullOrEmpty(consequenceId)) {
            Log.trace(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "handleRuleEngineResponse - Consequence id is missing, ignoring event  %s.",
                event.uniqueIdentifier
            )
            return
        }

        Log.trace(
            AnalyticsConstants.LOG_TAG,
            CLASS_NAME,
            "handleRuleEngineResponse - Submitting Rules Engine Track response content event (%s) for processing.",
            event.uniqueIdentifier
        )
        updateAnalyticsState(event, ANALYTICS_HARD_DEPENDENCIES + ANALYTICS_SOFT_DEPENDENCIES)
        val consequenceDetail = DataReader.optTypedMap(
            Any::class.java,
            triggeredConsequence,
            AnalyticsConstants.EventDataKeys.RuleEngine.RULES_RESPONSE_CONSEQUENCE_KEY_DETAIL,
            emptyMap()
        )
        handleTrackRequest(event, consequenceDetail)
    }

    /**
     * Handler for Configuration Response Content events.
     * Updates the [AnalyticsState] with the latest configuration.
     * Handles privacy status changes by clearing identifiers if privacy is opted out, or kicks
     * the database to resume processing if privacy is opted in.
     * When called for the first time, finishes boot sequence by creating the initial shared state.
     *
     * @param event the configuration response content event
     */
    private fun handleConfigurationResponseEvent(event: Event) {
        updateAnalyticsState(event, ANALYTICS_HARD_DEPENDENCIES + ANALYTICS_SOFT_DEPENDENCIES)
        if (analyticsState.privacyStatus == MobilePrivacyStatus.OPT_OUT) {
            handleOptOut(event)
        } else if (analyticsState.privacyStatus == MobilePrivacyStatus.OPT_IN) {
            analyticsDatabase.kick(false)
        }
        if (!sdkBootUpCompleted) {
            Log.trace(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "handleConfigurationResponseEvent - Publish analytics shared state on bootup."
            )
            sdkBootUpCompleted = true
            publishAnalyticsId(event)
        }
    }

    /**
     * Handler for Lifecycle Response Content events.
     * Handles response data from the Lifecycle extension by processing a track request.
     *
     * @param event the lifecycle response content event
     */
    private fun handleLifecycleEvents(event: Event) {
        if (event.type != EventType.LIFECYCLE || event.source != EventSource.RESPONSE_CONTENT) {
            return
        }
        updateAnalyticsState(event, ANALYTICS_HARD_DEPENDENCIES + ANALYTICS_SOFT_DEPENDENCIES)
        trackLifecycle(event)
    }

    /**
     * Handler from Generic Lifecycle Request Content events.
     * Handles public API events which request either a start or pause of Lifecycle activity. On
     * start requests, start a timer to wait for the Lifecycle response data. On pause requests,
     * stop the lifecycle timer if running.
     *
     * @param event the generic lifecycle request content event
     */
    private fun handleGenericLifecycleEvents(event: Event) {
        if (event.type != EventType.GENERIC_LIFECYCLE || event.source != EventSource.REQUEST_CONTENT) {
            return
        }
        when (event.eventData?.get(AnalyticsConstants.EventDataKeys.Lifecycle.LIFECYCLE_ACTION_KEY)) {
            AnalyticsConstants.EventDataKeys.Lifecycle.LIFECYCLE_START -> {
                if (analyticsTimer.isTimerRunning) {
                    Log.debug(
                        AnalyticsConstants.LOG_TAG,
                        CLASS_NAME,
                        "handleGenericLifecycleEvents - Exiting, Lifecycle timer is already running and this is a duplicate request"
                    )
                    return
                }
                analyticsDatabase.cancelWaitForAdditionalData(AnalyticsDatabase.DataType.REFERRER)
                analyticsDatabase.cancelWaitForAdditionalData(AnalyticsDatabase.DataType.LIFECYCLE)
                waitForLifecycleData()
            }
            AnalyticsConstants.EventDataKeys.Lifecycle.LIFECYCLE_PAUSE -> {
                analyticsTimer.cancelLifecycleTimer()
                analyticsTimer.cancelReferrerTimer()
            }
        }
    }

    /**
     * Start a timer to wait for lifecycle data after receiving a generic lifecycle request.
     */
    private fun waitForLifecycleData() {
        Log.debug(
            AnalyticsConstants.LOG_TAG,
            CLASS_NAME,
            "waitForLifecycleData - Lifecycle timer scheduled with timeout ${AnalyticsConstants.Default.DEFAULT_LIFECYCLE_RESPONSE_WAIT_TIMEOUT}"
        )
        analyticsDatabase.waitForAdditionalData(AnalyticsDatabase.DataType.LIFECYCLE)
        analyticsTimer.startLifecycleTimer(AnalyticsConstants.Default.DEFAULT_LIFECYCLE_RESPONSE_WAIT_TIMEOUT) {
            Log.warning(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "waitForLifecycleData - Lifecycle timeout has expired without Lifecycle data"
            )
            analyticsDatabase.cancelWaitForAdditionalData(AnalyticsDatabase.DataType.LIFECYCLE)
        }
    }

    /**
     * Handler for Acquisition Response Content events.
     *
     * @param event the acquisition response content event
     */
    private fun handleAcquisitionEvent(event: Event) {
        if (event.type != EventType.ACQUISITION || event.source != EventSource.RESPONSE_CONTENT) {
            return
        }
        updateAnalyticsState(event, ANALYTICS_HARD_DEPENDENCIES + ANALYTICS_SOFT_DEPENDENCIES)
        trackAcquisitionData(event)
    }

    /**
     * Handler for Analytics Request Identity events.
     * Handles public API requests to set the visitor identifier. If privacy is opted out, the
     * request is ignored.
     *
     * @param event the analytics request identity event
     */
    private fun handleAnalyticsRequestIdentityEvent(event: Event) {
        if (event.eventData?.containsKey(AnalyticsConstants.EventDataKeys.Analytics.VISITOR_IDENTIFIER) == true) {
            if (analyticsState.privacyStatus == MobilePrivacyStatus.OPT_OUT) {
                Log.debug(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "handleAnalyticsRequestIdentityEvent - Privacy is opted out, ignoring the update visitor identifier request."
                )
                return
            }

            try {
                analyticsProperties.vid = DataReader.getString(
                    event.eventData,
                    AnalyticsConstants.EventDataKeys.Analytics.VISITOR_IDENTIFIER
                )
            } catch (ex: DataReaderException) {
                Log.debug(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "handleAnalyticsRequestIdentityEvent - Failed to parse the visitor identifier to string, ignoring the update visitor identifier request."
                )
            }
        }

        publishAnalyticsId(event)
    }

    /**
     * Handler for Analytics Request Content events.
     * The Analytics Request Content event can contain a clearQueue, getQueueSize, sendQueuedHits, or internal track event.
     * If it is an internal track event, an internal track request will be queued containing the event's context data and action name.
     *
     * @param event the analytics request content event
     */
    private fun handleAnalyticsRequestContentEvent(event: Event) {
        val eventData = event.eventData
        if (eventData?.isNotEmpty() != true) {
            Log.warning(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "handleAnalyticsRequestContentEvent - Returning early, event data is null or empty."
            )
            return
        }
        when {
            eventData.containsKey(AnalyticsConstants.EventDataKeys.Analytics.CLEAR_HITS_QUEUE) -> {
                analyticsDatabase.reset()
            }
            isTrackActionOrTrackStateEvent(eventData) -> {
                updateAnalyticsState(
                    event,
                    ANALYTICS_HARD_DEPENDENCIES + ANALYTICS_SOFT_DEPENDENCIES
                )
                track(eventData, event.timestampInSeconds, false, event.uniqueIdentifier)
            }
            eventData.containsKey(AnalyticsConstants.EventDataKeys.Analytics.GET_QUEUE_SIZE) -> {
                val queueSize = analyticsDatabase.getQueueSize()
                val data: Map<String, Any> = mapOf(
                    AnalyticsConstants.EventDataKeys.Analytics.QUEUE_SIZE to queueSize
                )
                Log.debug(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "Dispatching Analytics hit queue size response event with eventdata $data"
                )
                api.dispatch(
                    Event.Builder(
                        "QueueSizeValue",
                        EventType.ANALYTICS,
                        EventSource.RESPONSE_CONTENT
                    ).inResponseToEvent(event).setEventData(data).build()
                )
            }
            eventData.containsKey(AnalyticsConstants.EventDataKeys.Analytics.FORCE_KICK_HITS) -> {
                analyticsDatabase.kick(true)
            }
        }
    }

    /**
     * Determines if [eventData] contains flags signaling the data is either for a track state
     * or track action request.
     *
     * @param eventData the tracking event data map
     * @return true if the {@code eventData} is for a track state or track action request.
     */
    private fun isTrackActionOrTrackStateEvent(eventData: Map<String, Any?>): Boolean {
        return eventData.containsKey(AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE) ||
            eventData.containsKey(AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION) ||
            eventData.containsKey(AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA)
    }

    /**
     * Processes Acquisition track events.
     * If waiting for the acquisition data, then try to append it to a existing hit.
     * Otherwise, send a new hit for acquisition data, and cancel the acquisition timer to
     * mark that the acquisition data has been received and processed.
     *
     * @param event the Acquisition event
     */
    private fun trackAcquisitionData(event: Event) {
        val acquisitionContextData: Map<String, String> = DataReader.optTypedMap(
            String::class.java,
            event.eventData,
            AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA,
            emptyMap()
        )

        // if it is waiting for the acquisition data, then append the acquisition data to the waiting hit and kick db queue
        if (analyticsTimer.isReferrerTimerRunning()) {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "trackAcquisition - Cancelling referrer timer"
            )
            analyticsTimer.cancelReferrerTimer()
        }
        if (analyticsDatabase.isHitWaiting()) {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "trackAcquisition - Append referrer data to pending hit"
            )
            analyticsDatabase.kickWithAdditionalData(
                AnalyticsDatabase.DataType.REFERRER,
                acquisitionContextData
            )
        } else {
            analyticsDatabase.cancelWaitForAdditionalData(AnalyticsDatabase.DataType.REFERRER)
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "trackAcquisition - Sending referrer data as separate tracking hit"
            )
            val acquisitionData: MutableMap<String, Any> = HashMap()
            acquisitionData[AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION] =
                AnalyticsConstants.TRACK_INTERNAL_ADOBE_LINK
            acquisitionData[AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA] =
                acquisitionContextData
            acquisitionData[AnalyticsConstants.EventDataKeys.Analytics.TRACK_INTERNAL] =
                true
            track(acquisitionData, event.timestampInSeconds, false, event.uniqueIdentifier)
        }
    }

    /**
     * Processes Lifecycle track events.
     * Converts the lifecycle event in internal analytics action. If backdate session and offline
     * tracking are enabled, and previous session length is present in the contextData map,
     * send a separate hit with the previous session information and the rest of the keys as a
     * Lifecycle action hit. If ignored session is present, it will be sent as part of the
     * Lifecycle hit and no SessionInfo hit will be sent.
     *
     * @param event the Lifecycle event
     */
    private fun trackLifecycle(event: Event) {
        val eventLifecycleContextData: Map<String, String?> = DataReader.optTypedMap(
            String::class.java,
            event.eventData,
            AnalyticsConstants.EventDataKeys.Lifecycle.LIFECYCLE_CONTEXT_DATA,
            null
        ) ?: run {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "trackLifecycle - Failed to track lifecycle event (context data was null or empty)"
            )
            return@trackLifecycle
        }

        // copy the event's data so it is not accidentally overwritten for other processors consuming this event
        val tempLifecycleContextData: MutableMap<String, String> =
            HashMap(eventLifecycleContextData)

        // convert lifecycle event data keys into context data keys for AnalyticsExtension
        val lifecycleContextData: MutableMap<String, String> = HashMap()

        // Removing them because they override current os and app id in the loop below.
        val previousOsVersion = tempLifecycleContextData.remove(
            AnalyticsConstants.EventDataKeys.Lifecycle.PREVIOUS_OS_VERSION
        )
        val previousAppIdVersion = tempLifecycleContextData.remove(
            AnalyticsConstants.EventDataKeys.Lifecycle.PREVIOUS_APP_ID
        )

        AnalyticsConstants.MAP_TO_CONTEXT_DATA_KEYS.forEach { entry ->
            val value = tempLifecycleContextData[entry.key]
            if (value != null && value.isNotEmpty()) {
                lifecycleContextData[entry.value] = value
                tempLifecycleContextData.remove(entry.key)
            }
        }

        lifecycleContextData.putAll(tempLifecycleContextData)

        // if it is a install event, then need to block the analytics queue for the referrer timeout unless
        // the acquisition data is received earlier.
        if (lifecycleContextData.containsKey(AnalyticsConstants.ContextDataKeys.INSTALL_EVENT_KEY)) {
            waitForAcquisitionData(
                TimeUnit.SECONDS.toMillis(analyticsState.referrerTimeout.toLong())
            )
        } else if (lifecycleContextData.containsKey(AnalyticsConstants.ContextDataKeys.LAUNCH_EVENT_KEY)) {
            // if it is a launch event, then need to block the analytics queue for the 500ms unless
            // the deep link data is received earlier.
            waitForAcquisitionData(
                AnalyticsConstants.Default.DEFAULT_LAUNCH_DEEPLINK_DATA_WAIT_TIMEOUT.toLong()
            )
        }

        // backdate hits
        if (analyticsState.isBackdateSessionInfoEnabled && analyticsState.isOfflineTrackingEnabled) {
            // crash
            if (lifecycleContextData.containsKey(AnalyticsConstants.ContextDataKeys.CRASH_EVENT_KEY)) {
                lifecycleContextData.remove(AnalyticsConstants.ContextDataKeys.CRASH_EVENT_KEY)
                backdateLifecycleCrash(
                    previousOsVersion,
                    previousAppIdVersion,
                    event.uniqueIdentifier
                )
            }

            // session info
            if (lifecycleContextData.containsKey(AnalyticsConstants.ContextDataKeys.PREVIOUS_SESSION_LENGTH)) {
                val previousSessionLength =
                    lifecycleContextData.remove(AnalyticsConstants.ContextDataKeys.PREVIOUS_SESSION_LENGTH)
                val previousSessionPauseTimestamp = tempLifecycleContextData.remove(
                    AnalyticsConstants.EventDataKeys.Lifecycle.PREVIOUS_SESSION_PAUSE_TIMESTAMP
                )?.toLong()
                backdateLifecycleSessionInfo(
                    previousSessionLength,
                    previousSessionPauseTimestamp,
                    previousOsVersion,
                    previousAppIdVersion,
                    event.uniqueIdentifier
                )
            }
        }

        // track the lifecycle data
        if (analyticsTimer.isLifecycleTimerRunning()) {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "trackLifecycle - Cancelling lifecycle timer"
            )
            analyticsTimer.cancelLifecycleTimer()
        }
        if (analyticsDatabase.isHitWaiting()) {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "trackLifecycle - Append lifecycle data to pending hit"
            )
            analyticsDatabase.kickWithAdditionalData(
                AnalyticsDatabase.DataType.LIFECYCLE,
                lifecycleContextData
            )
        } else {
            analyticsDatabase.cancelWaitForAdditionalData(AnalyticsDatabase.DataType.LIFECYCLE)
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "trackLifecycle - Sending lifecycle data as separate tracking hit"

            )
            val lifecycleData: MutableMap<String, Any> = HashMap()
            lifecycleData[AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION] =
                AnalyticsConstants.LIFECYCLE_INTERNAL_ACTION_NAME
            lifecycleData[AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA] =
                lifecycleContextData
            lifecycleData[AnalyticsConstants.EventDataKeys.Analytics.TRACK_INTERNAL] =
                true
            track(lifecycleData, event.timestampInSeconds, false, event.uniqueIdentifier)
        }
    }

    /**
     * Creates an internal analytics event with the previous lifecycle session info.
     *
     * @param previousSessionLength the length of the previous session
     * @param previousSessionPauseTimestamp the timestamp when the previous session was paused
     * @param previousOSVersion the OS version of the previous session
     * @param previousAppIdVersion the application identifier of the previous session
     * @param eventUniqueIdentifier the event identifier of the Lifecycle session event
     */
    private fun backdateLifecycleSessionInfo(
        previousSessionLength: String?,
        previousSessionPauseTimestamp: Long?,
        previousOSVersion: String?,
        previousAppIdVersion: String?,
        eventUniqueIdentifier: String
    ) {
        val sessionContextData: MutableMap<String, String> = HashMap()
        if (previousSessionLength != null) {
            sessionContextData[AnalyticsConstants.ContextDataKeys.PREVIOUS_SESSION_LENGTH] =
                previousSessionLength
        }
        if (previousOSVersion != null && previousOSVersion.isNotEmpty()) {
            sessionContextData[AnalyticsConstants.ContextDataKeys.OPERATING_SYSTEM] =
                previousOSVersion
        }
        if (previousAppIdVersion != null && previousAppIdVersion.isNotEmpty()) {
            sessionContextData[AnalyticsConstants.ContextDataKeys.APPLICATION_IDENTIFIER] =
                previousAppIdVersion
        }
        val lifecycleSessionData: MutableMap<String, Any> = HashMap()
        lifecycleSessionData[AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION] =
            AnalyticsConstants.SESSION_INFO_INTERNAL_ACTION_NAME
        lifecycleSessionData[AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA] =
            sessionContextData
        lifecycleSessionData[AnalyticsConstants.EventDataKeys.Analytics.TRACK_INTERNAL] =
            true
        val backdateTimestamp: Long =
            analyticsProperties.mostRecentHitTimeStampInSeconds.coerceAtLeast(
                previousSessionPauseTimestamp ?: 0
            )
        track(lifecycleSessionData, backdateTimestamp + 1, true, eventUniqueIdentifier)
    }

    /**
     * Creates an internal analytics event with the previous lifecycle unknown close session info.
     *
     * @param previousOSVersion the OS version of the previous session
     * @param previousAppIdVersion the application identifier of the previous session
     * @param eventUniqueIdentifier the event identifier of the Lifecycle session event
     */
    private fun backdateLifecycleCrash(
        previousOSVersion: String?,
        previousAppIdVersion: String?,
        eventUniqueIdentifier: String
    ) {
        val crashContextData: MutableMap<String, String> = HashMap()
        crashContextData[AnalyticsConstants.ContextDataKeys.CRASH_EVENT_KEY] =
            AnalyticsConstants.ContextDataValues.CRASH_EVENT
        if (previousOSVersion != null && previousOSVersion.isNotEmpty()) {
            crashContextData[AnalyticsConstants.ContextDataKeys.OPERATING_SYSTEM] =
                previousOSVersion
        }
        if (previousAppIdVersion != null && previousAppIdVersion.isNotEmpty()) {
            crashContextData[AnalyticsConstants.ContextDataKeys.APPLICATION_IDENTIFIER] =
                previousAppIdVersion
        }
        val lifecycleSessionData: MutableMap<String, Any> = HashMap()
        lifecycleSessionData[AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION] =
            AnalyticsConstants.CRASH_INTERNAL_ACTION_NAME
        lifecycleSessionData[AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA] =
            crashContextData
        lifecycleSessionData[AnalyticsConstants.EventDataKeys.Analytics.TRACK_INTERNAL] =
            true
        track(
            lifecycleSessionData,
            analyticsProperties.mostRecentHitTimeStampInSeconds + 1,
            true,
            eventUniqueIdentifier
        )
    }

    /**
     * Start a timer to wait for acquisition data after receiving a lifecycle launch request.
     *
     * @param timeout time in milliseconds to wait for acquisition data
     */
    private fun waitForAcquisitionData(timeout: Long) {
        Log.debug(
            AnalyticsConstants.LOG_TAG,
            CLASS_NAME,
            "waitForAcquisitionData - Referrer timer scheduled with timeout $timeout"
        )
        analyticsDatabase.waitForAdditionalData(AnalyticsDatabase.DataType.REFERRER)
        analyticsTimer.startReferrerTimer(timeout) {
            Log.warning(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "waitForAcquisitionData - Launch hit delay has expired without referrer data."
            )
            analyticsDatabase.cancelWaitForAdditionalData(AnalyticsDatabase.DataType.REFERRER)
        }
    }

    /**
     * Process privacy opt-out by clearing stored identifiers and database hits. Creates shared
     * state after clearing identifiers.
     *
     * @param event the privacy change triggering event
     */
    private fun handleOptOut(event: Event) {
        Log.debug(
            AnalyticsConstants.LOG_TAG,
            CLASS_NAME,
            "handleOptOut - Privacy status is opted-out. Queued Analytics hits, stored state data, and properties will be cleared."
        )
        analyticsDatabase.reset()
        analyticsProperties.reset()
        api.createSharedState(getSharedState(), event)
    }

    /**
     * Compiles data to use when creating a shared state.
     *
     * @returns map containing state data
     */
    private fun getSharedState(): Map<String, Any?> {
        val data = mutableMapOf<String, Any?>()
        analyticsProperties.aid?.let { aid ->
            data.put(
                AnalyticsConstants.EventDataKeys.Analytics.ANALYTICS_ID,
                aid
            )
        }
        analyticsProperties.vid?.let { vid ->
            data.put(
                AnalyticsConstants.EventDataKeys.Analytics.VISITOR_IDENTIFIER,
                vid
            )
        }
        return data
    }

    /**
     * Creates shared state with current identifiers and dispatches an analytics response identity
     * event with current identifiers.
     *
     * @param event the event which triggered the analytics identity request
     */
    private fun publishAnalyticsId(event: Event) {
        val data = getSharedState()
        api.createSharedState(data, event)
        val responseEvent = Event.Builder(
            "TrackingIdentifierValue",
            EventType.ANALYTICS,
            EventSource.RESPONSE_IDENTITY
        ).setEventData(data).inResponseToEvent(event).build()
        api.dispatch(responseEvent)
    }

    /**
     * Updates this instance of [AnalyticsState] with the shared state data for the extensions
     * listed in [dependencies] at the version of the given {@code event}.
     *
     * @param event the triggering event
     * @param dependencies list of extension names to retrieve state data
     *
     * @see AnalyticsState.update
     */
    private fun updateAnalyticsState(event: Event, dependencies: List<String>) {
        val map = dependencies.associateWith { extensionName ->
            api.getSharedState(extensionName, event, true, SharedStateResolution.ANY)?.value
        }
        analyticsState.update(map)
    }

    /**
     * Processes track requests by calling [track] if the given [data] contains
     * track action or track state data.
     *
     * @param event event which observed the tracking request
     * @param data analytics tracking data
     */
    private fun handleTrackRequest(event: Event, data: Map<String, Any?>) {
        if (data.isEmpty()) {
            Log.debug(AnalyticsConstants.LOG_TAG, CLASS_NAME, "handleTrackRequest - event data is null or empty.")
            return
        }
        if (isTrackActionOrTrackStateEvent(data)) {
            track(data, event.timestampInSeconds, false, event.uniqueIdentifier)
        }
    }

    /**
     * Track analytics requests.
     *
     * @param eventData map containing tracking data
     * @param timeStampInSeconds timestamp, in seconds, of tracking event
     * @param isBackdatedHit indicates whether the data corresponds to a backdated session
     * @param eventUniqueIdentifier the identifier of the tracking event
     */
    private fun track(
        eventData: Map<String, Any?>,
        timeStampInSeconds: Long,
        isBackdatedHit: Boolean,
        eventUniqueIdentifier: String
    ) {
        if (MobilePrivacyStatus.OPT_OUT == analyticsState.privacyStatus) {
            Log.warning(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "track - Dropping the Analytics track request, privacy status is opted out."
            )
            return
        }
        if (!analyticsState.isAnalyticsConfigured) {
            Log.warning(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "track - Dropping the Analytics track request, Analytics is not configured."
            )
            return
        }

        analyticsProperties.setMostRecentHitTimeStamp(timeStampInSeconds)

        val analyticsData = processAnalyticsContextData(timeStampInSeconds, eventData)
        val analyticsVars = processAnalyticsVars(eventData, timeStampInSeconds)
        val builtRequest =
            AnalyticsRequestSerializer.buildRequest(analyticsState, analyticsData, analyticsVars)

        analyticsDatabase.queue(
            builtRequest,
            timeStampInSeconds,
            eventUniqueIdentifier,
            isBackdatedHit
        )
    }

    /**
     * Creates the context data map from the [EventData] object and the current [AnalyticsState].
     *
     * @param state          [AnalyticsState] object representing the shared state of other dependent extensions
     * @param trackEventData [Map] object containing tracking data
     * @return [Map<String, String>] contains the context data
     */
    private fun processAnalyticsContextData(
        timeStampInSeconds: Long,
        trackEventData: Map<String, Any?>
    ): MutableMap<String, String> {
        val analyticsData: MutableMap<String, String> = HashMap()
        analyticsData.putAll(analyticsState.defaultData)
        val contextData = DataReader.optTypedMap(
            String::class.java,
            trackEventData,
            AnalyticsConstants.EventDataKeys.Analytics.CONTEXT_DATA,
            null
        )
        if (contextData != null) {
            val cleanedData = cleanContextData(contextData)
            analyticsData.putAll(cleanedData)
        }
        val actionName = DataReader.optString(
            trackEventData,
            AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
            null
        )
        val isInternal = DataReader.optBoolean(
            trackEventData,
            AnalyticsConstants.EventDataKeys.Analytics.TRACK_INTERNAL,
            false
        )
        if (!StringUtils.isNullOrEmpty(actionName)) {
            val actionKey =
                if (isInternal) AnalyticsConstants.ContextDataKeys.INTERNAL_ACTION_KEY else AnalyticsConstants.ContextDataKeys.ACTION_KEY
            analyticsData[actionKey] = actionName
        }
        val lifecycleSessionStartTimestamp = analyticsState.lifecycleSessionStartTimestamp
        if (lifecycleSessionStartTimestamp > 0) {
            val timeSinceLaunchInSeconds: Long =
                timeStampInSeconds - TimeUnit.MILLISECONDS.toSeconds(lifecycleSessionStartTimestamp)
            if (timeSinceLaunchInSeconds in 1..analyticsState.lifecycleMaxSessionLength) {
                analyticsData[AnalyticsConstants.ContextDataKeys.TIME_SINCE_LAUNCH_KEY] =
                    timeSinceLaunchInSeconds.toString()
            }
        }

        // Privacy status
        if (analyticsState.privacyStatus == MobilePrivacyStatus.UNKNOWN) {
            analyticsData[AnalyticsConstants.ANALYTICS_REQUEST_PRIVACY_MODE_KEY] =
                AnalyticsConstants.ANALYTICS_REQUEST_PRIVACY_MODE_UNKNOWN
        }
        val requestIdentifier = DataReader.optString(
            trackEventData,
            AnalyticsConstants.EventDataKeys.Analytics.REQUEST_EVENT_IDENTIFIER,
            null
        )
        if (requestIdentifier != null) {
            analyticsData[AnalyticsConstants.ContextDataKeys.EVENT_IDENTIFIER_KEY] =
                requestIdentifier
        }
        return analyticsData
    }

    /**
     * Creates the vars map from the [EventData] object and the current [AnalyticsState].
     *
     * @param state     [AnalyticsState] object representing the shared state of other dependent extensions
     * @param trackData [Map] object containing tracking data
     * @param timestamp the value timestamp to use for tracking
     * @return [Map<String, String>] contains the vars data
     */
    private fun processAnalyticsVars(
        trackData: Map<String, Any?>,
        timestamp: Long
    ): MutableMap<String, String> {
        val analyticsVars: MutableMap<String, String> = HashMap()

        // setup action and state name
        val actionName = DataReader.optString(
            trackData,
            AnalyticsConstants.EventDataKeys.Analytics.TRACK_ACTION,
            null
        )
        val stateName = DataReader.optString(
            trackData,
            AnalyticsConstants.EventDataKeys.Analytics.TRACK_STATE,
            null
        )

        // context: pe/pev2 values should always be present in track calls if there's action regardless of state.
        // If state is present then pageName = state name else pageName = app id to prevent hit from being discarded.
        if (!StringUtils.isNullOrEmpty(actionName)) {
            analyticsVars[AnalyticsConstants.ANALYTICS_REQUEST_IGNORE_PAGE_NAME_KEY] =
                AnalyticsConstants.IGNORE_PAGE_NAME_VALUE
            val isInternal = DataReader.optBoolean(
                trackData,
                AnalyticsConstants.EventDataKeys.Analytics.TRACK_INTERNAL,
                false
            )
            val actionPrefix =
                if (isInternal) AnalyticsConstants.INTERNAL_ACTION_PREFIX else AnalyticsConstants.ACTION_PREFIX
            analyticsVars[AnalyticsConstants.ANALYTICS_REQUEST_ACTION_NAME_KEY] =
                actionPrefix + actionName
        }
        analyticsState.applicationID?.let {
            analyticsVars[AnalyticsConstants.ANALYTICS_REQUEST_PAGE_NAME_KEY] = it
        }

        if (!StringUtils.isNullOrEmpty(stateName)) {
            analyticsVars[AnalyticsConstants.ANALYTICS_REQUEST_PAGE_NAME_KEY] =
                stateName
        }

        // add aid if available
        analyticsProperties.aid?.let {
            analyticsVars[AnalyticsConstants.ANALYTICS_REQUEST_ANALYTICS_ID_KEY] = it
        }

        // add visitor id if available
        analyticsProperties.vid?.let {
            analyticsVars[AnalyticsConstants.ANALYTICS_REQUEST_VISITOR_ID_KEY] = it
        }
        analyticsVars[AnalyticsConstants.ANALYTICS_REQUEST_CHARSET_KEY] =
            AnalyticsProperties.CHARSET
        analyticsVars[AnalyticsConstants.ANALYTICS_REQUEST_FORMATTED_TIMESTAMP_KEY] =
            TimeZone.TIMESTAMP_TIMEZONE_OFFSET

        if (analyticsState.isOfflineTrackingEnabled) {
            analyticsVars[AnalyticsConstants.ANALYTICS_REQUEST_STRING_TIMESTAMP_KEY] =
                timestamp.toString()
        }

        if (analyticsState.isVisitorIDServiceEnabled) {
            analyticsVars.putAll(analyticsState.analyticsIdVisitorParameters)
        }

        // Customer perspective defaults to foreground.
        analyticsVars[AnalyticsConstants.ANALYTICS_REQUEST_CUSTOMER_PERSPECTIVE_KEY] =
            AnalyticsConstants.APP_STATE_FOREGROUND
        if (ServiceProvider.getInstance().appContextService != null) {
            val appState = ServiceProvider.getInstance().appContextService.appState
            if (appState == AppState.BACKGROUND) {
                analyticsVars[AnalyticsConstants.ANALYTICS_REQUEST_CUSTOMER_PERSPECTIVE_KEY] =
                    AnalyticsConstants.APP_STATE_BACKGROUND
            }
        } else {
            Log.trace(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "processAnalyticsVars - Unable to access platform services to retrieve foreground/background state. Defaulting customer perspective to foreground."
            )
        }

        return analyticsVars
    }

    /**
     * Deletes the Analytics version 1.x database files.
     */
    private fun deleteDeprecatedV5HitDatabase() {
        SQLiteUtils.deleteDBFromCacheDir(AnalyticsConstants.DEPRECATED_1X_HIT_DATABASE_FILENAME)
    }

    /**
     * Remove entries with values which cannot be converted to [String].
     */
    private fun cleanContextData(eventData: Map<String, Any?>): Map<String, String> {
        return eventData.filterValues { it is String }.mapValues { it.value as String }
    }
}
