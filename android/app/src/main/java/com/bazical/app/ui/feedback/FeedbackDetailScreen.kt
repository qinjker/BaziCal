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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bazical.app.domain.model.Feedback
import com.bazical.app.domain.model.FeedbackStatus
import com.bazical.app.domain.model.FeedbackType
import com.bazical.app.domain.model.Reply
import com.bazical.app.ui.theme.*

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
            .background(Background)
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
                    CircularProgressIndicator(color = Primary)
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
                        color = Primary
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
            .background(Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Background)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "←",
                fontSize = 20.sp,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "反馈详情",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
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
        FeedbackStatus.pending -> WarningBg to Warning
        FeedbackStatus.reviewed -> InfoBg to Info
        FeedbackStatus.replied -> SuccessBg to Success
        FeedbackStatus.closed -> Background to TextSecondary
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
            .background(Surface)
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
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = feedback.content,
            fontSize = 15.sp,
            color = TextPrimary,
            lineHeight = 24.sp
        )
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
                        color = Success,
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
                color = if (isAdmin) TextSecondary else TextSecondary,
                modifier = Modifier.padding(start = if (isAdmin) 0.dp else 0.dp, bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .background(
                        color = if (isAdmin) Primary else Surface,
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
                    color = if (isAdmin) Surface else TextPrimary,
                    lineHeight = 20.sp
                )
            }

            Text(
                text = formatDateTime(reply.createdAt),
                fontSize = 10.sp,
                color = TextSecondary,
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
                        color = Primary,
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
            .background(Surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value = content,
            onValueChange = onContentChange,
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 15.sp,
                color = TextPrimary
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Primary),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(
                    color = Background,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (content.isEmpty()) {
                        Text(
                            text = "输入回复内容...",
                            fontSize = 15.sp,
                            color = TextSecondary
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
                    color = if (content.isNotBlank()) Primary else Disabled
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Surface,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "➤",
                    fontSize = 20.sp,
                    color = Surface
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