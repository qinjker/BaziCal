package com.bazical.app.ui.feedback

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazical.app.domain.repository.FeedbackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedbackDetailViewModel @Inject constructor(
    private val feedbackRepository: FeedbackRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val feedbackId: String = savedStateHandle.get<String>("id") ?: ""

    private val _uiState = MutableStateFlow(FeedbackDetailUiState())
    val uiState: StateFlow<FeedbackDetailUiState> = _uiState.asStateFlow()

    init {
        loadFeedbackDetail()
    }

    fun loadFeedbackDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            feedbackRepository.getFeedbackDetail(feedbackId)
                .onSuccess { feedback ->
                    _uiState.update {
                        it.copy(
                            feedback = feedback,
                            replies = feedback.replies,
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

    fun updateReplyContent(content: String) {
        _uiState.update { it.copy(replyContent = content) }
    }

    fun sendReply() {
        val content = _uiState.value.replyContent.trim()
        if (content.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, errorMessage = null) }
            feedbackRepository.addReply(feedbackId, content)
                .onSuccess {
                    _uiState.update { it.copy(isSending = false, replyContent = "") }
                    loadFeedbackDetail()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            errorMessage = error.message ?: "发送失败"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}