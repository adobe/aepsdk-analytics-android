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