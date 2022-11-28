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
package com.adobe.marketing.mobile.analytics

import com.adobe.marketing.mobile.VisitorID

/**
 * The @{link AnalyticsRequestSerializer} class is to help build the POST body for analytics request
 */
internal class AnalyticsRequestSerializer {

    companion object {
        private const val REQUEST_STRINGBUILDER_CAPACITY = 2048

        /**
         * Serializes the analytics data and vars into the request string that will be later on stored in
         * database as a new hit to be processed
         *
         * @param state [AnalyticsState] object represents the shared state of other dependent modules
         * @param data  analytics data map computed with [AnalyticsExtension.processAnalyticsContextData]
         * @param vars  analytics vars map computed with [AnalyticsExtension.processAnalyticsVars]
         * @return the serialized `String`
         */
        internal fun buildRequest(
            state: AnalyticsState,
            data: MutableMap<String, String>?,
            vars: MutableMap<String, String>
        ): String {
            val analyticsVars: MutableMap<String, Any> = HashMap(vars)

            // It takes the provided data map and removes key-value pairs where the key is null or is prefixed with "&&"
            // The prefixed ones will be moved in the vars map
            if (data != null) {
                val it: MutableIterator<Map.Entry<String?, String>> = data.entries.iterator()
                while (it.hasNext()) {
                    val (key, value) = it.next()
                    if (key == null) { // handle null keys
                        it.remove()
                    } else if (key.startsWith(AnalyticsConstants.VAR_ESCAPE_PREFIX)) {
                        analyticsVars[key.substring(AnalyticsConstants.VAR_ESCAPE_PREFIX.length)] =
                            value
                        it.remove()
                    }
                }
            }
            analyticsVars[AnalyticsConstants.ANALYTICS_REQUEST_CONTEXT_DATA_KEY] =
                ContextDataUtil.translateContextData(data)
            val requestString = StringBuilder(REQUEST_STRINGBUILDER_CAPACITY)
            requestString.append("ndh=1")

            // append visitor ids if we have them
            if (state.isVisitorIDServiceEnabled && state.serializedVisitorIDsList != null) {
                requestString.append(state.serializedVisitorIDsList)
            }

            // apply and serialize all values
            ContextDataUtil.serializeToQueryString(analyticsVars, requestString)
            return requestString.toString()
        }

        /**
         * Creates a `Map<String, String>` having the VisitorIDs information (types, ids and authentication state), puts it
         * under "cid" key for the analytics request and serializes it.
         *
         * @param visitorIDs the new `List<VisitorID>` that we want to process in the analytics format
         * @return null if the visitorIDs list is null
         */
        internal fun generateAnalyticsCustomerIdString(visitorIDs: List<VisitorID>?): String? {
            if (visitorIDs == null) {
                return null
            }
            val visitorIdMap = HashMap<String, String>()
            for (visitorID in visitorIDs) {
                visitorIdMap[serializeIdentifierKeyForAnalyticsID(visitorID.idType)] = visitorID.id
                visitorIdMap[serializeAuthenticationKeyForAnalyticsID(visitorID.idType)] =
                    visitorID.authenticationState.value.toString()
            }
            val translatedIds = HashMap<String, Any>()
            translatedIds["cid"] = ContextDataUtil.translateContextData(visitorIdMap)
            // apply and serialize all values
            val requestString = StringBuilder(REQUEST_STRINGBUILDER_CAPACITY)
            ContextDataUtil.serializeToQueryString(translatedIds, requestString)
            return requestString.toString()
        }

        /**
         * Serializes data into analytics format
         *
         * @param idType the idType value from the Visitor ID service
         * @return idType.id serialized identifier key for AID
         */
        private fun serializeIdentifierKeyForAnalyticsID(idType: String): String {
            return "$idType.id"
        }

        /**
         * Serializes data into analytics format
         *
         * @param idType the idType value from the Visitor ID service
         * @return idType.as serialized authentication key for AID
         */
        private fun serializeAuthenticationKeyForAnalyticsID(idType: String): String {
            return "$idType.as"
        }
    }
}