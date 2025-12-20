package me.mrafonso.runway.config.migration

import kotlinx.serialization.Serializable

@Serializable
data class OldPlaceholders(
    val placeholders: Map<String, String> = mutableMapOf()
)
