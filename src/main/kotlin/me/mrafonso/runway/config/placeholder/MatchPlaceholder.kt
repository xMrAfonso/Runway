package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MATCH")
data class MatchPlaceholder(
    val input: String,
    val case: List<CaseData>,
    val default: String,
    override val sanitized: Boolean = false
) : Placeholder
