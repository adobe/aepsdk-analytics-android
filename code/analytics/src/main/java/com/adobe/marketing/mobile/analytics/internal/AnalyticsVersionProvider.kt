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
