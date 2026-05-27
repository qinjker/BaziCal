import Foundation

/// 本地数据存储 - 使用 UserDefaults 存储用户数据
class UserDataStore: ObservableObject {
    static let shared = UserDataStore()

    private let defaults = UserDefaults.standard

    // Keys
    private enum Keys {
        static let hasCompletedOnboarding = "hasCompletedOnboarding"
        static let userBazi = "userBazi"
        static let deviceId = "deviceId"
        static let userId = "userId"
    }

    private init() {}

    // MARK: - Onboarding

    var hasCompletedOnboarding: Bool {
        get { defaults.bool(forKey: Keys.hasCompletedOnboarding) }
        set { defaults.set(newValue, forKey: Keys.hasCompletedOnboarding) }
    }

    // MARK: - User Bazi

    var userBazi: Bazi? {
        get {
            guard let data = defaults.data(forKey: Keys.userBazi) else { return nil }
            return try? JSONDecoder().decode(Bazi.self, from: data)
        }
        set {
            if let newValue = newValue {
                let data = try? JSONEncoder().encode(newValue)
                defaults.set(data, forKey: Keys.userBazi)
            } else {
                defaults.removeObject(forKey: Keys.userBazi)
            }
        }
    }

    // MARK: - Device ID

    var deviceId: String {
        if let existingId = defaults.string(forKey: Keys.deviceId) {
            return existingId
        }

        let newId = UUID().uuidString
        defaults.set(newId, forKey: Keys.deviceId)
        return newId
    }

    // MARK: - User ID

    var userId: String? {
        get { defaults.string(forKey: Keys.userId) }
        set { defaults.set(newValue, forKey: Keys.userId) }
    }

    // MARK: - Clear Data

    func clearAll() {
        defaults.removeObject(forKey: Keys.hasCompletedOnboarding)
        defaults.removeObject(forKey: Keys.userBazi)
        defaults.removeObject(forKey: Keys.userId)
    }
}