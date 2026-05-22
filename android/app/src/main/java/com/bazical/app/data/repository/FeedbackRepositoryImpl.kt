package com.bazical.app.data.repository

import com.bazical.app.data.remote.api.BaziApi
import com.bazical.app.data.remote.dto.FeedbackRequest
import com.bazical.app.data.remote.dto.ReplyRequest
import com.bazical.app.domain.model.Feedback
import com.bazical.app.domain.model.FeedbackStatus
import com.bazical.app.domain.model.FeedbackType
import com.bazical.app.domain.model.Reply
import com.bazical.app.domain.repository.FeedbackRepository
import com.bazical.app.utils.DeviceId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepositoryImpl @Inject constructor(
    private val api: BaziApi
) : FeedbackRepository {

    override suspend fun submitFeedback(
        type: String,
        content: String,
        contact: String?,
        deviceId: String
    ): Result<Unit> {
        return try {
            val request = FeedbackRequest(
                type = type,
                content = content,
                contact = contact,
                deviceId = deviceId
            )
            val response = api.submitFeedback(request)
            if (response.code == 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyFeedbacks(
        deviceId: String,
        page: Int,
        pageSize: Int
    ): Result<List<Feedback>> {
        return try {
            val response = api.getMyFeedbacks(deviceId, page, pageSize)
            val feedbacks = response.data?.feedbacks?.map { dto ->
                Feedback(
                    id = dto.id,
                    type = FeedbackType.fromString(dto.type),
                    content = dto.content,
                    status = FeedbackStatus.fromString(dto.status),
                    createdAt = dto.createdAt,
                    contact = dto.contact,
                    replies = dto.replies?.map { replyDto ->
                        Reply(
                            id = replyDto.id,
                            content = replyDto.content,
                            isFromAdmin = replyDto.authorType == "admin",
                            authorName = replyDto.authorName,
                            createdAt = replyDto.createdAt
                        )
                    } ?: emptyList()
                )
            } ?: emptyList()
            Result.success(feedbacks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFeedbackDetail(id: String): Result<Feedback> {
        return try {
            val response = api.getFeedbackDetail(id)
            val dto = response.data ?: throw Exception(response.message)
            val feedback = Feedback(
                id = dto.id,
                type = FeedbackType.fromString(dto.type),
                content = dto.content,
                status = FeedbackStatus.fromString(dto.status),
                createdAt = dto.createdAt,
                contact = dto.contact,
                replies = dto.replies?.map { replyDto ->
                    Reply(
                        id = replyDto.id,
                        content = replyDto.content,
                        isFromAdmin = replyDto.authorType == "admin",
                        authorName = replyDto.authorName,
                        createdAt = replyDto.createdAt
                    )
                } ?: emptyList()
            )
            Result.success(feedback)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addReply(feedbackId: String, content: String): Result<Unit> {
        return try {
            val request = ReplyRequest(content = content)
            val response = api.addFeedbackReply(feedbackId, request)
            if (response.code == 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}