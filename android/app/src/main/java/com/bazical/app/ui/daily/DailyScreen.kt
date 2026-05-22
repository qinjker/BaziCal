package com.bazical.app.ui.daily

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bazical.app.ui.components.BottomTabBar
import com.bazical.app.ui.components.TabItem
import com.bazical.app.ui.theme.Primary
import com.bazical.app.ui.theme.TextPrimary
import com.bazical.app.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DailyScreen(
    date: String,
    onNavigateBack: () -> Unit,
    currentRoute: String?,
    onTabClick: (TabItem) -> Unit,
    viewModel: DailyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    val displayDate = try {
        LocalDate.parse(date).format(dateFormatter)
    } catch (e: Exception) {
        date
    }

    val lunarDate = uiState.dayData?.lunarDate ?: ""
    val ganzhiYearMonth = "2026年5月" // TODO: 计算阴历年月

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4EF))
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Navbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFAF6F0))
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = displayDate,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$lunarDate",
                        fontSize = 13.sp,
                        color = TextTertiary,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.width(60.dp))
            }

            if (uiState.loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    uiState.dayData?.let { day ->
                        // User Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(18.dp),
                                    spotColor = Color.Black.copy(alpha = 0.06f)
                                )
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        spotColor = Color.Black.copy(alpha = 0.1f)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFFC84A3E), Color(0xFFA33D33))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "十",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "日主：${day.shishen}",
                                    fontSize = 15.sp,
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

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = day.shishen,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFD4A843)
                                )
                                Text(
                                    text = "今日能量",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB8A892),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        // Share Main Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = Color(0xFFC84A3E).copy(alpha = 0.3f)
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFFC84A3E), Color(0xFFA33D33))
                                    )
                                )
                                .clickable { }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "✨ 生成分享卡",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "分享今日能量给朋友",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Ganzhi Section
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
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color.White, Color(0xFFFAF8F4))
                                    )
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.ganzhi.joinToString(""),
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC84A3E),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Text(
                                    text = "$displayDate · ${day.ganzhi.joinToString("")}月",
                                    fontSize = 14.sp,
                                    color = TextTertiary,
                                    modifier = Modifier.padding(bottom = 18.dp)
                                )

                                // Ten God Tags
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val shishenTags = listOf(day.shishen, day.branchShishen).filter { it.isNotEmpty() }
                                    shishenTags.take(2).forEach { tag ->
                                        TenGodTag(tag, "gold")
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val remainingTags = listOf(day.branchShishen).filter { it.isNotEmpty() }
                                    remainingTags.forEach { tag ->
                                        TenGodTag(tag, "green")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    } // End of dayData?.let

                    // Message Card - Always show (independent of dayData)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(22.dp),
                                spotColor = Color.Black.copy(alpha = 0.08f)
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .padding(26.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💬",
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "今日寄语",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFB8A892),
                                    letterSpacing = 2.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Messages with elegant layout
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                uiState.messages.forEach { message ->
                                    Text(
                                        text = "「$message」",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary,
                                        lineHeight = 30.sp,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = Color.Black.copy(alpha = 0.08f)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ActionItem(
                                icon = Icons.Default.Share,
                                label = "分享",
                                isPrimary = true,
                                onClick = { }
                            )
                            ActionItem(
                                icon = Icons.Default.Save,
                                label = "保存",
                                isPrimary = false,
                                onClick = { }
                            )
                            ActionItem(
                                icon = Icons.Default.CalendarToday,
                                label = "日历",
                                isPrimary = false,
                                onClick = onNavigateBack
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        BottomTabBar(
            currentRoute = currentRoute,
            onTabClick = onTabClick
        )
    }
}

@Composable
private fun TenGodTag(text: String, colorType: String) {
    val backgroundColor = when (colorType) {
        "gold" -> Brush.linearGradient(colors = listOf(Color(0xFFE0B850), Color(0xFFC49A3A)))
        "green" -> Brush.linearGradient(colors = listOf(Color(0xFF689A78), Color(0xFF4A7A5A)))
        "red" -> Brush.linearGradient(colors = listOf(Color(0xFFD65A4E), Color(0xFFB8443A)))
        else -> Brush.linearGradient(colors = listOf(Color(0xFFE0B850), Color(0xFFC49A3A)))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun ActionItem(
    icon: ImageVector,
    label: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isPrimary) Brush.linearGradient(colors = listOf(Color(0xFFC84A3E), Color(0xFFA33D33)))
                    else Brush.linearGradient(colors = listOf(Color(0xFFFAF6F0), Color(0xFFF0EBE3)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPrimary) Color.White else TextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isPrimary) Color(0xFFC84A3E) else Color(0xFF5A4A3A)
        )
    }
}