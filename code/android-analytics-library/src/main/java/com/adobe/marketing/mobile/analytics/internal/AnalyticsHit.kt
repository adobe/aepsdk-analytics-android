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

internal class AnalyticsHit(val payload: String, val timestamp: Long, val eventIdentifier: String) {
    internal fun toDataEntity(): DataEntity {
        val map = mapOf<String, Any>(
            PAYLOAD to payload,
            TIMESTAMP to timestamp,
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