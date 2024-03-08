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

import org.junit.Assert
import org.junit.Test
import java.util.Arrays
import java.util.regex.Pattern
import kotlin.collections.HashMap

class AnalyticsRequestSerializerTests {
    private var analyticsState = AnalyticsState()

    @Test
    fun testGenerateAnalyticsCustomerIdString_happyFlow() {
        val visitorIDList: MutableList<Map<String, Any?>> = ArrayList()
        visitorIDList.add(
            mapOf(
                "ID_ORIGIN" to "d_cid_ic",
                "ID_TYPE" to "loginidhash",
                "ID" to "97717",
                "STATE" to 0 // unknown
            )
        )
        visitorIDList.add(
            mapOf(
                "ID_ORIGIN" to "d_cid_ic",
                "ID_TYPE" to "xboxlivehash",
                "ID" to "1629158955",
                "STATE" to 1 // authenticated
            )
        )
        visitorIDList.add(
            mapOf(
                "ID_ORIGIN" to "d_cid_ic",
                "ID_TYPE" to "psnidhash",
                "ID" to "1144032295",
                "STATE" to 2 // logged out
            )
        )
        visitorIDList.add(
            mapOf(
                "ID_ORIGIN" to "d_cid",
                "ID_TYPE" to "pushid",
                "ID" to "testPushId",
                "STATE" to 1 // authenticated
            )
        )
        val expectedString =
            "&cid.&loginidhash.&id=97717&as=0&.loginidhash&xboxlivehash.&id=1629158955&as=1&.xboxlivehash&psnidhash.&id=1144032295&as=2&.psnidhash&pushid.&id=testPushId&as=1&.pushid&.cid"
        val expectedList = Arrays.asList(*expectedString.split("&").toTypedArray())
        expectedList.sort()
        val analyticsIdString =
            AnalyticsRequestSerializer.generateAnalyticsCustomerIdString(visitorIDList)
        val testList = Arrays.asList(*analyticsIdString!!.split("&").toTypedArray())
        testList.sort()
        Assert.assertEquals(expectedList.size.toLong(), testList.size.toLong())
        Assert.assertTrue(
            String.format(
                "Lists don't match. \nExpected: %s\nActual  : %s",
                expectedList.toString(),
                testList.toString()
            ),
            expectedList == testList
        )
    }

    @Test
    fun testGenerateAnalyticsCustomerIdString_returnsNull_when_nullVIDList() {
        Assert.assertNull(AnalyticsRequestSerializer.generateAnalyticsCustomerIdString(null))
    }

    @Test
    fun testBuildRequest_happyFlow_ValidDataAndValidVars() {
        val vars: MutableMap<String, String> = HashMap()
        vars["v1"] = "evar1Value"
        vars["v2"] = "evar2Value"
        val data: MutableMap<String, String> = HashMap()
        data["testKey1"] = "val1"
        data["testKey2"] = "val2"
        val result: String = AnalyticsRequestSerializer.buildRequest(analyticsState, data, vars)
        Assert.assertEquals("&c.&testKey2=val2&testKey1=val1&.c", getContextData(result))
        Assert.assertEquals("ndh=1&v2=evar2Value&v1=evar1Value", getAdditionalData(result))
        Assert.assertTrue(getCidData(result).isEmpty())
    }

    @Test
    fun testBuildRequest_when_NullDataAndValidVars() {
        val vars: MutableMap<String, String> = HashMap()
        vars["v1"] = "evar1Value"
        vars["v2"] = "evar2Value"
        val result: String = AnalyticsRequestSerializer.buildRequest(analyticsState, null, vars)
        Assert.assertEquals("ndh=1&v2=evar2Value&v1=evar1Value", getAdditionalData(result))
        Assert.assertTrue(getCidData(result).isEmpty())
        Assert.assertTrue(getContextData(result).isEmpty())
    }

    @Test
    fun testBuildRequest_movesToVars_when_dataKeysPrefixed() {
        // the data keys which are prefixed with "&&" will be moved to analytics vars
        val vars: MutableMap<String, String> = HashMap()
        vars["v1"] = "evar1Value"
        val data: MutableMap<String, String> = HashMap()
        data["&&key1"] = "val1"
        data["key2"] = "val2"
        val result: String = AnalyticsRequestSerializer.buildRequest(analyticsState, data, vars)
        Assert.assertEquals("ndh=1&key1=val1&v1=evar1Value", getAdditionalData(result))
        Assert.assertEquals("&c.&key2=val2&.c", getContextData(result))
        Assert.assertTrue(getCidData(result).isEmpty())
    }

    @Test
    fun testBuildRequestWhenNullVisitorIdList() {
        // the data keys which are prefixed with "&&" will be moved to analytics vars
        analyticsState.update(
            mapOf(
                "com.adobe.module.identity" to mapOf(
                    "mid" to "testMID"
                ),
                "com.adobe.module.configuration" to mapOf(
                    "analytics.server" to "analyticsServer",
                    "experienceCloud.org" to "marketingServer"
                )
            )
        )
        val result: String = AnalyticsRequestSerializer.buildRequest(
            analyticsState,
            HashMap(
                mapOf(
                    "testKey1" to "val1",
                    "testKey2" to "val2"
                )
            ),
            null
        )
        Assert.assertTrue(result.contains("ndh=1"))
        Assert.assertTrue(result.contains("&c."))
        Assert.assertTrue(result.contains("&.c"))
        Assert.assertTrue(result.contains("&testKey1=val1"))
        Assert.assertTrue(result.contains("&testKey2=val2"))
    }

    @Test
    fun testBuildRequestWhenIncompleteVisitorIds_skipsIdsWithMissingIDType() {
        val visitorIDList: MutableList<Map<String, Any?>> = ArrayList()
        visitorIDList.add(
            mapOf(
                "ID_ORIGIN" to "d_cid_ic",
                "ID_TYPE" to "loginidhash",
                "ID" to "97717",
                "STATE" to 0 // unknown
            )
        )
        visitorIDList.add(
            mapOf(
                "ID_ORIGIN" to "d_cid_ic",
                "ID_TYPE" to null,
                "ID" to "11111",
                "STATE" to 0 // unknown
            )
        )
        analyticsState.update(
            mapOf(
                "com.adobe.module.identity" to mapOf(
                    "mid" to "testMID",
                    "visitoridslist" to visitorIDList
                ),
                "com.adobe.module.configuration" to mapOf(
                    "analytics.server" to "analyticsServer",
                    "experienceCloud.org" to "marketingServer"
                )
            )
        )
        val result: String = AnalyticsRequestSerializer.buildRequest(
            analyticsState,
            HashMap(
                mapOf(
                    "testKey1" to "val1",
                    "testKey2" to "val2"
                )
            ),
            null
        )
        val assertMessage: String = String.format("Result was: %s", result)
        Assert.assertTrue(assertMessage, result.contains("ndh=1"))
        Assert.assertTrue(assertMessage, result.contains("&c."))
        Assert.assertTrue(assertMessage, result.contains("&.c"))
        Assert.assertTrue(assertMessage, result.contains("&testKey1=val1"))
        Assert.assertTrue(assertMessage, result.contains("&testKey2=val2"))
        Assert.assertTrue(assertMessage, result.contains("&cid.&loginidhash.&as=0&id=97717&.loginidhash&"))
        Assert.assertFalse(assertMessage, result.contains("&id=11111&as=0&"))
    }

    @Test
    fun testBuildRequestWhenVisitorIdServiceNotEnabled_skipsSerializedIds() {
        // Note: this test increases coverage for the safety checks we have, but this scenario is not
        // expected in a prod workflow: missing orgid in config, but contains visitorids list
        val visitorIDList: MutableList<Map<String, Any?>> = ArrayList()
        visitorIDList.add(
            mapOf(
                "ID_ORIGIN" to "d_cid_ic",
                "ID_TYPE" to "loginidhash",
                "ID" to "97717",
                "STATE" to 0 // unknown
            )
        )
        analyticsState.update(
            mapOf(
                "com.adobe.module.identity" to mapOf(
                    "mid" to "testMID",
                    "visitoridslist" to visitorIDList
                ),
                "com.adobe.module.configuration" to mapOf(
                    "analytics.server" to "analyticsServer"
                )
            )
        )
        val result: String = AnalyticsRequestSerializer.buildRequest(
            analyticsState,
            HashMap(
                mapOf(
                    "testKey1" to "val1",
                    "testKey2" to "val2"
                )
            ),
            null
        )
        Assert.assertTrue(result.contains("ndh=1"))
        Assert.assertTrue(result.contains("&c."))
        Assert.assertTrue(result.contains("&.c"))
        Assert.assertTrue(result.contains("&testKey1=val1"))
        Assert.assertTrue(result.contains("&testKey2=val2"))
        Assert.assertFalse(result.contains("&cid.&loginidhash"))
    }

    @Test
    fun testBuildRequest_when_NullDataAndNullVars() {
        val result: String = AnalyticsRequestSerializer.buildRequest(analyticsState, null, null)
        Assert.assertEquals("ndh=1", getAdditionalData(result))
    }

    // --------------------- helper methods ----------------------
    private fun getCidData(source: String): String {
        val pattern = Pattern.compile(".*(&cid\\.(.*)&\\.cid).*")
        val matcher = pattern.matcher(source)
        return if (matcher.matches()) matcher.group(1) else ""
    }

    private fun getContextData(source: String): String {
        val pattern = Pattern.compile(".*(&c\\.(.*)&\\.c).*")
        val matcher = pattern.matcher(source)
        return if (matcher.matches()) matcher.group(1) else ""
    }

    private fun getAdditionalData(source: String): String {
        return source.replace(getCidData(source), "").replace(getContextData(source), "")
    }
}
