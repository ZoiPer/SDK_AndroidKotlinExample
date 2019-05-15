package com.zoiper.zdk.android.demokt.util

import android.widget.TextView
import java.util.*

/**
 * TimerDuration
 *
 * @since 1.2.2019 г.
 */
class TimerDuration(private val handler: android.os.Handler, private val timerTextView: TextView?) {

    private var currentDuration: Int = 0
    private var durationTimer: Timer? = null

    /**
     * Cancel the timer. This stops the timer but does not
     * delete the textview values.
     */
    fun cancel() = durationTimer?.cancel()

    /**
     * Setup the timer and the default textview and duration values.
     * The timer starts immediately after calling this method.
     */
    fun start() {
        setDefaultValues()
        if (durationTimer == null) {
            durationTimer = Timer()
            durationTimer?.schedule(object : TimerTask() {
                override fun run() {

                    handler.post { updateTimer() }
                }
            }, DELAY.toLong(), PERIOD.toLong())
        }
    }

    private fun setDefaultValues() {
        // Init textview first value.
        if (timerTextView != null) {
            timerTextView.text = getSecondsAsText(0)
        }
        currentDuration = 0
    }

    private fun updateTimer() {
        currentDuration++
        timerTextView?.text = getSecondsAsText(currentDuration)
    }

    private fun getSecondsAsText(seconds: Int): String {
        val secondsLeft = seconds % 3600 % 60
        val minutes = Math.floor((seconds % 3600 / 60).toDouble()).toInt()

        val mm = if (minutes < 10) "0$minutes" else minutes.toString()
        val ss = if (secondsLeft < 10) "0$secondsLeft" else secondsLeft.toString()

        return "$mm:$ss"
    }

    companion object {
        private const val DELAY = 0
        private const val PERIOD = 1000
    }
}
