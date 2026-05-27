import SwiftUI

/// 生日输入页面
struct BirthdayInputView: View {
    @StateObject private var viewModel = BirthdayViewModel()
    @State private var showingResult = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 32) {
                    // 标题区域
                    headerSection

                    // 生日输入表单
                    formSection

                    // 计算按钮
                    calculateButton
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 32)
            }
            .background(Color.brandBackground)
            .navigationTitle("输入生日")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("重置") {
                        viewModel.reset()
                    }
                    .foregroundColor(.brandRed)
                }
            }
            .navigationDestination(isPresented: $showingResult) {
                if let bazi = viewModel.calculatedBazi {
                    CalendarView(userBazi: bazi)
                }
            }
        }
    }

    // MARK: - Header Section

    private var headerSection: some View {
        VStack(spacing: 12) {
            Image(systemName: "calendar.circle.fill")
                .font(.system(size: 64))
                .foregroundColor(.brandRed)

            Text("每一天，都算数")
                .font(.headline)
                .foregroundColor(.brandText)

            Text("输入您的生日，开启专属能量日历")
                .font(.subheadline)
                .foregroundColor(.brandLightBrown)
        }
    }

    // MARK: - Form Section

    private var formSection: some View {
        VStack(spacing: 24) {
            // 生日日期选择
            VStack(alignment: .leading, spacing: 8) {
                Text("出生日期")
                    .font(.subheadline)
                    .foregroundColor(.brandText)

                DatePicker(
                    "选择日期",
                    selection: $viewModel.selectedDate,
                    in: ...Date(),
                    displayedComponents: .date
                )
                .datePickerStyle(.wheel)
                .labelsHidden()
                .frame(maxWidth: .infinity)
            }

            // 阴历/阳历切换
            VStack(alignment: .leading, spacing: 8) {
                Text("生日类型")
                    .font(.subheadline)
                    .foregroundColor(.brandText)

                Picker("生日类型", selection: $viewModel.birthdayType) {
                    Text("阳历").tag(BirthdayType.solar)
                    Text("阴历").tag(BirthdayType.lunar)
                }
                .pickerStyle(.segmented)
            }

            // 时辰选择
            VStack(alignment: .leading, spacing: 8) {
                Text("出生时辰")
                    .font(.subheadline)
                    .foregroundColor(.brandText)

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(TimeLabel.allCases) { label in
                            TimeLabelButton(
                                label: label,
                                isSelected: viewModel.selectedTimeLabel == label
                            ) {
                                viewModel.selectedTimeLabel = label
                            }
                        }
                    }
                }
            }

            // 错误提示
            if let error = viewModel.errorMessage {
                Text(error)
                    .font(.footnote)
                    .foregroundColor(.red)
                    .multilineTextAlignment(.center)
            }
        }
    }

    // MARK: - Calculate Button

    private var calculateButton: some View {
        Button {
            Task {
                await viewModel.calculateBazi()
                if viewModel.calculatedBazi != nil {
                    showingResult = true
                }
            }
        } label: {
            HStack {
                if viewModel.isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                } else {
                    Text("计算八字")
                        .font(.headline)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .background(Color.brandRed)
            .foregroundColor(.white)
            .cornerRadius(12)
        }
        .disabled(viewModel.isLoading)
    }
}

// MARK: - Time Label Button

struct TimeLabelButton: View {
    let label: TimeLabel
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label.rawValue)
                .font(.subheadline)
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(isSelected ? Color.brandRed : Color.clear)
                .foregroundColor(isSelected ? .white : .brandText)
                .cornerRadius(20)
                .overlay(
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(Color.brandRed, lineWidth: isSelected ? 0 : 1)
                )
        }
    }
}

#Preview {
    BirthdayInputView()
}