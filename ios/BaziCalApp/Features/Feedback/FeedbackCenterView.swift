import SwiftUI

/// 反馈中心页面
struct FeedbackCenterView: View {
    @StateObject private var viewModel = FeedbackCenterViewModel()
    var onNavigateToDetail: ((String) -> Void)?
    var currentTab: TabItem
    var onTabClick: (TabItem) -> Void

    var body: some View {
        VStack(spacing: 0) {
            // 顶部标题栏
            HStack {
                Text("反馈中心")
                    .font(.headline)
                    .foregroundColor(.brandText)
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .padding(.top, 8)
            .background(Color.white)

            // Tab 切换
            VStack(spacing: 0) {
                HStack(spacing: 0) {
                    TabButton(
                        text: "我的反馈",
                        isSelected: viewModel.uiState.selectedTab == .list,
                        onClick: { viewModel.selectTab(.list) }
                    )
                    TabButton(
                        text: "提交反馈",
                        isSelected: viewModel.uiState.selectedTab == .submit,
                        onClick: { viewModel.selectTab(.submit) }
                    )
                }
                .padding(.horizontal, 20)

                // 分隔线
                Rectangle()
                    .fill(Color(hex: "E8E0D5"))
                    .frame(height: 1)
            }
            .background(Color.white)

            // Content
            Group {
                switch viewModel.uiState.selectedTab {
                case .list:
                    FeedbackListContent(
                        feedbacks: viewModel.uiState.feedbacks,
                        isLoading: viewModel.uiState.isLoading,
                        onItemClick: { feedback in
                            onNavigateToDetail?(feedback.id)
                        },
                        onRetry: {
                            Task { await viewModel.loadFeedbacks() }
                        }
                    )
                case .submit:
                    SubmitFeedbackContent(
                        uiState: viewModel.uiState,
                        onTypeSelect: { viewModel.selectFeedbackType($0) },
                        onContentChange: { viewModel.updateFeedbackContent($0) },
                        onContactChange: { viewModel.updateFeedbackContact($0) },
                        onSubmit: {
                            Task { await viewModel.submitFeedback() }
                        }
                    )
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .background(Color.brandBackground)
    }
}

// MARK: - Tab Button

private struct TabButton: View {
    let text: String
    let isSelected: Bool
    let onClick: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            Text(text)
                .font(isSelected ? .system(size: 15, weight: .semibold) : .system(size: 15))
                .foregroundColor(isSelected ? .brandRed : .brandLightBrown)

            if isSelected {
                Rectangle()
                    .fill(Color.brandRed)
                    .frame(width: 40, height: 3)
                    .cornerRadius(2)
            } else {
                Spacer()
                    .frame(height: 11)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 16)
        .contentShape(Rectangle())
        .onTapGesture(perform: onClick)
    }
}

// MARK: - Feedback List Content

private struct FeedbackListContent: View {
    let feedbacks: [Feedback]
    let isLoading: Bool
    let onItemClick: (Feedback) -> Void
    let onRetry: () -> Void

    var body: some View {
        Group {
            if isLoading {
                VStack {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .brandRed))
                    Spacer()
                }
            } else if feedbacks.isEmpty {
                EmptyFeedbackState(onRetry: onRetry)
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(feedbacks) { feedback in
                            FeedbackItem(feedback: feedback, onClick: { onItemClick(feedback) })
                        }
                    }
                    .padding(16)
                }
            }
        }
    }
}

// MARK: - Feedback Item

private struct FeedbackItem: View {
    let feedback: Feedback
    let onClick: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Type + Time
            HStack {
                TypeBadge(type: feedback.type)
                Spacer()
                Text(formatTimeAgo(feedback.createdAt))
                    .font(.caption)
                    .foregroundColor(.brandLightBrown)
            }

            // Content preview
            Text(feedback.content)
                .font(.system(size: 14))
                .foregroundColor(.brandText)
                .lineLimit(2)

            // Reply preview
            if !feedback.replies.isEmpty {
                let lastReply = feedback.replies.last!
                HStack(spacing: 10) {
                    Circle()
                        .fill(lastReply.isFromAdmin ? Color.brandRed : Color.brandGreen)
                        .frame(width: 28, height: 28)
                        .overlay(
                            Text(lastReply.isFromAdmin ? "" : "")
                                .font(.system(size: 14))
                        )

                    VStack(alignment: .leading, spacing: 2) {
                        Text(lastReply.authorName)
                            .font(.caption)
                            .foregroundColor(.brandLightBrown)
                        Text(lastReply.content)
                            .font(.system(size: 13))
                            .foregroundColor(.brandLightBrown)
                            .lineLimit(1)
                    }

                    Spacer()

                    if feedback.hasUnreadReplies {
                        Circle()
                            .fill(Color.brandRed)
                            .frame(width: 8, height: 8)
                    }
                }
                .padding(12)
                .background(Color(hex: "F7F4EF"))
                .cornerRadius(12)
            }

            // Bottom: reply count + status
            HStack {
                Text("💬 \(feedback.replyCount)条回复")
                    .font(.caption)
                    .foregroundColor(.brandLightBrown)

                Spacer()

                StatusBadge(status: feedback.status)
            }
        }
        .padding(18)
        .background(Color.white)
        .cornerRadius(16)
        .onTapGesture(perform: onClick)
    }
}

// MARK: - Type Badge

private struct TypeBadge: View {
    let type: FeedbackType

    var body: some View {
        Text(type.rawValue)
            .font(.system(size: 12, weight: .medium))
            .foregroundColor(badgeTextColor)
            .padding(.horizontal, 12)
            .padding(.vertical, 5)
            .background(badgeBgColor)
            .cornerRadius(8)
    }

    private var badgeBgColor: Color {
        switch type {
        case .功能建议: return Color(hex: "E8F5E9")
        case .问题反馈: return Color(hex: "FFEBEE")
        case .体验优化: return Color(hex: "E3F2FD")
        case .其他: return Color(hex: "F7F4EF")
        }
    }

    private var badgeTextColor: Color {
        switch type {
        case .功能建议: return Color(hex: "4CAF50")
        case .问题反馈: return Color(hex: "C84A3E")
        case .体验优化: return Color(hex: "2196F3")
        case .其他: return .brandLightBrown
        }
    }
}

// MARK: - Status Badge

private struct StatusBadge: View {
    let status: FeedbackStatus

    var body: some View {
        Text(status.displayName)
            .font(.system(size: 11, weight: .medium))
            .foregroundColor(statusTextColor)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(statusBgColor)
            .cornerRadius(6)
    }

    private var statusBgColor: Color {
        switch status {
        case .pending: return Color(hex: "FFF8E1")
        case .reviewed: return Color(hex: "E3F2FD")
        case .replied: return Color(hex: "E8F5E9")
        case .closed: return Color(hex: "F7F4EF")
        }
    }

    private var statusTextColor: Color {
        switch status {
        case .pending: return Color(hex: "FF9800")
        case .reviewed: return Color(hex: "2196F3")
        case .replied: return Color(hex: "4CAF50")
        case .closed: return .brandLightBrown
        }
    }
}

// MARK: - Empty State

private struct EmptyFeedbackState: View {
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Spacer()

            Text("💬")
                .font(.system(size: 56))

            Text("还没有提交过反馈")
                .font(.system(size: 14))
                .foregroundColor(.brandLightBrown)

            Button(action: onRetry) {
                Text("立即反馈")
                    .font(.system(size: 14))
                    .foregroundColor(.white)
                    .padding(.horizontal, 24)
                    .padding(.vertical, 12)
                    .background(Color.brandRed)
                    .cornerRadius(14)
            }

            Spacer()
        }
    }
}

// MARK: - Submit Feedback Content

private struct SubmitFeedbackContent: View {
    let uiState: FeedbackCenterUiState
    let onTypeSelect: (String) -> Void
    let onContentChange: (String) -> Void
    let onContactChange: (String) -> Void
    let onSubmit: () -> Void

    @State private var contentText: String = ""
    @State private var contactText: String = ""

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                // Form card
                VStack(spacing: 14) {
                    // Type selection
                    HStack(spacing: 8) {
                        ForEach(FeedbackTypeItem.entries) { item in
                            TypeButton(
                                item: item,
                                isSelected: uiState.selectedType == item.displayName,
                                onClick: { onTypeSelect(item.displayName) }
                            )
                        }
                    }

                    // Content input
                    ZStack(alignment: .topLeading) {
                        TextEditor(text: $contentText)
                            .font(.system(size: 15))
                            .foregroundColor(.brandText)
                            .frame(height: 100)
                            .padding(14)
                            .background(Color(hex: "F7F4EF"))
                            .cornerRadius(12)
                            .onChange(of: contentText) { newValue in
                                onContentChange(newValue)
                            }

                        if contentText.isEmpty {
                            Text("请详细描述您遇到的问题或建议，至少10个字")
                                .font(.system(size: 15))
                                .foregroundColor(.brandLightBrown)
                                .padding(18)
                        }
                    }

                    Text("\(uiState.feedbackContent.count)/500")
                        .font(.caption)
                        .foregroundColor(.brandLightBrown)
                        .frame(maxWidth: .infinity, alignment: .trailing)

                    // Contact input
                    HStack {
                        Text("联系方式")
                            .font(.caption)
                            .foregroundColor(.brandLightBrown)

                        TextField("选填，便于我们回复您", text: $contactText)
                            .font(.system(size: 14))
                            .foregroundColor(.brandText)
                            .padding(.horizontal, 14)
                            .frame(height: 44)
                            .background(Color(hex: "F7F4EF"))
                            .cornerRadius(10)
                            .onChange(of: contactText) { newValue in
                                onContactChange(newValue)
                            }
                    }
                }
                .padding(18)
                .background(Color.white)
                .cornerRadius(16)

                // Submit button
                Button(action: onSubmit) {
                    HStack {
                        if uiState.isSubmitting {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("提交反馈")
                                .font(.system(size: 16, weight: .semibold))
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(
                        uiState.feedbackContent.count >= 10 ? Color.brandRed : Color.gray.opacity(0.3)
                    )
                    .foregroundColor(.white)
                    .cornerRadius(14)
                }
                .disabled(uiState.isSubmitting || uiState.feedbackContent.count < 10)

                // Error message
                if let error = uiState.errorMessage {
                    Text(error)
                        .font(.caption)
                        .foregroundColor(.brandRed)
                }
            }
            .padding(16)
        }
        .onAppear {
            contentText = uiState.feedbackContent
            contactText = uiState.feedbackContact
        }
    }
}

// MARK: - Type Button

private struct TypeButton: View {
    let item: FeedbackTypeItem
    let isSelected: Bool
    let onClick: () -> Void

    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: item.icon)
                .font(.system(size: 18))
            Text(item.displayName)
                .font(.system(size: 11))
        }
        .foregroundColor(isSelected ? .white : .brandLightBrown)
        .frame(maxWidth: .infinity)
        .padding(10)
        .background(isSelected ? Color.brandRed : Color(hex: "F7F4EF"))
        .cornerRadius(10)
        .onTapGesture(perform: onClick)
    }
}

// MARK: - Helper Functions

private func formatTimeAgo(_ dateString: String) -> String {
    guard dateString.count >= 10 else { return dateString }
    return dateString.substring(from: 5, to: 10).replacingOccurrences(of: "-", with: "月") + "日"
}

private extension String {
    func substring(from: Int, to: Int) -> String {
        let start = index(startIndex, offsetBy: from)
        let end = index(startIndex, offsetBy: to)
        return String(self[start..<end])
    }
}

#Preview {
    FeedbackCenterView(
        onNavigateToDetail: nil,
        currentTab: .feedback,
        onTabClick: { _ in }
    )
}