package com.bazical.app.wallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.bazical.app.wallpaper.controller.WallpaperController
import com.bazical.app.wallpaper.renderer.CalendarRenderer
import kotlinx.coroutines.*

class BaziCalWallpaperService : WallpaperService() {

    override fun onCreateEngine(): WallpaperService.Engine {
        return WallpaperEngine()
    }
}

class WallpaperEngine : WallpaperService.Engine() {

    private var renderer: CalendarRenderer? = null
    private var controller: WallpaperController? = null

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var currentYear: Int = 0
    private var currentMonth: Int = 0
    private var calendarDays: List<com.bazical.app.domain.model.CalendarDay> = emptyList()

    private var isVisible = false
    private var renderJob: Job? = null

    override fun onCreate(surfaceHolder: SurfaceHolder) {
        super.onCreate(surfaceHolder)
        renderer = CalendarRenderer(surfaceHolder)
        controller = renderer?.let { WallpaperController(it) }
        controller?.init()

        // Get screen density for font scaling
        val displayMetrics = resources.displayMetrics
        renderer?.setDensity(displayMetrics.density)

        // Set touch listening for scrolling
        isFocusable = true
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
        xPixelOffset: Float,
        yPixelOffset: Float,
        xOffsetStep: Float,
        yOffsetStep: Float,
        page: Int
    ) {
        // Handle page-based wallpaper scrolling (multiple pages)
        super.onOffsetsChanged(xOffset, yOffset, xPixelOffset, yPixelOffset, xOffsetStep, yPixelOffset, page)
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        controller?.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    fun setCalendarData(year: Int, month: Int, days: List<com.bazical.app.domain.model.CalendarDay>) {
        currentYear = year
        currentMonth = month
        calendarDays = days
        if (isVisible) {
            render()
        }
    }

    fun scrollToNextMonth() {
        controller?.scrollToNextMonth()
    }

    fun scrollToPrevMonth() {
        controller?.scrollToPrevMonth()
    }

    private fun scheduleRender() {
        renderJob?.cancel()
        renderJob = scope.launch {
            while (isVisible) {
                render()
                delay(16) // ~60fps
            }
        }
    }

    private fun stopRendering() {
        renderJob?.cancel()
        renderJob = null
    }

    private fun render() {
        if (currentYear == 0 || calendarDays.isEmpty()) {
            // Initialize with current date if no data
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