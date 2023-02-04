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

import com.adobe.marketing.mobile.Analytics
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.util.StringUtils

internal object AnalyticsVersionProvider {
    private const val FALLBACK_VERSION = "unknown"
    private const val OPERATING_SYSTEM = "AND"

    /**
     * Builds a version string for use in Analytics network calls.
     * The version string includes the [MobileCore] and [Analytics] versions,
     * plus an operating system and wrapper type identifier.
     *
     * @return a version string for use in network calls
     */
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

    /**
     * Get the Analytics version string for use in network calls.
     *
     * @param mobileCoreVersion the MobileCore version
     * @param analyticsVersion the Analytics extension version
     * @return a version string for use in network calls
     */
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

    /**
     * Creates a zero padded representation from the provided extension version.
     * For example, a version string of "2.0.12" is returned as "020012".
     *
     * @param versionString the version string to pad with zeros
     * @return a [String] containing a zero padded representation of the provided version.
     */
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
