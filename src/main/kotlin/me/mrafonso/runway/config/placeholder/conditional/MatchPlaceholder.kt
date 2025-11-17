package me.mrafonso.runway.config.placeholder.conditional

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.mrafonso.runway.config.placeholder.Placeholder

@Serializable
@SerialName("SWITCH")
data class MatchPlaceholder(
    val input: String,
    val case: Map<String, String>,
    val default: String
) : Placeholder
