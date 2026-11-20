package io.github.sushaant.messq.viewmodel.staff

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.data.model.SlotDto
import io.github.sushaant.messq.domain.repository.CrowdRepository
import io.github.sushaant.messq.domain.repository.MenuRepository
import io.github.sushaant.messq.domain.repository.SlotRepository
import io.github.sushaant.messq.state.staff.StaffUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class StaffViewModel(
    private val slotRepository: SlotRepository,
    private val crowdRepository: CrowdRepository,
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StaffUiState())
    val uiState: StateFlow<StaffUiState> = _uiState.asStateFlow()

    private var autoSlotTickerJob: Job? = null
    private var manualOverrideSlotId: String? = null

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    init {
        loadStaffDashboardData(LocalDate.now().toString())
        startAutoSlotSync()
    }

    /**
     * Periodically checks current time so the active slot & Gate QR auto-update
     * as the mess moves across slots throughout the day.
     */
    private fun startAutoSlotSync() {
        autoSlotTickerJob?.cancel()
        autoSlotTickerJob = viewModelScope.launch {
            while (isActive) {
                delay(30_000L) // Check every 30 seconds
                syncActiveSlotWithCurrentTime()
            }
        }
    }

    fun loadStaffDashboardData(date: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (val slotRes = slotRepository.getSlotsForDate(date)) {
                is Resource.Success -> {
                    val sortedSlots = slotRes.data.sortedBy { it.start_time }
                    val activeSlot = resolveSlotForCurrentTime(sortedSlots)
                    _uiState.update {
                        it.copy(
                            todaySlots = sortedSlots,
                            selectedSlotForQr = activeSlot,
                            gateQrPayload = activeSlot?.let(::buildGateQrPayload)
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(errorMessage = slotRes.exception.localizedMessage ?: "Failed to load slots")
                    }
                }
                Resource.Loading -> Unit
            }

            when (val telemetryRes = crowdRepository.getRecordedCrowdTelemetry(date)) {
                is Resource.Success -> _uiState.update { it.copy(crowdRecords = telemetryRes.data) }
                else -> Unit
            }

            when (val aiRes = crowdRepository.getAiCrowdPredictions()) {
                is Resource.Success -> _uiState.update { it.copy(latestAiPrediction = aiRes.data.firstOrNull()) }
                else -> Unit
            }

            menuRepository.refreshMenusFromCloud()
            viewModelScope.launch {
                menuRepository.getCachedMenus().collect { items ->
                    _uiState.update { it.copy(menuItems = items, isLoading = false) }
                }
            }
        }
    }

    fun syncActiveSlotWithCurrentTime() {
        val currentSlots = _uiState.value.todaySlots
        if (currentSlots.isEmpty()) return

        val resolved = resolveSlotForCurrentTime(currentSlots)
        if (resolved?.id != _uiState.value.selectedSlotForQr?.id ||
            buildGateQrPayloadOrNull(resolved) != _uiState.value.gateQrPayload
        ) {
            _uiState.update {
                it.copy(
                    selectedSlotForQr = resolved,
                    gateQrPayload = resolved?.let(::buildGateQrPayload)
                )
            }
        }
    }

    /**
     * Optional manual override if staff explicitly taps another slot on the QR screen.
     */
    fun selectSlotForGateQr(slot: SlotDto) {
        manualOverrideSlotId = slot.id
        _uiState.update {
            it.copy(
                selectedSlotForQr = slot,
                gateQrPayload = buildGateQrPayload(slot)
            )
        }
    }

    fun clearManualSlotOverride() {
        manualOverrideSlotId = null
        syncActiveSlotWithCurrentTime()
    }

    fun updateSlotConfig(
        slot: SlotDto,
        newMaxCapacity: Int,
        newStartTime: String,
        newEndTime: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val res = slotRepository.updateSlotConfig(slot.id, newMaxCapacity, newStartTime, newEndTime)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, successMessage = "${slot.meal_type} slot updated")
                    }
                    loadStaffDashboardData(slot.slot_date)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = res.exception.localizedMessage ?: "Failed to update slot"
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun resolveSlotForCurrentTime(slots: List<SlotDto>): SlotDto? {
        if (slots.isEmpty()) return null

        manualOverrideSlotId?.let { overrideId ->
            slots.firstOrNull { it.id == overrideId }?.let { return it }
        }

        val nowStr = LocalTime.now().format(timeFormatter)

        // 1. Slot currently active right now
        slots.firstOrNull { slot -> nowStr >= slot.start_time && nowStr <= slot.end_time }?.let { return it }

        // 2. Next upcoming slot today
        slots.firstOrNull { slot -> slot.start_time > nowStr }?.let { return it }

        // 3. Fallback to last slot of the day
        return slots.last()
    }

    private fun buildGateQrPayload(slot: SlotDto): String =
        "MESSQ|MAIN|${slot.slot_date}|${slot.start_time}"

    private fun buildGateQrPayloadOrNull(slot: SlotDto?): String? =
        slot?.let(::buildGateQrPayload)

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}