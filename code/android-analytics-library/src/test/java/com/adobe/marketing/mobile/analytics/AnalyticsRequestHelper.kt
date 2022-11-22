package com.adobe.marketing.mobile.analytics

private val CONTEXT_DATA_REGEX = Regex("(&c\\.(.*)&\\.c)")

internal fun extractQueryParamsFrom(url: String): Map<String, String> {
    val contextDataString = extractContextDataStringFrom(url) ?: run {
        return@extractQueryParamsFrom emptyMap()
    }
    val queryString = url.replace(contextDataString, "")
    val map = mutableMapOf<String, String>()
    queryString.split("&").forEach {
        if (it.isNotEmpty()) {
            val kvPair = it.split("=")
            map[kvPair[0]] = kvPair[1]
        }
    }
    return map
}

private fun extractContextDataStringFrom(url: String): String? {
    return CONTEXT_DATA_REGEX.find(url)?.value
}

internal fun extractContextDataFrom(url: String): Map<String, String> {
    val kvPairs = extractContextDataStringFrom(url)?.replace("&c.", "")?.replace("&.c","") ?: run {
        return@extractContextDataFrom emptyMap()
    }
    return ContextDataUtil.deserializeContextDataKeyValuePairs(kvPairs)
}

