package com.bazical.app.wallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.bazical.app.wallpaper.renderer.CalendarRenderer
import kotlinx.coroutines.*

class BaziCalWallpaperService : WallpaperService() {

    private var engine: EngineImpl? = null

    override fun onCreateEngine(): WallpaperService.Engine {
        if (engine == null) {
            engine = EngineImpl()
        }
        return engine!!
    }

    private inner class EngineImpl : WallpaperService.Engine() {

        private var renderer: CalendarRenderer? = null

        private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        private var currentYear: Int = 0
        private var currentMonth: Int = 0
        private var calendarDays: List<com.bazical.app.domain.model.CalendarDay> = emptyList()

        private var isVisible = false
        private var renderJob: Job? = null

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            renderer = CalendarRenderer(surfaceHolder)

            val displayMetrics = resources.displayMetrics
            renderer?.setDensity(displayMetrics.density)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            scheduleRender()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            stopRendering()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            isVisible = visible
            if (visible) {
                scheduleRender()
            } else {
                stopRendering()
            }
        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder) {
            if (isVisible) {
                render()
            }
        }

        override fun onOffsetsChanged(
            xOffset: Float,
            yOffset: Float,
            xOffsetStep: Float,
            yOffsetStep: Float,
            xPixelOffset: Int,
            yPixelOffset: Int
        ) {
            super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset)
        }

        override fun onTouchEvent(event: android.view.MotionEvent) {
            super.onTouchEvent(event)
        }

        fun setCalendarData(year: Int, month: Int, days: List<com.bazical.app.domain.model.CalendarDay>) {
            currentYear = year
            currentMonth = month
            calendarDays = days
            if (isVisible) {
                render()
            }
        }

        private fun scheduleRender() {
            renderJob?.cancel()
            renderJob = scope.launch {
                while (isVisible) {
                    render()
                    delay(16)
                }
            }
        }

        private fun stopRendering() {
            renderJob?.cancel()
            renderJob = null
        }

        private fun render() {
            if (currentYear == 0 || calendarDays.isEmpty()) {
                val now = java.time.LocalDate.now()
                if (currentYear == 0) {
                    currentYear = now.year
                    currentMonth = now.monthValue
                }
            }
            renderer?.render(currentYear, currentMonth, calendarDays)
        }

        override fun onDestroy() {
            stopRendering()
            scope.cancel()
            super.onDestroy()
        }
    }
}