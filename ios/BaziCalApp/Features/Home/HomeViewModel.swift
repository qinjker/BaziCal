import Foundation
import SwiftUI

/// 首页 UI 状态
struct HomeUiState {
    var birthday: Date?
    var timeValue: String = ""
    var hour: Int = 0
    var birthdayType: String = "solar"
    var showDatePicker: Bool = false
    var isLoading: Bool = false
    var calculated: Bool = false
    var error: String?
}

/// 首页视图模型
@MainActor
class HomeViewModel: ObservableObject {
    @Published var uiState = HomeUiState()
    @Published var calculatedBazi: Bazi?

    private let apiService = APIService.shared
    private let userDataStore = UserDataStore.shared

    init() {
        // 从本地存储加载已有数据
        if let bazi = userDataStore.userBazi {
            calculatedBazi = bazi
        }
    }

    /// 更新生日
    func updateBirthday(_ date: Date) {
        uiState.birthday = date
    }

    /// 更新时辰
    func updateTime(_ timeValue: String) {
        uiState.timeValue = timeValue
        uiState.hour = TimeLabel(rawValue: timeValue)?.hour ?? 0
    }

    /// 更新生日类型
    func updateBirthdayType(_ type: String) {
        uiState.birthdayType = type
    }

    /// 显示日期选择器
    func showDatePicker() {
        uiState.showDatePicker = true
    }

    /// 隐藏日期选择器
    func hideDatePicker() {
        uiState.showDatePicker = false
    }

    /// 计算八字
    func calculate() async {
        guard let birthday = uiState.birthday else {
            uiState.error = "请选择出生日期"
            return
        }

        uiState.isLoading = true
        uiState.error = nil

        do {
            let bazi = try await apiService.calculateBazi(
                birthday: birthday,
                hour: uiState.hour,
                minute: 0,
                birthdayType: BirthdayType(rawValue: uiState.birthdayType) ?? .solar
            )

            // 保存到本地
            userDataStore.userBazi = bazi
            userDataStore.hasCompletedOnboarding = true
            calculatedBazi = bazi
            uiState.isLoading = false
            uiState.calculated = true
        } catch {
            uiState.isLoading = false
            uiState.error = error.localizedDescription
        }
    }
}