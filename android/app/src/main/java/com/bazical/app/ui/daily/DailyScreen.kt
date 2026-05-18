package com.bazical.app.ui.daily

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyScreen(
    date: String,
    onNavigateBack: () -> Unit,
    viewModel: DailyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    val displayDate = try {
        LocalDate.parse(date).format(dateFormatter)
    } catch (e: Exception) {
        date
    }
    val dayOfWeek = try {
        LocalDate.parse(date).dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE)
    } catch (e: Exception) {
        ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = displayDate,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = dayOfWeek,
                    fontSize = 12.sp,
                    color = TextTertiary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { /* TODO: Share */ },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "分享",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (uiState.loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            uiState.dayData?.let { day ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Main info card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = Primary.copy(alpha = 0.12f)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .padding(28.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Gan Zhi display
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val stemColor = getStemColor(day.ganzhi.getOrNull(0)?.toString() ?: "")
                                val branchColor = getBranchColor(day.ganzhi.getOrNull(1)?.toString() ?: "")

                                Text(
                                    text = day.ganzhi.getOrNull(0)?.toString() ?: "",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = stemColor
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(
                                    text = day.ganzhi.getOrNull(1)?.toString() ?: "",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = branchColor
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Wu Xing
                            Text(
                                text = "五行：${day.wuxing}",
                                fontSize = 15.sp,
                                color = Secondary,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Two columns of info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                InfoItem("星宿", day.star)
                                InfoItem("吉凶", day.luck, if (day.luck == "吉") Success else Warning)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Tags section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text(
                                text = "今日十神",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextTertiary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 主十神标签
                                TenGodChip(
                                    text = day.shishen,
                                    color = Primary,
                                    isPrimary = true
                                )

                                // 地支十神标签
                                if (day.branchShishen.isNotEmpty()) {
                                    TenGodChip(
                                        text = day.branchShishen,
                                        color = Secondary,
                                        isPrimary = false
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Yi section
                    YiJiCard(
                        title = "宜",
                        icon = "✅",
                        items = day.yi,
                        backgroundColor = Color(0xFFF0FFF4),
                        accentColor = Success
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ji section
                    YiJiCard(
                        title = "忌",
                        icon = "⚠️",
                        items = day.ji,
                        backgroundColor = Color(0xFFFFF9F5),
                        accentColor = Warning
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Messages section
                    if (uiState.messages.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp)
                            ) {
                                Text(
                                    text = "正向寄语",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextTertiary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                uiState.messages.forEach { message ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFFAF6F0))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = message,
                                            fontSize = 14.sp,
                                            color = TextPrimary,
                                            lineHeight = 22.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, color: Color = TextPrimary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextTertiary
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun TenGodChip(text: String, color: Color, isPrimary: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPrimary) color.copy(alpha = 0.12f) else color.copy(alpha = 0.08f)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isPrimary) color else TextSecondary
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun YiJiCard(
    title: String,
    icon: String,
    items: List<String>,
    backgroundColor: Color,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(backgroundColor)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
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