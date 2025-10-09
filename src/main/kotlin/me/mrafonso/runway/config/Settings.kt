package me.mrafonso.runway.config

import dev.triumphteam.polaris.annotation.SerialComment
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val debug: Boolean = false,

    val prefixes: Prefixes = Prefixes(),

    @SerialComment([
        "Whether PlaceholderAPI or MiniPlaceholders should be hooked into Runway.",
        "Note: Both can be used at the same time."
    ])
    val placeholderHook: PlaceholderHook = PlaceholderHook(),

    @SerialComment(["Whether to disable italics in names and lores of items. (default: true)"])
    var disableItalics: Boolean = true,

    @SerialComment(["Listeners that Runway will listen to and intercept their packets."])
    val listeners: Listeners = Listeners()
) {

    @Serializable
    data class MiniMessagePrefix(
        @SerialComment(["Defines if a prefix is required for texts to be parsed by Runway."])
        val required: Boolean = true,

        @SerialComment(["The prefix used to identify texts to be parsed by Runway."])
        val prefix: String = "[mm]",
    )

    @Serializable
    data class PlaceholdersPrefix(
        @SerialComment(["Defines if a prefix is required for texts to be parsed by Runway."])
        val required: Boolean = true,

        @SerialComment(["The prefix used to identify texts to be parsed by PlaceholderAPI or MiniPlaceholders.",
            "Note: ",
            "- Placeholders will only be parsed if at least one hook is enabled.",
            "- If minimessage requires a prefix, this will only be parsed if the minimessage prefix is present."])
        val prefix: String = "[p]"
    )

    @Serializable
    data class Prefixes(
        val minimessage: MiniMessagePrefix = MiniMessagePrefix(),
        val placeholders: PlaceholdersPrefix = PlaceholdersPrefix()
    )

    @Serializable
    data class PlaceholderHook(
        var placeholderAPI: Boolean = true,
        var miniPlaceholders: Boolean = true
    )

    @Serializable
    data class Listeners(
        @SerialComment(["Whether to parse system messages, also known as plugin messages. (default: true)"])
        var systemMessages: Boolean = true,
    )
}
