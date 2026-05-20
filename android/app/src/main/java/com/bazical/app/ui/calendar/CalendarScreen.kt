package com.bazical.app.ui.calendar

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bazical.app.domain.model.CalendarDay
import com.bazical.app.ui.components.BottomTabBar
import com.bazical.app.ui.components.TabItem
import com.bazical.app.ui.theme.TextPrimary
import com.bazical.app.ui.theme.TextTertiary
import java.time.LocalDate

private const val TAG = "CalendarScreen"

@Composable
fun CalendarScreen(
    onNavigateToDaily: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    currentRoute: String?,
    onTabClick: (TabItem) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4EF))
    ) {
        // Date header with navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.previousMonth() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "上一月",
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "上一月",
                    fontSize = 13.sp,
                    color = TextTertiary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${uiState.year}年${uiState.month}月",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "${uiState.monthBranch ?: ""}月 · ${uiState.yearBranch ?: ""}",
                    fontSize = 13.sp,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.nextMonth() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "下一月",
                    fontSize = 13.sp,
                    color = TextTertiary
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "下一月",
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User info card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(18.dp),
                    spotColor = Color.Black.copy(alpha = 0.06f)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(14.dp),
                                spotColor = Color.Black.copy(alpha = 0.1f)
                            )
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFC84A3E), Color(0xFFA33D33))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "十",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "日主：${uiState.dayStem ?: ""}${uiState.dayBranch ?: ""}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "生于${uiState.userBirthday ?: ""}",
                            fontSize = 12.sp,
                            color = TextTertiary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = uiState.monthShishen ?: "",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD4A843)
                    )
                    Text(
                        text = "本月能量",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.Black.copy(alpha = 0.06f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Column {
                // Weekday headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("日", "一", "二", "三", "四", "五", "六").forEachIndexed { index, day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = if (index == 0 || index == 6) Color(0xFFC84A3E) else TextTertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Calendar grid with all days (including padding for other months)
                CalendarGridFull(
                    days = uiState.days,
                    year = uiState.year,
                    month = uiState.month,
                    onDayClick = { day -> onNavigateToDaily(day.date) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Tab Bar
        BottomTabBar(
            currentRoute = currentRoute,
            onTabClick = onTabClick
        )
    }
}

@Composable
private fun CalendarGridFull(
    days: List<CalendarDay>,
    year: Int,
    month: Int,
    onDayClick: (CalendarDay) -> Unit
) {
    val today = LocalDate.now()
    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday

    // Calculate total cells needed (6 weeks = 42 days)
    val totalCells = 42

    // Create list of all cells (including padding days from prev/next month)
    val allCells = mutableListOf<CalendarCellData>()

    // Calculate days from previous month
    val prevMonth = firstDayOfMonth.minusMonths(1)
    val daysInPrevMonth = prevMonth.lengthOfMonth()
    for (i in (daysInPrevMonth - startDayOfWeek + 1)..daysInPrevMonth) {
        allCells.add(CalendarCellData(
            dayNumber = i,
            lunarDate = null,
            isJieqi = false,
            ganzhiIndex = -1, // Indicates padding
            stem = "",
            shishen = "",
            branch = "",
            branchShishen = "",
            isOtherMonth = true,
            isToday = false,
            isWeekend = false,
            date = ""
        ))
    }

    // Add current month days
    Log.d(TAG, "CalendarGridFull: days.size=${days.size}, first day: ${
        if (days.isNotEmpty()) "date=${days[0].date}, ganzhi=${days[0].ganzhi}, shishen=${days[0].shishen}, branchShishen=${days[0].branchShishen}, lunarDate=${days[0].lunarDate}" else "empty"
    }")
    for (day in days) {
        val date = try { LocalDate.parse(day.date) } catch (e: Exception) { null }
        val dayNum = date?.dayOfMonth ?: 0
        val isTodayCell = date == today
        val dayOfWeek = date?.dayOfWeek?.value ?: 1
        val isWeekendCell = dayOfWeek == 0 || dayOfWeek == 6

        // Get stem and branch from ganzhi
        val stemChar = if (day.ganzhi.isNotEmpty()) day.ganzhi[0] else ""
        val branchChar = if (day.ganzhi.size > 1) day.ganzhi[1] else ""

        Log.d(TAG, "Day $dayNum: stem='$stemChar', branch='$branchChar', shishen='${day.shishen}', branchShishen='${day.branchShishen}'")

        // Build lunarDate with proper empty string handling
        val displayLunarDate = when {
            !day.jieqi.isNullOrEmpty() -> day.jieqi!!
            !day.lunarDate.isNullOrEmpty() -> day.lunarDate!!
            !day.holiday.isNullOrEmpty() -> day.holiday!!
            else -> null
        }
        val isJieqiDay = !day.jieqi.isNullOrEmpty()

        allCells.add(CalendarCellData(
            dayNumber = dayNum,
            lunarDate = displayLunarDate,
            isJieqi = isJieqiDay,
            ganzhiIndex = if (day.ganzhi.isNotEmpty()) 0 else -1,
            stem = stemChar,
            shishen = day.shishen,
            branch = branchChar,
            branchShishen = day.branchShishen,
            isOtherMonth = false,
            isToday = isTodayCell,
            isWeekend = isWeekendCell,
            date = day.date
        ))
    }

    // Add next month days
    val remainingCells = totalCells - allCells.size
    val nextMonth = firstDayOfMonth.plusMonths(1)
    for (i in 1..remainingCells) {
        allCells.add(CalendarCellData(
            dayNumber = i,
            lunarDate = null,
            isJieqi = false,
            ganzhiIndex = -1,
            stem = "",
            shishen = "",
            branch = "",
            branchShishen = "",
            isOtherMonth = true,
            isToday = false,
            isWeekend = false,
            date = ""
        ))
    }

    // Display in grid - 7 columns with flexible height
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Calculate rows needed (6 weeks = 6 rows)
        val rows = 6

        for (rowIndex in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (colIndex in 0 until 7) {
                    val cellIndex = rowIndex * 7 + colIndex
                    if (cellIndex < allCells.size) {
                        CalendarDayCellFromDesign(
                            cell = allCells[cellIndex],
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

data class CalendarCellData(
    val dayNumber: Int,
    val lunarDate: String?,
    val isJieqi: Boolean = false,
    val ganzhiIndex: Int,
    val stem: String,
    val shishen: String,
    val branch: String,
    val branchShishen: String,
    val isOtherMonth: Boolean,
    val isToday: Boolean,
    val isWeekend: Boolean,
    val date: String
)

@Composable
private fun CalendarDayCellFromDesign(
    cell: CalendarCellData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    cell.isToday -> Brush.linearGradient(colors = listOf(Color(0xFFC84A3E), Color(0xFFA33D33)))
                    cell.isOtherMonth -> SolidColor(Color(0xFFFAFAF8))
                    else -> SolidColor(Color(0xFFFAFAF8))
                }
            )
            .clickable(enabled = !cell.isOtherMonth && cell.date.isNotEmpty()) { }
            .padding(horizontal = 1.dp, vertical = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Day number
        Text(
            text = cell.dayNumber.toString(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 9.sp,
            color = when {
                cell.isToday -> Color.White
                cell.isOtherMonth -> TextPrimary.copy(alpha = 0.3f)
                cell.isWeekend -> Color(0xFFC84A3E)
                else -> TextPrimary
            }
        )

        // Lunar date or Jieqi
        val displayText = cell.lunarDate ?: ""
        if (displayText.isNotEmpty()) {
            val lunarColor = when {
                cell.isToday -> Color.White.copy(alpha = 0.7f)
                cell.isJieqi -> Color(0xFF10B981)
                displayText.contains("初一") || displayText.contains("十五") -> Color(0xFFE74C3C)
                else -> TextTertiary
            }
            Text(
                text = displayText,
                fontSize = 5.sp,
                lineHeight = 5.sp,
                color = lunarColor
            )
        }

        // Only show ganzhi rows for current month days
        if (!cell.isOtherMonth && cell.stem.isNotEmpty()) {
            // Stem + Shishen
            Text(
                text = cell.stem + (if (cell.shishen.isNotEmpty()) cell.shishen else ""),
                fontSize = 6.sp,
                lineHeight = 6.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (cell.isToday) Color.White else getStemColor(cell.stem)
            )

            // Branch + BranchShishen
            if (cell.branch.isNotEmpty()) {
                Text(
                    text = cell.branch + (if (cell.branchShishen.isNotEmpty()) cell.branchShishen else ""),
                    fontSize = 6.sp,
                    lineHeight = 6.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (cell.isToday) Color.White else getBranchColor(cell.branch)
                )
            }
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
        else -> TextPrimary
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
        else -> TextPrimary
    }
}