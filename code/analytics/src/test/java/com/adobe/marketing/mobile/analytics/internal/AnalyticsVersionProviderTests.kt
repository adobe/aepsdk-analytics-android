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

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsVersionProviderTests {

    @Test
    fun `buildVersion - happy path`() {
        assertEquals(
            "ANDN020000020000",
            AnalyticsVersionProvider.buildVersionString("2.0.0", "2.0.0")
        )
    }

    @Test
    fun `buildVersion - with single and double digit version numbers`() {
        assertEquals(
            "ANDC091827111213",
            AnalyticsVersionProvider.buildVersionString("11.12.13-C", "9.18.27")
        )
    }

    @Test
    fun `buildVersion - with double digit version numbers`() {
        assertEquals(
            "ANDF223344556677",
            AnalyticsVersionProvider.buildVersionString("55.66.77-F", "22.33.44")
        )
    }

    @Test
    fun `buildVersion - with invalid analytics version`() {
        assertEquals(
            "ANDX000000556677",
            AnalyticsVersionProvider.buildVersionString("55.66.77-X", "33.44")
        )
    }
}
