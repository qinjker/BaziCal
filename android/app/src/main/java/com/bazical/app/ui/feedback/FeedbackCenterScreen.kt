package com.bazical.app.ui.feedback

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bazical.app.domain.model.Feedback
import com.bazical.app.domain.model.FeedbackStatus
import com.bazical.app.domain.model.FeedbackType
import com.bazical.app.ui.components.BottomTabBar
import com.bazical.app.ui.components.TabItem
import com.bazical.app.ui.theme.*

@Composable
fun FeedbackCenterScreen(
    onNavigateToDetail: (String) -> Unit,
    currentRoute: String? = null,
    onTabClick: ((TabItem) -> Unit)? = null,
    viewModel: FeedbackCenterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            viewModel.resetSubmitSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .imePadding()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // 顶部 Tab 切换
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .padding(horizontal = 20.dp)
            ) {
                TabButton(
                    text = "📋 我的反馈",
                    isSelected = uiState.selectedTab == FeedbackTab.LIST,
                    onClick = { viewModel.selectTab(FeedbackTab.LIST) },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "✏️ 提交反馈",
                    isSelected = uiState.selectedTab == FeedbackTab.SUBMIT,
                    onClick = { viewModel.selectTab(FeedbackTab.SUBMIT) },
                    modifier = Modifier.weight(1f)
                )
            }

            when (uiState.selectedTab) {
                FeedbackTab.LIST -> {
                    FeedbackListContent(
                        feedbacks = uiState.feedbacks,
                        isLoading = uiState.isLoading,
                        onItemClick = onNavigateToDetail,
                        onRetry = { viewModel.loadFeedbacks() }
                    )
                }
                FeedbackTab.SUBMIT -> {
                    SubmitFeedbackContent(
                        uiState = uiState,
                        onTypeSelect = { viewModel.selectFeedbackType(it) },
                        onContentChange = { viewModel.updateFeedbackContent(it) },
                        onContactChange = { viewModel.updateFeedbackContact(it) },
                        onSubmit = { viewModel.submitFeedback() }
                    )
                }
            }
        }

        // 底部 TabBar
        if (currentRoute != null && onTabClick != null) {
            BottomTabBar(
                currentRoute = currentRoute,
                onTabClick = onTabClick
            )
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .background(
                        color = Primary,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
private fun FeedbackListContent(
    feedbacks: List<Feedback>,
    isLoading: Boolean,
    onItemClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        }
        feedbacks.isEmpty() -> {
            EmptyFeedbackState(onRetry = onRetry)
        }
        else -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(feedbacks) { feedback ->
                    FeedbackItem(
                        feedback = feedback,
                        onClick = { onItemClick(feedback.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackItem(
    feedback: Feedback,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .clickable { onClick() }
            .padding(18.dp)
    ) {
        Column {
            // 类型 + 时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypeBadge(type = feedback.type)
                Text(
                    text = formatTimeAgo(feedback.createdAt),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 内容预览
            Text(
                text = feedback.content,
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 最新回复预览 + 未读标记
            if (feedback.replies.isNotEmpty()) {
                val lastReply = feedback.replies.last()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Background,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = if (lastReply.isFromAdmin)
                                    Primary
                                else Success,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (lastReply.isFromAdmin) "👨‍💼" else "👤",
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lastReply.authorName,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = lastReply.content,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (feedback.hasUnreadReplies) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = Primary,
                                    shape = CircleShape
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 底部：回复数 + 状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💬 ${feedback.replyCount}条回复",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                StatusBadge(status = feedback.status)
            }
        }
    }
}

@Composable
private fun TypeBadge(type: FeedbackType) {
    val (bgColor, textColor) = when (type) {
        FeedbackType.功能建议 -> SuccessBg to Success
        FeedbackType.问题反馈 -> ErrorBg to Primary
        FeedbackType.体验优化 -> InfoBg to Info
        FeedbackType.其他 -> Background to TextSecondary
    }

    Box(
        modifier = Modifier
            .background(color = bgColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = type.displayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun StatusBadge(status: FeedbackStatus) {
    val (bgColor, textColor) = when (status) {
        FeedbackStatus.pending -> WarningBg to Warning
        FeedbackStatus.reviewed -> InfoBg to Info
        FeedbackStatus.replied -> SuccessBg to Success
        FeedbackStatus.closed -> Background to TextSecondary
    }

    Box(
        modifier = Modifier
            .background(color = bgColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.displayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun EmptyFeedbackState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "💬", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "还没有提交过反馈",
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("立即反馈", fontSize = 14.sp)
        }
    }
}

@Composable
private fun SubmitFeedbackContent(
    uiState: FeedbackCenterUiState,
    onTypeSelect: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onContactChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 表单卡片
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Surface)
                .padding(18.dp)
        ) {
            // 类型选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeedbackTypeItem.entries.forEach { typeItem ->
                    TypeButton(
                        typeItem = typeItem,
                        isSelected = uiState.selectedType == typeItem.displayName,
                        onClick = { onTypeSelect(typeItem.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 内容输入
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        color = Background,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {
                BasicTextField(
                    value = uiState.feedbackContent,
                    onValueChange = onContentChange,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(Primary),
                    modifier = Modifier.fillMaxSize(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.feedbackContent.isEmpty()) {
                                Text(
                                    text = "请描述您的建议或遇到的问题...",
                                    fontSize = 15.sp,
                                    color = TextSecondary
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
            Text(
                text = "${uiState.contentCharCount}/500",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 联系方式
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "联系方式",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = uiState.feedbackContact,
                    onValueChange = onContactChange,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        color = TextPrimary
                    ),
                    cursorBrush = SolidColor(Primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(
                            color = Background,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 14.dp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (uiState.feedbackContact.isEmpty()) {
                                Text(
                                    text = "选填",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 提交按钮
        Button(
            onClick = onSubmit,
            enabled = !uiState.isSubmitting && uiState.feedbackContent.length >= 10,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                disabledContainerColor = Disabled
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Surface,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "提交反馈",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 错误提示
        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                fontSize = 12.sp,
                color = Primary
            )
        }
    }
}

@Composable
private fun TypeButton(
    typeItem: FeedbackTypeItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                color = if (isSelected) Primary
                else Background
            )
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = typeItem.icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = typeItem.displayName,
            fontSize = 11.sp,
            color = if (isSelected) Surface
            else TextSecondary
        )
    }
}

private fun formatTimeAgo(dateString: String): String {
    // Simplified time formatting
    return try {
        dateString.substring(5, 10).replace("-", "月") + "日"
    } catch (e: Exception) {
        dateString
    }
}