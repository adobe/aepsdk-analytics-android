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
package com.adobe.marketing.mobile.analytics.function.test

import com.adobe.marketing.mobile.*
import com.adobe.marketing.mobile.analytics.AnalyticsExtension
import com.adobe.marketing.mobile.services.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import java.io.InputStream
import java.util.LinkedList
import java.util.Queue

internal typealias NetworkMonitor = (request: NetworkRequest) -> Unit

@Ignore
@RunWith(MockitoJUnitRunner.Silent::class)
internal open class AnalyticsFunctionalTestBase {
    companion object {
        private var networkMonitor: NetworkMonitor? = null

        @BeforeClass
        @JvmStatic
        fun setupClass() {
            ServiceProvider.getInstance().networkService = Networking { request, callback ->
                networkMonitor?.let { it(request) }
                callback.call(MockedHttpConnecting())
            }
        }
    }

    @Mock
    protected lateinit var mockedExtensionApi: ExtensionApi

    protected var mockedMainDataQueue: DataQueue = MockedDataQueue()

    protected var mockedReorderDataQueue: DataQueue = MockedDataQueue()

    @Mock
    protected lateinit var mockedNameCollection: NamedCollection

    private val mockedSharedState: MutableMap<String, Map<String, Any>> = mutableMapOf()

    @Before
    fun setup() {
        Mockito.reset(mockedExtensionApi)
        mockedMainDataQueue = MockedDataQueue()
        mockedReorderDataQueue = MockedDataQueue()
        Mockito.reset(mockedNameCollection)
        mockedSharedState.clear()

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

        val dataStoreServiceField =
            ServiceProvider.getInstance().javaClass.getDeclaredField("defaultDataStoreService")
        dataStoreServiceField.isAccessible = true
        dataStoreServiceField.set(serviceProvider, object : DataStoring {
            override fun getNamedCollection(name: String?): NamedCollection {
                return mockedNameCollection
            }
        })

    }

    fun monitorNetwork(networkMonitor: NetworkMonitor) {
        AnalyticsFunctionalTestBase.networkMonitor = networkMonitor
    }

    protected fun updateMockedSharedState(extensionNam: String, data: Map<String, Any>) {
        mockedSharedState[extensionNam] = data
    }

    protected fun dispatchGetQueueSizeEvent(analyticsExtension: AnalyticsExtension) {
        analyticsExtension.handleIncomingEvent(
            Event.Builder(
                "GetQueueSize",
                EventType.ANALYTICS,
                EventSource.REQUEST_CONTENT
            ).setEventData(
                mapOf(
                    "getqueuesize" to true
                )
            ).build()
        )
    }

    protected fun initializeAnalyticsExtensionWithPreset(
        configuration: Map<String, Any>?,
        identity: Map<String, Any>?
    ): AnalyticsExtension {
        configuration?.let { mockedSharedState["com.adobe.module.configuration"] = it }
        identity?.let { mockedSharedState["com.adobe.module.identity"] = it }
        Mockito.`when`(
            mockedExtensionApi.getSharedState(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(), anyOrNull(),
                ArgumentMatchers.any()
            )
        ).thenAnswer { invocation ->
            val extension = invocation.arguments[0] as? String
            val data = mockedSharedState[extension] ?: return@thenAnswer null
            return@thenAnswer SharedStateResult(SharedStateStatus.SET, data)
        }
        return AnalyticsExtension(
            mockedExtensionApi
        )
    }

}

private class MockedDataQueue : DataQueue {
    private val cache: Queue<DataEntity> = LinkedList()
    override fun add(dataEntity: DataEntity?): Boolean {
        return if (dataEntity == null) false else cache.add(dataEntity)
    }

    override fun peek(): DataEntity? {
        return cache.peek()
    }

    override fun peek(size: Int): List<DataEntity>? {
        return null
    }

    override fun remove(): Boolean {
        cache.remove()
        return true
    }

    override fun remove(p0: Int): Boolean {
        return true
    }

    override fun clear(): Boolean {
        cache.clear()
        return true
    }

    override fun count(): Int {
        return cache.size
    }

    override fun close() {
        TODO("Not yet implemented")
    }

}

private class MockedHttpConnecting : HttpConnecting {
    var rulesStream: InputStream? = null

    override fun getInputStream(): InputStream? {
        return null
    }

    override fun getErrorStream(): InputStream? {
        return null
    }

    override fun getResponseCode(): Int {
        return 300
    }

    override fun getResponseMessage(): String {
        return ""
    }

    override fun getResponsePropertyValue(responsePropertyKey: String?): String {
        return ""
    }

    override fun close() {
        rulesStream?.close()
    }

}