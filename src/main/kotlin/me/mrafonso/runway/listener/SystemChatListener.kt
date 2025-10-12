package me.mrafonso.runway.listeners

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.handler.ConfigHandler
import me.mrafonso.runway.handler.ProcessHandler
import me.mrafonso.runway.listener.AbstractPacketListener
import org.bukkit.entity.Player

class SystemChatListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    override fun onPacketPlaySend(e: PacketPlaySendEvent) {
        super.onPacketPlaySend(e)
        if (e.packetType != PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.systemMessages) return

        val player = e.getPlayer<Player?>()
        val packet = WrapperPlayServerSystemChatMessage(e)

//        if (message.startsWith("<lang:multiplayer.message_not_delivered:")) {
//            e.setCancelled(true)
//            return
//        }
//
//        if (message.contains("[actionbar]")) {
//            message = message.replace("[actionbar]", "")
//            e.setCancelled(true)
//            player.sendActionBar(handler.processComponent(message, player)!!)
//        } else {
        packet.message = handler.processComponent(packet.message, player) ?: return
//        }
    }
}