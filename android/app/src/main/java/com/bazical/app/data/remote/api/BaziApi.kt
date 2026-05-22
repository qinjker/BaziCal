package com.bazical.app.data.remote.api

import com.bazical.app.data.remote.dto.ApiResponse
import com.bazical.app.data.remote.dto.CalculateRequest
import com.bazical.app.data.remote.dto.CalculateResponse
import com.bazical.app.data.remote.dto.CalendarResponse
import com.bazical.app.data.remote.dto.FeedbackListResponse
import com.bazical.app.data.remote.dto.FeedbackRequest
import com.bazical.app.data.remote.dto.FeedbackDto
import com.bazical.app.data.remote.dto.ReplyRequest
import com.bazical.app.data.remote.dto.RepliesResponse
import com.bazical.app.data.remote.dto.SolarToLunarResponse
import com.bazical.app.data.remote.dto.DailyDetailResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BaziApi {

    @POST("api/v1/bazi/calculate")
    suspend fun calculateBazi(@Body request: CalculateRequest): ApiResponse<CalculateResponse>

    @GET("api/v1/bazi/calendar")
    suspend fun getCalendar(
        @Query("userId") userId: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): ApiResponse<CalendarResponse>

    @GET("api/v1/bazi/solar-to-lunar")
    suspend fun solarToLunar(@Query("date") date: String): ApiResponse<SolarToLunarResponse>

    @GET("api/v1/bazi/daily/{date}")
    suspend fun getDailyDetail(
        @Path("date") date: String,
        @Query("userId") userId: String
    ): ApiResponse<DailyDetailResponse>

    // ==================== 反馈相关 API ====================

    @POST("api/v1/feedback")
    suspend fun submitFeedback(@Body request: FeedbackRequest): ApiResponse<Map<String, Any>>

    @GET("api/v1/feedbacks")
    suspend fun getMyFeedbacks(
        @Query("device_id") deviceId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<FeedbackListResponse>

    @GET("api/v1/feedbacks/{id}")
    suspend fun getFeedbackDetail(@Path("id") id: String): ApiResponse<FeedbackDto>

    @GET("api/v1/feedbacks/{id}/replies")
    suspend fun getFeedbackReplies(@Path("id") id: String): ApiResponse<RepliesResponse>

    @POST("api/v1/feedbacks/{id}/replies")
    suspend fun addFeedbackReply(
        @Path("id") id: String,
        @Body request: ReplyRequest
    ): ApiResponse<Map<String, Any>>
}