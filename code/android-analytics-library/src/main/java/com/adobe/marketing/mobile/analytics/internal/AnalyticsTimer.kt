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

import java.util.*

/**
 * Encapsulates a [Timer] object and provides API to start/cancel the timer or check whether the timer is running.
 */
internal class AnalyticsTimer {

    companion object {
        private const val CLASS_NAME = "TimerState"
    }

    internal var isTimerRunning = false
    private val referrerTimerState = TimerState("ADBReferrerTimer");
    private val lifecycleTimerState = TimerState("ADBLifecycleTimer");
    fun cancelLifecycleTimer() {
        lifecycleTimerState.cancel()
    }

    fun cancelReferrerTimer() {
        referrerTimerState.cancel()
    }

    fun startLifecycleTimer(timeout: Long, task: () -> Unit) {
        lifecycleTimerState.startTimer(timeout) {
            task()
        }
    }

    fun startReferrerTimer(timeout: Long, task: () -> Unit) {
        referrerTimerState.startTimer(timeout) {
            task()
        }
    }

    fun isLifecycleTimerRunning(): Boolean {
        return lifecycleTimerState.isTimerRunning()
    }

    fun isReferrerTimerRunning(): Boolean {
        return referrerTimerState.isTimerRunning()
    }
}