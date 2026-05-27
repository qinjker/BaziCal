package com.bazical.app.wallpaper

import android.service.dreams.DreamService
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.bazical.app.ui.theme.BaziCalTheme

class BaziCalDreamService : DreamService() {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = true
        isFullscreen = true
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        // Use ComposeView for DreamService content
        val composeView = ComposeView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(composeView)
        composeView.setContent {
            BaziCalTheme {
                // TODO: Show calendar content here
                // For now, this is a placeholder
            }
        }
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
    }

    override fun onDetachFromWindow() {
        super.onDetachFromWindow()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}