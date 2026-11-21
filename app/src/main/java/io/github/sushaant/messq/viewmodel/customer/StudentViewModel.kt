package io.github.sushaant.messq.viewmodel.customer

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.data.model.SlotDto
import io.github.sushaant.messq.domain.repository.CrowdRepository
import io.github.sushaant.messq.domain.repository.MenuRepository
import io.github.sushaant.messq.domain.repository.SlotRepository
import io.github.sushaant.messq.domain.repository.AuthRepository
import io.github.sushaant.messq.state.customer.StudentUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class StudentViewModel(
    private val authRepository: AuthRepository,
    private val slotRepository: SlotRepository,
    private val menuRepository: MenuRepository,
    private val crowdRepository: CrowdRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentUiState())
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    init {
        loadStudentDashboardData(currentDateString())
    }

    fun currentDateString(): String = LocalDate.now().toString()

    fun currentTimeString(): String = LocalTime.now().format(timeFormatter)

    fun loadStudentDashboardData(date: String = currentDateString()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val slotResult = slotRepository.getSlotsForDate(date)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(availableSlots = slotResult.data.sortedBy { s -> s.start_time }) }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = slotResult.exception.localizedMessage ?: "Failed to load slots")
                    }
                }
                Resource.Loading -> Unit
            }

            menuRepository.refreshMenusFromCloud()

            when (val crowdResult = crowdRepository.getAiCrowdPredictions()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(latestAiPrediction = crowdResult.data.firstOrNull()) }
                }
                is Resource.Error -> Unit
                Resource.Loading -> Unit
            }

            viewModelScope.launch {
                menuRepository.getCachedMenus().collect { cachedItems ->
                    _uiState.update { it.copy(menuItems = cachedItems, isLoading = false) }
                }
            }
        }
    }

    fun bookSlot(slot: SlotDto, explicitUserId: String? = null) {
        viewModelScope.launch {
            val userId = explicitUserId ?: authRepository.getCurrentUserId()
            if (userId.isNullOrBlank()) {
                _uiState.update { it.copy(errorMessage = "Please sign in again to book a slot.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = slotRepository.bookSlot(userId, slot.id, slot.slot_date, slot.start_time)) {
                is Resource.Success -> {
                    val passPayload = "$userId|${slot.slot_date}|${slot.start_time}"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Booked ${slot.meal_type} (${slot.start_time})",
                            activePassData = passPayload
                        )
                    }
                    refreshSlotsOnly(slot.slot_date)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.localizedMessage ?: "Booking failed"
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    /**
     * Reads slot_date & slot_time from Staff QR (MESSQ|<GATE>|<SLOT_DATE>|<SLOT_TIME>),
     * falling back to device current date/time if omitted.
     */
    fun onStaffQrScanned(rawPayload: String, explicitUserId: String? = null) {
        viewModelScope.launch {
            val userId = explicitUserId ?: authRepository.getCurrentUserId()
            if (userId.isNullOrBlank()) {
                _uiState.update { it.copy(errorMessage = "User session not found. Please log in again.") }
                return@launch
            }

            val parts = rawPayload.trim().split("|")
            if (parts.isEmpty() || parts[0] != "MESSQ") {
                _uiState.update { it.copy(errorMessage = "Invalid MessQ QR code.") }
                return@launch
            }

            val slotDate = parts.getOrNull(2)?.takeIf { it.isNotBlank() } ?: currentDateString()
            val slotTime = parts.getOrNull(3)?.takeIf { it.isNotBlank() } ?: currentTimeString()

            _uiState.update { it.copy(isLoading = true, errorMessage = null, checkInResult = null) }
            when (val result = slotRepository.processStudentCheckIn(userId, slotDate, slotTime)) {
                is Resource.Success -> {
                    val message = when (result.data) {
                        "SUCCESS_BOOKED_CHECKIN" -> "Checked in for booked slot ($slotTime)"
                        "SUCCESS_WALKIN_LOGGED" -> "Walk-in recorded & checked in ($slotTime)"
                        "ALREADY_CHECKED_IN" -> "You are already checked in for this slot ($slotTime)"
                        "ERROR_SLOT_FULL" -> "Slot capacity is full"
                        "ERROR_NO_ACTIVE_SLOT" -> "No active slot found for $slotDate at $slotTime"
                        else -> result.data
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            checkInResult = message,
                            successMessage = message
                        )
                    }
                    refreshSlotsOnly(slotDate)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.localizedMessage ?: "Check-in failed"
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private suspend fun refreshSlotsOnly(date: String) {
        val res = slotRepository.getSlotsForDate(date)

        if (res is Resource.Success) {
            _uiState.update {
                it.copy(availableSlots = res.data.sortedBy { s -> s.start_time })
            }
        }
    }

    fun toggleMenuItemSelection(itemId: String) {
        _uiState.update { state ->
            val updatedIds = if (itemId in state.selectedMenuItemIds) {
                state.selectedMenuItemIds - itemId
            } else {
                state.selectedMenuItemIds + itemId
            }
            state.copy(selectedMenuItemIds = updatedIds)
        }
    }

    /**
     * Evaluates available menu items against student-entered nutritional tags/goals
     * (e.g., "high protein", "low calorie", "light", "heavy", or dish/category keywords)
     * and optional target calories / minimum protein, surfacing top matches + an AI-style summary.
     */
    fun recommendFoodByNutritionTags(
        nutritionTagsInput: String,
        targetCalories: Int? = null,
        minProteinGrams: Double? = null
    ) {
        val available = _uiState.value.menuItems.filter { it.isAvailable }
        if (available.isEmpty()) {
            _uiState.update {
                it.copy(
                    recommendedMenuItems = emptyList(),
                    aiRecommendationSummary = "No menu items available right now to analyze."
                )
            }
            return
        }

        val rawQuery = nutritionTagsInput.trim().lowercase()
        val tokens = rawQuery.split(",", " ", ";", "/").map { it.trim() }.filter { it.isNotEmpty() }

        val wantsHighProtein = "protein" in rawQuery || "high-protein" in rawQuery || "muscle" in rawQuery || "gym" in rawQuery
        val wantsLowCalorie = "low cal" in rawQuery || "low-calorie" in rawQuery || "low calorie" in rawQuery ||
                "light" in rawQuery || "diet" in rawQuery || "cut" in rawQuery || "loss" in rawQuery
        val wantsHighEnergy = "energy" in rawQuery || "heavy" in rawQuery || "bulk" in rawQuery || "carbs" in rawQuery

        val ranked = available.map { item ->
            val cal = item.calories ?: 0
            val protein = item.proteinGrams ?: 0.0
            var score = 0.0

            if (wantsHighProtein) {
                score += protein * 2.5
                if (protein >= 20.0) score += 20.0
            }

            if (wantsLowCalorie) {
                // Lower calories score higher when user asks for low calorie / light
                score += ((700 - cal).coerceAtLeast(0)) * 0.06
                if (cal in 1..400) score += 20.0
            }

            if (wantsHighEnergy) {
                score += (cal / 25.0)
            }

            if (!wantsHighProtein && !wantsLowCalorie && !wantsHighEnergy) {
                score += protein * 0.6
            }

            if (minProteinGrams != null && minProteinGrams > 0.0) {
                score += if (protein >= minProteinGrams) 25.0 else (protein / minProteinGrams) * 15.0
            }

            if (targetCalories != null && targetCalories > 0) {
                val delta = kotlin.math.abs(cal - targetCalories).toDouble()
                score += (25.0 - (delta / 25.0)).coerceAtLeast(0.0)
            }

            if (tokens.isNotEmpty()) {
                val searchable = "${item.name} ${item.category} ${item.description.orEmpty()}".lowercase()
                val matchedTokens = tokens.count { tok -> tok.length >= 2 && searchable.contains(tok) }
                score += matchedTokens * 15.0
            }

            item to score
        }
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }

        val topNames = ranked.joinToString(", ") { "${it.name} (${it.calories ?: 0} kcal, ${it.proteinGrams ?: 0.0}g P)" }
        val tagLabel = if (rawQuery.isNotBlank()) "\"$nutritionTagsInput\"" else "balanced nutrition"

        _uiState.update {
            it.copy(
                recommendedMenuItems = ranked,
                aiRecommendationSummary = "Based on $tagLabel, top picks: $topNames."
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null, checkInResult = null) }
    }
}