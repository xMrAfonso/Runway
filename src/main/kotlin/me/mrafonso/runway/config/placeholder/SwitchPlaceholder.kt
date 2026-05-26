package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SWITCH")
data class SwitchPlaceholder(
    val input: String,
    val case: List<CaseData>,
    val default: String,
    override val sanitized: Boolean = false
) : Placeholder
