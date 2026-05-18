package com.bazical.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SolarToLunarResponse(
    @Json(name = "year") val lunarYear: Int,
    @Json(name = "month") val lunarMonth: Int,
    @Json(name = "day") val lunarDay: Int
)