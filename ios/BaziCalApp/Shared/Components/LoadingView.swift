import SwiftUI

/// 加载状态视图
struct LoadingView: View {
    var body: some View {
        VStack(spacing: 16) {
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle(tint: .brandRed))
                .scaleEffect(1.5)

            Text("加载中...")
                .font(.subheadline)
                .foregroundColor(.brandLightBrown)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.brandBackground)
    }
}

#Preview {
    LoadingView()
}