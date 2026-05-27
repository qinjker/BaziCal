import Foundation
import CryptoKit

/// API 服务错误
enum APIServiceError: Error, LocalizedError {
    case invalidURL
    case networkError(Error)
    case decodingError(Error)
    case serverError(String)

    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "无效的 URL"
        case .networkError(let error):
            return "网络错误: \(error.localizedDescription)"
        case .decodingError(let error):
            return "数据解析错误: \(error.localizedDescription)"
        case .serverError(let message):
            return "服务器错误: \(message)"
        }
    }
}

/// API 服务 - 处理所有 HTTP 请求
@MainActor
class APIService: ObservableObject {
    static let shared = APIService()

    private let session: URLSession
    private let decoder: JSONDecoder
    private let appKey = "apkey20260519"

    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = Constants.requestTimeout
        self.session = URLSession(configuration: config)

        self.decoder = JSONDecoder()
        self.encoder = JSONEncoder()
    }

    private let encoder: JSONEncoder

    // MARK: - Signature

    private func generateSignature(body: String) -> (timestamp: String, signature: String) {
        let timestamp = String(Int(Date().timeIntervalSince1970 * 1000))
        let data = appKey + timestamp + body
        let hash = SHA256.hash(data: Data(data.utf8))
        let signature = hash.map { String(format: "%02x", $0) }.joined()
        return (timestamp, signature)
    }

    // MARK: - Calculate Bazi

    /// 计算八字
    func calculateBazi(
        birthday: Date,
        hour: Int,
        minute: Int,
        birthdayType: BirthdayType
    ) async throws -> Bazi {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let birthdayString = formatter.string(from: birthday)

        let request = CalculateBaziRequest(
            birthday: birthdayString,
            birthday_type: birthdayType.rawValue,
            hour: hour,
            minute: minute,
            device_id: UserDataStore.shared.deviceId
        )

        let response: CalculateBaziResponse = try await post(
            endpoint: .calculateBazi,
            body: request
        )

        guard response.code == 0, let data = response.data else {
            throw APIServiceError.serverError(response.message ?? "计算八字失败")
        }

        // 保存 userId 到本地
        UserDataStore.shared.userId = data.userId

        return data.bazi
    }

    // MARK: - Monthly Calendar

    /// 获取月历
    func getMonthlyCalendar(year: Int, month: Int) async throws -> [CalendarDay] {
        guard let userId = UserDataStore.shared.userId else {
            throw APIServiceError.serverError("用户未登录")
        }

        let response: MonthlyCalendarResponse = try await get(
            endpoint: .monthlyCalendar,
            queryItems: [
                URLQueryItem(name: "userId", value: userId),
                URLQueryItem(name: "year", value: String(year)),
                URLQueryItem(name: "month", value: String(month))
            ]
        )

        guard let data = response.data else {
            throw APIServiceError.serverError(response.message ?? "获取月历失败")
        }

        return data.days
    }

    // MARK: - Daily Detail

    /// 获取每日详情
    func getDailyDetail(date: String) async throws -> DailyDetail {
        guard let userId = UserDataStore.shared.userId else {
            throw APIServiceError.serverError("用户未登录")
        }

        let response: DailyDetailResponse = try await get(
            endpoint: .dailyDetail(date: date),
            queryItems: [URLQueryItem(name: "userId", value: userId)]
        )

        guard response.code == 0, let data = response.data else {
            throw APIServiceError.serverError(response.message ?? "获取每日详情失败")
        }

        return data
    }

    // MARK: - Private Methods

    private func get<T: Decodable>(endpoint: APIEndpoint, queryItems: [URLQueryItem]) async throws -> T {
        guard var urlComponents = URLComponents(string: Constants.apiBaseURL + endpoint.path) else {
            throw APIServiceError.invalidURL
        }

        urlComponents.queryItems = queryItems

        guard let url = urlComponents.url else {
            throw APIServiceError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let (timestamp, signature) = generateSignature(body: "{}")
        request.setValue(appKey, forHTTPHeaderField: "x-app-key")
        request.setValue(timestamp, forHTTPHeaderField: "x-timestamp")
        request.setValue(signature, forHTTPHeaderField: "x-signature")

        return try await performRequest(request)
    }

    private func post<T: Decodable, B: Encodable>(endpoint: APIEndpoint, queryItems: [URLQueryItem] = [], body: B) async throws -> T {
        guard var urlComponents = URLComponents(string: Constants.apiBaseURL + endpoint.path) else {
            throw APIServiceError.invalidURL
        }

        if !queryItems.isEmpty {
            urlComponents.queryItems = queryItems
        }

        guard let url = urlComponents.url else {
            throw APIServiceError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")

        let encoder = JSONEncoder()
        let bodyData = try encoder.encode(body)
        let bodyString = String(data: bodyData, encoding: .utf8) ?? "{}"
        let (timestamp, signature) = generateSignature(body: bodyString)

        request.setValue(appKey, forHTTPHeaderField: "x-app-key")
        request.setValue(timestamp, forHTTPHeaderField: "x-timestamp")
        request.setValue(signature, forHTTPHeaderField: "x-signature")
        request.httpBody = bodyData

        return try await performRequest(request)
    }

    private func performRequest<T: Decodable>(_ request: URLRequest) async throws -> T {
        do {
            let (data, response) = try await session.data(for: request)

            guard let httpResponse = response as? HTTPURLResponse else {
                throw APIServiceError.serverError("无效的响应")
            }

            guard (200...299).contains(httpResponse.statusCode) else {
                throw APIServiceError.serverError("HTTP \(httpResponse.statusCode)")
            }

            do {
                return try decoder.decode(T.self, from: data)
            } catch {
                throw APIServiceError.decodingError(error)
            }
        } catch let error as APIServiceError {
            throw error
        } catch {
            throw APIServiceError.networkError(error)
        }
    }

    // MARK: - Feedback APIs

    /// 提交反馈
    func submitFeedback(
        type: FeedbackType,
        content: String,
        contact: String?
    ) async throws -> Bool {
        let request = SubmitFeedbackRequest(
            type: type.rawValue,
            content: content,
            contact: contact,
            deviceId: UserDataStore.shared.deviceId
        )

        let _: SubmitReplyResponse = try await post(
            endpoint: .submitFeedback,
            body: request
        )
        return true
    }

    /// 获取我的反馈列表
    func getMyFeedbacks(page: Int = 1) async throws -> [Feedback] {
        // 后端使用 user_id 或 device_id 来查询
        let userId = UserDataStore.shared.userId ?? UserDataStore.shared.deviceId

        let response: FeedbackListResponse = try await get(
            endpoint: .myFeedbacks,
            queryItems: [
                URLQueryItem(name: "user_id", value: userId),
                URLQueryItem(name: "page", value: String(page))
            ]
        )

        guard let data = response.data else {
            return []
        }

        return data.feedbacks.map { $0.toFeedback() }
    }

    /// 获取反馈详情
    func getFeedbackDetail(id: String) async throws -> Feedback {
        let response: FeedbackDetailResponse = try await get(
            endpoint: .feedbackDetail(id: id),
            queryItems: []
        )

        guard let data = response.data else {
            throw APIServiceError.serverError("获取反馈详情失败")
        }

        return data.toFeedback()
    }

    /// 发送反馈回复
    func sendFeedbackReply(feedbackId: String, content: String) async throws -> Reply {
        let request = SubmitReplyRequest(content: content)

        let response: SubmitReplyResponse = try await post(
            endpoint: .addFeedbackReply(id: feedbackId),
            queryItems: [],
            body: request
        )

        guard response.code == 0 else {
            throw APIServiceError.serverError(response.message ?? "发送失败")
        }

        return Reply(
            id: UUID().uuidString,
            content: content,
            isFromAdmin: false,
            authorName: "我",
            createdAt: ISO8601DateFormatter().string(from: Date())
        )
    }
}