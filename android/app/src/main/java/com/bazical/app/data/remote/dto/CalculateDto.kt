package com.bazical.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "code") val code: Int,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: T?
)

@JsonClass(generateAdapter = true)
data class CalculateRequest(
    @Json(name = "name") val name: String,
    @Json(name = "birthday") val birthday: String,
    @Json(name = "hour") val hour: Int,
    @Json(name = "minute") val minute: Int,
    @Json(name = "gender") val gender: String,
    @Json(name = "device_id") val deviceId: String
)

@JsonClass(generateAdapter = true)
data class CalculateResponse(
    @Json(name = "userId") val userId: String,
    @Json(name = "bazi") val bazi: BaZiDto
)

@JsonClass(generateAdapter = true)
data class BaZiDto(
    @Json(name = "year") val year: PillarsDto,
    @Json(name = "month") val month: PillarsDto,
    @Json(name = "day") val day: PillarsDto,
    @Json(name = "hour") val hour: PillarsDto,
    @Json(name = "wuxing") val wuxing: WuXingDto,
    @Json(name = "shishen") val shishen: ShiShenDto
)

@JsonClass(generateAdapter = true)
data class PillarsDto(
    @Json(name = "stem") val stem: String,
    @Json(name = "branch") val branch: String
)

@JsonClass(generateAdapter = true)
data class WuXingDto(
    @Json(name = "mu") val mu: Int,
    @Json(name = "huo") val huo: Int,
    @Json(name = "tu") val tu: Int,
    @Json(name = "jin") val jin: Int,
    @Json(name = "shui") val shui: Int
)

@JsonClass(generateAdapter = true)
data class ShiShenDto(
    @Json(name = "year") val year: String,
    @Json(name = "month") val month: String,
    @Json(name = "day") val day: String,
    @Json(name = "hour") val hour: String
)