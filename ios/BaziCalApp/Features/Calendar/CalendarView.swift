import SwiftUI

/// 月历页面
struct CalendarView: View {
    @StateObject private var viewModel: CalendarViewModel
    @State private var selectedDate: String?

    var onDateSelected: ((String) -> Void)?

    init(userBazi: Bazi, onDateSelected: ((String) -> Void)? = nil) {
        _viewModel = StateObject(wrappedValue: CalendarViewModel(userBazi: userBazi))
        self.onDateSelected = onDateSelected
    }

    private let weekDays = ["日", "一", "二", "三", "四", "五", "六"]

    var body: some View {
        VStack(spacing: 0) {
            // 用户信息卡片
            userInfoCard

            // 星期标题行
            weekDaysHeader

            // 日历网格或错误状态
            if viewModel.errorMessage != nil || (viewModel.calendarDays.isEmpty && !viewModel.isLoading) {
                errorOrEmptyState
            } else {
                calendarGrid
            }

            Spacer()
        }
        .background(Color(hex: "F7F4EF"))
        .task {
            await viewModel.loadMonthlyCalendar()
        }
    }

    // MARK: - User Info Card

    private var userInfoCard: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text("日主")
                    .font(.footnote)
                    .foregroundColor(Color(hex: "8B7355"))

                Text(viewModel.dayMaster)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(Color(hex: "2C1810"))
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 2) {
                Text("待定")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(Color(hex: "D4A843"))

                Text("本月能量")
                    .font(.system(size: 11))
                    .foregroundColor(Color(hex: "B8A892"))
            }
        }
        .padding(20)
        .background(Color.white)
        .cornerRadius(18)
        .shadow(color: Color(hex: "2C1810").opacity(0.06), radius: 8, x: 0, y: 4)
        .padding(.horizontal, 20)
        .padding(.top, 16)
    }

    // MARK: - Week Days Header

    private var weekDaysHeader: some View {
        HStack(spacing: 0) {
            ForEach(Array(weekDays.enumerated()), id: \.element) { index, day in
                Text(day)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(isWeekendHeader(index) ? Color(hex: "C84A3E") : Color(hex: "B8A892"))
                    .frame(maxWidth: .infinity)
            }
        }
        .padding(.vertical, 10)
        .background(Color.white)
        .cornerRadius(12)
        .padding(.horizontal, 16)
    }

    private func isWeekendHeader(_ index: Int) -> Bool {
        return index == 0 || index == 6
    }

    // MARK: - Calendar Grid

    private var calendarGrid: some View {
        let days = viewModel.calendarDays

        return LazyVGrid(
            columns: Array(repeating: GridItem(.flexible(), spacing: 3), count: 7),
            spacing: 3
        ) {
            ForEach(days) { day in
                CalendarCellView(day: day)
                    .onTapGesture {
                        if day.isCurrentMonth {
                            if let callback = onDateSelected {
                                callback(day.date)
                            } else {
                                selectedDate = day.date
                            }
                        }
                    }
            }
        }
        .padding(12)
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color(hex: "2C1810").opacity(0.06), radius: 8, x: 0, y: 4)
        .padding(.horizontal, 16)
    }

    // MARK: - Error State

    @ViewBuilder
    private var errorOrEmptyState: some View {
        if viewModel.isLoading {
            VStack {
                Spacer()
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .brandRed))
                Spacer()
            }
        } else if let error = viewModel.errorMessage {
            VStack(spacing: 16) {
                Spacer()
                Image(systemName: "exclamationmark.triangle")
                    .font(.system(size: 48))
                    .foregroundColor(.brandLightBrown)
                Text(error)
                    .font(.subheadline)
                    .foregroundColor(.brandLightBrown)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
                Button("重试") {
                    Task { await viewModel.loadMonthlyCalendar() }
                }
                .foregroundColor(.brandRed)
                Spacer()
            }
        } else if days.isEmpty {
            VStack(spacing: 16) {
                Spacer()
                Image(systemName: "calendar.badge.exclamationmark")
                    .font(.system(size: 48))
                    .foregroundColor(.brandLightBrown)
                Text("暂无数据")
                    .font(.subheadline)
                    .foregroundColor(.brandLightBrown)
                Spacer()
            }
        }
    }

    private var days: [CalendarDay] {
        viewModel.calendarDays
    }
}

extension String: @retroactive Identifiable {
    public var id: String { self }
}

#Preview {
    CalendarView(userBazi: Bazi(
        year: GanZhi(stem: "甲", branch: "子"),
        month: GanZhi(stem: "乙", branch: "丑"),
        day: GanZhi(stem: "丙", branch: "寅"),
        hour: GanZhi(stem: "丁", branch: "卯"),
        wuxing: [:],
        shishen: [:]
    ))
}