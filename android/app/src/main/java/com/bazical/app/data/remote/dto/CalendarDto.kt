package com.bazical.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CalendarResponse(
    @Json(name = "days") val days: List<CalendarDayDto>
)

@JsonClass(generateAdapter = true)
data class CalendarDayDto(
    @Json(name = "date") val date: String,
    @Json(name = "ganzhi") val ganzhi: String,
    @Json(name = "wuxing") val wuxing: String,
    @Json(name = "yi") val yi: List<String>,
    @Json(name = "ji") val ji: List<String>,
    @Json(name = "star") val star: String,
    @Json(name = "luck") val luck: String
)