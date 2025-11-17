package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("RANDOM")
data class RandomPlaceholder(
    val value: List<String> = mutableListOf()
) : Placeholder
