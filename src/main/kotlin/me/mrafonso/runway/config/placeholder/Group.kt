package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Group(
    val prefix: String? = null,
    val condition: String? = null,
    @SerialName("legacy-placeholders")
    val legacyPlaceholders: Map<String, String> = emptyMap(),
    @SerialName("placeholders")
    val typedPlaceholders: Map<String, Placeholder> = emptyMap()
) {
    constructor(
        prefix: String? = null,
        condition: String? = null,
        placeholders: Map<String, Placeholder>
    ) : this(prefix, condition, emptyMap(), placeholders)

    val placeholders: Map<String, Placeholder>
        get() = legacyPlaceholders.mapValues { (_, value) -> TextPlaceholder(value) } + typedPlaceholders

    companion object {
        fun template(): Group {
            return Group(
                prefix = "example",
                condition = "true",
                typedPlaceholders = mapOf(
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
