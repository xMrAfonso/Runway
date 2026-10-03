package me.mrafonso.runway.config

import dev.triumphteam.polaris.annotation.SerialComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    @SerialComment([
        "Logs placeholder loading and resolution details to the console. (default: false)",
        "Can also be toggled at runtime with /runway debug."
    ])
    var debug: Boolean = false,

    val prefix: Prefix = Prefix(),

    @SerialComment(["Whether to disable the default italics in custom item names and lores. (default: true)"])
    var disableItalics: Boolean = true,

    val miniPlaceholders: MiniPlaceholders = MiniPlaceholders(),

    @SerialName("placeholder-api")
    val placeholderApi: PlaceholderApi = PlaceholderApi(),

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
            "How often Runway refreshes MiniPlaceholders global placeholders, in seconds. (default: 30)"
        ])
        @SerialName("refresh-rate")
        var refreshRate: Long = 30
    )

    @Serializable
    data class PlaceholderApi(
        @SerialComment([
            "Format returned by %runway_<placeholder>% to other plugins. (default: MINIMESSAGE)",
            "MINIMESSAGE: MiniMessage with every tag closed, so gradients don't leak into surrounding text.",
            "LEGACY: Section-sign colour codes (hex included), for plugins that don't understand MiniMessage."
        ])
        var output: PlaceholderOutput = PlaceholderOutput.MINIMESSAGE
    )

    enum class PlaceholderOutput { MINIMESSAGE, LEGACY }

    @Serializable
    data class Listeners(
        val chat: Chat = Chat(),
        @SerialComment(["Whether to parse system messages, also known as plugin messages. (default: true)"])
        val systemMessages: Listener = Listener(),
        @SerialComment(["Whether to parse actionbar messages. (default: true)"])
        val actionbar: Listener = Listener(),
        @SerialComment(["Whether to parse bossbar titles. (default: true)"])
        val bossbar: Listener = Listener(),
        val tablist: Listener = Listener(),
        val titles: Listener = Listener(),
        @SerialComment(["Whether to parse dialogs and all text/items inside them. (default: true)"])
        val dialogs: Listener = Listener(),
        @SerialComment(["Whether to parse entity metadata text, including text displays and entity nameplates. (default: true)"])
        val entityText: Listener = Listener(),
        @SerialComment(["Whether to parse player names from scoreboard teams and player info packets. (default: true)"])
        val playerNames: Listener = Listener(),
        val inventory: Inventory = Inventory(),
        val items: Listener = Listener(),
        val console: Console = Console()
    )

    @Serializable
    data class Console(
        @SerialComment([
            "Whether to parse console output. (default: true)",
            "The prefix is always required here: only text starting at a prefix, at the start",
            "of a line or after a space, and containing a tag is parsed, e.g. '[Plugin] $<red>Hello'."
        ])
        var enable: Boolean = true
    )

    @Serializable
    data class Chat(
        @SerialComment(["Whether to parse player chat messages. (default: true)"])
        var enable: Boolean = true,

        @SerialName("require-prefix")
        var requirePrefix: Boolean = false,

        @SerialComment([
            "Whether player chat content should be automatically sanitized.",
            "If false, player chat content can use the same tags as the chat renderer. (default: true)"
        ])
        var sanitize: Boolean = true
    )

    @Serializable
    data class Inventory(
        val title: Listener = Listener(),
        val items: Listener = Listener()
    )

    @Serializable
    data class Listener(
        var enable: Boolean = true,
        @SerialName("require-prefix")
        var requirePrefix: Boolean = false
    )

    fun requiresPrefix(listener: Listener): Boolean = prefix.required || listener.requirePrefix

    fun requiresPrefix(listener: Chat): Boolean = prefix.required || listener.requirePrefix
}
