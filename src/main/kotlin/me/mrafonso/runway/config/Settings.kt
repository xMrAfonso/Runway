package me.mrafonso.runway.config

import dev.triumphteam.polaris.annotation.SerialComment
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val debug: Boolean = false,

    val prefix: Prefix = Prefix(),

    @SerialComment(["Whether to disable italics in names and lores of items. (default: true)"])
    var disableItalics: Boolean = true,

    @SerialComment(["Listeners that Runway will listen to and intercept their packets."])
    val listeners: Listeners = Listeners()
) {

    @Serializable
    data class Prefix(
        @SerialComment([
            "Defines if a prefix is required for texts to be parsed by Runway. (default: true)",
            "If false, all texts will be parsed but you can block it by using '!PREFIX'.",
        ])
        var required: Boolean = true,

        @SerialComment(["The prefix used to identify texts to be parsed by Runway. (default: $)"])
        var value: String = "$",
    )

    @Serializable
    data class Listeners(
        @SerialComment(["Whether to parse system messages, also known as plugin messages. (default: true)"])
        var systemMessages: Boolean = true,
        var tablist: Boolean = true,
        var titles: Boolean = true,
        var scoreboards: Boolean = true,
        val inventory: Inventory = Inventory(),
        var items: Boolean = true
    )

    @Serializable
    data class Inventory(
        var title: Boolean = true,
        var items: Boolean = true
    )
}
