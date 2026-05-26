package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("CONDITIONAL")
data class ConditionalPlaceholder(
    val condition: String,

    @SerialName("if-true")
    val ifTrue: String,

    @SerialName("else")
    val ifElse: String? = null,

    override val sanitized: Boolean = false
) : Placeholder
