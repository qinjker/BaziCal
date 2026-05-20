package com.bazical.app.ui.daily

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazical.app.domain.repository.UserRepository
import com.bazical.app.domain.usecase.GetCalendarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyViewModel @Inject constructor(
    private val getCalendarUseCase: GetCalendarUseCase,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    init {
        val date = savedStateHandle.get<String>("date") ?: ""
        _uiState.update { it.copy(date = date) }
        loadUserData()
        if (date.isNotEmpty()) {
            loadDailyData(date)
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.getUser()?.let { user ->
                _uiState.update { it.copy(userBirthday = user.birthday) }
            }
        }
    }

    fun loadDailyData(date: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, date = date) }
            try {
                val localDate = LocalDate.parse(date)
                val result = getCalendarUseCase(localDate.year, localDate.monthValue)
                result.fold(
                    onSuccess = { calendarMonth ->
                        val dayData = calendarMonth.days.find { it.date == date }
                        _uiState.update { it.copy(dayData = dayData, loading = false) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(error = e.message, loading = false) }
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, loading = false) }
            }
        }
    }
}