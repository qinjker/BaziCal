package com.bazical.app.data.remote.api

import com.bazical.app.data.remote.dto.CalculateRequest
import com.bazical.app.data.remote.dto.CalculateResponse
import com.bazical.app.data.remote.dto.CalendarResponse
import com.bazical.app.data.remote.dto.SolarToLunarResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BaziApi {

    @POST("api/v1/bazi/calculate")
    suspend fun calculateBazi(@Body request: CalculateRequest): CalculateResponse

    @GET("api/v1/bazi/calendar")
    suspend fun getCalendar(
        @Query("userId") userId: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): CalendarResponse

    @GET("api/v1/bazi/solar-to-lunar")
    suspend fun solarToLunar(@Query("date") date: String): SolarToLunarResponse
}