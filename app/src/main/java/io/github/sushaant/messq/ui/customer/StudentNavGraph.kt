package io.github.sushaant.messq.ui.customer

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.sushaant.messq.data.model.SlotDto
import io.github.sushaant.messq.state.customer.StudentUiState
import io.github.sushaant.messq.ui.customer.tabs.StudentMenuTab
import io.github.sushaant.messq.ui.customer.tabs.StudentScanTab
import io.github.sushaant.messq.ui.customer.tabs.StudentSlotsTab
import io.github.sushaant.messq.ui.customer.tabs.StudentSuggestTab

sealed class StudentRoute(val route: String) {
    object Slots : StudentRoute("student_slots")
    object Menu : StudentRoute("student_menu")
    object Suggest : StudentRoute("student_suggest")
    object Scan : StudentRoute("student_scan")
}

@Composable
fun StudentNavGraph(
    navController: NavHostController,
    uiState: StudentUiState,
    onBookSlot: (SlotDto) -> Unit,
    onToggleSelectMenuItem: (String) -> Unit,
    onRecommendMeals: (tags: String, targetCalories: Int?, minProtein: Double?) -> Unit,
    onQrPayloadDetected: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = StudentRoute.Slots.route
    ) {
        composable(StudentRoute.Slots.route) {
            StudentSlotsTab(
                uiState = uiState,
                onBookSlot = onBookSlot
            )
        }
        composable(StudentRoute.Menu.route) {
            StudentMenuTab(
                uiState = uiState,
                onToggleSelectItem = onToggleSelectMenuItem
            )
        }
        composable(StudentRoute.Suggest.route) {
            StudentSuggestTab(
                uiState = uiState,
                onRecommendMeals = onRecommendMeals
            )
        }
        composable(StudentRoute.Scan.route) {
            StudentScanTab(
                uiState = uiState,
                onQrPayloadDetected = onQrPayloadDetected
            )
        }
    }
}