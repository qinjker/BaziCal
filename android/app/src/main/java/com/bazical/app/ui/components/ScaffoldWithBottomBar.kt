package com.bazical.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 通用页面包装器 - 为每个页面添加底部导航栏
 */
@Composable
fun ScaffoldWithBottomBar(
    currentRoute: String?,
    onTabClick: (TabItem) -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 主内容区域
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            content()
        }

        // 底部导航栏
        BottomTabBar(
            currentRoute = currentRoute,
            onTabClick = onTabClick
        )
    }
}