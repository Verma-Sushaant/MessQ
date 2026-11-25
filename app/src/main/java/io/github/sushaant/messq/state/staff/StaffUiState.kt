package io.github.sushaant.messq.state.staff

import io.github.sushaant.messq.data.local.room.entity.MenuItemEntity
import io.github.sushaant.messq.data.model.AiPredictionDto
import io.github.sushaant.messq.data.model.CrowdRecordDto
import io.github.sushaant.messq.data.model.SlotDto

data class StaffUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Slots & Active Counter QR
    val todaySlots: List<SlotDto> = emptyList(),
    val selectedSlotForQr: SlotDto? = null,
    val gateQrPayload: String? = null,
    val walkInCountsBySlotTime: Map<String, Int> = emptyMap(),

    // Live Telemetry & AI Insights
    val crowdRecords: List<CrowdRecordDto> = emptyList(),
    val latestAiPrediction: AiPredictionDto? = null,

    // Menu Items Overview
    val menuItems: List<MenuItemEntity> = emptyList()
)