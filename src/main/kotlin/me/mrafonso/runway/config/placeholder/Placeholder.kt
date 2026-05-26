package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.Serializable
@Serializable
sealed interface Placeholder {
    val sanitized: Boolean
}
