package com.adobe.marketing.mobile.analytics

import com.adobe.marketing.mobile.AdobeCallback
import com.adobe.marketing.mobile.services.Log
import java.util.*

/**
 * Encapsulates a [Timer] object and provides API to start/cancel the timer or check whether the timer is running.
 */
internal class TimerState(private val debugName: String) {
    private var isTimerRunning = false
    private var timeout: Long = 0
    private var timerTask: TimerTask? = null
    private var timer: Timer? = null
    private var callback: AdobeCallback<Boolean>? = null
    private val timerMutex: Any = Any()

    /**
     * Checks if the timer is still running.
     *
     * @return a `boolean` indicates whether there is a timer and it is still running
     */
    fun isTimerRunning(): Boolean {
        synchronized(timerMutex) { return timerTask != null && isTimerRunning }
    }

    /**
     * Starts the timer with the given `long` timeout value, and call the `AdobeCallback<Boolean>`
     * if the timer was not canceled before timeout.
     *
     * @param timeout  `long` timeout value for the timer
     * @param callback the `AdobeCallback<Boolean>` to be invoked once times out
     */
    fun startTimer(timeout: Long, callback: AdobeCallback<Boolean>?) {
        synchronized(timerMutex) {
            if (timerTask != null) {
                Log.debug(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "Timer has already started."
                )
                return
            }
            this.timeout = timeout
            isTimerRunning = true
            this.callback = callback
            try {
                timerTask = object : TimerTask() {
                    override fun run() {
                        isTimerRunning = false
                        if (this@TimerState.callback != null) {
                            this@TimerState.callback!!.call(true)
                        }
                    }
                }
                timer = Timer(debugName)
                timer!!.schedule(timerTask, timeout)
                Log.trace(
                    AnalyticsConstants.LOG_TAG,
                    CLASS_NAME,
                    "%s timer scheduled having timeout %s ms",
                    debugName,
                    this.timeout
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
            if (timer != null) {
                try {
                    timer!!.cancel()
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
                timerTask = null
            }

            // set is running to false regardless of whether the timer is null or not
            isTimerRunning = false
        }
    }

    companion object {
        private const val CLASS_NAME = "TimerState"
    }
}