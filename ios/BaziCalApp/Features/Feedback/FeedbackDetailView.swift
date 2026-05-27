import SwiftUI

/// 反馈详情页面
struct FeedbackDetailView: View {
    @StateObject private var viewModel: FeedbackDetailViewModel
    @State private var scrollToBottom = false

    var onNavigateBack: () -> Void
    var currentTab: TabItem
    var onTabClick: (TabItem) -> Void

    init(feedbackId: String, onNavigateBack: @escaping () -> Void, currentTab: TabItem, onTabClick: @escaping (TabItem) -> Void) {
        _viewModel = StateObject(wrappedValue: FeedbackDetailViewModel(feedbackId: feedbackId))
        self.onNavigateBack = onNavigateBack
        self.currentTab = currentTab
        self.onTabClick = onTabClick
    }

    var body: some View {
        VStack(spacing: 0) {
            // Navigation bar
            HStack {
                Button(action: onNavigateBack) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 20))
                        .foregroundColor(.brandText)
                        .frame(width: 40, height: 40)
                        .background(Color(hex: "F7F4EF"))
                        .cornerRadius(12)
                }

                Spacer()

                Text("反馈详情")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.brandText)

                Spacer()

                if let feedback = viewModel.uiState.feedback {
                    StatusBadge(status: feedback.status)
                } else {
                    Color.clear
                        .frame(width: 60, height: 24)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(Color.white)

            // Content
            Group {
                if viewModel.uiState.isLoading {
                    Spacer()
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .brandRed))
                    Spacer()
                } else if let feedback = viewModel.uiState.feedback {
                    ScrollViewReader { proxy in
                        ScrollView {
                            VStack(spacing: 12) {
                                // Original feedback card
                                OriginalFeedbackCard(feedback: feedback)

                                // Replies
                                ForEach(viewModel.uiState.replies) { reply in
                                    ReplyBubble(reply: reply)
                                }
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 12)
                        }
                        .onChange(of: viewModel.uiState.replies.count) { _ in
                            withAnimation {
                                scrollToBottom = true
                            }
                        }
                    }

                    // Input area
                    InputArea(
                        content: $viewModel.uiState.replyContent,
                        isSending: viewModel.uiState.isSending,
                        onSend: {
                            Task { await viewModel.sendReply() }
                        }
                    )
                } else {
                    Spacer()
                    Text(viewModel.uiState.errorMessage ?? "加载失败")
                        .font(.subheadline)
                        .foregroundColor(.brandRed)
                    Spacer()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)

            // Bottom Tab Bar
            BottomTabBar(currentTab: currentTab, onTabClick: onTabClick)
        }
        .background(Color.brandBackground)
    }
}

// MARK: - Status Badge

private struct StatusBadge: View {
    let status: FeedbackStatus

    var body: some View {
        Text(status.displayName)
            .font(.system(size: 12, weight: .medium))
            .foregroundColor(statusTextColor)
            .padding(.horizontal, 14)
            .padding(.vertical, 6)
            .background(statusBgColor)
            .cornerRadius(10)
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

// MARK: - Original Feedback Card

private struct OriginalFeedbackCard: View {
    let feedback: Feedback

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                TypeBadge(type: feedback.type)
                Spacer()
                Text(formatDateTime(feedback.createdAt))
                    .font(.caption)
                    .foregroundColor(.brandLightBrown)
            }

            Text(feedback.content)
                .font(.system(size: 15))
                .foregroundColor(.brandText)
                .lineSpacing(4)
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white)
        .cornerRadius(16)
    }
}

// MARK: - Type Badge

private struct TypeBadge: View {
    let type: FeedbackType

    var body: some View {
        Text(type.rawValue)
            .font(.system(size: 11, weight: .medium))
            .foregroundColor(badgeTextColor)
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(badgeBgColor)
            .cornerRadius(6)
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

// MARK: - Reply Bubble

private struct ReplyBubble: View {
    let reply: Reply

    var body: some View {
        HStack(spacing: 10) {
            if !reply.isFromAdmin {
                avatarView
            }

            VStack(alignment: reply.isFromAdmin ? .trailing : .leading, spacing: 4) {
                Text(reply.authorName)
                    .font(.caption)
                    .foregroundColor(.brandLightBrown)

                Text(reply.content)
                    .font(.system(size: 14))
                    .foregroundColor(reply.isFromAdmin ? .white : .brandText)
                    .padding(12)
                    .background(reply.isFromAdmin ? Color.brandRed : Color.white)
                    .cornerRadius(16, corners: reply.isFromAdmin ? [.topLeft, .topRight, .bottomLeft] : [.topLeft, .topRight, .bottomRight])

                Text(formatDateTime(reply.createdAt))
                    .font(.caption2)
                    .foregroundColor(.brandLightBrown)
            }
            .frame(maxWidth: 280, alignment: reply.isFromAdmin ? .trailing : .leading)

            if reply.isFromAdmin {
                avatarView
            }
        }
        .frame(maxWidth: .infinity, alignment: reply.isFromAdmin ? .trailing : .leading)
    }

    private var avatarView: some View {
        Circle()
            .fill(reply.isFromAdmin ? Color.brandRed : Color.brandGreen)
            .frame(width: 32, height: 32)
            .overlay(
                Text(reply.isFromAdmin ? "" : "")
                    .font(.system(size: 16))
            )
    }
}

// MARK: - Input Area

private struct InputArea: View {
    @Binding var content: String
    let isSending: Bool
    let onSend: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            TextField("输入回复内容...", text: $content)
                .font(.system(size: 15))
                .foregroundColor(.brandText)
                .padding(.horizontal, 16)
                .frame(height: 48)
                .background(Color(hex: "F7F4EF"))
                .cornerRadius(14)

            Button(action: onSend) {
                Group {
                    if isSending {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Image(systemName: "arrow.up")
                            .font(.system(size: 20))
                    }
                }
                .frame(width: 48, height: 48)
                .background(content.isBlank ? Color.gray.opacity(0.3) : Color.brandRed)
                .foregroundColor(.white)
                .cornerRadius(14)
            }
            .disabled(isSending || content.isBlank)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.white)
    }
}

// MARK: - Helper Functions

private func formatDateTime(_ dateString: String) -> String {
    guard dateString.count >= 16 else { return dateString }
    let datePart = dateString.substring(from: 5, to: 10).replacingOccurrences(of: "-", with: "月") + "日"
    let timePart = dateString.substring(from: 11, to: 16)
    return "\(datePart) \(timePart)"
}

private extension String {
    var isBlank: Bool {
        trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    func substring(from: Int, to: Int) -> String {
        let start = index(startIndex, offsetBy: from)
        let end = index(startIndex, offsetBy: to)
        return String(self[start..<end])
    }
}

// MARK: - Corner Radius Extension

extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(
            roundedRect: rect,
            byRoundingCorners: corners,
            cornerRadii: CGSize(width: radius, height: radius)
        )
        return Path(path.cgPath)
    }
}

#Preview {
    FeedbackDetailView(
        feedbackId: "test-id",
        onNavigateBack: {},
        currentTab: .feedback,
        onTabClick: { _ in }
    )
}