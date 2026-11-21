package io.github.sushaant.messq.ui.staff

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.sushaant.messq.data.model.SlotDto
import io.github.sushaant.messq.state.staff.StaffUiState
import io.github.sushaant.messq.ui.staff.tabs.StaffCrowdTab
import io.github.sushaant.messq.ui.staff.tabs.StaffGateQrTab
import io.github.sushaant.messq.ui.staff.tabs.StaffMenuTab
import io.github.sushaant.messq.ui.staff.tabs.StaffSlotsTab

sealed class StaffRoute(val route: String) {
    object GateQr : StaffRoute("staff_qr")
    object Slots : StaffRoute("staff_slots")
    object Crowd : StaffRoute("staff_crowd")
    object Menu: StaffRoute("staff_menu")
}

@Composable
fun StaffNavGraph(
    navController: NavHostController,
    uiState: StaffUiState,
    onSelectSlotForQr: (SlotDto) -> Unit,
    onResetAutoSlot: () -> Unit,
    onUpdateSlotConfig: (slot: SlotDto, maxCap: Int, startTime: String, endTime: String) -> Unit,
    onSaveMenuItem: (existingId: String?, name: String, category: String, description: String?) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = StaffRoute.GateQr.route
    ) {
        composable(StaffRoute.GateQr.route) {
            StaffGateQrTab(
                uiState = uiState,
                onSelectSlot = onSelectSlotForQr,
                onResetAuto = onResetAutoSlot
            )
        }
        composable(StaffRoute.Slots.route) {
            StaffSlotsTab(
                slots = uiState.todaySlots,
                onUpdateSlot = onUpdateSlotConfig
            )
        }
        composable(StaffRoute.Crowd.route) {
            StaffCrowdTab(uiState = uiState)
        }
        composable(StaffRoute.Menu.route) {
            StaffMenuTab(
                menuItems = uiState.menuItems,
                onSaveMenuItem = onSaveMenuItem
            )
        }
    }
}