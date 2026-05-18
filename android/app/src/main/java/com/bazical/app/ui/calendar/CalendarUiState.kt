package com.bazical.app.ui.calendar

import com.bazical.app.domain.model.BaZi
import com.bazical.app.domain.model.CalendarDay

data class CalendarUiState(
    val year: Int = 0,
    val month: Int = 0,
    val bazi: BaZi? = null,
    val days: List<CalendarDay> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    // Additional display fields
    val dayStem: String = "",
    val dayBranch: String = "",
    val userBirthday: String = "",
    val monthBranch: String? = null,
    val yearBranch: String? = null,
    val monthShishen: String? = null
)