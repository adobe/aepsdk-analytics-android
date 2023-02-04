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
import com.adobe.marketing.mobile.services.NamedCollection
import java.nio.charset.StandardCharsets

/**
 * [AnalyticsProperties] class hosts the properties needed by [AnalyticsExtension] when processing events.
 * @param dataStore the [NamedCollection] for persistently storing the Analytics properties.
 */
internal class AnalyticsProperties(
    private val dataStore: NamedCollection
) {
    companion object {
        val CHARSET: String = StandardCharsets.UTF_8.name()
    }

    /**
     * Resets these [AnalyticsProperties].
     * Clears the AID and VID values, plus clears the most recent hit timestamp in memory and
     * from the data store.
     */
    fun reset() {
        mostRecentHitTimeStampInSeconds = 0L
        vid = null
        aid = null
        dataStore.remove(AnalyticsConstants.DataStoreKeys.MOST_RECENT_HIT_TIMESTAMP_SECONDS)
    }

    /**
     * The Analytics Tracking Identifier
     */
    internal var aid: String? = null
        get() = dataStore.getString(AnalyticsConstants.DataStoreKeys.AID_KEY, null)

        @VisibleForTesting
        internal set(aidValue) {
            if (aidValue.isNullOrEmpty()) {
                dataStore.remove(AnalyticsConstants.DataStoreKeys.AID_KEY)
            } else {
                dataStore.setString(AnalyticsConstants.DataStoreKeys.AID_KEY, aidValue)
            }
            field = aidValue
        }

    /**
     * The Analytics Visitor Identifier
     */
    internal var vid: String? = null
        get() = dataStore.getString(AnalyticsConstants.DataStoreKeys.VISITOR_IDENTIFIER_KEY, null)
        internal set(vidValue) {
            if (vidValue.isNullOrEmpty()) {
                dataStore.remove(AnalyticsConstants.DataStoreKeys.VISITOR_IDENTIFIER_KEY)
            } else {
                dataStore.setString(AnalyticsConstants.DataStoreKeys.VISITOR_IDENTIFIER_KEY, vidValue)
            }
            field = vidValue
        }

    /**
     * The most recent hit timestamp which is the timestamp of the last processed track request.
     * Used when calculating the timestamp for backdated hits.
     * @return timestamp in seconds of the most recent hit or 0 if no timestamp was set
     */
    internal var mostRecentHitTimeStampInSeconds: Long = 0L
        get() = dataStore.getLong(
            AnalyticsConstants.DataStoreKeys.MOST_RECENT_HIT_TIMESTAMP_SECONDS,
            0L
        )
        private set

    /**
     * Set the most recent hit timestamp which is the timestamp of the last processed track request.
     * Sets the most recent hit timestamp only if the given [timestampInSeconds] is greater than
     * the currently stored timestamp.
     *
     * @param timestampInSeconds timestamp to set
     */
    internal fun setMostRecentHitTimeStamp(timestampInSeconds: Long) {
        if (mostRecentHitTimeStampInSeconds < timestampInSeconds) {
            dataStore.setLong(
                AnalyticsConstants.DataStoreKeys.MOST_RECENT_HIT_TIMESTAMP_SECONDS,
                timestampInSeconds
            )
            mostRecentHitTimeStampInSeconds = timestampInSeconds
        }
    }
}
