package me.mrafonso.runway.listeners

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.processing.ProcessHandler
import me.mrafonso.runway.listener.AbstractPacketListener
import org.bukkit.entity.Player
import java.awt.TextComponent

class SystemChatListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    override fun onPacketPlaySend(e: PacketPlaySendEvent) {
        super.onPacketPlaySend(e)
        if (e.packetType != PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.systemMessages) return

        val player = e.getPlayer<Player?>()
        val packet = WrapperPlayServerSystemChatMessage(e)

        val text = mm.serialize(packet.message)

        if (text.startsWith("<lang:multiplayer.message_not_delivered:")) {
            e.isCancelled = true
            return
        }

        packet.message = handler.processComponent(text, player) ?: return

        if (settings.prefix.required) text.drop(settings.prefix.value.length)
        if (text.contains("\\<silent>")) {
            e.isCancelled = true
        }
    }
}