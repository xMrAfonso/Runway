package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.Serializable

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
                        case = listOf(
                            CaseData(
                                comparison = "Yes",
                                output = "You said yes!"
                            ),
                            CaseData(
                                comparison = "No",
                                output = "You said no!"
                            )
                        ),
                        default = "You are undecided."
                    ),
                    "switch" to SwitchPlaceholder(
                        input = "100",
                        case = listOf(
                            CaseData(
                                comparison = "> 100",
                                output = "Way too much!"
                            ),
                            CaseData(
                                comparison = "< 100",
                                output = "Not enough!"
                            )
                        ),
                        default = "Balanced like the universe."
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
