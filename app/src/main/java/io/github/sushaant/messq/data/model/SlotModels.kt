package io.github.sushaant.messq.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SlotDto(
    val id: String,
    val meal_type: String,       // e.g., "Lunch", "Dinner"
    val start_time: String,      // e.g., "12:30:00"
    val end_time: String,        // e.g., "13:00:00"
    val max_capacity: Int,
    val current_booked_count: Int,
    val slot_date: String
)

@Serializable
data class SlotBookingInsertDto(
    val user_id: String,
    val slot_date: String,
    val slot_time: String,
    val status: String = "CONFIRMED"
)