package io.github.sushaant.messq.data.repository

import io.github.jan.supabase.postgrest.from
import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.common.safeApiCall
import io.github.sushaant.messq.data.model.SlotBookingInsertDto
import io.github.sushaant.messq.data.model.SlotDto
import io.github.sushaant.messq.data.remote.SupabaseClientInstance
import io.github.sushaant.messq.domain.repository.SlotRepository
import io.github.sushaant.messq.exception.MessQException
import kotlinx.serialization.Serializable

class SlotRepositoryImpl : SlotRepository {

    override suspend fun getSlotsForDate(date: String): Resource<List<SlotDto>> {
        return safeApiCall {
            SupabaseClientInstance.client
                .from("slots")
                .select {
                    filter { eq("slot_date", date) }
                }
                .decodeList<SlotDto>()
        }
    }

    override suspend fun bookSlot(userId: String, slotId: String, slotDate: String, slotTime: String): Resource<Unit> {
        return safeApiCall {
            // Verify capacity before attempting insert
            @Serializable
            data class SlotCapacityCheck(val max_capacity: Int, val current_booked_count: Int)

            val slotInfo = SupabaseClientInstance.client
                .from("slots")
                .select {
                    filter { eq("id", slotId) }
                }
                .decodeSingle<SlotCapacityCheck>()

            if (slotInfo.current_booked_count >= slotInfo.max_capacity) {
                throw MessQException.DatabaseException("Selected slot is completely full.")
            }

            // Insert booking record (Supabase Trigger automatically increments current_booked_count!)
            SupabaseClientInstance.client
                .from("slot_bookings")
                .insert(
                    SlotBookingInsertDto(
                        user_id = userId,
                        slot_date = slotDate,
                        slot_time = slotTime
                    )
                )

            Unit
        }
    }

    override suspend fun updateSlotConfig(
        slotId: String,
        newMaxCapacity: Int,
        newStartTime: String,
        newEndTime: String
    ): Resource<Unit> {
        return safeApiCall {
            // Staff-only operation to modify slot timings or capacity (e.g., weekend extensions)
            @Serializable
            data class SlotUpdateDto(
                val max_capacity: Int,
                val start_time: String,
                val end_time: String
            )

            SupabaseClientInstance.client
                .from("slots")
                .update(
                    SlotUpdateDto(
                        max_capacity = newMaxCapacity,
                        start_time = newStartTime,
                        end_time = newEndTime
                    )
                ) {
                    filter { eq("id", slotId) }
                }
            Unit
        }
    }
}