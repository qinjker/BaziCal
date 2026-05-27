import Foundation
import SwiftUI

/// 反馈中心 UI 状态
struct FeedbackCenterUiState {
    var selectedTab: FeedbackTab = .list
    var feedbacks: [Feedback] = []
    var isLoading: Bool = false
    var errorMessage: String?
    var selectedType: String = "功能建议"
    var feedbackContent: String = ""
    var feedbackContact: String = ""
    var isSubmitting: Bool = false
    var submitSuccess: Bool = false
}

/// 反馈中心 Tab
enum FeedbackTab {
    case list
    case submit
}

/// 反馈类型项
struct FeedbackTypeItem: Identifiable {
    let id = UUID()
    let type: FeedbackType
    let displayName: String
    let icon: String

    static var entries: [FeedbackTypeItem] {
        [
            FeedbackTypeItem(type: .功能建议, displayName: "功能建议", icon: "lightbulb"),
            FeedbackTypeItem(type: .问题反馈, displayName: "问题反馈", icon: "exclamationmark.triangle"),
            FeedbackTypeItem(type: .体验优化, displayName: "体验优化", icon: "sparkles"),
            FeedbackTypeItem(type: .其他, displayName: "其他", icon: "ellipsis")
        ]
    }
}

/// 反馈中心视图模型
@MainActor
class FeedbackCenterViewModel: ObservableObject {
    @Published var uiState = FeedbackCenterUiState()

    private let apiService = APIService.shared

    init() {
        Task {
            await loadFeedbacks()
        }
    }

    /// 选择 Tab
    func selectTab(_ tab: FeedbackTab) {
        uiState.selectedTab = tab
    }

    /// 加载反馈列表
    func loadFeedbacks() async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        do {
            uiState.feedbacks = try await apiService.getMyFeedbacks()
        } catch {
            uiState.errorMessage = error.localizedDescription
        }

        uiState.isLoading = false
    }

    /// 选择反馈类型
    func selectFeedbackType(_ type: String) {
        uiState.selectedType = type
    }

    /// 更新反馈内容
    func updateFeedbackContent(_ content: String) {
        if content.count <= 500 {
            uiState.feedbackContent = content
        }
    }

    /// 更新联系方式
    func updateFeedbackContact(_ contact: String) {
        uiState.feedbackContact = contact
    }

    /// 提交反馈
    func submitFeedback() async {
        guard uiState.feedbackContent.count >= 10 else {
            uiState.errorMessage = "内容至少10个字"
            return
        }

        uiState.isSubmitting = true
        uiState.errorMessage = nil

        do {
            guard let feedbackType = FeedbackType(rawValue: uiState.selectedType) else {
                throw APIServiceError.serverError("无效的反馈类型")
            }

            _ = try await apiService.submitFeedback(
                type: feedbackType,
                content: uiState.feedbackContent,
                contact: uiState.feedbackContact.isEmpty ? nil : uiState.feedbackContact
            )

            uiState.isSubmitting = false
            uiState.submitSuccess = true

            // 清空表单
            uiState.feedbackContent = ""
            uiState.feedbackContact = ""
            uiState.selectedType = "功能建议"

            // 重新加载列表
            await loadFeedbacks()

            // 切换到列表 Tab
            uiState.selectedTab = .list
        } catch {
            uiState.isSubmitting = false
            uiState.errorMessage = error.localizedDescription
        }
    }

    /// 重置提交成功状态
    func resetSubmitSuccess() {
        uiState.submitSuccess = false
    }

    /// 字符数计算
    var contentCharCount: Int {
        uiState.feedbackContent.count
    }
}