import Foundation
import SwiftUI

/// 生日输入视图模型
@MainActor
class BirthdayViewModel: ObservableObject {
    @Published var selectedDate: Date = Calendar.current.date(byAdding: .year, value: -30, to: Date()) ?? Date()
    @Published var selectedTimeLabel: TimeLabel = .子时
    @Published var birthdayType: BirthdayType = .solar
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?
    @Published var calculatedBazi: Bazi?

    private let apiService = APIService.shared

    /// 计算八字
    func calculateBazi() async {
        isLoading = true
        errorMessage = nil

        do {
            let hour = selectedTimeLabel.hour ?? 0
            calculatedBazi = try await apiService.calculateBazi(
                birthday: selectedDate,
                hour: hour,
                minute: 0,
                birthdayType: birthdayType
            )
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    /// 重置表单
    func reset() {
        selectedDate = Calendar.current.date(byAdding: .year, value: -30, to: Date()) ?? Date()
        selectedTimeLabel = .子时
        birthdayType = .solar
        calculatedBazi = nil
        errorMessage = nil
    }
}