package io.github.sushaant.messq.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CrowdRecordDto(
    val id: String,
    val slot_date: String,
    val slot_time: String,
    val congestion_score: Int,
    val crowd_level: String,       // 'LOW', 'MODERATE', 'HIGH'
    val is_actual: Boolean         // true = Recorded (past), false = Predicted
)

@Serializable
data class AiPredictionDto(
    val id: String,
    val prediction_time: String,
    val crowd_level: String,
    val expected_wait_minutes: Int,
    val ai_insight_summary: String?
)