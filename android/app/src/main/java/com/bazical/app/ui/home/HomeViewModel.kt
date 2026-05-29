package com.bazical.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazical.app.domain.model.Gender
import com.bazical.app.domain.model.User
import com.bazical.app.domain.usecase.CalculateBaziUseCase
import com.bazical.app.domain.usecase.SolarToLunarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calculateBaziUseCase: CalculateBaziUseCase,
    private val solarToLunarUseCase: SolarToLunarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateBirthday(timestamp: Long) {
        _uiState.update { it.copy(birthday = timestamp) }
        convertSolarToLunar(timestamp)
    }

    fun updateBirthdayType(type: String) {
        _uiState.update { it.copy(birthdayType = type) }
    }

    fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun hideDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    private fun convertSolarToLunar(timestamp: Long) {
        viewModelScope.launch {
            val dateStr = dateFormat.format(Date(timestamp))
            val result = solarToLunarUseCase(dateStr)
            if (result.isSuccess) {
                _uiState.update { it.copy(lunarDate = result.getOrNull()) }
            }
        }
    }

    fun calculate() {
        val state = _uiState.value
        if (state.birthday == null) {
            _uiState.update { it.copy(error = "请选择出生日期") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }

            val birthdayStr = dateFormat.format(Date(state.birthday))
            val user = User(
                userId = "",
                name = "用户", // 默认名字
                birthday = birthdayStr,
                hour = 12, // 默认午时
                minute = state.minute,
                gender = state.gender,
                birthdayType = state.birthdayType
            )

            val result = calculateBaziUseCase(user)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(loading = false, calculated = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(loading = false, error = e.message ?: "计算失败") }
                }
            )
        }
    }
}