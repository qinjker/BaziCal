import SwiftUI

@main
struct BaziCalApp: App {
    @State private var showSplash = true

    var body: some Scene {
        WindowGroup {
            ZStack {
                if showSplash {
                    SplashScreen(
                        onNavigateToHome: {
                            withAnimation {
                                showSplash = false
                            }
                        },
                        onNavigateToCalendar: {
                            withAnimation {
                                showSplash = false
                            }
                        }
                    )
                } else {
                    MainTabView()
                }
            }
            .ignoresSafeArea()
        }
    }
}