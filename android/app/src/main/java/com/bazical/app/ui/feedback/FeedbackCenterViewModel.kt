package com.bazical.app.ui.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazical.app.domain.repository.FeedbackRepository
import com.bazical.app.utils.DeviceId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackCenterViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackCenterUiState())
    val uiState: StateFlow<FeedbackCenterUiState> = _uiState.asStateFlow()

    init {
        loadFeedbacks()
    }

    fun loadFeedbacks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val deviceId = DeviceId.getDeviceId()
            feedbackRepository.getMyFeedbacks(deviceId)
                .onSuccess { feedbacks ->
                    _uiState.update {
                        it.copy(
                            feedbacks = feedbacks,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "加载失败"
                        )
                    }
                }
        }
    }

    fun selectTab(tab: FeedbackTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun selectFeedbackType(type: String) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun updateFeedbackContent(content: String) {
        _uiState.update {
            it.copy(
                feedbackContent = content,
                contentCharCount = content.length
            )
        }
    }

    fun updateFeedbackContact(contact: String) {
        _uiState.update { it.copy(feedbackContact = contact) }
    }

    fun submitFeedback() {
        val state = _uiState.value
        if (state.feedbackContent.length < 10) {
            _uiState.update { it.copy(errorMessage = "反馈内容至少10个字") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val deviceId = DeviceId.getDeviceId()
            feedbackRepository.submitFeedback(
                type = state.selectedType,
                content = state.feedbackContent,
                contact = state.feedbackContact.ifBlank { null },
                deviceId = deviceId
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitSuccess = true,
                            selectedTab = FeedbackTab.LIST,
                            feedbackContent = "",
                            feedbackContact = "",
                            contentCharCount = 0
                        )
                    }
                    loadFeedbacks()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "提交失败"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetSubmitSuccess() {
        _uiState.update { it.copy(submitSuccess = false) }
    }
}