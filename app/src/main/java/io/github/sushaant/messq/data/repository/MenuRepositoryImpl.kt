package io.github.sushaant.messq.data.repository

import io.github.jan.supabase.postgrest.from
import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.common.safeApiCall
import io.github.sushaant.messq.data.local.room.dao.MenuDao
import io.github.sushaant.messq.data.local.room.entity.MenuItemEntity
import io.github.sushaant.messq.data.model.MenuDto
import io.github.sushaant.messq.data.remote.SupabaseClientInstance
import io.github.sushaant.messq.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow

class MenuRepositoryImpl(
    private val menuDao: MenuDao
): MenuRepository {

    override fun getCachedMenus(): Flow<List<MenuItemEntity>> {
        return menuDao.getCachedMenus()
    }

    override suspend fun refreshMenusFromCloud(): Resource<Unit> {
        return safeApiCall {
            // Fetch remote rows from Supabase table 'menu_items'
            val response = SupabaseClientInstance.client
                .from("menu_items")
                .select()
                .decodeList<MenuDto>()

            // Map DTOs to Room Entities
            val entities = response.map { dto ->
                MenuItemEntity(
                    id = dto.id,
                    name = dto.name,
                    category = dto.category,
                    description = dto.description,
                    calories = dto.calories,
                    proteinGrams = dto.protein_grams,
                    isAvailable = dto.is_available,
                )
            }

            // Update room local cache and enforce 7-record rolling limit
            menuDao.insertMenus(entities)
            menuDao.enforceSevenRecordLimit()

            Unit
        }
    }
}