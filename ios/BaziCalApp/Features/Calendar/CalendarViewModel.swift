import Foundation
import SwiftUI

/// 月历视图模型
@MainActor
class CalendarViewModel: ObservableObject {
    @Published var currentYear: Int
    @Published var currentMonth: Int
    @Published var calendarDays: [CalendarDay] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String?

    let userBazi: Bazi
    private let apiService = APIService.shared

    init(userBazi: Bazi) {
        self.userBazi = userBazi

        let now = Date()
        let calendar = Calendar.current
        self.currentYear = calendar.component(.year, from: now)
        self.currentMonth = calendar.component(.month, from: now)
    }

    /// 加载月历
    func loadMonthlyCalendar() async {
        isLoading = true
        errorMessage = nil

        do {
            calendarDays = try await apiService.getMonthlyCalendar(
                year: currentYear,
                month: currentMonth
            )
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    /// 上个月
    func previousMonth() {
        if currentMonth == 1 {
            currentMonth = 12
            currentYear -= 1
        } else {
            currentMonth -= 1
        }
        Task { await loadMonthlyCalendar() }
    }

    /// 下个月
    func nextMonth() {
        if currentMonth == 12 {
            currentMonth = 1
            currentYear += 1
        } else {
            currentMonth += 1
        }
        Task { await loadMonthlyCalendar() }
    }

    /// 当前月份标题
    var monthTitle: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy年M月"
        var components = DateComponents()
        components.year = currentYear
        components.month = currentMonth
        components.day = 1
        if let date = Calendar.current.date(from: components) {
            return formatter.string(from: date)
        }
        return "\(currentYear)年\(currentMonth)月"
    }

    /// 日主信息（如"戊土"）
    var dayMaster: String {
        guard let dayStem = userBazi.day.stem.first else { return "" }
        let wuxing = getWuxing(for: dayStem)
        return "\(dayStem)\(wuxing)"
    }

    /// 获取五行
    private func getWuxing(for stem: Character) -> String {
        let map: [Character: String] = [
            "甲": "木", "乙": "木",
            "丙": "火", "丁": "火",
            "戊": "土", "己": "土",
            "庚": "金", "辛": "金",
            "壬": "水", "癸": "水"
        ]
        return map[stem] ?? ""
    }
}