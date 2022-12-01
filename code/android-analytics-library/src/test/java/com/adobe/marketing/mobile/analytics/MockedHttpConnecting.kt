package com.adobe.marketing.mobile.analytics

import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.NetworkRequest
import java.io.InputStream

internal typealias NetworkMonitor = (request: NetworkRequest) -> Unit

internal class MockedHttpConnecting : HttpConnecting {
    private var rulesStream: InputStream? = null
    internal var responseCode = 300
    internal var responseProperties = mapOf<String, String>()

    override fun getInputStream(): InputStream? {
        return null
    }

    override fun getErrorStream(): InputStream? {
        return null
    }

    override fun getResponseCode(): Int {
        return responseCode
    }

    override fun getResponseMessage(): String {
        return ""
    }

    override fun getResponsePropertyValue(responsePropertyKey: String?): String {
        return responseProperties[responsePropertyKey] ?: ""
    }

    override fun close() {
        rulesStream?.close()
    }

}