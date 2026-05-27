import SwiftUI

/// Tab 项目枚举
enum TabItem: String, CaseIterable {
    case calendar = "月历"
    case today = "今日"
    case home = "生辰"
    case feedback = "反馈"

    var icon: String {
        switch self {
        case .calendar: return "calendar"
        case .today: return "sun.max"
        case .home: return "house"
        case .feedback: return "bubble.left.and.bubble.right"
        }
    }

    var selectedIcon: String {
        switch self {
        case .calendar: return "calendar.circle.fill"
        case .today: return "sun.max.fill"
        case .home: return "house.fill"
        case .feedback: return "bubble.left.and.bubble.right.fill"
        }
    }
}

/// 底部导航栏组件
struct BottomTabBar: View {
    let currentTab: TabItem
    let onTabClick: (TabItem) -> Void

    var body: some View {
        HStack(spacing: 0) {
            ForEach(TabItem.allCases, id: \.self) { tab in
                TabBarItem(
                    tab: tab,
                    isSelected: currentTab == tab,
                    onTap: { onTabClick(tab) }
                )
            }
        }
        .frame(height: 84)
        .background(Color.white)
        .shadow(color: Color.black.opacity(0.05), radius: 8, x: 0, y: -4)
    }
}

/// 单个 Tab Bar Item
private struct TabBarItem: View {
    let tab: TabItem
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 4) {
                Image(systemName: isSelected ? tab.selectedIcon : tab.icon)
                    .font(.system(size: 24))
                    .foregroundColor(isSelected ? .brandRed : .brandLightBrown)

                Text(tab.rawValue)
                    .font(.caption2)
                    .foregroundColor(isSelected ? .brandRed : .brandLightBrown)
            }
            .frame(maxWidth: .infinity)
        }
    }
}

#Preview {
    BottomTabBar(currentTab: .home, onTabClick: { _ in })
}