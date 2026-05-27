import SwiftUI

extension Font {
    // MARK: - Typography (中文八字应用)

    /// 大标题 34pt Bold
    static let largeTitle = Font.system(size: 34, weight: .bold)

    /// 标题 28pt Bold
    static let title = Font.system(size: 28, weight: .bold)

    /// 副标题 22pt Semibold
    static let headline = Font.system(size: 22, weight: .semibold)

    /// 正文 17pt Regular
    static let body = Font.system(size: 17, weight: .regular)

    /// 辅助文字 15pt Regular
    static let subheadline = Font.system(size: 15, weight: .regular)

    /// 注释 13pt Regular
    static let footnote = Font.system(size: 13, weight: .regular)

    // MARK: - Calendar Cell Fonts (月历单元格)

    /// 第1行：公历日期 12pt
    static let calendarDate: Font = .system(size: 12, weight: .regular)

    /// 第2行：农历/节气/节日 8pt
    static let calendarLunar: Font = .system(size: 8, weight: .regular)

    /// 第3-4行：天干地支+十神 10pt
    static let calendarGanZhi: Font = .system(size: 10, weight: .regular)

    // MARK: - Daily Detail Fonts (每日详情)

    /// 干支大字展示 72pt
    static let ganZhiLarge: Font = .system(size: 72, weight: .bold)

    /// 十神标签 14pt
    static let shiShen: Font = .system(size: 14, weight: .medium)

    /// 寄语内容 16pt
    static let message: Font = .system(size: 16, weight: .regular)
}

// MARK: - Text Style Modifiers

struct BrandTextStyle: ViewModifier {
    let font: Font
    let color: Color

    func body(content: Content) -> some View {
        content
            .font(font)
            .foregroundColor(color)
    }
}

extension View {
    func brandStyle(_ font: Font, color: Color = .brandText) -> some View {
        modifier(BrandTextStyle(font: font, color: color))
    }
}