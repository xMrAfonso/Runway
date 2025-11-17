package me.mrafonso.runway.config.placeholder.conditional

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.mrafonso.runway.config.placeholder.Placeholder

@Serializable
@SerialName("CONDITIONAL")
data class ConditionalPlaceholder(
    val condition: String,
    val ifTrue: String,

    @SerialName("else")
    val ifElse: String? = null
) : Placeholder
