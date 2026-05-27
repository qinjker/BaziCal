import SwiftUI

/// 启动画面 - 显示应用 Logo 并自动导航
struct SplashScreen: View {
    @State private var isAnimating = false
    @State private var showTitle = false

    var onNavigateToHome: () -> Void
    var onNavigateToCalendar: () -> Void

    var body: some View {
        ZStack {
            Color.brandBackground
                .ignoresSafeArea()

            VStack(spacing: 24) {
                Spacer()

                // Logo
                Image(systemName: "calendar.circle.fill")
                    .font(.system(size: 100))
                    .foregroundColor(.brandRed)
                    .scaleEffect(isAnimating ? 1.0 : 0.5)
                    .opacity(isAnimating ? 1.0 : 0)

                // 标题
                VStack(spacing: 8) {
                    Text("能量日历")
                        .font(.system(size: 32, weight: .bold))
                        .foregroundColor(.brandText)

                    Text("每一天，都算数")
                        .font(.subheadline)
                        .foregroundColor(.brandLightBrown)
                        .opacity(showTitle ? 1.0 : 0)
                }

                Spacer()

                // 加载指示器
                if showTitle {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .brandRed))
                        .scaleEffect(1.2)
                }

                Spacer()
                    .frame(height: 80)
            }
        }
        .onAppear {
            withAnimation(.easeOut(duration: 0.8)) {
                isAnimating = true
            }

            withAnimation(.easeIn(duration: 0.5).delay(0.5)) {
                showTitle = true
            }

            // 2秒后自动导航
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                navigate()
            }
        }
    }

    private func navigate() {
        if UserDataStore.shared.hasCompletedOnboarding && UserDataStore.shared.userBazi != nil {
            onNavigateToCalendar()
        } else {
            onNavigateToHome()
        }
    }
}

#Preview {
    SplashScreen(
        onNavigateToHome: {},
        onNavigateToCalendar: {}
    )
}