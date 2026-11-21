package io.github.sushaant.messq.domain.repository

import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.data.local.room.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    // Fetches menus with conditional version checking and local Room fallback
    fun getCachedMenus(): Flow<List<MenuItemEntity>>
    suspend fun refreshMenusFromCloud(): Resource<Unit>
    /**
     * Automatically approximates nutrition via AI (Supabase Edge Function 'estimate-menu-nutrition')
     * when adding a new dish or updating an existing dish version, then saves to 'menu_items'
     * and refreshes Room cache.
     */
    suspend fun saveMenuItemWithAiNutrition(
        existingId: String? = null,
        name: String,
        category: String,
        description: String?,
        isAvailable: Boolean = true
    ): Resource<Unit>
}