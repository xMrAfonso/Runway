package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TEXT")
data class TextPlaceholder(
    val value: String,
    override val sanitized: Boolean = false
) : Placeholder
