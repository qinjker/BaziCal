package com.bazical.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SolarToLunarResponse(
    @Json(name = "lunarYear") val lunarYear: Int,
    @Json(name = "lunarMonth") val lunarMonth: Int,
    @Json(name = "lunarDay") val lunarDay: Int,
    @Json(name = "isLeapMonth") val isLeapMonth: Boolean
)