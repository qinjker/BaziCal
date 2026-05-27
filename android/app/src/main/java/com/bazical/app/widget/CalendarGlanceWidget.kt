package com.bazical.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            CalendarContent()
        }
    }

    @Composable
    private fun CalendarContent() {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
        val primaryColor = Color(0xFF2C1810)
        val secondaryColor = Color(0xFF8B7355)
        val weekendColor = Color(0xFFC84A3E)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAF8))
                .padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "八字历",
                style = TextStyle(
                    color = ColorProvider(day = primaryColor, night = primaryColor),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = today.format(formatter),
                style = TextStyle(
                    color = ColorProvider(day = secondaryColor, night = secondaryColor),
                    fontSize = 10.sp
                )
            )

            Row(
                modifier = GlanceModifier.padding(top = 8.dp)
            ) {
                Column {
                    val weekdays = listOf("一", "二", "三", "四", "五", "六", "日")
                    weekdays.forEachIndexed { index, day ->
                        val textColor = if (index >= 5) weekendColor else secondaryColor
                        Text(
                            text = day,
                            style = TextStyle(
                                color = ColorProvider(day = textColor, night = textColor),
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

class CalendarGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalendarGlanceWidget()
}