package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.Serializable

@Serializable
data class Group(
    val prefix: String? = null,
    val condition: String? = null
)
