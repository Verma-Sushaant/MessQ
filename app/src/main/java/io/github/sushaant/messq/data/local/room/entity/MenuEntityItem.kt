package io.github.sushaant.messq.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_menus")
data class MenuEntityItem(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val description: String?,
    val calories: Int?,
    val proteinGrams: Double?,
    val isAvailable: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)