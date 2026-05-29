package me.mrafonso.runway.listener

import io.papermc.paper.chat.ChatRenderer
import io.papermc.paper.event.player.AsyncChatEvent
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import java.util.concurrent.atomic.AtomicLong

class ChatListener(
    private val processHandler: ProcessHandler,
    private val configHandler: ConfigHandler,
) : Listener {
    private val plainText = PlainTextComponentSerializer.plainText()
    private val miniMessage = MiniMessage.miniMessage()
    private val markerId = AtomicLong()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChatMessage(e: AsyncChatEvent) {
        val settings = configHandler.get<Settings>()
        if (!settings.listeners.chat.enable) return

        val text = plainText.serialize(e.message())
        val requirePrefix = settings.requiresPrefix(settings.listeners.chat)
        if (!requirePrefix && text.startsWith("!${settings.prefix.value}")) return

        val marker = if (settings.listeners.chat.sanitize) "<sanitized>" else ""
        val input = if (text.startsWith(settings.prefix.value)) {
            "${settings.prefix.value}$marker${text.drop(settings.prefix.value.length)}"
        } else {
            "$marker$text"
        }

        val processedMessage = processHandler.processComponent(
            input,
            e.player,
            requirePrefix,
        ) ?: return

        e.message(processedMessage)
        e.renderer(processRenderer(e.renderer(), settings))
    }

    /**
     * This is needed to allow processing of the entire message with the renderer's formatting rules applied,
     * without having to re-parse the message content for MiniMessage tags, which would cause issues with unsanitized renderers.
     *
     * @param renderer The original [ChatRenderer] to wrap.
     * @param settings The current [Settings] to check for prefix and sanitization.
     * @return A new [ChatRenderer] that processes the rendered message with a marker, then replaces the marker with the original message after processing.
     */
    private fun processRenderer(renderer: ChatRenderer, settings: Settings): ChatRenderer {
        return ChatRenderer { source, sourceDisplayName, message, viewer ->
            // Render with a marker instead of the real message so renderer formatting can be parsed
            // without parsing player content again under the unsanitized renderer rules.
            val messageMarker = "<runway:marker:${markerId.incrementAndGet()}:${source.uniqueId}>"

            val rendered =
                renderer.render(source, sourceDisplayName, Component.text(messageMarker), viewer)

            val processed =
                processHandler.processComponent("${settings.prefix.value}${miniMessage.serialize(rendered)}", source, false)
                    ?: rendered

            // Put the already-processed message back into the final rendered chat component.
            processed.replaceText {
                it.matchLiteral(messageMarker)
                    .replacement(message)
            }
        }
    }
}
