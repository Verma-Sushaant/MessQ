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
    private val slotRepository: SlotRepository,
    private val menuRepository: MenuRepository,
    private val crowdRepository: CrowdRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentUiState())
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    init {
        loadStudentDashboardData(currentDateString())
    }

    fun currentDateString(): String = LocalDate.now().toString()

    fun currentTimeString(): String =
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:00"))

    fun loadStudentDashboardData(date: String = currentDateString()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val slotResult = slotRepository.getSlotsForDate(date)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(availableSlots = slotResult.data) }
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

    fun bookSlot(userId: String, slot: SlotDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = slotRepository.bookSlot(userId, slot.id, slot.slot_date, slot.start_time)) {
                is Resource.Success -> {
                    val passPayload = "$userId|${slot.slot_date}|${slot.start_time}"
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Slot booked (${slot.meal_type} • ${slot.start_time})",
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
     * Parses Staff/Gate QR payload: MESSQ|<GATE>|<SLOT_DATE>|<SLOT_TIME>
     * Falls back to dynamic device date & time if fields are omitted.
     */
    fun onStaffQrScanned(rawPayload: String, userId: String) {
        val parts = rawPayload.trim().split("|")
        if (parts.isEmpty() || parts[0] != "MESSQ") {
            _uiState.update { it.copy(errorMessage = "Invalid MessQ QR code.") }
            return
        }

        val slotDate = parts.getOrNull(2)?.takeIf { it.isNotBlank() } ?: currentDateString()
        val slotTime = parts.getOrNull(3)?.takeIf { it.isNotBlank() } ?: currentTimeString()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, checkInResult = null) }
            when (val result = slotRepository.processStudentCheckIn(userId, slotDate, slotTime)) {
                is Resource.Success -> {
                    val message = when (result.data) {
                        "SUCCESS_BOOKED_CHECKIN" -> "Entry verified for booked slot ($slotTime)!"
                        "SUCCESS_WALKIN_LOGGED" -> "Walk-in recorded & checked in ($slotTime)!"
                        "ERROR_SLOT_FULL" -> "Slot capacity is full."
                        "ERROR_NO_ACTIVE_SLOT" -> "No active slot found for $slotDate at $slotTime."
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
                it.copy(availableSlots = res.data)
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null, checkInResult = null) }
    }
}