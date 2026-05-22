package com.bazical.app.ui.feedback

import com.bazical.app.domain.model.Feedback
import com.bazical.app.domain.model.Reply

data class FeedbackDetailUiState(
    val feedback: Feedback? = null,
    val replies: List<Reply> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val replyContent: String = "",
    val errorMessage: String? = null
)