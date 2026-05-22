package com.bazical.app.domain.repository

import com.bazical.app.domain.model.Feedback
import com.bazical.app.domain.model.Reply

interface FeedbackRepository {
    suspend fun submitFeedback(type: String, content: String, contact: String?, deviceId: String): Result<Unit>
    suspend fun getMyFeedbacks(deviceId: String, page: Int = 1, pageSize: Int = 20): Result<List<Feedback>>
    suspend fun getFeedbackDetail(id: String): Result<Feedback>
    suspend fun addReply(feedbackId: String, content: String): Result<Unit>
}