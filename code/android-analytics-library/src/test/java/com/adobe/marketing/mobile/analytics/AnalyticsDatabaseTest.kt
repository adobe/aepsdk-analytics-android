package com.adobe.marketing.mobile.analytics

import com.adobe.marketing.mobile.services.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.*
import kotlin.collections.ArrayList

@RunWith(MockitoJUnitRunner.Silent::class)
class AnalyticsDatabaseTest {
    var mockedMainDataQueue: DataQueue = MemoryDataQueue()

    var mockedReorderDataQueue: DataQueue = MemoryDataQueue()

    @Mock
    private lateinit var mockedAnalyticsState: AnalyticsState

    @Before
    fun setup() {
        mockedMainDataQueue.clear()
        mockedReorderDataQueue.clear()
        Mockito.reset(mockedAnalyticsState)

        val serviceProvider = ServiceProvider.getInstance()
        val dataQueueServiceField =
            ServiceProvider.getInstance().javaClass.getDeclaredField("dataQueueService")
        dataQueueServiceField.isAccessible = true
        dataQueueServiceField.set(serviceProvider, object : DataQueuing {
            override fun getDataQueue(name: String?): DataQueue {
                return when (name) {
                    "com.adobe.module.analytics" -> {
                        mockedMainDataQueue
                    }
                    "com.adobe.module.analyticsreorderqueue" -> {
                        mockedReorderDataQueue
                    }
                    else -> {
                        mockedMainDataQueue
                    }
                }
            }

        })
    }

    private fun initAnalyticsDatabase(mockedProcessor: HitProcessing = MockedHitProcessing()): AnalyticsDatabase {
        return AnalyticsDatabase(mockedProcessor, mockedAnalyticsState)
    }

    @Test
    fun `kick with additional data - move hit to main queue`() {
        val analyticsDatabase = initAnalyticsDatabase()
        //main queue
        analyticsDatabase.queue("payload1", 123456789L, "id1", false)
        analyticsDatabase.waitForAdditionalData(AnalyticsDatabase.DataType.LIFECYCLE)
        //reorder queue
        analyticsDatabase.queue("payload2", 123456789L, "id2", false)
        assertEquals(1, mockedMainDataQueue.count())
        assertEquals(1, mockedReorderDataQueue.count())

        assertEquals("payload1", AnalyticsHit.from(mockedMainDataQueue.peek()).payload)
        assertEquals("payload2", AnalyticsHit.from(mockedReorderDataQueue.peek()).payload)

        analyticsDatabase.kickWithAdditionalData(AnalyticsDatabase.DataType.LIFECYCLE, null)
        assertEquals(2, mockedMainDataQueue.count())
        assertEquals(0, mockedReorderDataQueue.count())
        val list = mockedMainDataQueue.peek(2)
        assertEquals("payload1", AnalyticsHit.from(list[0]).payload)
        assertEquals("payload2", AnalyticsHit.from(list[1]).payload)
    }

    @Test
    fun `kick with additional data - append data to the first hit`() {
        val analyticsDatabase = initAnalyticsDatabase()
        //main queue
        analyticsDatabase.queue("payload1", 123456789L, "id1", false)
        analyticsDatabase.waitForAdditionalData(AnalyticsDatabase.DataType.LIFECYCLE)
        analyticsDatabase.waitForAdditionalData(AnalyticsDatabase.DataType.REFERRER)
        //reorder queue
        analyticsDatabase.queue("payload2", 123456789L, "id2", false)
        analyticsDatabase.kickWithAdditionalData(
            AnalyticsDatabase.DataType.LIFECYCLE, mapOf(
                "lk1" to "v1",
                "lk2" to "v2"
            )
        )
        analyticsDatabase.queue("payload3", 123456789L, "id2", false)

        assertEquals(1, mockedMainDataQueue.count())
        assertEquals(2, mockedReorderDataQueue.count())

        assertEquals("payload1", AnalyticsHit.from(mockedMainDataQueue.peek()).payload)
        val list = mockedReorderDataQueue.peek(2)
        assertEquals("payload2", AnalyticsHit.from(list[0]).payload)
        assertEquals("payload3", AnalyticsHit.from(list[1]).payload)

        analyticsDatabase.kickWithAdditionalData(
            AnalyticsDatabase.DataType.REFERRER, mapOf(
                "rk1" to "v1",
                "rk2" to "v2"
            )
        )
        assertEquals(3, mockedMainDataQueue.count())
        assertEquals(0, mockedReorderDataQueue.count())
        val list2 = mockedMainDataQueue.peek(3)
        assertEquals("payload1", AnalyticsHit.from(list2[0]).payload)
        assertEquals("payload3", AnalyticsHit.from(list2[2]).payload)
        val secondHitWithAppendedData = AnalyticsHit.from(list2[1]).payload
        assertTrue(secondHitWithAppendedData.contains("lk1=v1"))
        assertTrue(secondHitWithAppendedData.contains("lk2=v2"))
        assertTrue(secondHitWithAppendedData.contains("rk1=v1"))
        assertTrue(secondHitWithAppendedData.contains("rk2=v2"))
    }

    @Test
    fun `queue() - append to reorder queue when waiting`() {
        val analyticsDatabase = initAnalyticsDatabase()
        analyticsDatabase.waitForAdditionalData(AnalyticsDatabase.DataType.REFERRER)
        //reorder queue
        analyticsDatabase.queue("payload1", 123456789L, "id1", false)
        analyticsDatabase.queue("payload2", 123456789L, "id2", false)

        assertEquals(0, mockedMainDataQueue.count())
        assertEquals(2, mockedReorderDataQueue.count())
        val list = mockedReorderDataQueue.peek(2)
        assertEquals("payload1", AnalyticsHit.from(list[0]).payload)
        assertEquals("payload2", AnalyticsHit.from(list[1]).payload)
    }

    @Test
    fun `queue() - append to main queue when not waiting`() {
        val analyticsDatabase = initAnalyticsDatabase()

        //main queue
        analyticsDatabase.queue("payload1", 123456789L, "id1", false)
        analyticsDatabase.queue("payload2", 123456789L, "id2", false)

        assertEquals(2, mockedMainDataQueue.count())
        assertEquals(0, mockedReorderDataQueue.count())
        val list = mockedMainDataQueue.peek(2)
        assertEquals("payload1", AnalyticsHit.from(list[0]).payload)
        assertEquals("payload2", AnalyticsHit.from(list[1]).payload)
    }

    @Test
    fun `queue() - kick with batch limit`() {
        val mockedHitProcessing = MockedHitProcessing()
        val analyticsDatabase = initAnalyticsDatabase(mockedHitProcessing)
        Mockito.`when`(mockedAnalyticsState.batchLimit).thenReturn(2)
        Mockito.`when`(mockedAnalyticsState.isOptIn).thenReturn(true)
        Mockito.`when`(mockedAnalyticsState.isAnalyticsConfigured).thenReturn(true)
        Mockito.`when`(mockedAnalyticsState.isOfflineTrackingEnabled).thenReturn(true)

        //main queue
        analyticsDatabase.queue("payload1", 123456789L, "id1", false)
        analyticsDatabase.queue("payload2", 123456789L, "id2", false)

        Thread.sleep(50)
        assertEquals(0, mockedHitProcessing.processedHits.size)
        analyticsDatabase.queue("payload3", 123456789L, "id3", false)
        Thread.sleep(50)
        assertEquals(3, mockedHitProcessing.processedHits.size)
        assertEquals("payload1", AnalyticsHit.from(mockedHitProcessing.processedHits[0]).payload)
        assertEquals("payload2", AnalyticsHit.from(mockedHitProcessing.processedHits[1]).payload)
        assertEquals("payload3", AnalyticsHit.from(mockedHitProcessing.processedHits[2]).payload)
    }

    @Test
    fun `queue() - kick with offline disabled`() {
        val mockedHitProcessing = MockedHitProcessing()
        val analyticsDatabase = initAnalyticsDatabase(mockedHitProcessing)
        Mockito.`when`(mockedAnalyticsState.isOptIn).thenReturn(true)
        Mockito.`when`(mockedAnalyticsState.isAnalyticsConfigured).thenReturn(true)
        Mockito.`when`(mockedAnalyticsState.isOfflineTrackingEnabled).thenReturn(false)

        //main queue
        analyticsDatabase.queue("payload1", 123456789L, "id1", false)

        Thread.sleep(50)
        assertEquals(1, mockedHitProcessing.processedHits.size)
        assertEquals("payload1", AnalyticsHit.from(mockedHitProcessing.processedHits[0]).payload)
    }

    @Test
    fun `queue() - kick with privacy changes`() {
        val mockedHitProcessing = MockedHitProcessing()
        val analyticsDatabase = initAnalyticsDatabase(mockedHitProcessing)
        Mockito.`when`(mockedAnalyticsState.isOptIn).thenReturn(false)
        Mockito.`when`(mockedAnalyticsState.isAnalyticsConfigured).thenReturn(true)
        Mockito.`when`(mockedAnalyticsState.batchLimit).thenReturn(1)
        Mockito.`when`(mockedAnalyticsState.isOfflineTrackingEnabled).thenReturn(true)

        //main queue
        analyticsDatabase.queue("payload1", 123456789L, "id1", false)
        Thread.sleep(50)
        assertEquals(0, mockedHitProcessing.processedHits.size)

        Mockito.`when`(mockedAnalyticsState.isOptIn).thenReturn(true)

        analyticsDatabase.queue("payload2", 123456789L, "id2", false)
        Thread.sleep(50)
        assertEquals(2, mockedHitProcessing.processedHits.size)
        assertEquals("payload1", AnalyticsHit.from(mockedHitProcessing.processedHits[0]).payload)
        assertEquals("payload2", AnalyticsHit.from(mockedHitProcessing.processedHits[1]).payload)
    }

    @Test
    fun `queue() - kick before configuration is ready`() {
        val mockedHitProcessing = MockedHitProcessing()
        val analyticsDatabase = initAnalyticsDatabase(mockedHitProcessing)
        Mockito.`when`(mockedAnalyticsState.isOptIn).thenReturn(true)
        Mockito.`when`(mockedAnalyticsState.isAnalyticsConfigured).thenReturn(false)
        Mockito.`when`(mockedAnalyticsState.batchLimit).thenReturn(1)
        Mockito.`when`(mockedAnalyticsState.isOfflineTrackingEnabled).thenReturn(true)

        //main queue
        analyticsDatabase.queue("payload1", 123456789L, "id1", false)
        Thread.sleep(50)
        assertEquals(0, mockedHitProcessing.processedHits.size)

        Mockito.`when`(mockedAnalyticsState.isAnalyticsConfigured).thenReturn(true)

        analyticsDatabase.queue("payload2", 123456789L, "id2", false)
        Thread.sleep(50)
        assertEquals(2, mockedHitProcessing.processedHits.size)
        assertEquals("payload1", AnalyticsHit.from(mockedHitProcessing.processedHits[0]).payload)
        assertEquals("payload2", AnalyticsHit.from(mockedHitProcessing.processedHits[1]).payload)
    }

    @Test
    fun `queue() - drop backdated hits when not waiting`() {
        val analyticsDatabase = initAnalyticsDatabase()

        //main queue
        analyticsDatabase.queue("payload1", 123456789L, "id1", false)
        analyticsDatabase.queue("payload2", 123456789L, "id2", true)

        assertEquals(1, mockedMainDataQueue.count())

        assertEquals("payload1", AnalyticsHit.from(mockedMainDataQueue.peek()).payload)
    }

    @Test
    fun `queue() - add backdated hits to main queue when waiting`() {
        val analyticsDatabase = initAnalyticsDatabase()

        analyticsDatabase.queue("payload1", 123456789L, "id1", false)

        analyticsDatabase.waitForAdditionalData(AnalyticsDatabase.DataType.LIFECYCLE)

        analyticsDatabase.queue("payload2", 123456789L, "id2", true)

        assertEquals(2, mockedMainDataQueue.count())
        val list = mockedMainDataQueue.peek(2)
        assertEquals("payload1", AnalyticsHit.from(list[0]).payload)
        assertEquals("payload2", AnalyticsHit.from(list[1]).payload)
    }

    @Test
    fun `reset()`() {
        val analyticsDatabase = initAnalyticsDatabase()

        analyticsDatabase.queue("payload1", 123456789L, "id1", false)

        analyticsDatabase.waitForAdditionalData(AnalyticsDatabase.DataType.LIFECYCLE)

        analyticsDatabase.queue("payload2", 123456789L, "id2", true)

        analyticsDatabase.reset()

        assertEquals(0, mockedMainDataQueue.count())
        assertEquals(0, mockedReorderDataQueue.count())
    }

    @Test
    fun `force kick`() {
        val mockedHitProcessing = MockedHitProcessing()
        val analyticsDatabase = initAnalyticsDatabase(mockedHitProcessing)
        Mockito.`when`(mockedAnalyticsState.isOptIn).thenReturn(true)
        Mockito.`when`(mockedAnalyticsState.isAnalyticsConfigured).thenReturn(true)
        Mockito.`when`(mockedAnalyticsState.batchLimit).thenReturn(10)
        Mockito.`when`(mockedAnalyticsState.isOfflineTrackingEnabled).thenReturn(true)

        //main queue
        analyticsDatabase.queue("payload1", 123456789L, "id1", false)
        analyticsDatabase.queue("payload2", 123456789L, "id2", false)

        Thread.sleep(50)
        assertEquals(0, mockedHitProcessing.processedHits.size)

        analyticsDatabase.kick(true)

        Thread.sleep(50)
        assertEquals(2, mockedHitProcessing.processedHits.size)
        assertEquals("payload1", AnalyticsHit.from(mockedHitProcessing.processedHits[0]).payload)
        assertEquals("payload2", AnalyticsHit.from(mockedHitProcessing.processedHits[1]).payload)
    }
}

private class MockedHitProcessing : HitProcessing {
    val processedHits = ArrayList<DataEntity>()
    override fun retryInterval(dataEntity: DataEntity): Int {
        return 1
    }

    override fun processHit(dataEntity: DataEntity, result: HitProcessingResult) {
        processedHits.add(dataEntity)
        result.complete(true)
    }
}


