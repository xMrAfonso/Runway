package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.Serializable
import me.mrafonso.runway.config.placeholder.conditional.ConditionalPlaceholder
import me.mrafonso.runway.config.placeholder.conditional.MatchPlaceholder

@Serializable
data class Group(
    val prefix: String? = null,
    val condition: String? = null,
    val placeholders: Map<String, Placeholder> = emptyMap()
) {
    companion object {
        fun template(): Group {
            return Group(
                prefix = "example",
                condition = "true",
                placeholders = mapOf(
                    "text" to TextPlaceholder("Hello world!"),
                    "number" to NumberPlaceholder(0.0),
                    "match" to MatchPlaceholder(
                        input = "Maybe",
                        case = mapOf(
                            "I don\'t know" to "Result for case 1",
                            "Maybe" to "Result for case 2"
                        ),
                        default = "Default result"
                    ),
                    "conditional" to ConditionalPlaceholder(
                        condition = "true",
                        ifTrue = "Condition was true",
                        ifElse = "Condition was false"
                    )
                )
            )
        }
    }
}
