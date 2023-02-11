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

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.MobilePrivacyStatus
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.analytics.internal.AnalyticsExtension
import com.adobe.marketing.mobile.analytics.internal.MemoryDataQueue
import com.adobe.marketing.mobile.analytics.internal.MockedHttpConnecting
import com.adobe.marketing.mobile.analytics.internal.NetworkMonitor
import com.adobe.marketing.mobile.services.AppContextService
import com.adobe.marketing.mobile.services.AppState
import com.adobe.marketing.mobile.services.DataQueue
import com.adobe.marketing.mobile.services.DataQueuing
import com.adobe.marketing.mobile.services.DataStoring
import com.adobe.marketing.mobile.services.NamedCollection
import com.adobe.marketing.mobile.services.Networking
import com.adobe.marketing.mobile.services.ServiceProvider
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull

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

    protected var mockedMainDataQueue: DataQueue = MemoryDataQueue()

    protected var mockedReorderDataQueue: DataQueue = MemoryDataQueue()

    @Mock
    protected lateinit var mockedNameCollection: NamedCollection

    @Mock
    protected lateinit var mockedAppContextService: AppContextService

    private val mockedSharedState: MutableMap<String, Map<String, Any>> = mutableMapOf()

    @Before
    fun setup() {
        Mockito.reset(mockedExtensionApi)
        mockedMainDataQueue = MemoryDataQueue()
        mockedReorderDataQueue = MemoryDataQueue()
        Mockito.reset(mockedNameCollection)
        mockedSharedState.clear()

        val serviceProvider = ServiceProvider.getInstance()
        val dataQueueServiceField =
            ServiceProvider.getInstance().javaClass.getDeclaredField("dataQueueService")
        dataQueueServiceField.isAccessible = true
        dataQueueServiceField.set(
            serviceProvider,
            object : DataQueuing {
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
            }
        )

        val dataStoreServiceField =
            ServiceProvider.getInstance().javaClass.getDeclaredField("defaultDataStoreService")
        dataStoreServiceField.isAccessible = true
        dataStoreServiceField.set(
            serviceProvider,
            object : DataStoring {
                override fun getNamedCollection(name: String?): NamedCollection {
                    return mockedNameCollection
                }
            }
        )

        `when`(mockedAppContextService.appState).thenReturn(AppState.FOREGROUND)
        val appContextField =
            ServiceProvider.getInstance().javaClass.getDeclaredField("overrideAppContextService")
        appContextField.isAccessible = true
        appContextField.set(
            serviceProvider,
            mockedAppContextService
        )
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
        `when`(
            mockedExtensionApi.getSharedState(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                anyOrNull(),
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

    protected fun config(privacyStatus: MobilePrivacyStatus): Map<String, Any> {
        return mapOf(
            "analytics.server" to "test.com",
            "analytics.rsids" to "rsid",
            "global.privacy" to privacyStatus.value,
            "experienceCloud.org" to "orgid",
            "analytics.batchLimit" to 0,
            "analytics.offlineEnabled" to true,
            "analytics.backdatePreviousSessionInfo" to true,
            "analytics.launchHitDelay" to 1
        )
    }

    protected fun defaultIdentity(): Map<String, Any> {
        return mapOf(
            "mid" to "mid",
            "blob" to "blob",
            "locationhint" to "lochint"
        )
    }
}
