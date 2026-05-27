import SwiftUI

/// 月历单元格视图
struct CalendarCellView: View {
    let day: CalendarDay

    var body: some View {
        VStack(spacing: 3) {
            // 第1行：公历日期
            Text("\(solarDate)")
                .font(.system(size: 14, weight: .semibold))
                .foregroundColor(dateColor)

            // 第2行：农历/节气/节日
            Text(solarTermText)
                .font(.system(size: 9))
                .foregroundColor(solarTermColor)
                .lineLimit(1)
                .frame(height: 12)

            // 第3行：天干 + 十神
            HStack(spacing: 3) {
                Text(stemText)
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(stemColor)
                Text(day.shishen ?? "")
                    .font(.system(size: 9))
                    .foregroundColor(shishenColor)
            }
            .frame(height: 14)

            // 第4行：地支 + 地支十神
            HStack(spacing: 3) {
                Text(branchText)
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundColor(branchColor)
                Text(day.branchShishen ?? "")
                    .font(.system(size: 9))
                    .foregroundColor(shishenColor)
            }
            .frame(height: 14)
        }
        .frame(maxWidth: .infinity, minHeight: 78)
        .padding(.vertical, 6)
        .padding(.horizontal, 4)
        .background(cellBackground)
        .cornerRadius(12)
    }

    // MARK: - Computed Properties

    private var solarDate: Int {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        if let date = formatter.date(from: day.date) {
            return Calendar.current.component(.day, from: date)
        }
        return 0
    }

    private var isCurrentMonth: Bool {
        true  // Default to true, can be computed from date
    }

    // MARK: - Text Content

    private var solarTermText: String {
        day.jieqi ?? day.lunarDate ?? ""
    }

    private var stemText: String {
        guard isCurrentMonth, let ganzhi = day.ganzhi else { return "" }
        return String(ganzhi.prefix(1))
    }

    private var branchText: String {
        guard isCurrentMonth, let ganzhi = day.ganzhi else { return "" }
        return String(ganzhi.suffix(1))
    }

    // MARK: - Colors

    // 天干颜色
    private var stemColor: Color {
        guard let ganzhi = day.ganzhi else { return Color(hex: "2C1810") }
        let stem = String(ganzhi.prefix(1))
        switch stem {
        case "甲": return Color(hex: "4ade80")    // green
        case "乙": return Color(hex: "86efac")     // light-green
        case "丙": return Color(hex: "f87171")    // red
        case "丁": return Color(hex: "fca5a5")    // light-red
        case "戊": return Color(hex: "a78bfa")    // purple
        case "己": return Color(hex: "c4b5fd")    // light-purple
        case "庚": return Color(hex: "fbbf24")     // gold
        case "辛": return Color(hex: "fde047")     // light-gold
        case "壬": return Color(hex: "60a5fa")    // blue
        case "癸": return Color(hex: "93c5fd")    // light-blue
        default: return Color(hex: "2C1810")
        }
    }

    // 地支颜色
    private var branchColor: Color {
        guard let ganzhi = day.ganzhi else { return Color(hex: "5A4A3A") }
        let branch = String(ganzhi.suffix(1))
        switch branch {
        case "子": return Color(hex: "60a5fa")
        case "丑": return Color(hex: "a78bfa")
        case "寅": return Color(hex: "4ade80")
        case "卯": return Color(hex: "86efac")
        case "辰": return Color(hex: "a78bfa")
        case "巳": return Color(hex: "f87171")
        case "午": return Color(hex: "fca5a5")
        case "未": return Color(hex: "a78bfa")
        case "申": return Color(hex: "fbbf24")
        case "酉": return Color(hex: "fde047")
        case "戌": return Color(hex: "a78bfa")
        case "亥": return Color(hex: "60a5fa")
        default: return Color(hex: "5A4A3A")
        }
    }

    // 十神颜色（始终为浅棕色）
    private var shishenColor: Color {
        day.isToday ? Color.white.opacity(0.7) : Color(hex: "8B7355")
    }

    private var dateColor: Color {
        if day.isToday {
            return .white
        }
        if isWeekend {
            return Color(hex: "C84A3E")
        }
        return Color(hex: "2C1810")
    }

    private var solarTermColor: Color {
        if day.isToday {
            return Color.white.opacity(0.7)
        }
        if day.jieqi != nil {
            return Color(hex: "5A8A6A")
        }
        if day.lunarDate == "初一" || day.lunarDate == "十五" {
            return Color(hex: "C84A3E")
        }
        return Color(hex: "8B7355")
    }

    @ViewBuilder
    private var cellBackground: some View {
        if day.isToday {
            LinearGradient(
                colors: [Color(hex: "C84A3E"), Color(hex: "A33D33")],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        } else {
            Color(hex: "FAFAF8")
        }
    }

    private var isWeekend: Bool {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        if let date = formatter.date(from: day.date) {
            let weekday = Calendar.current.component(.weekday, from: date)
            return weekday == 1 || weekday == 7
        }
        return false
    }
}

#Preview {
    CalendarCellView(day: CalendarDay(
        date: "2024-01-15",
        ganzhi: "壬寅",
        wuxing: nil,
        yi: nil,
        ji: nil,
        star: nil,
        luck: nil,
        shishen: "正官",
        jieqi: nil,
        lunarDate: "初一",
        holiday: nil,
        branchShishen: "比肩"
    ))
}