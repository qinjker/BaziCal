package com.bazical.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FeedbackRequest(
    @Json(name = "type") val type: String,
    @Json(name = "content") val content: String,
    @Json(name = "contact") val contact: String? = null,
    @Json(name = "device_id") val deviceId: String
)

@JsonClass(generateAdapter = true)
data class ReplyRequest(
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class FeedbackDto(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "content") val content: String,
    @Json(name = "status") val status: String,
    @Json(name = "user_id") val userId: String? = null,
    @Json(name = "device_id") val deviceId: String? = null,
    @Json(name = "contact") val contact: String? = null,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "replies") val replies: List<ReplyDto>? = null
)

@JsonClass(generateAdapter = true)
data class ReplyDto(
    @Json(name = "id") val id: String,
    @Json(name = "content") val content: String,
    @Json(name = "author_type") val authorType: String,
    @Json(name = "author_name") val authorName: String,
    @Json(name = "created_at") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class FeedbackListResponse(
    @Json(name = "feedbacks") val feedbacks: List<FeedbackDto>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "pageSize") val pageSize: Int
)

@JsonClass(generateAdapter = true)
data class RepliesResponse(
    @Json(name = "replies") val replies: List<ReplyDto>
)

@JsonClass(generateAdapter = true)
data class EmptyResponse(
    @Json(name = "success") val success: Boolean = true
)