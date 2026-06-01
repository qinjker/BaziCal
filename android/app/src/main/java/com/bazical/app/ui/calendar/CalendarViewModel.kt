package com.bazical.app.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazical.app.domain.usecase.CalculateBaziUseCase
import com.bazical.app.domain.usecase.GetCalendarUseCase
import com.bazical.app.domain.usecase.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private const val TAG = "CalendarViewModel"

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getCalendarUseCase: GetCalendarUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val calculateBaziUseCase: CalculateBaziUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val calendar = Calendar.getInstance()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        calendar.time = java.util.Date()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        _uiState.update { it.copy(year = year, month = month) }
        loadCalendar(year, month)
        loadUserBaZi()
    }

    fun loadCalendar(year: Int, month: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, year = year, month = month) }
            Log.d(TAG, "loadCalendar: year=$year, month=$month")
            val result = getCalendarUseCase(year, month)
            result.fold(
                onSuccess = { calendarMonth ->
                    Log.d(TAG, "loadCalendar success: ${calendarMonth.days.size} days")
                    if (calendarMonth.days.isNotEmpty()) {
                        val firstDay = calendarMonth.days.first()
                        Log.d(TAG, "First day: date=${firstDay.date}, ganzhi=${firstDay.ganzhi}, shishen=${firstDay.shishen}, branchShishen=${firstDay.branchShishen}, lunarDate=${firstDay.lunarDate}")
                    } else {
                        Log.w(TAG, "loadCalendar: days is EMPTY!")
                    }
                    _uiState.update { it.copy(days = calendarMonth.days, loading = false) }
                },
                onFailure = { e ->
                    Log.e(TAG, "loadCalendar failure: ${e.message}", e)
                    _uiState.update { it.copy(error = e.message, loading = false) }
                }
            )
        }
    }

    private fun loadUserBaZi() {
        viewModelScope.launch {
            val user = getUserUseCase()
            if (user != null) {
                val result = calculateBaziUseCase(user)
                if (result.isSuccess) {
                    val bazi = result.getOrNull()?.second
                    if (bazi != null) {
                        _uiState.update {
                            it.copy(
                                bazi = bazi,
                                dayStem = bazi.day.stem,
                                dayBranch = bazi.day.branch,
                                userBirthday = user.birthday,
                                monthBranch = bazi.month.branch,
                                yearBranch = bazi.year.branch,
                                monthShishen = bazi.shishen.month
                            )
                        }
                    }
                }
            }
        }
    }

    fun previousMonth() {
        val state = _uiState.value
        var month = state.month - 1
        var year = state.year
        if (month < 1) {
            month = 12
            year -= 1
        }
        loadCalendar(year, month)
    }

    fun nextMonth() {
        val state = _uiState.value
        var month = state.month + 1
        var year = state.year
        if (month > 12) {
            month = 1
            year += 1
        }
        loadCalendar(year, month)
    }

    fun goToToday() {
        calendar.time = java.util.Date()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        loadCalendar(year, month)
    }
}