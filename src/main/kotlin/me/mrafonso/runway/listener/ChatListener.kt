package me.mrafonso.runway.listener

import io.papermc.paper.event.player.AsyncChatDecorateEvent
import io.papermc.paper.event.player.AsyncChatEvent
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.processing.ProcessHandler
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatListener(val processHandler: ProcessHandler, configHandler: ConfigHandler) : Listener {

    @EventHandler
    fun onChatMessage(e: AsyncChatDecorateEvent) {
        // Implementation for chat message handling will go here
        println(e.result())
        e.result(processHandler.processComponent(e.result()) ?: e.result())
    }
}