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

import com.adobe.marketing.mobile.analytics.internal.TimerState
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class TimerStateTests {

    @Test
    fun `start() + cancel()`() {
        val timer = TimerState("timer_1")
        timer.startTimer(20L) {
            fail("timeout")
        }
        assertTrue(timer.isTimerRunning())
        Thread.sleep(10L)
        timer.cancel()
        assertFalse(timer.isTimerRunning())
    }

    @Test
    fun `start() - timeout`() {
        val countDownLatch = CountDownLatch(1)
        val timer = TimerState("timer_1")
        timer.startTimer(5L) {
            countDownLatch.countDown()
        }
        assertTrue(timer.isTimerRunning())
        assertTrue(countDownLatch.await(7, TimeUnit.MILLISECONDS))
        assertFalse(timer.isTimerRunning())
    }

    @Test
    fun `start() - duplicated triggering without error`() {
        val countDownLatch = CountDownLatch(1)
        val timer = TimerState("timer_1")
        timer.startTimer(5L) {
            countDownLatch.countDown()
        }
        timer.startTimer(1L) {
            fail("should never call it")
        }
        assertTrue(countDownLatch.await(6, TimeUnit.MILLISECONDS))
    }

    @Test
    fun `cancel() - not started`() {
        val timer = TimerState("timer_1")
        timer.cancel()
        assertFalse(timer.isTimerRunning())
    }
}