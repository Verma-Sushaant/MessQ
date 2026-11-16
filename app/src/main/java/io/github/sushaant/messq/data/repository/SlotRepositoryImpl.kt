package io.github.sushaant.messq.data.repository

import io.github.jan.supabase.postgrest.from
import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.common.safeApiCall
import io.github.sushaant.messq.data.model.SlotBookingInsertDto
import io.github.sushaant.messq.data.model.SlotDto
import io.github.sushaant.messq.data.remote.SupabaseClientInstance
import io.github.sushaant.messq.domain.repository.SlotRepository

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
            // Insert booking record into 'slot_bookings'
            SupabaseClientInstance.client
                .from("slot_bookings")
                .insert(
                    SlotBookingInsertDto(
                        user_id = userId,
                        slot_date = slotDate,
                        slot_time = slotTime
                    )
                )

            // Note: Supabase atomic RPC (Remote Procedure Call) or trigger
            // should ideally increment 'current_booked_count' in 'slots' table.

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
            // Staff-only operation governed by RLS policies
            SupabaseClientInstance.client
                .from("slots")
                .update(
                    {
                        // Pass updated fields mapping your Supabase table schema
                        // e.g., max_capacity = newMaxCapacity, etc.
                    }
                ) {
                    filter { eq("id", slotId) }
                }

            Unit
        }
    }
}