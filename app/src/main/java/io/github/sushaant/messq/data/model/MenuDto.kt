package io.github.sushaant.messq.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MenuDto(
    val id: String,
    val name: String,
    val category: String,
    val description: String? = null,
    val calories: Int? = null,
    val protein_grams: Double? = null,
    val is_available: Boolean = true
)