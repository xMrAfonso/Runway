package me.mrafonso.runway.config

import dev.triumphteam.polaris.annotation.SerialComment
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val debug: Boolean = false,

    val requirePrefix: RequirePrefix = RequirePrefix(),

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
    data class RequirePrefix(
        @SerialComment(["Is [mm] required for packets to be parsed by MiniMessage? (default: true)"])
        val minimessage: Boolean = true,
        @SerialComment(["Is [p] required for packets to be parsed by PlaceholderAPI/MiniPlaceholders? (default: true)"])
        val placeholders: Boolean = true
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
