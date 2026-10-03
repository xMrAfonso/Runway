package me.mrafonso.runway.listener

import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.UUID

class ChatListener(
    private val processHandler: ProcessHandler,
    private val configHandler: ConfigHandler,
) : Listener {
    private val plainText = PlainTextComponentSerializer.plainText()
    private val miniMessage = MiniMessage.miniMessage()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChatMessage(e: AsyncChatEvent) {
        val settings = configHandler.get<Settings>()
        if (!settings.listeners.chat.enable) return

        // The chat format comes from the server, so it's parsed whether or not the player's message is.
        e.renderer(processRenderer(e.renderer(), settings))

        val text = plainText.serialize(e.message())
        val prefix = settings.prefix.value
        if (text.startsWith("!$prefix")) return

        val marker = if (settings.listeners.chat.sanitize) "<sanitized>" else ""
        val input = if (text.startsWith(prefix)) "$prefix$marker${text.drop(prefix.length)}" else "$marker$text"

        processHandler.processComponent(input, e.player, settings.requiresPrefix(settings.listeners.chat))
            ?.let { e.message(it) }
    }

    /**
     * Wraps the renderer so its output (the chat format) is parsed by Runway, while the player's message,
     * already processed above, is never parsed again under the format's unsanitized rules.
     *
     * The renderer is given a unique tag in place of the message. Parsing the rendered output then turns
     * that tag back into the message component. This has to be a real tag rather than text to find and
     * replace afterwards: formatting that spans the message (e.g. an unclosed gradient from a rank
     * placeholder) splits text into one component per character, so a text marker could no longer be found.
     *
     * @param renderer The original [ChatRenderer] to wrap.
     * @param settings The current [Settings] to read the prefix from.
     */
    internal fun processRenderer(renderer: ChatRenderer, settings: Settings): ChatRenderer {
        return ChatRenderer { source, sourceDisplayName, message, viewer ->
            // Random per render, so players can't type the tag or guess another message's tag.
            val tagName = "runway_message_${UUID.randomUUID().toString().replace("-", "")}"
            val marker = "<$tagName>"

            val rendered = renderer.render(source, sourceDisplayName, Component.text(marker), viewer)

            val processed = processHandler.processComponent(
                "${settings.prefix.value}${miniMessage.serialize(rendered)}",
                source,
                false,
                Placeholder.component(tagName, message)
            ) ?: rendered

            // Safety net for anywhere the tag wasn't parsed; the marker is still intact text there.
            processed.replaceText(
                TextReplacementConfig.builder()
                    .matchLiteral(marker)
                    .replacement(message)
                    .build()
            )
        }
    }
}
