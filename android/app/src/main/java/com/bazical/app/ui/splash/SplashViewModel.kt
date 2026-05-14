package com.bazical.app.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazical.app.domain.usecase.HasUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val hasUserDataUseCase: HasUserDataUseCase
) : ViewModel() {

    private val _hasUserData = MutableStateFlow<Boolean?>(null)
    val hasUserData: StateFlow<Boolean?> = _hasUserData.asStateFlow()

    init {
        checkUserData()
    }

    private fun checkUserData() {
        viewModelScope.launch {
            _hasUserData.value = hasUserDataUseCase()
        }
    }
}