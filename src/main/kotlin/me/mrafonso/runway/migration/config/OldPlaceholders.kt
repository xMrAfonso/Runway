package me.mrafonso.runway.migration.config

import kotlinx.serialization.Serializable

@Serializable
data class OldPlaceholders(
    val customPlaceholders: Map<String, String> = mutableMapOf("a" to "b")
)
