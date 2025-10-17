package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("INDEXED")
data class IndexedPlaceholder(
    val value: List<String> = mutableListOf()
) : IPlaceholder
