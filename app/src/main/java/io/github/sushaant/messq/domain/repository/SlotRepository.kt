package io.github.sushaant.messq.domain.repository

import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.data.model.SlotDto

interface SlotRepository {
    suspend fun getSlotsForDate(date: String): Resource<List<SlotDto>>
    suspend fun bookSlot(userId: String, slotId: String, slotDate: String, slotTime: String): Resource<Unit>
    suspend fun updateSlotConfig(slotId: String, newMaxCapacity: Int, newStartTime: String, newEndTime: String): Resource<Unit> // For Staff
    suspend fun processStudentCheckIn(userId: String, slotDate: String, slotTime: String): Resource<String>
}