package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.Serializable

@Serializable
data class Placeholders(
    val placeholders: Map<String, IPlaceholder> = mutableMapOf(
        "player" to GroupPlaceholder(
            value = mutableMapOf(
                "name" to SinglePlaceholder(
                    value = "Afonso"
                ),
                "isOnline" to ConditionalPlaceholder(
                    condition = "true",
                    ifTrue = SinglePlaceholder(
                        value = "Yes"
                    ),
                    ifElse = SinglePlaceholder(
                        value = "No"
                    )
                ),
                "cascade" to ConditionalPlaceholder(
                    condition = "false",
                    ifTrue = IndexedPlaceholder(
                        value = listOf("Value1", "Value2", "Value3")
                    )
                )
            )
        )
    )
)