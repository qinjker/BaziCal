package com.bazical.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bazical.app.ui.theme.TextTertiary

enum class TabItem(
    val icon: ImageVector,
    val label: String,
    val route: String
) {
    Calendar(icon = Icons.Default.CalendarMonth, label = "月历", route = "calendar"),
    Today(icon = Icons.Default.Today, label = "今日", route = "today"),
    Home(icon = Icons.Default.Home, label = "生辰", route = "home"),
    Feedback(icon = Icons.Default.Feedback, label = "反馈", route = "feedback")
}

@Composable
fun BottomTabBar(
    currentRoute: String?,
    onTabClick: (TabItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(0xFFC84A3E)
    val inactiveColor = TextTertiary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TabItem.entries.forEach { tab ->
            val isActive = when {
                tab == TabItem.Calendar && currentRoute == "calendar" -> true
                tab == TabItem.Today && currentRoute == "today" -> true
                tab == TabItem.Home && currentRoute == "home" -> true
                tab == TabItem.Feedback && currentRoute == "feedback" -> true
                else -> false
            }

            TabItemView(
                icon = tab.icon,
                label = tab.label,
                isActive = isActive,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                onClick = { onTabClick(tab) }
            )
        }
    }
}

@Composable
private fun TabItemView(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) activeColor else inactiveColor,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (isActive) activeColor else inactiveColor,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}