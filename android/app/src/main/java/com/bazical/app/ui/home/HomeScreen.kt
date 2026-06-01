package com.bazical.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bazical.app.ui.components.BottomTabBar
import com.bazical.app.ui.components.TabItem
import com.bazical.app.ui.theme.Primary
import com.bazical.app.ui.theme.Surface
import com.bazical.app.ui.theme.TextPrimary
import com.bazical.app.ui.theme.TextSecondary
import com.bazical.app.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onNavigateToCalendar: () -> Unit,
    currentRoute: String?,
    onTabClick: (TabItem) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDateFormat = remember { SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()) }

    LaunchedEffect(uiState.calculated) {
        if (uiState.calculated) {
            onNavigateToCalendar()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Navigation bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 20.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            IconButton(
                onClick = onNavigateToCalendar,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = TextPrimary
                )
            }
            Text(
                text = "输入生日",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 44.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // Page header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "你的生日",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "我们将基于此为你生成专属能量日历",
                fontSize = 14.sp,
                color = TextTertiary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Form card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.Black.copy(alpha = 0.06f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Column {
                // Birthday label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "出生年月日",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Birthday input - Single elegant field
                val displayDate = uiState.birthday?.let {
                    SimpleDateFormat("yyyy 年 MM 月 dd 日", Locale.getDefault()).format(Date(it))
                } ?: ""

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFAF6F0))
                        .border(1.5.dp, Color(0xFFE8E0D5), RoundedCornerShape(16.dp))
                        .clickable { viewModel.showDatePicker() },
                    contentAlignment = Alignment.Center
                ) {
                    if (displayDate.isNotEmpty()) {
                        Text(
                            text = displayDate,
                            fontSize = 18.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "点击选择生日",
                                fontSize = 16.sp,
                                color = TextTertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "选择日期",
                                tint = TextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE8E0D5))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Calendar type section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "生日类型",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalendarTypeTag(
                        name = "阳历",
                        hint = "公历生日",
                        selected = uiState.birthdayType == "solar",
                        onClick = { viewModel.updateBirthdayType("solar") },
                        modifier = Modifier.weight(1f)
                    )
                    CalendarTypeTag(
                        name = "阴历",
                        hint = "农历生日",
                        selected = uiState.birthdayType == "lunar",
                        onClick = { viewModel.updateBirthdayType("lunar") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // What is solar/lunar tip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF0F7FF))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "📖",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (uiState.birthdayType == "solar") "什么是阳历生日？" else "什么是阴历生日？",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (uiState.birthdayType == "solar")
                            "阳历也叫公历，是国际通用的日历。比如1月1日、10月1日就是阳历日期。大部分身份证上的生日都是阳历。"
                        else
                            "阴历也叫农历，是中国的传统历法。比如春节是正月初一、端午是五月初五、中秋是八月十五。有些地区习惯过农历生日。",
                        fontSize = 12.sp,
                        color = Color(0xFF5A6A7A),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Submit button
        Button(
            onClick = viewModel::calculate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 24.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Primary.copy(alpha = 0.3f)
                ),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(16.dp),
            enabled = !uiState.loading
        ) {
            if (uiState.loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "开启我的日历",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hint
        Text(
            text = "点击即表示你同意我们的服务条款",
            fontSize = 12.sp,
            color = TextTertiary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(100.dp))
        }

        // Bottom Tab Bar
        BottomTabBar(
            currentRoute = currentRoute,
            onTabClick = onTabClick
        )
    }

    // Date picker dialog
    if (uiState.showDatePicker) {
        val currentYear = uiState.birthday?.let {
            java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()).format(Date(it)).toInt()
        } ?: 1990
        val currentMonth = uiState.birthday?.let {
            java.text.SimpleDateFormat("MM", java.util.Locale.getDefault()).format(Date(it)).toInt()
        } ?: 1
        val currentDay = uiState.birthday?.let {
            java.text.SimpleDateFormat("dd", java.util.Locale.getDefault()).format(Date(it)).toInt()
        } ?: 1

        WheelDatePickerDialog(
            initialYear = currentYear,
            initialMonth = currentMonth,
            initialDay = currentDay,
            onDateSelected = { year, month, day ->
                val calendar = java.util.Calendar.getInstance()
                calendar.set(year, month - 1, day)
                viewModel.updateBirthday(calendar.timeInMillis)
                viewModel.hideDatePicker()
            },
            onDismiss = { viewModel.hideDatePicker() }
        )
    }
}

@Composable
private fun DatePickerField(
    value: String,
    displayValue: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFAF6F0))
            .border(1.5.dp, Color(0xFFE8E0D5), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayValue,
            fontSize = 17.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CalendarTypeTag(
    name: String,
    hint: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFFFFF5F4) else Color(0xFFFAF6F0))
            .border(1.5.dp, if (selected) Primary else Color(0xFFE8E0D5), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (selected) Primary else TextPrimary
            )
            Text(
                text = hint,
                fontSize = 11.sp,
                color = if (selected) Primary.copy(alpha = 0.7f) else TextTertiary
            )
        }
    }
}

@Composable
private fun WheelDatePickerDialog(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    onDateSelected: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val yearRange = (1900..2100).toList()
    val monthRange = (1..12).toList()
    val currentYearIndex = yearRange.indexOf(initialYear).coerceAtLeast(0)
    val currentMonthIndex = monthRange.indexOf(initialMonth).coerceAtLeast(0)
    val daysInMonth = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.YEAR, initialYear)
        set(java.util.Calendar.MONTH, initialMonth - 1)
    }.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val dayRange = (1..daysInMonth).toList()
    val currentDayIndex = dayRange.indexOf(initialDay.coerceAtMost(daysInMonth)).coerceAtLeast(0)

    var yearIndex by remember { mutableStateOf(currentYearIndex) }
    var monthIndex by remember { mutableStateOf(currentMonthIndex) }
    var dayIndex by remember { mutableStateOf(currentDayIndex) }

    val dayCount = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.YEAR, yearRange[yearIndex])
        set(java.util.Calendar.MONTH, monthRange[monthIndex] - 1)
    }.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "选择日期",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WheelPicker(
                    items = yearRange,
                    selectedIndex = yearIndex,
                    onSelectedIndexChanged = { yearIndex = it },
                    modifier = Modifier.weight(1f),
                    itemHeight = 40.dp
                )

                WheelPicker(
                    items = monthRange,
                    selectedIndex = monthIndex,
                    onSelectedIndexChanged = { monthIndex = it },
                    modifier = Modifier.weight(1f),
                    itemHeight = 40.dp,
                    suffix = "月"
                )

                WheelPicker(
                    items = (1..dayCount).toList(),
                    selectedIndex = dayIndex.coerceAtMost(dayCount - 1),
                    onSelectedIndexChanged = { dayIndex = it },
                    modifier = Modifier.weight(1f),
                    itemHeight = 40.dp,
                    suffix = "日"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消", color = TextTertiary)
                }
                TextButton(
                    onClick = {
                        onDateSelected(
                            yearRange[yearIndex],
                            monthRange[monthIndex],
                            dayIndex + 1
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("确定", color = Primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun WheelPicker(
    items: List<Int>,
    selectedIndex: Int,
    onSelectedIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: androidx.compose.ui.unit.Dp = 40.dp,
    suffix: String = ""
) {
    Box(
        modifier = modifier
            .height(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFAF6F0)),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            contentPadding = PaddingValues(vertical = (160.dp - itemHeight) / 2)
        ) {
            items(items.size) { index ->
                val isSelected = index == selectedIndex
                Text(
                    text = "${items[index]}$suffix",
                    fontSize = if (isSelected) 18.sp else 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) TextPrimary else TextTertiary,
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable { onSelectedIndexChanged(index) },
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        )
    }
}