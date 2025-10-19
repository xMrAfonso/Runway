package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TEXT")
data class TextPlaceholder(
    override val group: String? = null,
    val value: String
) : Placeholder
