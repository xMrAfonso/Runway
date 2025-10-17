package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TEXT")
data class SinglePlaceholder(
    val value: String
) : IPlaceholder
