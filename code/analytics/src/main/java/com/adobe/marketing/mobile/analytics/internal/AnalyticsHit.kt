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

import com.adobe.marketing.mobile.services.DataEntity
import org.json.JSONObject

/**
 * Class representing an Analytics hit.
 *
 * @param payload the hit payload
 * @param timestampSec timestamp of the hit in seconds
 * @param eventIdentifier the identifier of the event which triggered the hit
 */
internal class AnalyticsHit(val payload: String, val timestampSec: Long, val eventIdentifier: String) {

    /**
     * Serializes this hit to a [DataEntity] for use in a [HitQueue].
     * If the hit fails to serialize, then an [DataEntity] with empty data is returned.
     *
     * @return this hit serialized as a [DataEntity]
     */
    internal fun toDataEntity(): DataEntity {
        val map = mapOf<String, Any>(
            PAYLOAD to payload,
            TIMESTAMP to timestampSec,
            EVENT_IDENTIFIER to eventIdentifier
        )
        val json = try {
            JSONObject(map).toString()
        } catch (e: Exception) {
            EMPTY_JSON
        }
        return DataEntity(json)
    }

    companion object {
        private const val PAYLOAD = "payload"
        private const val TIMESTAMP = "timestamp"
        private const val EVENT_IDENTIFIER = "eventIdentifier"
        private const val EMPTY_JSON = ""

        /**
         * Deserializes a [DataEntity] to an [AnalyticsHit].
         *
         * @return an [AnalyticsHit] deserialized from a [DataEntity]
         */
        internal fun from(dataEntity: DataEntity): AnalyticsHit {
            val json = dataEntity.data ?: EMPTY_JSON
            val jsonObject = try {
                JSONObject(json)
            } catch (e: Exception) {
                JSONObject()
            }
            return AnalyticsHit(
                jsonObject.optString(PAYLOAD),
                jsonObject.optLong(TIMESTAMP),
                jsonObject.optString(EVENT_IDENTIFIER)
            )
        }
    }
}
