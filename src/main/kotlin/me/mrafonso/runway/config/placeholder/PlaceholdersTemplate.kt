package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.Serializable

@Serializable
data class PlaceholdersTemplate(
    val groups: Map<String, Group> = mapOf(
        "player" to Group(
            prefix = "p"
        )
    ),
    val placeholders: Map<String, Placeholder> = mutableMapOf(
        "name" to TextPlaceholder(
            group = "player",
            value = "Afonso"
        ),
        "is_online" to ConditionalPlaceholder(
            group = "player",
            condition = "true",
            ifTrue = "Yes",
            ifElse = "No"
        ),
        "cascade" to ConditionalPlaceholder(
            group = "player",
            condition = "false",
            ifTrue = "Value1",
        )
    )
)