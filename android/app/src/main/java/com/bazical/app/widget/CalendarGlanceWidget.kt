package com.bazical.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.column
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Row
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .background(Color(0xFFFAFAF8))
                        .padding(12)
                ) {
                    Text(
                        text = "八字历",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF2C1810)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        text = today.format(formatter),
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF8B7355)),
                            fontSize = 10.sp
                        )
                    )

                    Row(modifier = GlanceModifier.padding(8)) {
                        Column {
                            listOf("一", "二", "三", "四", "五", "六", "日").forEachIndexed { index, day ->
                                val textColor = if (index >= 5) Color(0xFFC84A3E) else Color(0xFF8B7355)
                                Text(
                                    text = day,
                                    style = TextStyle(
                                        color = ColorProvider(textColor),
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class CalendarGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalendarGlanceWidget()
}