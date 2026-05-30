package me.mrafonso.runway.config.placeholder

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
class Group(
    val prefix: String? = null,
    val condition: String? = null,
    @SerialName("text-placeholders")
    @Serializable(with = TextPlaceholdersSerializer::class)
    val textPlaceholders: Map<String, TextPlaceholder> = emptyMap(),
    @SerialName("placeholders")
    val typedPlaceholders: Map<String, Placeholder> = emptyMap()
) {
    constructor(
        prefix: String? = null,
        condition: String? = null,
        placeholders: Map<String, Placeholder>
    ) : this(prefix, condition, emptyMap(), placeholders)

    val placeholders: Map<String, Placeholder>
        get() = textPlaceholders + typedPlaceholders

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

private object TextPlaceholdersSerializer : KSerializer<Map<String, TextPlaceholder>> {
    private val delegate = MapSerializer(String.serializer(), String.serializer())

    override val descriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): Map<String, TextPlaceholder> {
        return delegate.deserialize(decoder).mapValues { (_, value) -> TextPlaceholder(value) }
    }

    override fun serialize(encoder: Encoder, value: Map<String, TextPlaceholder>) {
        delegate.serialize(encoder, value.mapValues { (_, placeholder) -> placeholder.value })
    }
}
