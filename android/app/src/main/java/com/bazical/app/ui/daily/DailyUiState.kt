package com.bazical.app.ui.daily

import com.bazical.app.domain.model.CalendarDay

data class DailyUiState(
    val date: String = "",
    val dayData: CalendarDay? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val messages: List<String> = emptyList()
)