package com.adobe.marketing.mobile.analytics

import com.adobe.marketing.mobile.Analytics
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.util.StringUtils

internal object AnalyticsVersionProvider {
    private const val FALLBACK_VERSION = "unknown"
    private const val OPERATING_SYSTEM = "AND"

    internal fun buildVersionString(
        coreExtensionVersion: String = MobileCore.extensionVersion(),
        analyticsExtensionVersion: String = Analytics.extensionVersion()
    ): String {
        val analyticsVersion =
            getVersionString(coreExtensionVersion, analyticsExtensionVersion)
        return if (!StringUtils.isNullOrEmpty(analyticsVersion)) {
            analyticsVersion
        } else {
            FALLBACK_VERSION
        }
    }

    private fun getVersionString(mobileCoreVersion: String, analyticsVersion: String): String {
        var coreVersion = mobileCoreVersion
        var wrapperType = "N"
        val mobileCoreVersionInfo = coreVersion.split("-").toTypedArray()
        if (mobileCoreVersionInfo.size == 2) {
            coreVersion = mobileCoreVersionInfo[0]
            wrapperType = mobileCoreVersionInfo[1]
        }
        val mobileCoreFormattedVersion = getFormattedVersion(coreVersion)
        val analyticsFormattedVersion = getFormattedVersion(analyticsVersion)

        // version format <operatingsystem><wrappertype><analyticsversion><coreversion>
        return OPERATING_SYSTEM + wrapperType + analyticsFormattedVersion + mobileCoreFormattedVersion
    }

    private fun getFormattedVersion(versionString: String): String {
        var formattedVersionString = "000000"
        val versionInfo = versionString.split(".").toTypedArray()
        if (versionInfo.size == 3) {
            val major = if (versionInfo[0].length == 1) "0" + versionInfo[0] else versionInfo[0]
            val minor = if (versionInfo[1].length == 1) "0" + versionInfo[1] else versionInfo[1]
            val build = if (versionInfo[2].length == 1) "0" + versionInfo[2] else versionInfo[2]
            formattedVersionString = major + minor + build
        }
        return formattedVersionString
    }
}