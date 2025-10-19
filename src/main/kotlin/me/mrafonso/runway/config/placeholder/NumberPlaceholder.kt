package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("NUMBER")
data class NumberPlaceholder(
    override val group: String? = null,
    val value: Double
) : Placeholder
