package com.bazical.app.ui.feedback

import com.bazical.app.domain.model.Feedback

data class FeedbackCenterUiState(
    val selectedTab: FeedbackTab = FeedbackTab.LIST,
    val feedbacks: List<Feedback> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null,
    // Submit form state
    val selectedType: String = "功能建议",
    val feedbackContent: String = "",
    val feedbackContact: String = "",
    val contentCharCount: Int = 0
)

enum class FeedbackTab {
    LIST, SUBMIT
}

enum class FeedbackTypeItem(val displayName: String, val icon: String) {
    SUGGESTION("功能建议", "💡"),
    BUG("问题反馈", "🐛"),
    OPTIMIZATION("体验优化", "✨"),
    OTHER("其他", "📌");

    companion object {
        fun fromDisplayName(name: String): FeedbackTypeItem {
            return entries.find { it.displayName == name } ?: SUGGESTION
        }
    }
}