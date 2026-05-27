import SwiftUI

/// 每日详情页面
struct DailyDetailView: View {
    @StateObject private var viewModel: DailyViewModel
    @ObservedObject private var userDataStore = UserDataStore.shared

    var onNavigateBack: (() -> Void)?
    var currentTab: TabItem?
    var onTabClick: ((TabItem) -> Void)?

    init(date: String, userBazi: Bazi, onNavigateBack: (() -> Void)? = nil, currentTab: TabItem? = nil, onTabClick: ((TabItem) -> Void)? = nil) {
        _viewModel = StateObject(wrappedValue: DailyViewModel(date: date))
        self.onNavigateBack = onNavigateBack
        self.currentTab = currentTab
        self.onTabClick = onTabClick
    }

    var body: some View {
        GeometryReader { geometry in
            VStack(spacing: 0) {
                // Navigation bar with date
                navigationBar

                // Scrollable content - takes remaining height minus bottom bar
                ScrollView {
                    VStack(spacing: 20) {
                        // 顶部用户信息卡
                        userCard

                        // 主分享按钮
                        shareMainButton

                        // 干支大字区域
                        ganzhiSection

                        // 寄语卡片 - 左对齐
                        messageCard
                            .frame(maxWidth: .infinity, alignment: .leading)

                        Spacer()
                            .frame(height: 20)
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 16)
                }
                .frame(maxHeight: .infinity)
                .background(Color(hex: "F7F4EF"))

                // 底部分享功能栏 - 固定在底部
                bottomActionBar
            }
            .frame(height: geometry.size.height)
            .background(Color(hex: "F7F4EF"))
        }
        .task {
            await viewModel.loadDailyDetail()
        }
    }

    // MARK: - Navigation Bar

    private var navigationBar: some View {
        HStack {
            if let back = onNavigateBack {
                Button(action: back) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 22))
                        .foregroundColor(Color(hex: "2C1810"))
                        .frame(width: 48, height: 48)
                        .background(Color(hex: "F7F4EF"))
                        .cornerRadius(14)
                }
            } else {
                Color.clear.frame(width: 48, height: 48)
            }

            Spacer()

            VStack(spacing: 3) {
                Text(viewModel.dateDisplay)
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(Color(hex: "2C1810"))

                if let detail = viewModel.dailyDetail {
                    Text(detail.date)
                        .font(.system(size: 13))
                        .foregroundColor(Color(hex: "8B7355"))
                }
            }

            Spacer()

            Color.clear.frame(width: 48, height: 48)
        }
        .padding(.horizontal, 18)
        .padding(.top, 14)
        .padding(.bottom, 24)
        .background(Color(hex: "F7F4EF"))
    }

    // MARK: - User Card

    private var userCard: some View {
        HStack {
            // Avatar
            ZStack {
                RoundedRectangle(cornerRadius: 12)
                    .fill(LinearGradient(
                        colors: [Color(hex: "C84A3E"), Color(hex: "A33D33")],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ))
                    .frame(width: 44, height: 44)

                Text("十")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.white)
            }

            // User info
            VStack(alignment: .leading, spacing: 2) {
                if let bazi = userDataStore.userBazi {
                    Text("日主：\(bazi.day.stem)\(bazi.day.branch)")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(Color(hex: "2C1810"))
                }

                Text("生于1990年")
                    .font(.system(size: 12))
                    .foregroundColor(Color(hex: "8B7355"))
            }

            Spacer()

            // Today's energy
            VStack(alignment: .trailing, spacing: 2) {
                Text(viewModel.dailyDetail?.shishen ?? "")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(Color(hex: "D4A843"))

                Text("今日能量")
                    .font(.system(size: 11))
                    .foregroundColor(Color(hex: "B8A892"))
            }
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(18)
        .shadow(color: Color(hex: "2C1810").opacity(0.06), radius: 8, x: 0, y: 4)
    }

    // MARK: - Share Main Button

    private var shareMainButton: some View {
        Button(action: {
            // Share action
        }) {
            VStack(spacing: 4) {
                Text("✨ 生成分享卡")
                    .font(.system(size: 17, weight: .semibold))

                Text("分享今日能量给朋友")
                    .font(.system(size: 12))
                    .opacity(0.8)
            }
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .background(
                LinearGradient(
                    colors: [Color(hex: "C84A3E"), Color(hex: "A33D33")],
                    startPoint: .topLeading,
                    endPoint: .bottomTrailing
                )
            )
            .cornerRadius(16)
            .shadow(color: Color(hex: "C84A3E").opacity(0.3), radius: 12, x: 0, y: 8)
        }
    }

    // MARK: - GanZhi Section

    private var ganzhiSection: some View {
        VStack(spacing: 16) {
            // GanZhi large text
            if let detail = viewModel.dailyDetail {
                Text(detail.ganzhi)
                    .font(.system(size: 72, weight: .bold, design: .serif))
                    .foregroundColor(Color(hex: "C84A3E"))
                    .tracking(10)
                    .shadow(color: Color(hex: "C84A3E").opacity(0.25), radius: 20, x: 0, y: 4)

                Text("2026年5月15日 · 乙巳月")
                    .font(.system(size: 14))
                    .foregroundColor(Color(hex: "8B7355"))
            } else if viewModel.isLoading {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .brandRed))
            }

            // TenGod tags in 2 rows
            if let detail = viewModel.dailyDetail {
                VStack(spacing: 8) {
                    // First row - 天干十神
                    HStack(spacing: 8) {
                        TenGodTag(text: detail.shishen, color: .gold)
                        TenGodTag(text: "比肩", color: .gold)
                    }

                    // Second row - 地支十神
                    HStack(spacing: 8) {
                        TenGodTag(text: "七杀", color: .green)
                        TenGodTag(text: detail.branchShishen, color: .green)
                        TenGodTag(text: "偏印", color: .red)
                    }
                }
            }
        }
        .padding(.vertical, 24)
        .padding(.horizontal, 24)
        .frame(maxWidth: .infinity)
        .background(
            LinearGradient(
                colors: [Color.white, Color(hex: "FAF8F4")],
                startPoint: .top,
                endPoint: .bottom
            )
        )
        .cornerRadius(20)
        .shadow(color: Color(hex: "2C1810").opacity(0.06), radius: 8, x: 0, y: 4)
    }

    // MARK: - Message Card

    @ViewBuilder
    private var messageCard: some View {
        if let detail = viewModel.dailyDetail, let messages = detail.messages, !messages.isEmpty {
            VStack(alignment: .leading, spacing: 0) {
                // Header with icon
                HStack(spacing: 10) {
                    Text("💬")
                        .font(.system(size: 20))

                    Text("今日寄语")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(Color(hex: "B8A892"))
                        .tracking(2)
                }
                .padding(.bottom, 18)

                // Main message - serif font
                Text("「\(messages.first ?? "")」")
                    .font(.system(size: 20, weight: .semibold, design: .serif))
                    .foregroundColor(Color(hex: "2C1810"))
                    .lineSpacing(8)
                    .padding(.bottom, 20)

                // List items
                if messages.count > 1 {
                    VStack(spacing: 0) {
                        ForEach(1..<messages.count, id: \.self) { index in
                            HStack(alignment: .top, spacing: 0) {
                                Circle()
                                    .fill(Color(hex: "D4A843"))
                                    .frame(width: 6, height: 6)
                                    .padding(.top, 6)

                                Text(messages[index])
                                    .font(.system(size: 14))
                                    .foregroundColor(Color(hex: "5A4A3A"))
                                    .padding(.leading, 20)
                                    .padding(.vertical, 12)
                            }

                            if index < messages.count - 1 {
                                Divider()
                                    .background(Color(hex: "E8E0D5").opacity(0.5))
                            }
                        }
                    }
                }
            }
            .padding(26)
            .background(Color.white)
            .cornerRadius(22)
            .shadow(color: Color(hex: "2C1810").opacity(0.08), radius: 10, x: 0, y: 4)
            .overlay(
                RoundedRectangle(cornerRadius: 22)
                    .stroke(Color(hex: "E8E0D5").opacity(0.6), lineWidth: 1)
            )
        }
    }

    // MARK: - Bottom Action Bar

    private var bottomActionBar: some View {
        HStack(spacing: 0) {
            // Share button - primary
            ActionButton(icon: "star.fill", text: "分享", isPrimary: true)
            // Save button
            ActionButton(icon: "square.and.arrow.down", text: "保存", isPrimary: false)
            // Calendar button
            ActionButton(icon: "calendar", text: "日历", isPrimary: false)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 16)
        .background(Color.white)
        .cornerRadius(20)
        .shadow(color: Color(hex: "2C1810").opacity(0.08), radius: 12, x: 0, y: 6)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(Color(hex: "E8E0D5").opacity(0.6), lineWidth: 1)
        )
        .padding(.horizontal, 20)
        .padding(.bottom, 10)
    }
}

// MARK: - TenGod Tag

private struct TenGodTag: View {
    let text: String
    let color: TagColor

    enum TagColor {
        case gold, green, red
    }

    var backgroundGradient: LinearGradient {
        switch color {
        case .gold:
            return LinearGradient(
                colors: [Color(hex: "E0B850"), Color(hex: "C49A3A")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        case .green:
            return LinearGradient(
                colors: [Color(hex: "689A78"), Color(hex: "4A7A5A")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        case .red:
            return LinearGradient(
                colors: [Color(hex: "D65A4E"), Color(hex: "B8443A")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        }
    }

    var body: some View {
        Text(text)
            .font(.system(size: 14, weight: .medium))
            .foregroundColor(.white)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
            .background(backgroundGradient)
            .cornerRadius(18)
            .shadow(color: .black.opacity(0.12), radius: 5, x: 0, y: 3)
    }
}

// MARK: - Action Button

private struct ActionButton: View {
    let icon: String
    let text: String
    let isPrimary: Bool

    var body: some View {
        VStack(spacing: 6) {
            ZStack {
                if isPrimary {
                    RoundedRectangle(cornerRadius: 14)
                        .fill(
                            LinearGradient(
                                colors: [Color(hex: "C84A3E"), Color(hex: "A33D33")],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 48, height: 48)
                        .shadow(color: Color(hex: "C84A3E").opacity(0.3), radius: 6, x: 0, y: 4)
                } else {
                    RoundedRectangle(cornerRadius: 14)
                        .fill(
                            LinearGradient(
                                colors: [Color(hex: "FAF6F0"), Color(hex: "F0EBE3")],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 48, height: 48)
                }

                Image(systemName: icon)
                    .font(.system(size: 22))
                    .foregroundColor(isPrimary ? .white : Color(hex: "5A4A3A"))
            }

            Text(text)
                .font(.system(size: 12, weight: .medium))
                .foregroundColor(isPrimary ? Color(hex: "C84A3E") : Color(hex: "5A4A3A"))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 8)
        .padding(.horizontal, 12)
        .background(Color.clear)
    }
}

#Preview {
    DailyDetailView(date: "2024-01-15", userBazi: Bazi(
        year: GanZhi(stem: "甲", branch: "子"),
        month: GanZhi(stem: "乙", branch: "丑"),
        day: GanZhi(stem: "丙", branch: "寅"),
        hour: GanZhi(stem: "丁", branch: "卯"),
        wuxing: [:],
        shishen: [:]
    ))
}