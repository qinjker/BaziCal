import SwiftUI

/// 八字卡片组件
struct BaziCard: View {
    let bazi: Bazi

    var body: some View {
        VStack(spacing: 16) {
            Text("您的八字")
                .font(.headline)
                .foregroundColor(.brandText)

            VStack(spacing: 12) {
                BaziRow(label: "年柱", ganZhi: bazi.year)
                BaziRow(label: "月柱", ganZhi: bazi.month)
                BaziRow(label: "日柱", ganZhi: bazi.day)
                BaziRow(label: "时柱", ganZhi: bazi.hour)
            }
        }
        .padding()
        .background(Color.white)
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.05), radius: 4, x: 0, y: 2)
    }
}

// MARK: - Bazi Row

struct BaziRow: View {
    let label: String
    let ganZhi: GanZhi

    var body: some View {
        HStack {
            Text(label)
                .font(.subheadline)
                .foregroundColor(.brandLightBrown)
                .frame(width: 60, alignment: .leading)

            Text(ganZhi.display)
                .font(.headline)
                .foregroundColor(.brandText)

            Spacer()

            Text(baziShiShen)
                .font(.subheadline)
                .foregroundColor(.brandGold)
        }
    }

    private var baziShiShen: String {
        // 这里应该用 bazi.shishen 根据天干获取对应的十神
        // 简化实现
        ""
    }
}

#Preview {
    BaziCard(bazi: Bazi(
        year: GanZhi(stem: "甲", branch: "子"),
        month: GanZhi(stem: "乙", branch: "丑"),
        day: GanZhi(stem: "丙", branch: "寅"),
        hour: GanZhi(stem: "丁", branch: "卯"),
        wuxing: [:],
        shishen: [:]
    ))
    .padding()
    .background(Color.brandBackground)
}