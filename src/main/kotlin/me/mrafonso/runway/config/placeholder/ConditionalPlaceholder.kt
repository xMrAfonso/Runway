package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("CONDITIONAL")
data class ConditionalPlaceholder(
    override val group: String? = null,
    val condition: String,
    val ifTrue: String,

    @SerialName("else")
    val ifElse: String? = null
) : Placeholder
