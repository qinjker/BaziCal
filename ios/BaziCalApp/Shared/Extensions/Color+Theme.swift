import SwiftUI

extension Color {
    // MARK: - Brand Colors (新中式极简风格)

    /// 主色朱砂红 #C84A3E
    static let brandRed = Color(hex: "C84A3E")

    /// 暖金色 #D4A843
    static let brandGold = Color(hex: "D4A843")

    /// 墨绿色 #5A8A6A
    static let brandGreen = Color(hex: "5A8A6A")

    /// 文字主色 #2C1810
    static let brandText = Color(hex: "2C1810")

    /// 背景色 #F7F4EF
    static let brandBackground = Color(hex: "F7F4EF")

    /// 深棕色 #5A4A3A
    static let brandBrown = Color(hex: "5A4A3A")

    /// 浅棕色 #8B7355
    static let brandLightBrown = Color(hex: "8B7355")

    /// 今日渐变结束色 #A33D33
    static let brandRedDark = Color(hex: "A33D33")

    // MARK: - Helper Init

    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        case 8: // ARGB (32-bit)
            (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}

// MARK: - Gradients

extension LinearGradient {
    /// 今日标识背景渐变
    static let todayGradient = LinearGradient(
        colors: [.brandRed, .brandRedDark],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )
}