package com.bazical.app.wallpaper

import android.service.dreams.DreamService
import android.view.SurfaceHolder
import com.bazical.app.wallpaper.renderer.CalendarRenderer
import kotlinx.coroutines.*
import java.time.LocalDate

class DreamService : DreamService() {

    private var renderer: CalendarRenderer? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var currentYear: Int = LocalDate.now().year
    private var currentMonth: Int = LocalDate.now().monthValue
    private var calendarDays: List<com.bazical.app.domain.model.CalendarDay> = emptyList()

    private var isVisible = false
    private var renderJob: Job? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Configure dream settings
        isInteractive = true
        isFullscreen = true
    }

    override fun onCreate() {
        super.onCreate()
        renderer = CalendarRenderer(SurfaceHolderImpl())
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
        super.onDestroy()
    }

    // Minimal SurfaceHolder implementation for DreamService
    inner class SurfaceHolderImpl : SurfaceHolder {
        override fun addCallback(callback: SurfaceHolder.Callback?) {}
        override fun removeCallback(callback: SurfaceHolder.Callback?) {}
        override fun isCreating(): Boolean = false
        override fun setType(type: Int) {}
        override fun setFormat(format: Int) {}
        override fun setSize(width: Int, height: Int) {}
        override fun setKeepScreenOn(on: Boolean) {}
        override fun lockCanvas(): android.graphics.Canvas? = null
        override fun lockCanvas(dirty: android.graphics.Rect): android.graphics.Canvas? = null
        override fun unlockCanvasAndPost(canvas: android.graphics.Canvas?) {}
        override fun getSurface(): android.view.Surface? = null
        override fun getSurfaceFrame(): android.graphics.Rect? = null
        override fun getMetrics(out: android.util.DisplayMetrics?) {}
    }
}

class BaziCalDreamService : DreamService() {

    private var service: DreamService? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        service = this
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
    }

    override fun onDetachFromWindow() {
        super.onDetachFromWindow()
        service = null
    }

    override fun onDestroy() {
        service = null
        super.onDestroy()
    }
}