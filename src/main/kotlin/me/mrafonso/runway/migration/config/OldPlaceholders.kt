package me.mrafonso.runway.migration.config

import kotlinx.serialization.Serializable

@Serializable
data class OldPlaceholders(
    val placeholders: Map<String, String> = mutableMapOf()
)
