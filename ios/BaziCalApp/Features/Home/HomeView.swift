import SwiftUI

/// 首页 - 生日输入页面
struct HomeView: View {
    @StateObject private var viewModel = HomeViewModel()
    @State private var showDatePicker = false

    var onNavigateToCalendar: () -> Void
    var currentTab: TabItem
    var onTabClick: (TabItem) -> Void

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 20) {
                    // Header
                    headerSection

                    // Form card
                    formCard

                    // Error message
                    if let error = viewModel.uiState.error {
                        Text(error)
                            .font(.footnote)
                            .foregroundColor(.red)
                            .padding(.horizontal, 24)
                    }

                    // Tip box
                    tipBox
                }
                .padding(.bottom, 100)
            }
            .background(Color.brandBackground)

            // Submit Button
            submitButton
        }
        .onChange(of: viewModel.uiState.calculated) { calculated in
            if calculated {
                onNavigateToCalendar()
            }
        }
        .sheet(isPresented: $showDatePicker) {
            DatePickerSheet(
                selectedDate: viewModel.uiState.birthday ?? Date(),
                onDateSelected: { date in
                    viewModel.updateBirthday(date)
                }
            )
        }
    }

    // MARK: - Header Section

    private var headerSection: some View {
        VStack(spacing: 10) {
            Text("你的生日")
                .font(.system(size: 26, weight: .bold))
                .foregroundColor(.brandText)

            Text("我们将基于此为你生成专属能量日历")
                .font(.subheadline)
                .foregroundColor(.brandLightBrown)
        }
        .padding(.top, 24)
    }

    // MARK: - Form Card

    private var formCard: some View {
        VStack(spacing: 24) {
            // Birthday section
            VStack(alignment: .leading, spacing: 12) {
                Text("出生年月日")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(.brandText)

                HStack(spacing: 10) {
                    DatePickerField(
                        label: "年",
                        value: viewModel.uiState.birthday?.yearString ?? "",
                        onTap: { showDatePicker = true }
                    )
                    DatePickerField(
                        label: "月",
                        value: viewModel.uiState.birthday?.monthString ?? "",
                        onTap: { showDatePicker = true }
                    )
                    DatePickerField(
                        label: "日",
                        value: viewModel.uiState.birthday?.dayString ?? "",
                        onTap: { showDatePicker = true }
                    )
                }
            }

            // Time section
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    Text("出生时辰（选填）")
                        .font(.system(size: 15, weight: .medium))
                        .foregroundColor(.brandText)

                    Spacer()

                    Text("选填可让结果更精准")
                        .font(.caption)
                        .foregroundColor(.brandLightBrown)
                }

                FlowLayout(spacing: 8) {
                    TimeTagButton(
                        text: "未知",
                        selected: viewModel.uiState.timeValue.isEmpty,
                        onTap: { viewModel.updateTime("") }
                    )
                    ForEach(TimeLabel.allCases.filter { $0 != .未知 }) { label in
                        TimeTagButton(
                            text: label.rawValue,
                            selected: viewModel.uiState.timeValue == label.rawValue,
                            onTap: { viewModel.updateTime(label.rawValue) }
                        )
                    }
                }
            }

            Divider()
                .background(Color(hex: "E8E0D5"))

            // Calendar type section
            VStack(alignment: .leading, spacing: 12) {
                Text("生日类型")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(.brandText)

                HStack(spacing: 10) {
                    CalendarTypeButton(
                        name: "阳历",
                        hint: "公历生日",
                        selected: viewModel.uiState.birthdayType == "solar",
                        onTap: { viewModel.updateBirthdayType("solar") }
                    )
                    CalendarTypeButton(
                        name: "阴历",
                        hint: "农历生日",
                        selected: viewModel.uiState.birthdayType == "lunar",
                        onTap: { viewModel.updateBirthdayType("lunar") }
                    )
                }
            }
        }
        .padding(24)
        .background(Color.white)
        .cornerRadius(20)
        .shadow(color: .black.opacity(0.06), radius: 8, x: 0, y: 4)
        .padding(.horizontal, 24)
    }

    // MARK: - Tip Box

    private var tipBox: some View {
        HStack(alignment: .top, spacing: 10) {
            Image(systemName: "lightbulb")
                .foregroundColor(.brandGold)

            VStack(alignment: .leading, spacing: 4) {
                Text(viewModel.uiState.birthdayType == "solar" ? "什么是阳历生日？" : "什么是阴历生日？")
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(.brandText)

                Text(viewModel.uiState.birthdayType == "solar"
                     ? "阳历也叫公历，是国际通用的日历。比如1月1日、10月1日就是阳历日期。大部分身份证上的生日都是阳历。"
                     : "阴历也叫农历，是中国的传统历法。比如春节是正月初一、端午是五月初五、中秋是八月十五。有些地区习惯过农历生日。")
                    .font(.system(size: 12))
                    .foregroundColor(Color(hex: "5A6A7A"))
                    .lineSpacing(4)
            }
        }
        .padding(14)
        .background(Color(hex: "F0F7FF"))
        .cornerRadius(12)
        .padding(.horizontal, 24)
    }

    // MARK: - Submit Button

    private var submitButton: some View {
        Button {
            Task {
                await viewModel.calculate()
            }
        } label: {
            HStack {
                if viewModel.uiState.isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                } else {
                    Text("开启我的日历")
                        .font(.system(size: 17, weight: .semibold))
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(Color.brandRed)
            .foregroundColor(.white)
            .cornerRadius(16)
            .shadow(color: Color.brandRed.opacity(0.3), radius: 14, x: 0, y: 10)
        }
        .disabled(viewModel.uiState.isLoading)
        .padding(.horizontal, 24)
    }
}

// MARK: - Sub-components

private struct DatePickerField: View {
    let label: String
    let value: String
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 4) {
                Text(value.isEmpty ? label : value)
                    .font(.system(size: 16))
                    .foregroundColor(value.isEmpty ? .brandLightBrown : .brandText)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 54)
            .background(Color(hex: "FAF6F0"))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color(hex: "E8E0D5"), lineWidth: 1.5)
            )
        }
    }
}

private struct TimeTagButton: View {
    let text: String
    let selected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            Text(text)
                .font(.system(size: 14))
                .foregroundColor(selected ? .white : .brandLightBrown)
                .padding(.horizontal, 14)
                .padding(.vertical, 10)
                .background(selected ? Color.brandRed : Color(hex: "FAF6F0"))
                .cornerRadius(10)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(selected ? Color.brandRed : Color(hex: "E8E0D5"), lineWidth: 1.5)
                )
        }
    }
}

private struct CalendarTypeButton: View {
    let name: String
    let hint: String
    let selected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 4) {
                Text(name)
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(selected ? .brandRed : .brandText)
                Text(hint)
                    .font(.system(size: 11))
                    .foregroundColor(selected ? .brandRed.opacity(0.7) : .brandLightBrown)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 48)
            .background(selected ? Color(hex: "FFF5F4") : Color(hex: "FAF6F0"))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(selected ? Color.brandRed : Color(hex: "E8E0D5"), lineWidth: 1.5)
            )
        }
    }
}

private struct DatePickerSheet: View {
    let selectedDate: Date
    let onDateSelected: (Date) -> Void

    @State private var date: Date
    @Environment(\.dismiss) private var dismiss

    init(selectedDate: Date, onDateSelected: @escaping (Date) -> Void) {
        self.selectedDate = selectedDate
        self.onDateSelected = onDateSelected
        _date = State(initialValue: selectedDate)
    }

    var body: some View {
        NavigationStack {
            DatePicker(
                "选择日期",
                selection: $date,
                in: ...Date(),
                displayedComponents: .date
            )
            .datePickerStyle(.graphical)
            .padding()
            .navigationTitle("选择日期")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("取消") {
                            dismiss()
                        }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("确定") {
                            onDateSelected(date)
                            dismiss()
                        }
                    }
                }
        }
    }
}

// MARK: - Date Extensions

private extension Date {
    var yearString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy"
        return formatter.string(from: self)
    }

    var monthString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MM"
        return formatter.string(from: self)
    }

    var dayString: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd"
        return formatter.string(from: self)
    }
}

// MARK: - FlowLayout

struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = FlowResult(in: proposal.width ?? 0, subviews: subviews, spacing: spacing)
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = FlowResult(in: bounds.width, subviews: subviews, spacing: spacing)
        for (index, subview) in subviews.enumerated() {
            subview.place(at: CGPoint(x: bounds.minX + result.positions[index].x,
                                      y: bounds.minY + result.positions[index].y),
                          proposal: .unspecified)
        }
    }

    struct FlowResult {
        var size: CGSize = .zero
        var positions: [CGPoint] = []

        init(in maxWidth: CGFloat, subviews: Subviews, spacing: CGFloat) {
            var x: CGFloat = 0
            var y: CGFloat = 0
            var rowHeight: CGFloat = 0

            for subview in subviews {
                let size = subview.sizeThatFits(.unspecified)
                if x + size.width > maxWidth && x > 0 {
                    x = 0
                    y += rowHeight + spacing
                    rowHeight = 0
                }
                positions.append(CGPoint(x: x, y: y))
                rowHeight = max(rowHeight, size.height)
                x += size.width + spacing
            }

            self.size = CGSize(width: maxWidth, height: y + rowHeight)
        }
    }
}

#Preview {
    HomeView(onNavigateToCalendar: {}, currentTab: .home, onTabClick: { _ in })
}