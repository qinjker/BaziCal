import Foundation
import SwiftUI

/// 反馈详情 UI 状态
struct FeedbackDetailUiState {
    var feedback: Feedback?
    var replies: [Reply] = []
    var isLoading: Bool = false
    var errorMessage: String?
    var replyContent: String = ""
    var isSending: Bool = false
}

/// 反馈详情视图模型
@MainActor
class FeedbackDetailViewModel: ObservableObject {
    @Published var uiState = FeedbackDetailUiState()

    private let apiService = APIService.shared
    private let feedbackId: String

    init(feedbackId: String) {
        self.feedbackId = feedbackId
        Task {
            await loadFeedbackDetail()
        }
    }

    /// 加载反馈详情
    func loadFeedbackDetail() async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        do {
            uiState.feedback = try await apiService.getFeedbackDetail(id: feedbackId)
            uiState.replies = uiState.feedback?.replies ?? []
        } catch {
            uiState.errorMessage = error.localizedDescription
        }

        uiState.isLoading = false
    }

    /// 更新回复内容
    func updateReplyContent(_ content: String) {
        uiState.replyContent = content
    }

    /// 发送回复
    func sendReply() async {
        guard !uiState.replyContent.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return
        }

        uiState.isSending = true

        do {
            let reply = try await apiService.sendFeedbackReply(
                feedbackId: feedbackId,
                content: uiState.replyContent
            )
            uiState.replies.append(reply)
            uiState.replyContent = ""
        } catch {
            uiState.errorMessage = error.localizedDescription
        }

        uiState.isSending = false
    }
}