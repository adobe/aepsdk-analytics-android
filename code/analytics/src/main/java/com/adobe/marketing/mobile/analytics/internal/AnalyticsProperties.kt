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
 */
internal class AnalyticsProperties(private val dataStore: NamedCollection) {

    companion object {
        val CHARSET: String = StandardCharsets.UTF_8.name()
    }

    fun reset() {
        mostRecentHitTimeStampInSeconds = 0L
        vid = null
        aid = null
        dataStore.remove(AnalyticsConstants.DataStoreKeys.MOST_RECENT_HIT_TIMESTAMP_SECONDS)
    }

    internal var aid: String? = dataStore.getString(AnalyticsConstants.DataStoreKeys.AID_KEY, null)
        @VisibleForTesting
        internal set(aid) {
            if (aid == null || aid.isEmpty()) {
                dataStore.remove(AnalyticsConstants.DataStoreKeys.AID_KEY)
            } else {
                dataStore.setString(AnalyticsConstants.DataStoreKeys.AID_KEY, aid)
            }
            field = aid
        }

    internal var vid: String? =
        dataStore.getString(AnalyticsConstants.DataStoreKeys.VISITOR_IDENTIFIER_KEY, null)
        internal set(vid) {
            if (vid == null || vid.isEmpty()) {
                dataStore.remove(AnalyticsConstants.DataStoreKeys.VISITOR_IDENTIFIER_KEY)
            } else {
                dataStore.setString(AnalyticsConstants.DataStoreKeys.VISITOR_IDENTIFIER_KEY, vid)
            }
            field = vid
        }

    internal var mostRecentHitTimeStampInSeconds: Long =
        dataStore.getLong(
            AnalyticsConstants.DataStoreKeys.MOST_RECENT_HIT_TIMESTAMP_SECONDS,
            0L
        )
        private set

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
