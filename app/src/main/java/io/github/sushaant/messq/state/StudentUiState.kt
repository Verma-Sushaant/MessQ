package io.github.sushaant.messq.state

import io.github.sushaant.messq.data.local.room.entity.MenuItemEntity
import io.github.sushaant.messq.data.model.AiPredictionDto
import io.github.sushaant.messq.data.model.SlotDto

data class StudentUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    // Live Slots & Menu
    val availableSlots: List<SlotDto> = emptyList(),
    val menuItems: List<MenuItemEntity> = emptyList(),

    // AI Prediction & Crowd
    val latestAiPrediction: AiPredictionDto? = null,

    // QR Pass Data (Generated upon successful booking)
    val activePassData: String? = null
)