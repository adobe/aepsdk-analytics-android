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

import com.adobe.marketing.mobile.AdobeCallback
import com.adobe.marketing.mobile.services.Log
import java.util.*

/**
 * Encapsulates a [Timer] object and provides API to start/cancel the timer or check whether the timer is running.
 */
internal class TimerState(private val debugName: String) {

    companion object {
        private const val CLASS_NAME = "TimerState"
    }

    private var isTimerRunning = false
    private var timer: Timer? = null
    private val timerMutex = Any()

    /**
     * Checks if the timer is still running.
     *
     * @return a `boolean` indicates whether there is a timer and it is still running
     */
    fun isTimerRunning(): Boolean {
        synchronized(timerMutex) { return isTimerRunning }
    }

    /**
     * Starts the timer with the given `long` timeout value, and call the `AdobeCallback<Boolean>`
     * if the timer was not canceled before timeout.
     *
     * @param timeout  `long` timeout value for the timer
     * @param callback the `AdobeCallback<Boolean>` to be invoked once times out
     */
    fun startTimer(timeout: Long = 0, callback: AdobeCallback<Boolean>) {
        synchronized(timerMutex) {
            if (isTimerRunning) {
                Log.debug(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "Timer has already started."
                )
                return
            }
            isTimerRunning = true
            try {
                timer = Timer(debugName)
                timer?.schedule(object : TimerTask() {
                    override fun run() {
                        isTimerRunning = false
                        callback.call(true)
                    }
                }, timeout)
                Log.trace(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "%s timer scheduled having timeout %s ms",
                    debugName,
                    timeout
                )
            } catch (e: Exception) {
                Log.warning(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "Error creating %s timer, failed with error: (%s)",
                    debugName,
                    e
                )
            }
        }
    }

    /**
     * Cancels the timer and sets the state back to normal.
     */
    fun cancel() {
        synchronized(timerMutex) {
            try {
                timer?.cancel()
                Log.trace(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "%s timer was canceled",
                    debugName
                )
            } catch (e: Exception) {
                Log.warning(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "Error cancelling %s timer, failed with error: (%s)",
                    debugName,
                    e
                )
            }

            // set is running to false regardless of whether the timer is null or not
            isTimerRunning = false
        }
    }

}