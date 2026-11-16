package io.github.sushaant.messq.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.sushaant.messq.data.local.room.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
    @Query("SELECT * FROM cached_menus ORDER BY cachedAt DESC")
    fun getCachedMenus(): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenus(menu: List<MenuItemEntity>)

    @Query("DELETE FROM cached_menus WHERE id NOT IN (SELECT id FROM cached_menus ORDER BY cachedAt DESC LIMIT 7)")
    suspend fun enforceSevenRecordLimit()

    @Query("DELETE FROM cached_menus")
    suspend fun clearCache()
}