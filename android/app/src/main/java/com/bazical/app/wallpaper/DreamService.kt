package com.bazical.app.wallpaper

import android.service.dreams.DreamService
import android.view.SurfaceHolder
import com.bazical.app.wallpaper.renderer.CalendarRenderer
import kotlinx.coroutines.*
import java.time.LocalDate

class BaziCalDreamService : DreamService() {

    private var renderer: CalendarRenderer? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var currentYear: Int = LocalDate.now().year
    private var currentMonth: Int = LocalDate.now().monthValue
    private var calendarDays: List<com.bazical.app.domain.model.CalendarDay> = emptyList()

    private var isVisible = false
    private var renderJob: Job? = null
    private var surfaceHolder: SurfaceHolder? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = true
        isFullscreen = true
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onSurfaceCreated(holder: SurfaceHolder) {
        super.onSurfaceCreated(holder)
        surfaceHolder = holder
        renderer = CalendarRenderer(holder)
        scheduleRender()
    }

    override fun onSurfaceDestroyed(holder: SurfaceHolder) {
        super.onSurfaceDestroyed(holder)
        stopRendering()
        surfaceHolder = null
        renderer = null
    }

    override fun onVisibilityChanged(visible: Boolean) {
        isVisible = visible
        if (visible) {
            scheduleRender()
        } else {
            stopRendering()
        }
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
    }

    override fun onDreamingStopped() {
        stopRendering()
        super.onDreamingStopped()
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
                delay(16) // ~60fps
            }
        }
    }

    private fun stopRendering() {
        renderJob?.cancel()
        renderJob = null
    }

    private fun render() {
        renderer?.render(currentYear, currentMonth, calendarDays)
    }

    override fun onDestroy() {
        stopRendering()
        scope.cancel()
        surfaceHolder = null
        renderer = null
        super.onDestroy()
    }
}