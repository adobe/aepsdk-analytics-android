package com.adobe.marketing.mobile.analytics

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