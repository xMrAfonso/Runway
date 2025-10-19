package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("INDEXED")
data class IndexedPlaceholder(
    override val group: String? = null,
    val value: List<String> = mutableListOf()
) : Placeholder
