import Foundation
import SwiftUI

/// 每日详情视图模型
@MainActor
class DailyViewModel: ObservableObject {
    @Published var dailyDetail: DailyDetail?
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?

    let date: String
    private let apiService = APIService.shared

    init(date: String) {
        self.date = date
    }

    /// 加载每日详情
    func loadDailyDetail() async {
        isLoading = true
        errorMessage = nil

        do {
            dailyDetail = try await apiService.getDailyDetail(date: date)
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    /// 日期显示
    var dateDisplay: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        if let date = formatter.date(from: date) {
            formatter.dateFormat = "M月d日"
            return formatter.string(from: date)
        }
        return date
    }

    /// 星期显示
    var weekdayDisplay: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        if let date = formatter.date(from: date) {
            formatter.dateFormat = "EEEE"
            return formatter.string(from: date)
        }
        return ""
    }
}