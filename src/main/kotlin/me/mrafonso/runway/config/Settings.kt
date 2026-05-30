package me.mrafonso.runway.config

import dev.triumphteam.polaris.annotation.SerialComment
import kotlinx.serialization.SerialName
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
            "How often Runway refreshes MiniPlaceholders global placeholders, in seconds. (default: 30)"
        ])
        @SerialName("refresh-rate")
        var refreshRate: Long = 30
    )

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
        val items: Listener = Listener()
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
