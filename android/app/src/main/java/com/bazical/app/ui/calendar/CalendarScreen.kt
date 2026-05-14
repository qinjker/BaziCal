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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bazical.app.domain.model.CalendarDay
import com.bazical.app.ui.theme.Primary
import com.bazical.app.ui.theme.Secondary
import com.bazical.app.ui.theme.Success
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
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1117), Color(0xFF161B22), Color(0xFF0D1117))
                )
            )
            .padding(16.dp)
    ) {
        // Header with navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "上月",
                    tint = Primary
                )
            }
            Text(
                text = "${uiState.year}年${uiState.month}月",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "下月",
                    tint = Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BaZi display
        uiState.bazi?.let { bazi ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "四柱八字",
                        style = MaterialTheme.typography.titleMedium,
                        color = Primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BaZiPillar("年", bazi.year.stem, bazi.year.branch)
                        BaZiPillar("月", bazi.month.stem, bazi.month.branch)
                        BaZiPillar("日", bazi.day.stem, bazi.day.branch)
                        BaZiPillar("时", bazi.hour.stem, bazi.hour.branch)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekday headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Loading or calendar grid
        if (uiState.loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
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

@Composable
private fun BaZiPillar(label: String, stem: String, branch: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = stem,
            style = MaterialTheme.typography.titleMedium,
            color = Primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = branch,
            style = MaterialTheme.typography.bodyMedium,
            color = Secondary
        )
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
    val luckColor = when (day.luck) {
        "吉" -> Success
        "凶" -> Warning
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val dayNumber = try {
                LocalDate.parse(day.date).dayOfMonth
            } catch (e: Exception) { 0 }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .then(if (isToday) Modifier.background(Primary, CircleShape) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayNumber.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = day.ganzhi.take(2),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            if (day.luck.isNotEmpty()) {
                Text(
                    text = day.luck,
                    style = MaterialTheme.typography.labelSmall,
                    color = luckColor
                )
            }
        }
    }
}