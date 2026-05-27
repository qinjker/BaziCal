package com.bazical.app.wallpaper.controller

import android.view.MotionEvent
import java.time.LocalDate

class WallpaperController {
    private var currentYear: Int = LocalDate.now().year
    private var currentMonth: Int = LocalDate.now().monthValue

    fun init() {}

    fun onTouchEvent(event: MotionEvent): Boolean {
        return true
    }

    fun scrollToNextMonth() {
        if (currentMonth == 12) {
            currentMonth = 1
            currentYear++
        } else {
            currentMonth++
        }
    }

    fun scrollToPrevMonth() {
        if (currentMonth == 1) {
            currentMonth = 12
            currentYear--
        } else {
            currentMonth--
        }
    }

    fun setCurrentDate(year: Int, month: Int) {
        currentYear = year
        currentMonth = month
    }
}