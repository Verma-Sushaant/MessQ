package io.github.sushaant.messq.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.sushaant.messq.data.local.datastore.UserPreferencesManager
import io.github.sushaant.messq.ui.auth.AuthScreen
import io.github.sushaant.messq.ui.customer.StudentScreen
import io.github.sushaant.messq.ui.splash.SplashScreen
import io.github.sushaant.messq.ui.staff.StaffScreen
import io.github.sushaant.messq.viewmodel.AuthViewModel
import io.github.sushaant.messq.viewmodel.customer.StudentViewModel
import io.github.sushaant.messq.viewmodel.staff.StaffViewModel

sealed class RootRoute(val route: String) {
    object Splash : RootRoute("splash")
    object Auth : RootRoute("auth")
    object StudentDashboard : RootRoute("student_dashboard")
    object StaffDashboard : RootRoute("staff_dashboard")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MessQNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel,
    studentViewModel: StudentViewModel,
    staffViewModel: StaffViewModel,
    preferencesManager: UserPreferencesManager
) {
    NavHost(
        navController = navController,
        startDestination = RootRoute.Splash.route
    ) {
        composable(RootRoute.Splash.route) {
            SplashScreen(
                preferencesManager = preferencesManager,
                onNavigateToAuth = {
                    navController.navigate(RootRoute.Auth.route) {
                        popUpTo(RootRoute.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToStudent = {
                    navController.navigate(RootRoute.StudentDashboard.route) {
                        popUpTo(RootRoute.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToStaff = {
                    navController.navigate(RootRoute.StaffDashboard.route) {
                        popUpTo(RootRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(RootRoute.Auth.route) {
            val uiState by authViewModel.uiState.collectAsState()
            AuthScreen(
                uiState = uiState,
                onLoginClick = { email, pass -> authViewModel.login(email, pass) },
                onSignUpClick = { email, pass, name, roll, role ->
                    authViewModel.signUp(email, pass, name, roll, role)
                },
                onClearError = { authViewModel.resetState() },
                onAuthSuccess = { role ->
                    val target = if (role == "staff" || role == "admin") {
                        RootRoute.StaffDashboard.route
                    } else {
                        RootRoute.StudentDashboard.route
                    }
                    navController.navigate(target) {
                        popUpTo(RootRoute.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(RootRoute.StudentDashboard.route) {
            val studentState by studentViewModel.uiState.collectAsState()
            StudentScreen(
                uiState = studentState,
                onBookSlot = { slot -> studentViewModel.bookSlot(slot) },
                onToggleSelectMenuItem = { itemId -> studentViewModel.toggleMenuItemSelection(itemId) },
                onRecommendMeals = { tags, targetCal, minProtein ->
                    studentViewModel.recommendFoodByNutritionTags(tags, targetCal, minProtein)
                },
                onQrPayloadDetected = { rawPayload ->
                    studentViewModel.onStaffQrScanned(rawPayload)
                },
                onRefreshData = { studentViewModel.loadStudentDashboardData() },
                onClearMessages = { studentViewModel.clearMessages() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(RootRoute.Auth.route) {
                        popUpTo(RootRoute.StudentDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(RootRoute.StaffDashboard.route) {
            val staffState by staffViewModel.uiState.collectAsState()
            StaffScreen(
                uiState = staffState,
                onSelectSlotForQr = { slot -> staffViewModel.selectSlotForGateQr(slot) },
                onResetAutoSlot = { staffViewModel.clearManualSlotOverride() },
                onUpdateSlotConfig = { slot, maxCap, start, end ->
                    staffViewModel.updateSlotConfig(slot, maxCap, start, end)
                },
                onSaveMenuItem = { existingId, name, category, description ->
                    staffViewModel.saveMenuWithAiNutrition(existingId, name, category, description)
                },
                onRefreshData = { staffViewModel.loadStaffDashboardData() },
                onClearMessages = { staffViewModel.clearMessages() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(RootRoute.Auth.route) {
                        popUpTo(RootRoute.StaffDashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}