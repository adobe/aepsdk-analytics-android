package com.adobe.marketing.mobile.analytics

import com.adobe.marketing.mobile.services.*

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

    internal fun queue(
        payload: String,
        timestamp: Long,
        eventIdentifier: String,
        isBackdateHit: Boolean
    ) {
        Log.debug(
            AnalyticsConstants.LOG_TAG,
            CLASS_NAME,
            "queueHit - $payload isBackdateHit:$isBackdateHit"
        )
        val hitData = AnalyticsHit(payload, timestamp, eventIdentifier).toDataEntity().data ?: run {
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

    fun reset() {
        hitQueue.suspend()
        mainQueue.clear()
        reorderQueue.clear()
        additionalData = emptyMap()
        waitingForLifecycle = false
        waitingForReferrer = false
    }

    fun cancelWaitForAdditionalData(dataType: DataType) {
        Log.debug(
            AnalyticsConstants.LOG_TAG,
            CLASS_NAME,
            "cancelWaitForAdditionalData - $dataType"
        )
        kickWithAdditionalData(dataType, null)
    }

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

    fun isHitWaiting(): Boolean {
        return reorderQueue.count() > 0
    }

    fun kickWithAdditionalData(dataType: DataType, data: Map<String, Any?>?) {
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
        data?.let {
            additionalData.plus(data)
        }
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
            analyticsHit.timestamp,
            analyticsHit.eventIdentifier
        ).toDataEntity().data
        return DataEntity(hitData)
    }

    private fun waitingForAdditionalData(): Boolean {
        return waitingForReferrer || waitingForLifecycle
    }

}