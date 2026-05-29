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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

                // Date pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Year picker
                    DatePickerField(
                        value = uiState.birthday?.let {
                            SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "",
                        displayValue = uiState.birthday?.let {
                            SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "年",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.showDatePicker() }
                    )

                    // Month picker
                    DatePickerField(
                        value = uiState.birthday?.let {
                            SimpleDateFormat("MM", Locale.getDefault()).format(Date(it)).padStart(2, '0')
                        } ?: "",
                        displayValue = uiState.birthday?.let {
                            SimpleDateFormat("MM", Locale.getDefault()).format(Date(it)).padStart(2, '0')
                        } ?: "月",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.showDatePicker() }
                    )

                    // Day picker
                    DatePickerField(
                        value = uiState.birthday?.let {
                            SimpleDateFormat("dd", Locale.getDefault()).format(Date(it)).padStart(2, '0')
                        } ?: "",
                        displayValue = uiState.birthday?.let {
                            SimpleDateFormat("dd", Locale.getDefault()).format(Date(it)).padStart(2, '0')
                        } ?: "日",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.showDatePicker() }
                    )
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
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.birthday ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { viewModel.hideDatePicker() },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { viewModel.updateBirthday(it) }
                        viewModel.hideDatePicker()
                    }
                ) {
                    Text("确定", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDatePicker() }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFAF6F0))
            .border(1.5.dp, Color(0xFFE8E0D5), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayValue,
            fontSize = 16.sp,
            color = TextPrimary
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