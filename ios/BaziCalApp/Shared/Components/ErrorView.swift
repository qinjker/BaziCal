import SwiftUI

/// 错误状态视图
struct ErrorView: View {
    let message: String
    let retryAction: (() -> Void)?

    init(message: String, retryAction: (() -> Void)? = nil) {
        self.message = message
        self.retryAction = retryAction
    }

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle.fill")
                .font(.system(size: 48))
                .foregroundColor(.brandRed)

            Text("出错了")
                .font(.headline)
                .foregroundColor(.brandText)

            Text(message)
                .font(.subheadline)
                .foregroundColor(.brandLightBrown)
                .multilineTextAlignment(.center)

            if let retryAction = retryAction {
                Button(action: retryAction) {
                    Text("重试")
                        .font(.subheadline)
                        .foregroundColor(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(Color.brandRed)
                        .cornerRadius(20)
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.brandBackground)
    }
}

#Preview {
    ErrorView(message: "网络连接失败，请检查网络设置") {
        print("Retry")
    }
}