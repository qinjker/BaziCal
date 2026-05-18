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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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
import com.bazical.app.ui.theme.Secondary
import com.bazical.app.ui.theme.Success
import com.bazical.app.ui.theme.TextPrimary
import com.bazical.app.ui.theme.TextSecondary
import com.bazical.app.ui.theme.TextTertiary
import com.bazical.app.ui.theme.Warning
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    onNavigateToDaily: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Date header with navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous month button
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

            // Current month display
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

            // Next month button
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
                    // Avatar
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
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Primary, PrimaryVariant)
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

                    Spacer(modifier = Modifier.size(12.dp))

                    Column {
                        Text(
                            text = "日主：${uiState.dayStem}${uiState.dayBranch}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "生于${uiState.userBirthday}",
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
                        text = uiState.monthShishen ?: "正财",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Secondary
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

        Spacer(modifier = Modifier.height(20.dp))

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
                            color = if (index == 0 || index == 6) Primary else TextTertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Loading or calendar grid
                if (uiState.loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                } else {
                    CalendarGrid(
                        days = uiState.days,
                        onDayClick = { day -> onNavigateToDaily(day.date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    days: List<CalendarDay>,
    onDayClick: (CalendarDay) -> Unit
) {
    val today = LocalDate.now()
    val firstDayOfMonth = days.firstOrNull()?.date?.let {
        try { LocalDate.parse(it) } catch (e: Exception) { null }
    } ?: LocalDate.now()
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
                isToday = try { LocalDate.parse(day.date) == today } catch (e: Exception) { false },
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
        LocalDate.parse(day.date).dayOfMonth
    } catch (e: Exception) { 0 }

    val isWeekend = try {
        val dow = LocalDate.parse(day.date).dayOfWeek.value % 7
        dow == 0 || dow == 6
    } catch (e: Exception) { false }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isToday) {
                    Brush.linearGradient(colors = listOf(Primary, PrimaryVariant))
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
                    isWeekend -> Primary
                    else -> TextPrimary
                }
            )

            // Lunar date or holiday
            val displayText = day.holiday ?: day.lunarDate ?: ""
            if (displayText.isNotEmpty()) {
                Text(
                    text = displayText,
                    fontSize = 9.sp,
                    color = when {
                        isToday -> Color.White.copy(alpha = 0.7f)
                        day.holiday != null -> Color(0xFFE74C3C)
                        day.jieqi != null -> Success
                        else -> TextTertiary
                    },
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            // Gan Zhi row
            if (day.ganzhi.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val stemColor = getStemColor(day.ganzhi.getOrNull(0)?.toString() ?: "")
                    val branchColor = getBranchColor(day.ganzhi.getOrNull(1)?.toString() ?: "")

                    Text(
                        text = day.ganzhi.getOrNull(0)?.toString() ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isToday) Color.White else stemColor
                    )
                    Text(
                        text = day.ganzhi.getOrNull(1)?.toString() ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isToday) Color.White else branchColor
                    )
                }
            }

            // Shi Shen
            if (day.shishen.isNotEmpty()) {
                Text(
                    text = day.shishen,
                    fontSize = 9.sp,
                    color = if (isToday) Color.White.copy(alpha = 0.7f) else TextTertiary
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
        else -> Color(0xFF2C1810)
    }
}