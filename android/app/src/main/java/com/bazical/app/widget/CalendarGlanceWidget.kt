package com.bazical.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.preferencesDataStore
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
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class CalendarGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            CalendarContent(context)
        }
    }

    @Composable
    private fun CalendarContent(context: Context) {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
        val primaryColor = Color(0xFF2C1810)
        val secondaryColor = Color(0xFF8B7355)
        val weekendColor = Color(0xFFC84A3E)
        val todayBgColor = Color(0xFFC84A3E)

        // Load user BaZi data
        val userBaZi = loadUserBaZi(context)
        val year = today.year
        val month = today.monthValue

        // Get month calendar data
        val monthDays = getMonthDaysWithBazi(year, month, userBaZi)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAF8))
                .padding(8.dp),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Top
        ) {
            // Title bar with user day pillar info
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "八字历",
                    style = TextStyle(
                        color = ColorProvider(primaryColor),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = if (userBaZi != null) "【${userBaZi.dayStem}${userBaZi.dayBranch}】" else "",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFD4A843)),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = " ${today.format(formatter)}",
                    style = TextStyle(
                        color = ColorProvider(secondaryColor),
                        fontSize = 9.sp
                    )
                )
            }

            // Weekday headers
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                repeat(7) { index ->
                    val isWeekend = index == 0 || index == 6
                    Text(
                        text = listOf("日", "一", "二", "三", "四", "五", "六")[index],
                        style = TextStyle(
                            color = ColorProvider(if (isWeekend) weekendColor else secondaryColor),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                    )
                }
            }

            // Date grid
            monthDays.forEach { week ->
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    week.forEach { dayInfo ->
                        DayCellWidget(
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
    private fun DayCellWidget(
        dayInfo: DayCellInfoWidget,
        todayBgColor: Color,
        primaryColor: Color,
        secondaryColor: Color,
        weekendColor: Color,
        modifier: GlanceModifier
    ) {
        val bgColor = if (dayInfo.isToday) todayBgColor else Color(0xFFFAFAF8)
        val textColor = if (dayInfo.isToday) Color.White else if (dayInfo.isWeekend) weekendColor else primaryColor
        val stemColor = if (dayInfo.isToday) Color.White else getStemColor(dayInfo.stem)
        val branchColor = if (dayInfo.isToday) Color.White else getBranchColor(dayInfo.branch)

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(1.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row 1: Gregorian date
            Text(
                text = dayInfo.dayNumber.toString(),
                style = TextStyle(
                    color = ColorProvider(textColor),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            )

            // Row 2: Lunar/jieqi
            if (dayInfo.lunarDate.isNotEmpty()) {
                val displayColor = when {
                    dayInfo.isToday -> Color.White.copy(alpha = 0.7f)
                    dayInfo.isJieqi -> Color(0xFF10B981)
                    dayInfo.lunarDate.contains("初一") || dayInfo.lunarDate.contains("十五") -> Color(0xFFE74C3C)
                    else -> secondaryColor
                }
                Text(
                    text = dayInfo.lunarDate,
                    style = TextStyle(
                        color = ColorProvider(displayColor),
                        fontSize = 7.sp
                    ),
                    maxLines = 1
                )
            }

            // Row 3: Stem + Shishen
            if (dayInfo.stem.isNotEmpty()) {
                Text(
                    text = "${dayInfo.stem} ${dayInfo.shishen}",
                    style = TextStyle(
                        color = ColorProvider(stemColor),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1
                )
            }

            // Row 4: Branch + BranchShishen
            if (dayInfo.branch.isNotEmpty()) {
                Text(
                    text = "${dayInfo.branch} ${dayInfo.branchShishen}",
                    style = TextStyle(
                        color = ColorProvider(branchColor),
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

    private fun loadUserBaZi(context: Context): UserBaZiData? {
        return try {
            runBlocking {
                withContext(Dispatchers.IO) {
                    val sharedPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    val baziJsonStr = sharedPrefs.getString("bazi_json", null) ?: return@withContext null
                    parseUserBaZi(baziJsonStr)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseUserBaZi(baziJson: String): UserBaZiData? {
        return try {
            val parts = baziJson.split("|")
            if (parts.size < 4) return null

            val (yearStem, yearBranch) = parts[0].split(",")
            val (monthStem, monthBranch) = parts[1].split(",")
            val (dayStem, dayBranch) = parts[2].split(",")
            val (hourStem, hourBranch) = parts[3].split(",")

            UserBaZiData(
                yearStem = yearStem,
                yearBranch = yearBranch,
                monthStem = monthStem,
                monthBranch = monthBranch,
                dayStem = dayStem,
                dayBranch = dayBranch,
                hourStem = hourStem,
                hourBranch = hourBranch
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getMonthDaysWithBazi(
        year: Int,
        month: Int,
        userBaZi: UserBaZiData?
    ): List<List<DayCellInfoWidget>> {
        val today = LocalDate.now()
        val firstDayOfMonth = LocalDate.of(year, month, 1)
        val daysInMonth = firstDayOfMonth.lengthOfMonth()
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

        val stems = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
        val branches = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

        val userDayStemIdx = if (userBaZi != null) {
            stems.indexOf(userBaZi.dayStem).takeIf { it >= 0 } ?: 0
        } else {
            0
        }

        val monthDays = mutableListOf<DayCellInfoWidget>()
        for (day in 1..daysInMonth) {
            val date = LocalDate.of(year, month, day)
            val isToday = date == today
            val dayOfWeek = date.dayOfWeek.value % 7
            val isWeekend = dayOfWeek == 0 || dayOfWeek == 6

            val dayIndex = (day - 1) % 10
            val branchIndex = (day - 1) % 12

            val shishen = if (userBaZi != null) {
                calculateShishen(userDayStemIdx, dayIndex)
            } else ""
            val branchShishen = if (userBaZi != null) {
                calculateShishen(userDayStemIdx, branchIndex)
            } else ""

            val lunarNums = listOf("初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
                "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
                "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十")
            val lunarIndex = (day - 1) % 30

            monthDays.add(DayCellInfoWidget(
                dayNumber = day,
                lunarDate = lunarNums[lunarIndex],
                isJieqi = false,
                stem = stems[dayIndex],
                shishen = shishen,
                branch = branches[branchIndex],
                branchShishen = branchShishen,
                isToday = isToday,
                isWeekend = isWeekend
            ))
        }

        val weeks = mutableListOf<List<DayCellInfoWidget>>()
        var currentDay = 0

        for (week in 0 until 6) {
            val weekDays = mutableListOf<DayCellInfoWidget>()
            for (dayOfWeek in 0 until 7) {
                val cellIndex = week * 7 + dayOfWeek
                if (cellIndex < startDayOfWeek || currentDay >= daysInMonth) {
                    weekDays.add(DayCellInfoWidget(
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

    private fun calculateShishen(userDayStemIdx: Int, targetIdx: Int): String {
        val userElementIdx = Math.floor(userDayStemIdx.toDouble() / 2).toInt() % 5
        val targetElementIdx = Math.floor(targetIdx.toDouble() / 2).toInt() % 5

        var relation = targetElementIdx - userElementIdx
        if (relation < 0) relation += 5

        val userIsYang = userDayStemIdx % 2 == 0
        val targetIsYang = targetIdx % 2 == 0
        val isSameYinYang = userIsYang == targetIsYang

        return when (relation) {
            0 -> if (isSameYinYang) "比肩" else "劫财"
            1 -> if (isSameYinYang) "食神" else "伤官"
            2 -> if (isSameYinYang) "偏财" else "正财"
            3 -> if (isSameYinYang) "七杀" else "正官"
            4 -> if (isSameYinYang) "偏印" else "正印"
            else -> "比肩"
        }
    }
}

data class UserBaZiData(
    val yearStem: String,
    val yearBranch: String,
    val monthStem: String,
    val monthBranch: String,
    val dayStem: String,
    val dayBranch: String,
    val hourStem: String,
    val hourBranch: String
)

data class DayCellInfoWidget(
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