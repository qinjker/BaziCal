package com.bazical.app.domain.model

data class Feedback(
    val id: String,
    val type: FeedbackType,
    val content: String,
    val status: FeedbackStatus,
    val createdAt: String,
    val contact: String? = null,
    val replies: List<Reply> = emptyList()
) {
    val hasUnreadReplies: Boolean
        get() = replies.isNotEmpty() && replies.any { !it.isFromAdmin }

    val replyCount: Int
        get() = replies.size
}

data class Reply(
    val id: String,
    val content: String,
    val isFromAdmin: Boolean,
    val authorName: String,
    val createdAt: String
)

enum class FeedbackType(val displayName: String) {
    功能建议("功能建议"),
    问题反馈("问题反馈"),
    体验优化("体验优化"),
    其他("其他");

    companion object {
        fun fromString(value: String): FeedbackType {
            return entries.find { it.displayName == value || it.name == value } ?: 其他
        }
    }
}

enum class FeedbackStatus(val displayName: String) {
    pending("待处理"),
    reviewed("已查看"),
    replied("已回复"),
    closed("已关闭");

    companion object {
        fun fromString(value: String): FeedbackStatus {
            return entries.find { it.name == value } ?: pending
        }
    }
}