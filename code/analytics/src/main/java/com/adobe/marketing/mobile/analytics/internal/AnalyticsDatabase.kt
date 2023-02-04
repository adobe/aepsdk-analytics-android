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

import com.adobe.marketing.mobile.services.DataEntity
import com.adobe.marketing.mobile.services.DataQueue
import com.adobe.marketing.mobile.services.HitProcessing
import com.adobe.marketing.mobile.services.HitQueuing
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.PersistentHitQueue
import com.adobe.marketing.mobile.services.ServiceProvider

/**
 * The Analytics hit database which queues and persists hits before processing.
 *
 * Analytics hit reordering
 * If backDateSessionInfo and offlineTracking is enabled, we should send hit with previous session
 * information / crash information before sending any hits for current session.
 * (We get this information from `lifecycle.responseContent` event)
 * Lifecycle information for current session should be attached to queued hit or as separate hit
 * (If we have no queued hit) for every lifecycle session. (We get this information from
 * `lifecycle.responseContent` event)
 * Referrer information for current install/launch should be attached to queued hit or as separate
 * hit (If we have no queued hit) (We get this information from `acquisition.responseContent` event)
 *
 * Given that Lifecycle, Acquisition and MobileServices extensions are optional we rely on timeouts
 * to wait for each of the above events and reorder hits
 * Any `genericTrack` request we receive before `genericLifecycle` event is processed and reported
 * to backend. (If lifecycle extension is implemented, we recommend calling
 * MobileCore.lifecycleStart() before any track calls.)
 * After receiving `genericLifecycle` event, we wait
 * `AnalyticsConstants.Default.LIFECYCLE_RESPONSE_WAIT_TIMEOUT` for `lifecycle.responseContent` event
 * If we receive `lifecycle.responseContent` before timeout, we append lifecycle data to first
 * waiting hit. It is sent as a separate hit if we don't have any waiting hit
 * After receiving `lifecycle.responseContent` we wait for `acquisition.responseContent`. If it is
 * install we wait for `analyticsState.launchHitDelay` and for launch we wait for
 * `AnalyticsConstants.Default.LAUNCH_DEEPLINK_DATA_WAIT_TIMEOUT`
 * If we receive `acquisition.responseContent` before timeout, we append lifecycle data to first
 * waiting hit. It is sent as a separate hit if we don't have any waiting hit
 * Any `genericTrack` request we receive when waiting for `lifecycle.responseContent` or
 * `acquisition.responseContent` is placed in the reorder queue till we receive these events or
 * until timeout
 *
 * @param processor the Analytics hit processor which processes the hits
 * @param analyticsState the [AnalyticsState] holds current state data from dependent extensions
 */
internal class AnalyticsDatabase(
    private val processor: HitProcessing,
    private val analyticsState: AnalyticsState
) {
    internal enum class DataType { REFERRER, LIFECYCLE }

    private val hitQueue: HitQueuing
    private val mainQueue: DataQueue
    private val reorderQueue: DataQueue
    private var waitingForLifecycle = false
    private var waitingForReferrer = false
    private var additionalData: Map<String, Any> = emptyMap()

    companion object {
        private const val CLASS_NAME = "AnalyticsDatabase"
    }

    init {
        val mainDataQueue =
            ServiceProvider.getInstance().dataQueueService.getDataQueue(AnalyticsConstants.DATA_QUEUE_NAME)
        val reorderDataQueue =
            ServiceProvider.getInstance().dataQueueService.getDataQueue(AnalyticsConstants.REORDER_QUEUE_NAME)
        this.mainQueue = mainDataQueue
        this.reorderQueue = reorderDataQueue
        this.hitQueue = PersistentHitQueue(mainDataQueue, processor)
        moveHitsFromReorderQueue()
    }

    /**
     * Move hits from the "reorder" queue to the "main" queue.
     * The "reorder" queue is empty after this operation.
     */
    private fun moveHitsFromReorderQueue() {
        val count = reorderQueue.count()
        if (count <= 0) {
            Log.trace(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "moveHitsFromReorderQueue - No hits in reorder queue"
            )
            return
        }
        Log.trace(
            AnalyticsConstants.LOG_TAG,
            CLASS_NAME,
            "moveHitsFromReorderQueue - Moving queued hits $count from reorder queue -> main queue"
        )
        reorderQueue.peek(count)?.let { list ->
            list.forEach { mainQueue.add(it) }
        }
        reorderQueue.clear()
    }

    /**
     * Queue hits to the appropriate queue. Hits are queued to the "main" queue unless they are
     * waiting on additional data, then they are queued to the "reorder" queue. Backdated hits, however,
     * are added to the "main" queue if waiting for additional data and dropped otherwise.
     *
     * @param payload the hit payload
     * @param timestampSec the hit timestamp in seconds
     * @param eventIdentifier the identifier of the triggering event
     * @param isBackdateHit true if this is a backdated hit
     */
    internal fun queue(
        payload: String,
        timestampSec: Long,
        eventIdentifier: String,
        isBackdateHit: Boolean
    ) {
        Log.debug(
            AnalyticsConstants.LOG_TAG,
            CLASS_NAME,
            "queueHit - $payload isBackdateHit:$isBackdateHit"
        )
        val hitData = AnalyticsHit(payload, timestampSec, eventIdentifier).toDataEntity().data ?: run {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "queueHit - Dropping Analytics hit, failed to encode AnalyticsHit"
            )
            return@queue
        }
        val hit = DataEntity(hitData)
        if (isBackdateHit) {
            if (waitingForAdditionalData()) {
                Log.debug(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "queueHit - Queueing backdated hit"
                )
                mainQueue.add(hit)
            } else {
                Log.debug(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "queueHit - Dropping backdate hit, as we have begun processing hits for current session"
                )
            }
        } else {
            if (waitingForAdditionalData()) {
                Log.debug(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "queueHit - Queueing hit in reorder queue as we are waiting for additional data"
                )
                reorderQueue.add(hit)
            } else {
                Log.debug(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "queueHit - Queueing hit in main queue"
                )
                mainQueue.add(hit)
            }
        }
        kick(false)
    }

    /**
     * Resume processing of the hit queue. If, however, Analytics is not configured or the privacy
     * status is not opted in, then this operation is ignored.
     *
     * @param ignoreBatchLimit if true, ignores the current queue count and batch limit configuration
     */
    fun kick(ignoreBatchLimit: Boolean) {
        Log.trace(
            AnalyticsConstants.LOG_TAG,
            CLASS_NAME,
            "Kick - ignoreBatchLimit $ignoreBatchLimit."
        )
        if (!analyticsState.isAnalyticsConfigured) {
            Log.trace(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "Kick - Failed to kick database hits (Analytics is not configured)."
            )
            return
        }
        if (!analyticsState.isOptIn) {
            Log.trace(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "Kick - Failed to kick database hits (Privacy status is not opted-in)."
            )
            return
        }

        val count = mainQueue.count()
        val overBatchLimit =
            !analyticsState.isOfflineTrackingEnabled || count > analyticsState.batchLimit
        if (overBatchLimit || ignoreBatchLimit) {
            Log.trace(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "Kick - Begin processing database hits"
            )
            hitQueue.beginProcessing()
        }
    }

    /**
     * Resets the database by suspending processing of the hit queue and clearing hits from both
     * the "main" and "reorder" queues.
     */
    fun reset() {
        hitQueue.suspend()
        mainQueue.clear()
        reorderQueue.clear()
        additionalData = emptyMap()
        waitingForLifecycle = false
        waitingForReferrer = false
    }

    /**
     * Cancels the wait for additional data request for the given [DataType].
     *
     * @param dataType the [DataType] for which to cancel the wait for additional data request
     */
    fun cancelWaitForAdditionalData(dataType: DataType) {
        Log.debug(
            AnalyticsConstants.LOG_TAG,
            CLASS_NAME,
            "cancelWaitForAdditionalData - $dataType"
        )
        kickWithAdditionalData(dataType, null)
    }

    /**
     * Signals the database that additional data for currently queued hits is pending.
     *
     * @param dataType the [DataType] for which additional data is pending
     */
    fun waitForAdditionalData(dataType: DataType) {
        Log.debug(
            AnalyticsConstants.LOG_TAG,
            CLASS_NAME,
            "waitForAdditionalData - $dataType"
        )
        when (dataType) {
            DataType.REFERRER -> this.waitingForReferrer = true
            DataType.LIFECYCLE -> this.waitingForLifecycle = true
        }
    }

    /**
     * Determines if any hits are waiting for additional data.
     *
     * @return true if any hits are waiting for additional data
     */
    fun isHitWaiting(): Boolean {
        return reorderQueue.count() > 0
    }

    /**
     * Resume processing of the hit queue with additional data.
     * Appends the given [data] to the first hit in the "reorder" queue and moves that hit
     * to the "main" queue. Moves all other hits from the "reorder" queue to the "main" queue.
     * Clears the "waiting for additional data" flag for the given [dataType].
     *
     * @param dataType the [DataType] (source) of the given [data]
     * @param data the additional data to add to the waiting hit
     */
    fun kickWithAdditionalData(dataType: DataType, data: Map<String, Any>?) {
        if (!waitingForAdditionalData()) {
            return
        }
        Log.debug(
            AnalyticsConstants.LOG_TAG,
            CLASS_NAME,
            "KickWithAdditionalData - $dataType - $data"
        )
        when (dataType) {
            DataType.REFERRER -> this.waitingForReferrer = false
            DataType.LIFECYCLE -> this.waitingForLifecycle = false
        }
        data?.let { additionalData = additionalData.plus(it) }
        if (!waitingForAdditionalData()) {
            Log.debug(
                AnalyticsConstants.LOG_TAG,
                CLASS_NAME,
                "KickWithAdditionalData - done waiting for additional data"
            )
            if (isHitWaiting()) {
                reorderQueue.peek()?.let { firstHit ->
                    val appendedHit = appendAdditionalData(additionalData, firstHit)
                    mainQueue.add(appendedHit)
                    reorderQueue.remove()
                }
            }
            moveHitsFromReorderQueue()
            additionalData = emptyMap()
        }
        kick(false)
    }

    /**
     * Appends additional data to a hit.
     *
     * @param additionalData the data to append to the [DataEntity]
     * @param dataEntity the [DataEntity] to append the data
     * @return the given [dataEntity] with the appended [additionalData]
     */
    private fun appendAdditionalData(
        additionalData: Map<String, Any>,
        dataEntity: DataEntity
    ): DataEntity {
        val analyticsHit = AnalyticsHit.from(dataEntity)
        val payload = ContextDataUtil.appendContextData(
            additionalData as Map<String, String>,
            analyticsHit.payload
        )
        val hitData = AnalyticsHit(
            payload,
            analyticsHit.timestampSec,
            analyticsHit.eventIdentifier
        ).toDataEntity().data
        return DataEntity(hitData)
    }

    /**
     * Determines whether a hit is waiting for additional data.
     *
     * @return true if a hit is waiting for additional data
     */
    private fun waitingForAdditionalData(): Boolean {
        return waitingForReferrer || waitingForLifecycle
    }

    /**
     * Return the current queue size.
     * The queue size is the count from both the "main" queue and "reorder" queue.
     */
    internal fun getQueueSize(): Int {
        return mainQueue.count() + reorderQueue.count()
    }
}
