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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider as GlanceColorProvider
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
        val todayBgColor = Color(0xFFC84A3E)

        // 获取本月数据
        val year = today.year
        val month = today.monthValue
        val days = getMonthDays(year, month)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAF8))
                .padding(8.dp),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Top
        ) {
            // 标题栏
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "八字历",
                    style = TextStyle(
                        color = GlanceColorProvider(day = primaryColor, night = primaryColor),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "  ${today.format(formatter)}",
                    style = TextStyle(
                        color = GlanceColorProvider(day = secondaryColor, night = secondaryColor),
                        fontSize = 9.sp
                    )
                )
            }

            // 星期标题行
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                repeat(7) { index ->
                    Text(
                        text = listOf("日", "一", "二", "三", "四", "五", "六")[index],
                        style = TextStyle(
                            color = GlanceColorProvider(
                                day = if (index == 0 || index == 6) weekendColor else secondaryColor,
                                night = if (index == 0 || index == 6) weekendColor else secondaryColor
                            ),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                        textAlign = androidx.glance.text.TextAlign.Center
                    )
                }
            }

            // 日期网格
            days.forEach { week ->
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    week.forEach { dayInfo ->
                        DayCell(
                            dayInfo = dayInfo,
                            todayBgColor = todayBgColor,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            weekendColor = weekendColor,
                            modifier = GlanceModifier.defaultWeight()
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun DayCell(
        dayInfo: DayCellInfo,
        todayBgColor: Color,
        primaryColor: Color,
        secondaryColor: Color,
        weekendColor: Color,
        modifier: GlanceModifier
    ) {
        val bgColor = if (dayInfo.isToday) todayBgColor else Color(0xFFFAFAF8)
        val textColor = if (dayInfo.isToday) Color.White else if (dayInfo.isWeekend) weekendColor else primaryColor
        val lunarColor = if (dayInfo.isToday) Color.White.copy(alpha = 0.7f) else secondaryColor
        val stemColor = if (dayInfo.isToday) Color.White else getStemColor(dayInfo.stem)
        val branchColor = if (dayInfo.isToday) Color.White else getBranchColor(dayInfo.branch)

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(1.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 第1行：公历日期
            Text(
                text = dayInfo.dayNumber.toString(),
                style = TextStyle(
                    color = GlanceColorProvider(day = textColor, night = textColor),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            // 第2行：农历/节气
            if (dayInfo.lunarDate.isNotEmpty()) {
                val displayColor = if (dayInfo.isToday) {
                    Color.White.copy(alpha = 0.7f)
                } else if (dayInfo.isJieqi) {
                    Color(0xFF10B981)
                } else if (dayInfo.lunarDate.contains("初一") || dayInfo.lunarDate.contains("十五")) {
                    Color(0xFFE74C3C)
                } else {
                    secondaryColor
                }
                Text(
                    text = dayInfo.lunarDate,
                    style = TextStyle(
                        color = GlanceColorProvider(day = displayColor, night = displayColor),
                        fontSize = 7.sp
                    ),
                    maxLines = 1
                )
            }

            // 第3行：天干+十神
            if (dayInfo.stem.isNotEmpty()) {
                Text(
                    text = "${dayInfo.stem} ${dayInfo.shishen}",
                    style = TextStyle(
                        color = GlanceColorProvider(day = stemColor, night = stemColor),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1
                )
            }

            // 第4行：地支+地支十神
            if (dayInfo.branch.isNotEmpty()) {
                Text(
                    text = "${dayInfo.branch} ${dayInfo.branchShishen}",
                    style = TextStyle(
                        color = GlanceColorProvider(day = branchColor, night = branchColor),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1
                )
            }
        }
    }

    private fun getStemColor(stem: String): Color {
        return when (stem) {
            "甲" -> Color(0xFF4ade80)
            "乙" -> Color(0xFF86efac)
            "丙" -> Color(0xFFf87171)
            "丁" -> Color(0xFFfca5a5)
            "戊" -> Color(0xFFa78bfa)
            "己" -> Color(0xFFc4b5fd)
            "庚" -> Color(0xFFfbbf24)
            "辛" -> Color(0xFFfde047)
            "壬" -> Color(0xFF60a5fa)
            "癸" -> Color(0xFF93c5fd)
            else -> Color(0xFF2C1810)
        }
    }

    private fun getBranchColor(branch: String): Color {
        return when (branch) {
            "子" -> Color(0xFF60a5fa)
            "丑" -> Color(0xFFa78bfa)
            "寅" -> Color(0xFF4ade80)
            "卯" -> Color(0xFF86efac)
            "辰" -> Color(0xFFa78bfa)
            "巳" -> Color(0xFFf87171)
            "午" -> Color(0xFFfca5a5)
            "未" -> Color(0xFFa78bfa)
            "申" -> Color(0xFFfbbf24)
            "酉" -> Color(0xFFfde047)
            "戌" -> Color(0xFFa78bfa)
            "亥" -> Color(0xFF60a5fa)
            else -> Color(0xFF5A4A3A)
        }
    }

    private fun getMonthDays(year: Int, month: Int): List<List<DayCellInfo>> {
        val today = LocalDate.now()
        val firstDayOfMonth = LocalDate.of(year, month, 1)
        val daysInMonth = firstDayOfMonth.lengthOfMonth()
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday

        // 生成当月日期数据（模拟数据，实际应从API获取）
        val monthDays = mutableListOf<DayCellInfo>()
        for (day in 1..daysInMonth) {
            val date = LocalDate.of(year, month, day)
            val isToday = date == today
            val dayOfWeek = date.dayOfWeek.value % 7
            val isWeekend = dayOfWeek == 0 || dayOfWeek == 6

            // 模拟天干地支（实际应从API获取）
            val stemIndex = (day - 1) % 10
            val branchIndex = (day - 1) % 12
            val stems = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
            val branches = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
            val shishens = listOf("正官", "七杀", "正印", "枭神", "比肩", "劫财", "食神", "伤官", "正财", "偏财")
            val branchShishens = listOf("比肩", "劫财", "食神", "伤官", "正财", "偏财", "正官", "七杀", "正印", "枭神", "比肩", "劫财")

            // 模拟农历日期
            val lunarNums = listOf("初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
                "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
                "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十")
            val lunarIndex = (day - 1) % 30

            monthDays.add(DayCellInfo(
                dayNumber = day,
                lunarDate = lunarNums[lunarIndex],
                isJieqi = false,
                stem = stems[stemIndex],
                shishen = shishens[stemIndex],
                branch = branches[branchIndex],
                branchShishen = branchShishens[branchIndex],
                isToday = isToday,
                isWeekend = isWeekend
            ))
        }

        // 构建6周的数据
        val weeks = mutableListOf<List<DayCellInfo>>()
        var currentDay = 0

        for (week in 0 until 6) {
            val weekDays = mutableListOf<DayCellInfo>()
            for (dayOfWeek in 0 until 7) {
                val cellIndex = week * 7 + dayOfWeek
                if (cellIndex < startDayOfWeek || currentDay >= daysInMonth) {
                    // 填充空白或其他月的日期
                    weekDays.add(DayCellInfo(
                        dayNumber = 0,
                        lunarDate = "",
                        isJieqi = false,
                        stem = "",
                        shishen = "",
                        branch = "",
                        branchShishen = "",
                        isToday = false,
                        isWeekend = false
                    ))
                } else {
                    weekDays.add(monthDays[currentDay])
                    currentDay++
                }
            }
            weeks.add(weekDays)
            if (currentDay >= daysInMonth) break
        }

        return weeks
    }
}

data class DayCellInfo(
    val dayNumber: Int,
    val lunarDate: String,
    val isJieqi: Boolean,
    val stem: String,
    val shishen: String,
    val branch: String,
    val branchShishen: String,
    val isToday: Boolean,
    val isWeekend: Boolean
)

class CalendarGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalendarGlanceWidget()
}