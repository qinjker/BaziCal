package com.bazical.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.bazical.app.ui.theme.Primary
import com.bazical.app.ui.theme.PrimaryVariant
import com.bazical.app.ui.theme.TextPrimary
import com.bazical.app.ui.theme.TextSecondary
import com.bazical.app.ui.theme.TextTertiary
import com.bazical.app.ui.theme.Warning
import com.bazical.app.ui.theme.Success
import com.bazical.app.ui.theme.Secondary

@Composable
fun CalendarScreen(
    onNavigateToDaily: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToToday: () -> Unit,
    onNavigateToFeedback: () -> Unit,
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

                // Calendar grid
                CalendarGrid(
                    days = uiState.days,
                    onDayClick = { day -> onNavigateToDaily(day.date) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabItem(
                emoji = "📅",
                label = "月历",
                isActive = true,
                onClick = { }
            )
            TabItem(
                emoji = "✨",
                label = "今日",
                isActive = false,
                onClick = onNavigateToToday
            )
            TabItem(
                emoji = "📝",
                label = "生辰",
                isActive = false,
                onClick = onNavigateToHome
            )
            TabItem(
                emoji = "💬",
                label = "反馈",
                isActive = false,
                onClick = onNavigateToFeedback
            )
        }
    }
}

@Composable
private fun TabItem(
    emoji: String,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) Color(0xFFC84A3E) else TextTertiary,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun CalendarGrid(
    days: List<CalendarDay>,
    onDayClick: (CalendarDay) -> Unit
) {
    val today = java.time.LocalDate.now()
    val firstDayOfMonth = days.firstOrNull()?.date?.let {
        try { java.time.LocalDate.parse(it) } catch (e: Exception) { null }
    } ?: java.time.LocalDate.now()
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.height(400.dp)
    ) {
        // Empty cells for days before month starts
        items(startDayOfWeek) {
            Box(modifier = Modifier.aspectRatio(1f))
        }

        // Calendar days
        items(days) { day ->
            CalendarDayCell(
                day = day,
                isToday = try { java.time.LocalDate.parse(day.date) == today } catch (e: Exception) { false },
                onClick = { onDayClick(day) }
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val dayNumber = try {
        java.time.LocalDate.parse(day.date).dayOfMonth
    } catch (e: Exception) { 0 }

    val isWeekend = try {
        val dow = java.time.LocalDate.parse(day.date).dayOfWeek.value % 7
        dow == 0 || dow == 6
    } catch (e: Exception) { false }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isToday) {
                    Brush.linearGradient(colors = listOf(Color(0xFFC84A3E), Color(0xFFA33D33)))
                } else {
                    SolidColor(Color(0xFFFAFAF8))
                }
            )
            .clickable(onClick = onClick)
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day number
            Text(
                text = dayNumber.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    isToday -> Color.White
                    isWeekend -> Color(0xFFC84A3E)
                    else -> TextPrimary
                }
            )

            // Lunar date or holiday
            val displayText = day.holiday ?: day.jieqi ?: day.lunarDate ?: ""
            if (displayText.isNotEmpty()) {
                Text(
                    text = displayText,
                    fontSize = 9.sp,
                    color = when {
                        isToday -> Color.White.copy(alpha = 0.7f)
                        day.holiday != null -> Color(0xFFE74C3C)
                        day.jieqi != null -> Color(0xFF5A8A6A)
                        else -> TextTertiary
                    },
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            // Gan Zhi row 1: stem + shishen
            if (day.ganzhi.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val stemColor = getStemColor(day.ganzhi.getOrNull(0)?.toString() ?: "")
                    Text(
                        text = day.ganzhi.getOrNull(0)?.toString() ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isToday) Color.White else stemColor
                    )
                    Text(
                        text = day.shishen,
                        fontSize = 9.sp,
                        color = if (isToday) Color.White.copy(alpha = 0.7f) else TextTertiary,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            // Gan Zhi row 2: branch + branchShishen
            if (day.ganzhi.length > 1) {
                Row(
                    modifier = Modifier.padding(top = 1.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val branchColor = getBranchColor(day.ganzhi.getOrNull(1)?.toString() ?: "")
                    Text(
                        text = day.ganzhi.getOrNull(1)?.toString() ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isToday) Color.White else branchColor
                    )
                    Text(
                        text = day.branchShishen,
                        fontSize = 9.sp,
                        color = if (isToday) Color.White.copy(alpha = 0.7f) else TextTertiary,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
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