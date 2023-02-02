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
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.services.DataEntity
import com.adobe.marketing.mobile.services.HitProcessing
import com.adobe.marketing.mobile.services.HitProcessingResult
import com.adobe.marketing.mobile.services.HttpMethod
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import com.adobe.marketing.mobile.util.StreamUtils
import com.adobe.marketing.mobile.util.TimeUtils
import com.adobe.marketing.mobile.util.UrlUtils

internal class AnalyticsHitProcessor(
    private val analyticsState: AnalyticsState,
    private val extensionApi: ExtensionApi
) : HitProcessing {

    companion object {
        private const val CLASS_NAME = "AnalyticsHitProcessor"
        private const val HIT_QUEUE_RETRY_TIME_SECONDS = 30
        private const val TIMESTAMP_DISABLED_WAIT_THRESHOLD_SECONDS = 60
        private const val CONTENT_TYPE_URL_ENCODED = "application/x-www-form-urlencoded"
    }

    private val networkService: Networking = ServiceProvider.getInstance().networkService
    private var lastHitTimestamp: Long = 0L

    private val version = AnalyticsVersionProvider.buildVersionString()

    override fun retryInterval(dataEntity: DataEntity): Int {
        return HIT_QUEUE_RETRY_TIME_SECONDS
    }

    override fun processHit(entity: DataEntity, processingResult: HitProcessingResult) {
        val analyticsHit = AnalyticsHit.from(entity)
        val eventIdentifier = analyticsHit.eventIdentifier
        var payload = analyticsHit.payload
        var timestamp = analyticsHit.timestamp

        if (payload.isEmpty()) {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "processHit - Dropping Analytics hit, payload is empty."
            )
            processingResult.complete(true)
            return
        }

        if (timestamp < analyticsState.lastResetIdentitiesTimestamp) {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "processHit - Dropping Analytics hit, resetIdentities API was called after this request."
            )
            processingResult.complete(true)
            return
        }

        if (!analyticsState.isOfflineTrackingEnabled && timestamp < TimeUtils.getUnixTimeInSeconds() - TIMESTAMP_DISABLED_WAIT_THRESHOLD_SECONDS) {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "processHit - Dropping Analytics hit, timestamp exceeds offline disabled wait threshold"
            )
            processingResult.complete(true)
            return
        }

        if (analyticsState.isOfflineTrackingEnabled && (timestamp - lastHitTimestamp) < 0) {
            val newTimestamp = lastHitTimestamp + 1
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "processHit - Adjusting out of order hit timestamp $analyticsHit.timestamp -> $newTimestamp"
            )
            payload = replaceTimestampInPayload(payload, timestamp, newTimestamp)
            timestamp = newTimestamp
        }
        val url = getAnalyticsBaseUrl(analyticsState) ?: run {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "processHit - Retrying Analytics hit, error generating base url."
            )
            processingResult.complete(false)
            return@processHit
        }

        if (analyticsState.isAssuranceSessionActive) {
            payload += AnalyticsConstants.ANALYTICS_REQUEST_DEBUG_API_PAYLOAD
        }
        val header = mapOf(
            AnalyticsConstants.EventDataKeys.Analytics.CONTENT_TYPE_HEADER to CONTENT_TYPE_URL_ENCODED
        )
        val networkRequest = NetworkRequest(
            url,
            HttpMethod.POST,
            payload.toByteArray(Charsets.UTF_8),
            header,
            AnalyticsConstants.CONNECTION_TIMEOUT_SEC,
            AnalyticsConstants.CONNECTION_TIMEOUT_SEC
        )

        networkService.connectAsync(networkRequest) { connection ->
            if (connection == null) {
                Log.warning(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "processHit - Retrying Analytics hit, there is currently no network connectivity"
                )
                processingResult.complete(false)
                return@connectAsync
            }
            var doneProcessingResult: Boolean
            when (connection.responseCode) {
                200 -> {
                    Log.debug(
                        AnalyticsConstants.LOG_TAG,
                        CLASS_NAME,
                        "processHit - Analytics hit request with url ($url) and payload ($payload) sent successfully"
                    )
                    val httpHeaders = mapOf(
                        AnalyticsConstants.EventDataKeys.Analytics.ETAG_HEADER to connection.getResponsePropertyValue(
                            AnalyticsConstants.EventDataKeys.Analytics.ETAG_HEADER
                        ),
                        AnalyticsConstants.EventDataKeys.Analytics.SERVER_HEADER to connection.getResponsePropertyValue(
                            AnalyticsConstants.EventDataKeys.Analytics.SERVER_HEADER
                        ),
                        AnalyticsConstants.EventDataKeys.Analytics.CONTENT_TYPE_HEADER to connection.getResponsePropertyValue(
                            AnalyticsConstants.EventDataKeys.Analytics.CONTENT_TYPE_HEADER
                        )
                    )
                    val responseString = StreamUtils.readAsString(connection.inputStream)

                    val eventData: Map<String, Any?> = mapOf(
                        AnalyticsConstants.EventDataKeys.Analytics.ANALYTICS_SERVER_RESPONSE to responseString,
                        AnalyticsConstants.EventDataKeys.Analytics.HEADERS_RESPONSE to httpHeaders,
                        AnalyticsConstants.EventDataKeys.Analytics.HIT_HOST to url,
                        AnalyticsConstants.EventDataKeys.Analytics.HIT_URL to payload,
                        AnalyticsConstants.EventDataKeys.Analytics.REQUEST_EVENT_IDENTIFIER to eventIdentifier
                    )

                    // Dispatch response only if the hit was sent after the reset Identities was called.
                    // So that we only populate UUID if the hit was sent after reset in case where AAMForwarding is enabled.
                    if (timestamp > analyticsState.lastResetIdentitiesTimestamp) {
                        Log.debug(
                            AnalyticsConstants.LOG_TAG,
                            CLASS_NAME,
                            "processHit - Dispatching Analytics hit response."
                        )
                        extensionApi.dispatch(
                            Event.Builder(
                                "AnalyticsResponse",
                                EventType.ANALYTICS,
                                EventSource.RESPONSE_CONTENT
                            ).setEventData(eventData).build()
                        )
                    }
                    lastHitTimestamp = timestamp
                    doneProcessingResult = true
                }
                in arrayOf(408, 504, 503, -1) -> {
                    Log.warning(
                        AnalyticsConstants.LOG_TAG,
                        CLASS_NAME,
                        "processHit - Retrying Analytics hit, request with url $url failed with recoverable status code ${connection.responseCode}"
                    )
                    doneProcessingResult = false
                }
                else -> {
                    Log.warning(
                        AnalyticsConstants.LOG_TAG,
                        CLASS_NAME,
                        "processHit - Dropping Analytics hit, request with url $url failed with error and unrecoverable status code ${connection.responseCode}"
                    )
                    doneProcessingResult = true
                }
            }
            connection.close()
            processingResult.complete(doneProcessingResult)
        }
    }

    private fun replaceTimestampInPayload(payload: String, oldTs: Long, newTs: Long): String {
        val oldTSString = "&ts=$oldTs"
        val newTSString = "&ts=$newTs"
        return payload.replaceFirst(oldTSString, newTSString)
    }

    private fun getAnalyticsBaseUrl(state: AnalyticsState): String? {
        if (!state.isAnalyticsConfigured) {
            return null
        }
        val baseUrl =
            "https://${state.host}/b/ss/${state.rsids ?: ""}/${getAnalyticsResponseType(state)}/$version/s${(0..100000000).random()}"
        if (!UrlUtils.isValidUrl(baseUrl)) {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "getAnalyticsBaseUrl - Error building Analytics base URL, returning null"
            )
            return null
        }
        return baseUrl
    }

    private fun getAnalyticsResponseType(state: AnalyticsState): String {
        return if (state.isAnalyticsForwardingEnabled) "10" else "0"
    }

    @VisibleForTesting
    internal fun getLastHitTimestamp(): Long {
        return this.lastHitTimestamp
    }

    @VisibleForTesting
    internal fun setLastHitTimestamp(timestamp: Long) {
        this.lastHitTimestamp = timestamp
    }
}
