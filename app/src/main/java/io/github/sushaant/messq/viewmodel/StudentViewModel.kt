package io.github.sushaant.messq.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.data.model.SlotDto
import io.github.sushaant.messq.domain.repository.CrowdRepository
import io.github.sushaant.messq.domain.repository.MenuRepository
import io.github.sushaant.messq.domain.repository.SlotRepository
import io.github.sushaant.messq.state.StudentUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StudentViewModel(
    private val slotRepository: SlotRepository,
    private val menuRepository: MenuRepository,
    private val crowdRepository: CrowdRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentUiState())
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    init {
        loadStudentDashboardData(date = "2026-11-18") // Pass current date or dynamic date
    }

    fun loadStudentDashboardData(date: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Fetch slots for date
            when (val slotResult = slotRepository.getSlotsForDate(date)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(availableSlots = slotResult.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = slotResult.exception.localizedMessage ?: "Failed to load slots") }
                }
                Resource.Loading -> {}
            }

            // 2. Refresh menus from cloud to local cache
            menuRepository.refreshMenusFromCloud()

            // 3. Fetch latest AI crowd predictions
            when (val crowdResult = crowdRepository.getAiCrowdPredictions()) {
                is Resource.Success -> {
                    val latest = crowdResult.data.firstOrNull()
                    _uiState.update { it.copy(latestAiPrediction = latest) }
                }
                is Resource.Error -> {
                    // Non-fatal error for telemetry
                }
                Resource.Loading -> {}
            }

            // 4. Observe cached menu items from Room DB
            viewModelScope.launch {
                menuRepository.getCachedMenus().collect { cachedItems ->
                    _uiState.update { it.copy(menuItems = cachedItems, isLoading = false) }
                }
            }
        }
    }

    fun bookSlot(userId: String, slot: SlotDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = slotRepository.bookSlot(userId, slot.id, slot.slot_date, slot.start_time)) {
                is Resource.Success -> {
                    // Generate QR Pass payload string for check-in: userId|slotDate|slotTime
                    val passPayload = "$userId|${slot.slot_date}|${slot.start_time}"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Slot successfully booked!",
                            activePassData = passPayload
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.localizedMessage ?: "Booking failed"
                        )
                    }
                }
                Resource.Loading -> {}
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}