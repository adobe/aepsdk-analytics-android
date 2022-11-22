/* **************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2017 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 *
 **************************************************************************/
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