package io.github.sushaant.messq.domain.repository

import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.data.model.AiPredictionDto
import io.github.sushaant.messq.data.model.CrowdRecordDto

interface CrowdRepository {
    suspend fun getRecordedCrowdTelemetry(date: String): Resource<List<CrowdRecordDto>>
    suspend fun getAiCrowdPredictions(): Resource<List<AiPredictionDto>>
}