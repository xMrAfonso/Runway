package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.Serializable

@Serializable
public sealed interface Placeholder {
    val group: String?
}
