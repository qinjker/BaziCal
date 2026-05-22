package com.bazical.app.ui.daily

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazical.app.domain.model.CalendarDay
import com.bazical.app.domain.repository.BaziRepository
import com.bazical.app.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DailyViewModel @Inject constructor(
    private val baziRepository: BaziRepository,
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
                val userId = userRepository.getUserId() ?: ""
                if (userId.isNotEmpty()) {
                    val result = baziRepository.getDailyDetail(userId, date)
                    result.fold(
                        onSuccess = { detail ->
                            _uiState.update {
                                it.copy(
                                    dayData = CalendarDay(
                                        date = detail.date,
                                        ganzhi = listOf(detail.ganzhi[0].toString(), detail.ganzhi[1].toString()),
                                        wuxing = "",
                                        yi = emptyList(),
                                        ji = emptyList(),
                                        star = "",
                                        luck = "",
                                        shishen = detail.shishen,
                                        jieqi = null,
                                        lunarDate = null,
                                        holiday = null,
                                        branchShishen = detail.branchShishen
                                    ),
                                    loading = false,
                                    messages = detail.messages
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.update { it.copy(error = e.message, loading = false) }
                        }
                    )
                } else {
                    _uiState.update { it.copy(error = "User not found", loading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, loading = false) }
            }
        }
    }
}