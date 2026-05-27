package com.bazical.app.wallpaper

import android.service.dreams.DreamService
import android.widget.FrameLayout
import com.bazical.app.ui.theme.BaziCalTheme

class BaziCalDreamService : DreamService() {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = true
        isFullscreen = true
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        val layout = FrameLayout(this)
        setContentView(layout)
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}