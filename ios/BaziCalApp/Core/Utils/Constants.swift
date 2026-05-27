import Foundation

/// API 配置常量
enum Constants {
    /// API 基础 URL - 开发环境使用本地服务器，生产环境使用云服务器
    static let apiBaseURL = "http://rili.jingyan99.com/api"

    /// 请求超时时间（秒）
    static let requestTimeout: TimeInterval = 30
}

// MARK: - API Endpoints

enum APIEndpoint {
    /// 计算八字
    case calculateBazi
    /// 获取月历
    case monthlyCalendar
    /// 获取每日详情
    case dailyDetail(date: String)
    /// 提交反馈
    case submitFeedback
    /// 获取我的反馈列表
    case myFeedbacks
    /// 获取反馈详情
    case feedbackDetail(id: String)
    /// 获取反馈回复
    case feedbackReplies(id: String)
    /// 添加反馈回复
    case addFeedbackReply(id: String)

    var path: String {
        switch self {
        case .calculateBazi:
            return "/v1/bazi/calculate"
        case .monthlyCalendar:
            return "/v1/bazi/calendar"
        case .dailyDetail(let date):
            return "/v1/bazi/daily/\(date)"
        case .submitFeedback:
            return "/v1/feedback"
        case .myFeedbacks:
            return "/v1/feedbacks"
        case .feedbackDetail(let id):
            return "/v1/feedbacks/\(id)"
        case .feedbackReplies(let id):
            return "/v1/feedbacks/\(id)/replies"
        case .addFeedbackReply(let id):
            return "/v1/feedbacks/\(id)/replies"
        }
    }

    var url: URL? {
        URL(string: Constants.apiBaseURL + path)
    }
}

// MARK: - 时辰标签

enum TimeLabel: String, CaseIterable, Identifiable {
    case 子时 = "子时"
    case 丑时 = "丑时"
    case 寅时 = "寅时"
    case 卯时 = "卯时"
    case 辰时 = "辰时"
    case 巳时 = "巳时"
    case 午时 = "午时"
    case 未时 = "未时"
    case 申时 = "申时"
    case 酉时 = "酉时"
    case 戌时 = "戌时"
    case 亥时 = "亥时"
    case 未知 = "未知"

    var id: String { rawValue }

    /// 时辰对应的小时（用于 API 调用）
    var hour: Int? {
        switch self {
        case .子时: return 23
        case .丑时: return 1
        case .寅时: return 3
        case .卯时: return 5
        case .辰时: return 7
        case .巳时: return 9
        case .午时: return 11
        case .未时: return 13
        case .申时: return 15
        case .酉时: return 17
        case .戌时: return 19
        case .亥时: return 21
        case .未知: return nil
        }
    }
}

// MARK: - 生日类型

enum BirthdayType: String, Codable {
    case solar = "solar"   // 阳历
    case lunar = "lunar"   // 阴历
}