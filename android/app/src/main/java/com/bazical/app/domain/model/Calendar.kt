package com.bazical.app.domain.model

data class CalendarDay(
    val date: String,
    val ganzhi: List<String>,
    val wuxing: String,
    val yi: List<String>,
    val ji: List<String>,
    val star: String,
    val luck: String,
    val shishen: String,
    val jieqi: String?,
    val lunarDate: String?,
    val holiday: String?,
    val branchShishen: String
)

data class CalendarMonth(
    val year: Int,
    val month: Int,
    val days: List<CalendarDay>
)