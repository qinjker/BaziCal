package com.bazical.app.ui.feedback

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bazical.app.domain.model.Feedback
import com.bazical.app.domain.model.FeedbackStatus
import com.bazical.app.domain.model.FeedbackType
import com.bazical.app.domain.model.Reply
import com.bazical.app.ui.theme.Color

@Composable
fun FeedbackDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: FeedbackDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.replies.size) {
        if (uiState.replies.isNotEmpty()) {
            listState.animateScrollToItem(uiState.replies.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Background)
            .imePadding()
    ) {
        // 导航栏
        NavBar(
            feedback = uiState.feedback,
            onNavigateBack = onNavigateBack
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.Primary)
                }
            }
            uiState.feedback != null -> {
                // 对话区域
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 原始反馈卡片
                    item {
                        OriginalFeedbackCard(feedback = uiState.feedback!!)
                    }

                    // 回复列表
                    items(uiState.replies) { reply ->
                        ReplyBubble(reply = reply)
                    }
                }

                // 底部输入框
                InputArea(
                    content = uiState.replyContent,
                    isSending = uiState.isSending,
                    onContentChange = { viewModel.updateReplyContent(it) },
                    onSend = { viewModel.sendReply() }
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "加载失败",
                        color = Color.Primary
                    )
                }
            }
        }
    }
}

@Composable
private fun NavBar(
    feedback: Feedback?,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Background)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "←",
                fontSize = 20.sp,
                color = Color.TextPrimary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "反馈详情",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.TextPrimary,
            modifier = Modifier.weight(1f)
        )

        feedback?.let {
            StatusBadge(status = it.status)
        }
    }
}

@Composable
private fun StatusBadge(status: FeedbackStatus) {
    val (bgColor, textColor) = when (status) {
        FeedbackStatus.pending -> Color.WarningBg to Color.Warning
        FeedbackStatus.reviewed -> Color.InfoBg to Color.Info
        FeedbackStatus.replied -> Color.SuccessBg to Color.Success
        FeedbackStatus.closed -> Color.Background to Color.TextSecondary
    }

    Box(
        modifier = Modifier
            .background(color = bgColor, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = status.displayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun OriginalFeedbackCard(feedback: Feedback) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Surface)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TypeBadge(type = feedback.type)
            Text(
                text = formatDateTime(feedback.createdAt),
                fontSize = 12.sp,
                color = Color.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = feedback.content,
            fontSize = 15.sp,
            color = Color.TextPrimary,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun TypeBadge(type: FeedbackType) {
    val (bgColor, textColor) = when (type) {
        FeedbackType.功能建议 -> Color.SuccessBg to Color.Success
        FeedbackType.问题反馈 -> Color.ErrorBg to Color.Primary
        FeedbackType.体验优化 -> Color.InfoBg to Color.Info
        FeedbackType.其他 -> Color.Background to Color.TextSecondary
    }

    Box(
        modifier = Modifier
            .background(color = bgColor, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = type.displayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun ReplyBubble(reply: Reply) {
    val isAdmin = reply.isFromAdmin

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAdmin) Arrangement.End else Arrangement.Start
    ) {
        if (!isAdmin) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color.Success,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👤", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = reply.authorName,
                fontSize = 11.sp,
                color = if (isAdmin) Color.TextSecondary else Color.TextSecondary,
                modifier = Modifier.padding(start = if (isAdmin) 0.dp else 0.dp, bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .background(
                        color = if (isAdmin) Color.Primary else Color.Surface,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isAdmin) 16.dp else 4.dp,
                            bottomEnd = if (isAdmin) 4.dp else 16.dp
                        )
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = reply.content,
                    fontSize = 14.sp,
                    color = if (isAdmin) Color.Surface else Color.TextPrimary,
                    lineHeight = 20.sp
                )
            }

            Text(
                text = formatDateTime(reply.createdAt),
                fontSize = 10.sp,
                color = Color.TextSecondary,
                modifier = Modifier.padding(top = 6.dp, start = if (isAdmin) 0.dp else 0.dp),
                textAlign = if (isAdmin) Alignment.End else Alignment.Start
            )
        }

        if (isAdmin) {
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = Color.Primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👨‍💼", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun InputArea(
    content: String,
    isSending: Boolean,
    onContentChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value = content,
            onValueChange = onContentChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 15.sp,
                color = Color.TextPrimary
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Primary),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(
                    color = Color.Background,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (content.isEmpty()) {
                        Text(
                            text = "输入回复内容...",
                            fontSize = 15.sp,
                            color = Color.TextSecondary
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    color = if (content.isNotBlank()) Color.Primary else Color.Disabled
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Surface,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "➤",
                    fontSize = 20.sp,
                    color = Color.Surface
                )
            }
        }
    }
}

private fun formatDateTime(dateString: String): String {
    return try {
        // Format: 2024-05-18T14:32:00 -> 5月18日 14:32
        val datePart = dateString.substring(5, 10)
        val timePart = dateString.substring(11, 16)
        "${datePart.replace("-", "月")}日 $timePart"
    } catch (e: Exception) {
        dateString
    }
}