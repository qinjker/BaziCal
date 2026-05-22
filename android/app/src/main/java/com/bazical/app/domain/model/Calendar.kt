package com.bazical.app.domain.model

data class CalendarDay(
    val date: String,
    val ganzhi: List<String>,  // 改为 List<String>，如 ["甲", "午"]
    val wuxing: String,
    val yi: List<String>,
    val ji: List<String>,
    val star: String,
    val luck: String,
    val shishen: String,
    val jieqi: String?,
    val lunarDate: String?,
    val holiday: String?,
    val branchShishen: String,
    val messages: List<String> = emptyList()
)

data class CalendarMonth(
    val year: Int,
    val month: Int,
    val days: List<CalendarDay>
)