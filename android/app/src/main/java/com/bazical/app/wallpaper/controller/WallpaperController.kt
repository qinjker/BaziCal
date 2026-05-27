package com.bazical.app.wallpaper.controller

import android.view.GestureDetector
import android.view.MotionEvent
import com.bazical.app.wallpaper.renderer.CalendarRenderer
import kotlinx.coroutines.*
import java.time.LocalDate
import kotlin.math.abs

class WallpaperController(
    private val renderer: CalendarRenderer
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var currentYear: Int = LocalDate.now().year
    private var currentMonth: Int = LocalDate.now().monthValue

    private var autoScrollEnabled = false
    private var autoScrollIntervalMs: Long = 10000 // 10 seconds default
    private var autoScrollJob: Job? = null

    private var gestureDetector: GestureDetector? = null
    private var touchStartX = 0f
    private val minSwipeDistance = 100f

    fun init() {
        gestureDetector = GestureDetector(object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean = true

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val diffX = e2.x - e1.x
                if (abs(diffX) > minSwipeDistance) {
                    if (diffX > 0) {
                        scrollToPrevMonth()
                    } else {
                        scrollToNextMonth()
                    }
                    return true
                }
                return false
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                // Could be used to show/hide UI elements
                return true
            }
        })
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                stopAutoScroll()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (autoScrollEnabled) {
                    startAutoScroll()
                }
            }
        }
        return gestureDetector?.onTouchEvent(event) ?: false
    }

    fun setAutoScroll(enabled: Boolean, intervalMs: Long = autoScrollIntervalMs) {
        autoScrollEnabled = enabled
        autoScrollIntervalMs = intervalMs
        if (enabled) {
            startAutoScroll()
        } else {
            stopAutoScroll()
        }
    }

    fun scrollToNextMonth() {
        if (currentMonth == 12) {
            currentMonth = 1
            currentYear++
        } else {
            currentMonth++
        }
        notifyRenderer()
    }

    fun scrollToPrevMonth() {
        if (currentMonth == 1) {
            currentMonth = 12
            currentYear--
        } else {
            currentMonth--
        }
        notifyRenderer()
    }

    fun setCurrentDate(year: Int, month: Int) {
        currentYear = year
        currentMonth = month
        notifyRenderer()
    }

    private fun startAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = scope.launch {
            while (autoScrollEnabled) {
                delay(autoScrollIntervalMs)
                scrollToNextMonth()
            }
        }
    }

    private fun stopAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = null
    }

    private fun notifyRenderer() {
        // This will be called to trigger a re-render with new month data
        // The actual data loading should be handled by the service/repository
    }

    fun release() {
        stopAutoScroll()
        scope.cancel()
    }
}