package com.bazical.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bazical.app.ui.calendar.CalendarScreen
import com.bazical.app.ui.components.TabItem
import com.bazical.app.ui.daily.DailyScreen
import com.bazical.app.ui.feedback.FeedbackCenterScreen
import com.bazical.app.ui.feedback.FeedbackDetailScreen
import com.bazical.app.ui.home.HomeScreen
import com.bazical.app.ui.splash.SplashScreen

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Calendar : Screen("calendar")
    data object Daily : Screen("daily/{date}") {
        fun createRoute(date: String) = "daily/$date"
    }
    data object FeedbackCenter : Screen("feedback_center")
    data object FeedbackDetail : Screen("feedback_detail/{id}") {
        fun createRoute(id: String) = "feedback_detail/$id"
    }
}

@Composable
fun BaziCalNavHost(
    navController: NavHostController = rememberNavController()
) {
    var currentRoute by remember { mutableStateOf<String?>(null) }

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
            currentRoute = "home"
            HomeScreen(
                onNavigateToCalendar = {
                    navController.navigate(Screen.Calendar.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                currentRoute = currentRoute,
                onTabClick = { tab ->
                    when (tab) {
                        TabItem.Calendar -> {
                            navController.navigate(Screen.Calendar.route) {
                                popUpTo(Screen.Calendar.route) { inclusive = true }
                            }
                        }
                        TabItem.Today -> {
                            val today = java.time.LocalDate.now().toString()
                            navController.navigate(Screen.Daily.createRoute(today))
                        }
                        TabItem.Home -> {
                            // Already on home
                        }
                        TabItem.Feedback -> {
                            navController.navigate(Screen.FeedbackCenter.route)
                        }
                    }
                }
            )
        }

        composable(Screen.Calendar.route) {
            currentRoute = "calendar"
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
                },
                currentRoute = currentRoute,
                onTabClick = { tab ->
                    when (tab) {
                        TabItem.Calendar -> {
                            if (currentRoute != "calendar") {
                                navController.navigate(Screen.Calendar.route) {
                                    popUpTo(Screen.Calendar.route) { inclusive = true }
                                }
                            }
                        }
                        TabItem.Today -> {
                            val today = java.time.LocalDate.now().toString()
                            navController.navigate(Screen.Daily.createRoute(today))
                        }
                        TabItem.Home -> {
                            if (currentRoute != "home") {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Calendar.route) { inclusive = true }
                                }
                            }
                        }
                        TabItem.Feedback -> {
                            // TODO: Navigate to feedback
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.Daily.route,
            arguments = listOf(
                navArgument("date") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            currentRoute = "today"
            val date = backStackEntry.arguments?.getString("date") ?: ""
            DailyScreen(
                date = date,
                onNavigateBack = { navController.popBackStack() },
                currentRoute = currentRoute,
                onTabClick = { tab ->
                    when (tab) {
                        TabItem.Calendar -> {
                            navController.navigate(Screen.Calendar.route) {
                                popUpTo(Screen.Calendar.route) { inclusive = true }
                            }
                        }
                        TabItem.Today -> {
                            // Already on today
                        }
                        TabItem.Home -> {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Calendar.route) { inclusive = true }
                            }
                        }
                        TabItem.Feedback -> {
                            navController.navigate(Screen.FeedbackCenter.route)
                        }
                    }
                }
            )
        }

        composable(Screen.FeedbackCenter.route) {
            currentRoute = "feedback"
            FeedbackCenterScreen(
                onNavigateToDetail = { id ->
                    navController.navigate(Screen.FeedbackDetail.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.FeedbackDetail.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType }
            )
        ) {
            FeedbackDetailScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}