package io.github.sushaant.messq.data.repository

import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.from
import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.common.safeApiCall
import io.github.sushaant.messq.data.local.room.dao.MenuDao
import io.github.sushaant.messq.data.local.room.entity.MenuItemEntity
import io.github.sushaant.messq.data.model.MenuDto
import io.github.sushaant.messq.data.remote.SupabaseClientInstance
import io.github.sushaant.messq.domain.repository.MenuRepository
import io.ktor.client.call.body
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

class MenuRepositoryImpl(
    private val menuDao: MenuDao
) : MenuRepository {

    @Serializable
    private data class NutritionEstimateRequest(
        val name: String,
        val category: String,
        val description: String?
    )

    @Serializable
    private data class EstimatedNutritionPayload(
        val calories: Int = 350,
        val protein_grams: Double = 12.0,
        val carbs_grams: Double = 45.0,
        val fat_grams: Double = 12.0,
        val ai_tags: List<String> = emptyList(),
        val ai_explanation: String? = null
    )

    @Serializable
    private data class EdgeNutritionResponse(
        val success: Boolean = false,
        val data: EstimatedNutritionPayload? = null
    )

    @Serializable
    private data class MenuItemUpsertDto(
        val id: String? = null,
        val name: String,
        val category: String,
        val description: String?,
        val calories: Int,
        val protein_grams: Double,
        val carbs_grams: Double,
        val fat_grams: Double,
        val ai_tags: List<String>,
        val ai_explanation: String?,
        val is_available: Boolean
    )

    override fun getCachedMenus(): Flow<List<MenuItemEntity>> {
        return menuDao.getCachedMenus()
    }

    override suspend fun refreshMenusFromCloud(): Resource<Unit> {
        return safeApiCall {
            val response = SupabaseClientInstance.client
                .from("menu_items")
                .select()
                .decodeList<MenuDto>()

            val entities = response.map { dto ->
                MenuItemEntity(
                    id = dto.id,
                    name = dto.name,
                    category = dto.category,
                    description = dto.description,
                    calories = dto.calories,
                    proteinGrams = dto.protein_grams,
                    isAvailable = dto.is_available
                )
            }

            menuDao.insertMenus(entities)
            menuDao.enforceSevenRecordLimit()
            Unit
        }
    }

    override suspend fun saveMenuItemWithAiNutrition(
        existingId: String?,
        name: String,
        category: String,
        description: String?,
        isAvailable: Boolean
    ): Resource<Unit> {
        return safeApiCall {
            // 1. Call Supabase Edge Function to approximate nutritional values with Gemini AI
            val edgeResult = SupabaseClientInstance.client.functions
                .invoke(
                    function = "estimate-menu-nutrition",
                    body = NutritionEstimateRequest(
                        name = name,
                        category = category,
                        description = description
                    )
                )
                .body<EdgeNutritionResponse>()

            val est = edgeResult.data ?: EstimatedNutritionPayload()

            // 2. Insert new dish or update existing dish version in 'menu_items'
            val payload = MenuItemUpsertDto(
                id = existingId,
                name = name,
                category = category,
                description = description,
                calories = est.calories,
                protein_grams = est.protein_grams,
                carbs_grams = est.carbs_grams,
                fat_grams = est.fat_grams,
                ai_tags = est.ai_tags,
                ai_explanation = est.ai_explanation,
                is_available = isAvailable
            )

            if (existingId != null) {
                SupabaseClientInstance.client
                    .from("menu_items")
                    .update(payload) {
                        filter { eq("id", existingId) }
                    }
            } else {
                SupabaseClientInstance.client
                    .from("menu_items")
                    .insert(payload)
            }

            // 3. Refresh local Room cache so saved/updated menu items appear right away
            val latestMenus = SupabaseClientInstance.client
                .from("menu_items")
                .select()
                .decodeList<MenuDto>()
                .map { dto ->
                    MenuItemEntity(
                        id = dto.id,
                        name = dto.name,
                        category = dto.category,
                        description = dto.description,
                        calories = dto.calories,
                        proteinGrams = dto.protein_grams,
                        isAvailable = dto.is_available
                    )
                }

            menuDao.insertMenus(latestMenus)
            menuDao.enforceSevenRecordLimit()
            Unit
        }
    }
}