package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("NUMBER")
data class NumberPlaceholder(
    val value: Double
) : Placeholder
