/*
  Copyright 2023 Adobe. All rights reserved.
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.io.FileInputStream
import java.util.Properties

@RunWith(MockitoJUnitRunner.Silent::class)
class AnalyticsExtensionVersionTest {
    private val PROPERTY_MODULE_VERSION = "moduleVersion"

    @Test
    fun internalExtensionVersion_publicExtensionVersion_asEqual() {
        assertEquals(AnalyticsConstants.EXTENSION_VERSION, Analytics.extensionVersion())
    }

    @Test
    @Ignore
    fun extensionVersion_verifyModuleVersionInPropertiesFile_asEqual() {
        val properties: Properties = loadGradleProperties()
        assertNotNull(Analytics.extensionVersion())
        assertFalse(Analytics.extensionVersion().isEmpty())
        val moduleVersion: String = properties.getProperty(PROPERTY_MODULE_VERSION)
        assertNotNull(moduleVersion)
        assertFalse(moduleVersion.isEmpty())
        assertEquals(
            java.lang.String.format(
                "Expected version to match in gradle.properties (%s) and extensionVersion API (%s)",
                moduleVersion,
                Analytics.extensionVersion()
            ),
            moduleVersion,
            Analytics.extensionVersion()
        )
    }

    private fun loadGradleProperties(): Properties {
        val properties = Properties()

        val gradlePropertiesPath = "../gradle.properties"
        FileInputStream(gradlePropertiesPath).use { properties.load(it) }
        return properties
    }
}
