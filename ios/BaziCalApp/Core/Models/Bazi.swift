import Foundation

/// 天干地支组合
struct GanZhi: Codable, Equatable {
    let stem: String  // 天干
    let branch: String  // 地支

    var display: String {
        "\(stem)\(branch)"
    }
}

/// 八字结果
struct Bazi: Codable {
    let year: GanZhi
    let month: GanZhi
    let day: GanZhi
    let hour: GanZhi
    let wuxing: [String: Int]
    let shishen: [String: String]
}

/// 计算八字请求
struct CalculateBaziRequest: Codable {
    let name: String
    let birthday: String
    let birthday_type: String
    let hour: Int
    let minute: Int
    let gender: String
    let device_id: String?

    init(birthday: String, birthday_type: String, hour: Int, minute: Int, device_id: String?) {
        self.name = "用户"
        self.birthday = birthday
        self.birthday_type = birthday_type
        self.hour = hour
        self.minute = minute
        self.gender = "男"
        self.device_id = device_id
    }
}

/// 计算八字响应数据
struct CalculateBaziData: Codable {
    let userId: String
    let bazi: Bazi
}

/// 计算八字响应
struct CalculateBaziResponse: Codable {
    let code: Int
    let message: String?
    let data: CalculateBaziData?
}

/// 月历单元格数据
struct CalendarDay: Codable, Identifiable {
    let date: String
    let ganzhi: String?
    let wuxing: String?
    let yi: [String]?
    let ji: [String]?
    let star: String?
    let luck: String?
    let shishen: String?
    let jieqi: String?
    let lunarDate: String?
    let holiday: String?
    let branchShishen: String?

    var id: String { date }

    var isToday: Bool {
        let today = ISO8601DateFormatter().string(from: Date())
        return date == today
    }

    var isCurrentMonth: Bool {
        true  // Default to true, can be computed from date if needed
    }
}

/// 月历响应
struct MonthlyCalendarResponse: Codable {
    let code: Int
    let message: String?
    let data: MonthlyCalendarData?
}

struct MonthlyCalendarData: Codable {
    let days: [CalendarDay]
}

/// 每日详情响应
struct DailyDetailResponse: Codable {
    let code: Int
    let message: String?
    let data: DailyDetail?
}

/// 每日详情数据
struct DailyDetail: Codable {
    let date: String
    let ganzhi: String
    let shishen: String
    let branchShishen: String
    let energy: EnergyInfo?
    let lucky: LuckyInfo?
    let messages: [String]?
    let tags: [String]?
}

/// 能量信息
struct EnergyInfo: Codable {
    let level: Int
    let description: String
}

/// 幸运信息
struct LuckyInfo: Codable {
    let direction: String?
    let time: String?
    let color: String?
    let number: Int?
}

/// 今日寄语
struct Message: Codable {
    let energy: String      // 能量描述
    let action: String      // 行动建议
}
    