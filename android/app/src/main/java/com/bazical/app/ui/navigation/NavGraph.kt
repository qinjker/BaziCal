package com.bazical.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bazical.app.ui.calendar.CalendarScreen
import com.bazical.app.ui.daily.DailyScreen
import com.bazical.app.ui.home.HomeScreen
import com.bazical.app.ui.splash.SplashScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Calendar : Screen("calendar")
    data object Daily : Screen("daily/{date}") {
        fun createRoute(date: String) = "daily/$date"
    }
}

@Composable
fun BaziCalNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateToDaily = { date ->
                    navController.navigate(Screen.Daily.createRoute(date))
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Calendar.route) { inclusive = true }
                    }
                },
                onNavigateToToday = {
                    val today = java.time.LocalDate.now().toString()
                    navController.navigate(Screen.Daily.createRoute(today))
                },
                onNavigateToFeedback = {
                    // TODO: Navigate to feedback
                }
            )
        }

        composable(
            route = Screen.Daily.route,
            arguments = listOf(
                navArgument("date") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            DailyScreen(
                date = date,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}