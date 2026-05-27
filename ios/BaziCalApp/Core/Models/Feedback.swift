import Foundation

/// 反馈类型
enum FeedbackType: String, Codable, CaseIterable, Identifiable {
    case 功能建议 = "功能建议"
    case 问题反馈 = "问题反馈"
    case 体验优化 = "体验优化"
    case 其他 = "其他"

    var id: String { rawValue }

    var icon: String {
        switch self {
        case .功能建议: return "lightbulb"
        case .问题反馈: return "exclamationmark.triangle"
        case .体验优化: return "sparkles"
        case .其他: return "ellipsis"
        }
    }
}

/// 反馈状态
enum FeedbackStatus: String, Codable {
    case pending = "pending"
    case reviewed = "reviewed"
    case replied = "replied"
    case closed = "closed"

    var displayName: String {
        switch self {
        case .pending: return "待处理"
        case .reviewed: return "已查看"
        case .replied: return "已回复"
        case .closed: return "已关闭"
        }
    }
}

/// 反馈项目
struct Feedback: Codable, Identifiable {
    let id: String
    let type: FeedbackType
    let content: String
    let status: FeedbackStatus
    let createdAt: String
    let contact: String?
    let replies: [Reply]

    var hasUnreadReplies: Bool {
        replies.contains { !$0.isFromAdmin }
    }

    var replyCount: Int {
        replies.count
    }
}

/// 回复
struct Reply: Codable, Identifiable {
    let id: String
    let content: String
    let isFromAdmin: Bool
    let authorName: String
    let createdAt: String
}

// MARK: - API Request/Response Models

/// 提交反馈请求
struct SubmitFeedbackRequest: Codable {
    let type: String
    let content: String
    let contact: String?
    let deviceId: String

    enum CodingKeys: String, CodingKey {
        case type, content, contact
        case deviceId = "device_id"
    }
}

/// 提交回复请求
struct SubmitReplyRequest: Codable {
    let content: String
}

/// 反馈列表响应
struct FeedbackListResponse: Codable {
    let code: Int
    let message: String?
    let data: FeedbackListData?
}

struct FeedbackListData: Codable {
    let feedbacks: [FeedbackDto]
    let total: Int
    let page: Int
    let pageSize: Int
}

/// 反馈详情响应
struct FeedbackDetailResponse: Codable {
    let code: Int
    let message: String?
    let data: FeedbackDto?
}

/// 反馈 DTO
struct FeedbackDto: Codable {
    let id: String
    let type: String
    let content: String
    let status: String
    let deviceId: String?
    let contact: String?
    let createdAt: String
    let replies: [ReplyDto]?

    func toFeedback() -> Feedback {
        Feedback(
            id: id,
            type: FeedbackType(rawValue: type) ?? .其他,
            content: content,
            status: FeedbackStatus(rawValue: status) ?? .pending,
            createdAt: createdAt,
            contact: contact,
            replies: replies?.map { $0.toReply() } ?? []
        )
    }
}

/// 回复 DTO
struct ReplyDto: Codable {
    let id: String
    let content: String
    let authorType: String
    let authorName: String
    let createdAt: String

    func toReply() -> Reply {
        Reply(
            id: id,
            content: content,
            isFromAdmin: authorType == "admin",
            authorName: authorName,
            createdAt: createdAt
        )
    }
}

/// 提交回复响应
struct SubmitReplyResponse: Codable {
    let code: Int
    let message: String?
}