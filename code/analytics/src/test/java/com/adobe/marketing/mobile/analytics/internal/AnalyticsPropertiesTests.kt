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

import com.adobe.marketing.mobile.services.NamedCollection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.secondValue
import org.mockito.kotlin.thirdValue
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner.Silent::class)
class AnalyticsPropertiesTests {

    @Mock
    private lateinit var mockedNameCollection: NamedCollection

    @Before
    fun setup() {
        Mockito.reset(mockedNameCollection)
    }

    @Test
    fun `reset()`() {
        val analyticsProperties = AnalyticsProperties(mockedNameCollection)
        analyticsProperties.setMostRecentHitTimeStamp(123456L)
        analyticsProperties.vid = "vid"
        analyticsProperties.aid = "aid"
        Mockito.reset(mockedNameCollection)
        analyticsProperties.reset()
        assertNull(analyticsProperties.vid)
        assertNull(analyticsProperties.aid)
        assertEquals(0L, analyticsProperties.mostRecentHitTimeStampInSeconds)
        val removeKeyCaptor = ArgumentCaptor.forClass(
            String::class.java
        )
        verify(mockedNameCollection, times(3)).remove(removeKeyCaptor.capture())
        assertEquals("ADOBEMOBILE_STOREDDEFAULTS_VISITOR_IDENTIFIER", removeKeyCaptor.firstValue)
        assertEquals("ADOBEMOBILE_STOREDDEFAULTS_AID", removeKeyCaptor.secondValue)
        assertEquals("mostRecentHitTimestampSeconds", removeKeyCaptor.thirdValue)
    }
}
