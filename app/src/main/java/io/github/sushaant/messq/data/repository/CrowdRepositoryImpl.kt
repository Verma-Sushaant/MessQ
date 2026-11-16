package io.github.sushaant.messq.data.repository

import io.github.jan.supabase.postgrest.from
import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.common.safeApiCall
import io.github.sushaant.messq.data.model.AiPredictionDto
import io.github.sushaant.messq.data.model.CrowdRecordDto
import io.github.sushaant.messq.data.remote.SupabaseClientInstance
import io.github.sushaant.messq.domain.repository.CrowdRepository

class CrowdRepositoryImpl : CrowdRepository {

    override suspend fun getRecordedCrowdTelemetry(date: String): Resource<List<CrowdRecordDto>> {
        return safeApiCall {
            // Queries past actuals from 'crowd_records' table
            SupabaseClientInstance.client
                .from("crowd_records")
                .select {
                    filter { eq("slot_date", date) }
                }
                .decodeList<CrowdRecordDto>()
        }
    }

    override suspend fun getAiCrowdPredictions(): Resource<List<AiPredictionDto>> {
        return safeApiCall {
            // Queries forecasted AI insights from 'ai_crowd_predictions' table[cite: 5]
            SupabaseClientInstance.client
                .from("ai_crowd_predictions")
                .select()
                .decodeList<AiPredictionDto>()
        }
    }
}