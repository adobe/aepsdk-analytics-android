package com.adobe.marketing.mobile.analytics

import com.adobe.marketing.mobile.services.DataEntity
import com.adobe.marketing.mobile.services.DataQueue
import java.util.*

class MemoryDataQueue : DataQueue {
    private val queue = LinkedList<DataEntity>()
    override fun add(dataEntity: DataEntity?): Boolean {
        if (dataEntity == null) return false
        return queue.add(dataEntity)
    }

    override fun peek(): DataEntity? {
        return queue.peek()
    }

    override fun peek(size: Int): List<DataEntity> {
        return if (size > queue.size) {
            queue.toList()
        } else {
            queue.takeLast(size)
        }
    }

    override fun remove(): Boolean {
        return queue.remove() != null
    }

    override fun remove(size: Int): Boolean {
        (1..size).forEach { _ ->
            if (queue.remove() == null) return false
        }
        return true
    }

    override fun clear(): Boolean {
        queue.clear()
        return true
    }

    override fun count(): Int {
        return queue.size
    }

    override fun close() {
    }
}