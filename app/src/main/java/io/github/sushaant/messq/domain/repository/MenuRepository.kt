package io.github.sushaant.messq.domain.repository

import io.github.sushaant.messq.common.Resource
import io.github.sushaant.messq.data.local.room.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    // Fetches menus with conditional version checking and local Room fallback
    fun getCachedMenus(): Flow<List<MenuItemEntity>>
    suspend fun refreshMenusFromCloud(): Resource<Unit>
}