package me.mrafonso.runway.config

import dev.triumphteam.polaris.annotation.SerialComment
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val debug: Boolean = false,

    val prefix: Prefix = Prefix(),

    @SerialComment(["Whether to disable italics in names and lores of items. (default: true)"])
    var disableItalics: Boolean = true,

    val miniPlaceholders: MiniPlaceholders = MiniPlaceholders(),

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
    data class MiniPlaceholders(
        @SerialComment([
            "How often Runway refreshes MiniPlaceholders audience global placeholders, in seconds. (default: 30)",
            "Set to 0 or below to refresh every parse."
        ])
        var audienceGlobalPlaceholdersRefreshSeconds: Long = 30
    )

    @Serializable
    data class Listeners(
        val chat: Chat = Chat(),
        @SerialComment(["Whether to parse system messages, also known as plugin messages. (default: true)"])
        var systemMessages: Boolean = true,
        var tablist: Boolean = true,
        var titles: Boolean = true,
        @SerialComment(["Whether to parse dialogs and all text/items inside them. (default: true)"])
        var dialogs: Boolean = true,
        var scoreboards: Boolean = true,
        val inventory: Inventory = Inventory(),
        var items: Boolean = true
    )

    @Serializable
    data class Chat(
        @SerialComment(["Whether to parse player chat messages. (default: true)"])
        var enabled: Boolean = true,

        @SerialComment([
            "Whether player chat content should be automatically sanitized.",
            "If false, player chat content can use the same tags as the chat renderer. (default: true)"
        ])
        var sanitize: Boolean = true
    )

    @Serializable
    data class Inventory(
        var title: Boolean = true,
        var items: Boolean = true
    )
}
