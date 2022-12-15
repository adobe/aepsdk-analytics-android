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

private val CONTEXT_DATA_REGEX = Regex("(&c\\.(.*)&\\.c)")

internal fun extractQueryParamsFrom(url: String): Map<String, String> {
    val contextDataString = extractContextDataStringFrom(url) ?: run {
        return@extractQueryParamsFrom emptyMap()
    }
    val queryString = url.replace(contextDataString, "")
    val map = mutableMapOf<String, String>()
    var preKey = ""
    queryString.split("&").forEach {
        if (it.isNotEmpty()) {
            val kvPair = it.split("=")
            if (kvPair.size == 2) {
                map[kvPair[0]] = kvPair[1]
                preKey = kvPair[0]
            }
            if (kvPair.size == 1 && preKey.isNotEmpty()) {
                map[preKey] = "${map.get(preKey)}&${kvPair[0]}"
            }
        }
    }
    return map
}

private fun extractContextDataStringFrom(url: String): String? {
    return CONTEXT_DATA_REGEX.find(url)?.value
}

internal fun extractContextDataFrom(url: String): Map<String, String> {
    val kvPairs = extractContextDataStringFrom(url)?.replace("&c.", "")?.replace("&.c", "") ?: run {
        return@extractContextDataFrom emptyMap()
    }
    return ContextDataUtil.deserializeContextDataKeyValuePairs(kvPairs)
}

internal fun extractContextDataKVPairFrom(url: String): String {
    val kvPairs = extractContextDataStringFrom(url)?.replace("&c.", "")?.replace("&.c", "") ?: run {
        return@extractContextDataKVPairFrom ""
    }
    return kvPairs
}

