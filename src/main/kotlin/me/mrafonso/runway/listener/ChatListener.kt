package me.mrafonso.runway.listener

import io.papermc.paper.event.player.AsyncChatDecorateEvent
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class ChatListener(
    private val processHandler: ProcessHandler,
    private val configHandler: ConfigHandler,
) : Listener {
    private val plainText = PlainTextComponentSerializer.plainText()

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onChatMessage(e: AsyncChatDecorateEvent) {
        val settings = configHandler.get<Settings>()
        val text = plainText.serialize(e.result())
        val sanitizedText = "${settings.prefix.value}<sanitized>$text"

        e.result(processHandler.processComponent(sanitizedText, e.player()) ?: return)
    }
}
