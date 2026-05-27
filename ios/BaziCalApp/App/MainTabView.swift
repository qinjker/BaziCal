import SwiftUI

/// 主 Tab 视图 - 应用的主导航容器
struct MainTabView: View {
    @State private var currentTab: TabItem = .home
    @State private var userBazi: Bazi?
    @State private var selectedDate: String = ""
    @State private var showDailyDetail: Bool = false
    @State private var feedbackId: String?

    var body: some View {
        VStack(spacing: 0) {
            switch currentTab {
            case .home:
                HomeView(
                    onNavigateToCalendar: {
                        currentTab = .calendar
                    },
                    currentTab: currentTab,
                    onTabClick: { tab in
                        handleTabClick(tab)
                    }
                )
            case .calendar:
                CalendarContainerView(
                    userBazi: UserDataStore.shared.userBazi,
                    currentTab: currentTab,
                    onTabClick: { tab in
                        handleTabClick(tab)
                    },
                    onDateSelected: { date in
                        selectedDate = date
                        showDailyDetail = true
                    }
                )
            case .today:
                TodayContainerView(
                    currentTab: currentTab,
                    onTabClick: { tab in
                        handleTabClick(tab)
                    }
                )
            case .feedback:
                FeedbackCenterView(
                    onNavigateToDetail: { id in
                        feedbackId = id
                    },
                    currentTab: currentTab,
                    onTabClick: { tab in
                        handleTabClick(tab)
                    }
                )
            }

            // Bottom Tab Bar
            BottomTabBar(currentTab: currentTab, onTabClick: { tab in
                handleTabClick(tab)
            })
        }
        .fullScreenCover(isPresented: $showDailyDetail) {
            DailyDetailContainerView(
                date: selectedDate,
                onDismiss: {
                    showDailyDetail = false
                },
                currentTab: currentTab,
                onTabClick: { tab in
                    handleTabClick(tab)
                    showDailyDetail = false
                }
            )
        }
        .sheet(item: $feedbackId) { id in
            NavigationStack {
                FeedbackDetailView(
                    feedbackId: id,
                    onNavigateBack: {
                        feedbackId = nil
                    },
                    currentTab: currentTab,
                    onTabClick: { tab in
                        handleTabClick(tab)
                        feedbackId = nil
                    }
                )
            }
        }
    }

    private func handleTabClick(_ tab: TabItem) {
        if tab == .home && currentTab == .home {
            return
        }
        currentTab = tab
    }
}

// MARK: - Container Views

private struct CalendarContainerView: View {
    let userBazi: Bazi?
    let currentTab: TabItem
    let onTabClick: (TabItem) -> Void
    let onDateSelected: (String) -> Void

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Text("能量日历")
                    .font(.headline)
                    .foregroundColor(.brandText)
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .padding(.top, 8)
            .background(Color.white)

            if let bazi = userBazi {
                CalendarView(userBazi: bazi, onDateSelected: onDateSelected)
            } else {
                Text("正在加载...")
                    .foregroundColor(.brandLightBrown)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
    }
}

private struct TodayContainerView: View {
    let currentTab: TabItem
    let onTabClick: (TabItem) -> Void

    var body: some View {
        VStack(spacing: 0) {
            Group {
                if let bazi = UserDataStore.shared.userBazi {
                    todayContent(bazi: bazi)
                } else {
                    noBaziContent
                }
            }
        }
    }

    @ViewBuilder
    private func todayContent(bazi: Bazi) -> some View {
        DailyDetailView(
            date: todayDateString,
            userBazi: bazi,
            onNavigateBack: { },
            currentTab: currentTab,
            onTabClick: { tab in
                onTabClick(tab)
            }
        )
    }

    private var todayDateString: String {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "yyyy-MM-dd"
        return dateFormatter.string(from: Date())
    }

    private var noBaziContent: some View {
        VStack(spacing: 20) {
            Image(systemName: "calendar.badge.exclamationmark")
                .font(.system(size: 64))
                .foregroundColor(.brandLightBrown)

            Text("请先完成生辰配置")
                .font(.headline)
                .foregroundColor(.brandText)

            Text("在首页输入生日，开启专属能量日历")
                .font(.subheadline)
                .foregroundColor(.brandLightBrown)
                .multilineTextAlignment(.center)

            Button("去配置") {
                onTabClick(.home)
            }
            .foregroundColor(.brandRed)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.brandBackground)
    }
}

private struct DailyDetailContainerView: View {
    let date: String
    let onDismiss: () -> Void
    let currentTab: TabItem
    let onTabClick: (TabItem) -> Void

    var body: some View {
        VStack(spacing: 0) {
            Group {
                if let bazi = UserDataStore.shared.userBazi {
                    DailyDetailView(
                        date: date,
                        userBazi: bazi,
                        onNavigateBack: onDismiss,
                        currentTab: currentTab,
                        onTabClick: { tab in
                            onTabClick(tab)
                            if tab != .today {
                                onDismiss()
                            }
                        }
                    )
                } else {
                    VStack {
                        Text("正在加载...")
                            .foregroundColor(.brandLightBrown)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
        }
    }
}