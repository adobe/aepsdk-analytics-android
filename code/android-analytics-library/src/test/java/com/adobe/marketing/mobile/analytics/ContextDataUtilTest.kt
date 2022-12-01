package com.adobe.marketing.mobile.analytics

import com.adobe.marketing.mobile.analytics.ContextDataUtil.cleanContextDataKey
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test


class ContextDataUtilTest {
    private val testData: MutableMap<String, String> = HashMap()
    private val expectedResult: MutableMap<String, String> = HashMap()

    @Before
    fun setup() {
        testData["key1"] = "val1"
        testData["key2A"] = "val2A"
        testData["key3.A"] = "val3.A"
        testData["key4...B"] = "val4...B"
        testData["key5!"] = "val!"
        testData["key6@"] = "val@"
        testData["key7#"] = "val#"
        testData["key8$"] = "val$"
        testData["key9%"] = "val%"
        expectedResult["key1"] = "val1"
        expectedResult["key2A"] = "val2A"
        expectedResult["key3.A"] = "val3.A" // this one should have an inner A key
        expectedResult["key4.B"] = "val4...B" // this one should have an inner B key
        expectedResult["key5"] = "val!"
        expectedResult["key6"] = "val@"
        expectedResult["key7"] = "val#"
        expectedResult["key8"] = "val$"
        expectedResult["key9"] = "val%"
    }

    @Test
    @Throws(Exception::class)
    fun testTranslateContextData() {
        val result = ContextDataUtil.translateContextData(testData)
        assertEquals(expectedResult.size, result.size())
        var cDataObj: ContextData
        assertEquals(expectedResult["key1"], result["key1"].value)
        assertEquals(expectedResult["key2A"], result["key2A"].value)
        cDataObj = result["key3"]
        var cDataSubObj = cDataObj.data
        assertTrue(cDataSubObj.containsKey("A"))
        var cDataValObj = cDataSubObj["A"] as ContextData?
        assertEquals(expectedResult["key3.A"], cDataValObj!!.value)
        cDataObj = result["key4"]
        cDataSubObj = cDataObj.data
        assertTrue(cDataSubObj.containsKey("B"))
        cDataValObj = cDataSubObj["B"] as ContextData?
        assertEquals(expectedResult["key4.B"], cDataValObj!!.value)
        assertEquals(expectedResult["key5"], result["key5"].value)
        assertEquals(expectedResult["key6"], result["key6"].value)
        assertEquals(expectedResult["key7"], result["key7"].value)
        assertEquals(expectedResult["key8"], result["key8"].value)
        assertEquals(expectedResult["key9"], result["key9"].value)
    }

    @Test
    @Throws(Exception::class)
    fun testCleanContextDataDictionary() {
        val result = ContextDataUtil.cleanContextDataDictionary(testData)
        assertEquals(expectedResult.size.toLong(), result.size.toLong())
        assertEquals(expectedResult["key1"], result["key1"])
        assertEquals(expectedResult["key2"], result["key2"])
        assertEquals(expectedResult["key3.A"], result["key3.A"])
        assertEquals(expectedResult["key4.B"], result["key4.B"])
        assertEquals(expectedResult["key5"], result["key5"])
        assertEquals(expectedResult["key6"], result["key6"])
        assertEquals(expectedResult["key7"], result["key7"])
        assertEquals(expectedResult["key8"], result["key8"])
        assertEquals(expectedResult["key9"], result["key9"])
    }

    @Test
    @Throws(Exception::class)
    fun testSerializeToQueryString_When_DictionaryIsNull() {
        val test = StringBuilder()
        ContextDataUtil.serializeToQueryString(null, test)
        assertNotNull(test)
        assertTrue(test.toString().isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun testSerializeToQueryString_When_DictionaryWithNullKey() {
        val dict = HashMap<String?, Any>()
        dict[null] = "val1"
        dict["key2"] = "val2"
        dict["key3"] = "val3"
        val test = StringBuilder()
        ContextDataUtil.serializeToQueryString(dict, test)
        val result = test.toString()
        assertFalse(result.contains("valq"))
        assertTrue(result.contains("&key2=val2"))
        assertTrue(result.contains("&key3=val3"))
    }

    @Test
    @Throws(Exception::class)
    fun testSerializeToQueryString_When_ValuesAreString() {
        val dict = HashMap<String, Any>()
        dict["key1"] = "val1"
        dict["key2"] = "val2"
        dict["key3"] = "val3"
        val test = StringBuilder()
        ContextDataUtil.serializeToQueryString(dict, test)
        val result = test.toString()
        assertTrue(result.contains("&key3=val3"))
        assertTrue(result.contains("&key2=val2"))
        assertTrue(result.contains("&key1=val1"))
    }

    @Test
    @Throws(Exception::class)
    fun testSerializeToQueryString_When_ValueNonString() {
        val dict: MutableMap<String, Any> = HashMap()
        dict["key1"] = 5
        val test = StringBuilder()
        ContextDataUtil.serializeToQueryString(dict, test)
        assertEquals("&key1=5", test.toString())
    }

    @Test
    @Throws(Exception::class)
    fun testSerializeToQueryString_When_ValueIsContextDataInstance() {
        val data2 = ContextData()
        data2.value = "val2"
        val data = ContextData()
        data.value = "val1"
        data.put("subkey1", data2)
        val dict = HashMap<String, Any>()
        dict["key1"] = data
        val result = StringBuilder()
        ContextDataUtil.serializeToQueryString(dict, result)
        assertEquals(
            "Context data objects did not serialize properly",
            "&key1=val1&key1.&subkey1=val2&.key1",
            result.toString()
        )
    }

    @Test
    @Throws(Exception::class)
    fun testSerializeToQueryString_When_ValueIsArrayList() {
        val list = ArrayList<String>()
        list.add("TestArrayList1")
        list.add("TestArrayList2")
        list.add("TestArrayList3")
        list.add("TestArrayList4")
        val dict: MutableMap<String, Any> = HashMap()
        dict["key1"] = list
        val result = StringBuilder()
        ContextDataUtil.serializeToQueryString(dict, result)
        assertEquals(
            "&key1=TestArrayList1%2CTestArrayList2%2CTestArrayList3%2CTestArrayList4",
            result.toString()
        )
    }

    @Test
    @Throws(Exception::class)
    fun testJoin() {
        val list: MutableList<String?> = ArrayList()
        list.add("TestArrayList1")
        list.add("TestArrayList2")
        list.add("TestArrayList3")
        list.add("TestArrayList4")
        assertEquals(
            "TestArrayList1,TestArrayList2,TestArrayList3,TestArrayList4",
            ContextDataUtil.join(list, ",")
        )
    }

    @Test
    @Throws(Exception::class)
    fun testCleanContextDataKey_AllowsUnderscore() {
        assertEquals("__key__", cleanContextDataKey("__key__"))
    }

    @Test
    @Throws(Exception::class)
    fun testCleanContextDataKey_RemovesSpecialCharacters() {
        val specialCharacters = arrayOf(
            "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "+", "=", "{", "}",
            "[", "]", "|", "\\", ":", ";", "\"", "'", "<", ">", ",", "/", "?", "~", "`", " "
        )
        val expectedKey = "key"
        for (character in specialCharacters) {
            assertEquals(
                "Assertion failed for [key$character]",
                expectedKey,
                cleanContextDataKey("key$character")
            )
        }
    }

    @Test
    @Throws(Exception::class)
    fun testCleanContextData_KeyMultiPeriod() {
        val expectedKey = "key.key"
        assertEquals(expectedKey, cleanContextDataKey("key.key"))
        assertEquals(expectedKey, cleanContextDataKey("key..key"))
        assertEquals(expectedKey, cleanContextDataKey("key...key"))
        assertEquals(expectedKey, cleanContextDataKey("key....key"))
    }

    @Test
    @Throws(Exception::class)
    fun testCleanContextDataKey_PeriodBeginningEnd() {
        val expectedKey = "key.key"
        assertEquals(expectedKey, cleanContextDataKey(".key.key."))
        assertEquals(expectedKey, cleanContextDataKey("..key..key.."))
        assertEquals(expectedKey, cleanContextDataKey("...key...key..."))
        assertEquals(expectedKey, cleanContextDataKey("....key....key...."))
    }

    @Test
    @Throws(Exception::class)
    fun testCleanContextData_KeyUnicode() {
        assertEquals("test", cleanContextDataKey("test网页"))
    }

    @Test
    @Throws(Exception::class)
    fun testCleanContextDataKeyReturnsNull_When_NullKey() {
        assertNull(cleanContextDataKey(null))
    }

    @Test
    @Throws(Exception::class)
    fun testCleanContextDataKeyReturnsNull_When_EmptyString() {
        assertNull(cleanContextDataKey(""))
    }

    @Test
    @Throws(Exception::class)
    fun testCleanContextDataKeyReturnsNull_When_KeyHasOnlyPeriods() {
        assertNull(cleanContextDataKey("......."))
    }

    @Test
    @Throws(Exception::class)
    fun testCleanContextDataKeyReturnsNull_When_OnlyDisallowedCharacters() {
        assertNull(cleanContextDataKey("???????&!@#!@#*&(**^^@#(@#()$)"))
    }

    @Test
    @Throws(Exception::class)
    fun testAppendContextData_When_EmptySource() {
        assertEquals(
            "", ContextDataUtil.appendContextData(
                mapOf(
                    "new-key" to "value"
                ), ""
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun testAppendContextData_When_NoContextDataInSource() {
        assertEquals(
            "abcde&c.&newkey=value&.c", ContextDataUtil.appendContextData(
                mapOf(
                    "new-key" to "value"
                ), "abcde"
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun testAppendContextData_When_NullSource() {
        assertNull(
            ContextDataUtil.appendContextData(
                mapOf(
                    "new-key" to "value"
                ), null
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun testAppendContextData_When_NullReferrerData() {
        assertEquals(
            "&c.&newkey=value&.c",
            ContextDataUtil.appendContextData(null, "&c.&newkey=value&.c")
        )
    }

    @Test
    @Throws(Exception::class)
    fun testAppendContextData_When_ContextDataOnePair() {
        assertEquals(
            "&c.&key=value&.c", ContextDataUtil.appendContextData(
                mapOf(
                    "key" to "value"
                ), "&c.&.c"
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun testAppendContextData_When_ContextDataTwoPair() {
        val result = ContextDataUtil.appendContextData(
            mapOf(
                "key" to "value",
                "key1" to "value1"
            ), "&c.&.c"
        )
        assertTrue(contextDataInCorrectSequence(result, "key=value", "&c.", "&.c"))
        assertTrue(contextDataInCorrectSequence(result, "key1=value1", "&c.", "&.c"))
    }

    @Test
    @Throws(Exception::class)
    fun testAppendContextData_When_ContextDataWithNestedKeyName() {
        val result = ContextDataUtil.appendContextData(
            mapOf(
                "key" to "value",
                "key.nest" to "value1"
            ), "&c.&.c"
        )
        assertTrue(contextDataInCorrectSequence(result, "key=value", "&c.", "&.c"))
        assertTrue(contextDataInCorrectSequence(result, "nest=value1", "&key.", "&.key"))
    }

    @Test
    @Throws(Exception::class)
    fun testAppendContextData_When_NestedKeyNameOverrideOldValue() {
        val result =
            ContextDataUtil.appendContextData(
                mapOf(
                    "key" to "new-value",
                    "key.nest" to "new-value1"
                ), "&c.&key=value&key.&nest=value1&.key&.c"
            )
        assertTrue(contextDataInCorrectSequence(result, "key=new-value", "&c.", "&.c"))
        assertTrue(contextDataInCorrectSequence(result, "nest=new-value1", "&key.", "&.key"))
    }

    @Test
    @Throws(Exception::class)
    fun testAppendContextData_When_NestedKeyNameAppendToExistingLevel() {
        val result = ContextDataUtil.appendContextData(
            mapOf(
                "key.new" to "value",
                "key1.new" to "value"
            ),
            "&c.&key=value&key.&nest=value1&.key&key1.&nest=value1&.key1&.c"
        )
        assertTrue(contextDataInCorrectSequence(result, "new=value", "&key1.", "&.key1"))
        assertTrue(contextDataInCorrectSequence(result, "nest=value1", "&key1.", "&.key1"))
        assertTrue(contextDataInCorrectSequence(result, "key=value", "&c.", "&.c"))
        assertTrue(contextDataInCorrectSequence(result, "new=value", "&key.", "&.key"))
        assertTrue(contextDataInCorrectSequence(result, "nest=value1", "&key.", "&.key"))
    }

    @Test
    @Throws(Exception::class)
    fun testAppendContextData_When_NestedKeyNameAppendToExistingLevel_4Level() {
        val result = ContextDataUtil.appendContextData(
            mapOf(
                "level1.level2.level3.level4.new" to "new",
                "key1.new" to "value",
                "key.new" to "value"
            ),
            "&c.&key=value&key.&nest=value1&.key&key1.&nest=value1&.key1&level1.&level2.&level3.&level4.&old=old&.level4&.level3&.level2&.level1&.c"
        )
        assertTrue(contextDataInCorrectSequence(result, "&level2.", "&level1.", "&.level1"))
        assertTrue(contextDataInCorrectSequence(result, "&level3.", "&level2.", "&.level2"))
        assertTrue(contextDataInCorrectSequence(result, "&level4.", "&level3.", "&.level3"))
        assertTrue(contextDataInCorrectSequence(result, "old=old", "&level4.", "&.level4"))
        assertTrue(contextDataInCorrectSequence(result, "new=new", "&level4.", "&.level4"))
    }

    @Test
    @Throws(Exception::class)
    fun testAppendContextData_When_ContextDataWithUTF8() {
        val result = ContextDataUtil.appendContextData(
            mapOf(
                "level1.level2.level3.level4.new" to "中文",
                "key1.new" to "value",
                "key.new" to "value"
            ),
            "&c.&key=value&key.&nest=value1&.key&key1.&nest=value1&.key1&level1.&level2.&level3.&level4.&old=old&.level4&.level3&.level2&.level1&.c"
        )
        assertTrue(
            contextDataInCorrectSequence(
                result,
                "new=%E4%B8%AD%E6%96%87",
                "&level4.",
                "&.level4"
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun testAppendContextData_When_ContextDataUTF8_And_SourceContainsUTF8() {
        val result = ContextDataUtil.appendContextData(
            mapOf(
                "level1.level2.level3.level4.new" to "中文",
                "key1.new" to "value",
                "key.new" to "value"
            ),
            "&c.&key=value&key.&nest=value1&.key&key1.&nest=%E4%B8%AD%E6%96%87&.key1&level1.&level2.&level3.&level4.&old=old&.level4&.level3&.level2&.level1&.c"
        )
        assertTrue(
            contextDataInCorrectSequence(
                result,
                "new=%E4%B8%AD%E6%96%87",
                "&level4.",
                "&.level4"
            )
        )
        assertTrue(
            contextDataInCorrectSequence(
                result,
                "nest=%E4%B8%AD%E6%96%87",
                "&key1.",
                "&.key1"
            )
        )
    }

    @Test
    @Throws(Exception::class)
    fun testAppendContextData_When_SourceIsARealHit() {
        val result = ContextDataUtil.appendContextData(
            mapOf(
                "key1.new" to "value",
                "key.new" to "value"
            ),
            "ndh=1&pe=lnk_o&pev2=ADBINTERNAL%3ALifecycle&pageName=My%20Application%201.0%20%281%29&t=00%2F00%2F0000%2000%3A00%3A00%200%20360&ts=1432159549&c.&a.&DeviceName=SAMSUNG-SGH-I337&Resolution=1080x1920&OSVersion=Android%204.3&CarrierName=&internalaction=Lifecycle&AppID=My%20Application%201.0%20%281%29&Launches=1&InstallEvent=InstallEvent&DayOfWeek=4&InstallDate=5%2F20%2F2015&LaunchEvent=LaunchEvent&DailyEngUserEvent=DailyEngUserEvent&RunMode=Application&HourOfDay=16&MonthlyEngUserEvent=MonthlyEngUserEvent&.a&.c&mid=45872199741202307594993613744306256830&ce=UTF-8"
        )
        assertTrue(contextDataInCorrectSequence(result, "new=value", "&key.", "&.key"))
        assertTrue(contextDataInCorrectSequence(result, "new=value", "&key1.", "&.key1"))
    }

    private fun contextDataInCorrectSequence(
        source: String,
        target: String,
        start: String,
        end: String
    ): Boolean {
        val startIndex = source.indexOf(start)
        val endIndex = source.indexOf(end, startIndex)
        val targetIndex = source.indexOf(target, startIndex)
        return targetIndex >= 0 && targetIndex <= endIndex
    }
}