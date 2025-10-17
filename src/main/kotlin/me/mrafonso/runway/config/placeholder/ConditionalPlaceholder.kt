package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("CONDITIONAL")
data class ConditionalPlaceholder(
    val condition: String,
    val ifTrue: IPlaceholder,

    @SerialName("else")
    val ifElse: IPlaceholder? = null
) : IPlaceholder
