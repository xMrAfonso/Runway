package me.mrafonso.runway.listener.packet

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerActionBar
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import org.bukkit.entity.Player

class ActionbarPacketListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    override fun handlePacket(e: PacketPlaySendEvent) {
        if (e.packetType != PacketType.Play.Server.ACTION_BAR) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.actionbar.enable) return
        val requirePrefix = settings.requiresPrefix(settings.listeners.actionbar)

        val player = e.getPlayer<Player>()
        val packet = WrapperPlayServerActionBar(e)
        handler.processComponent(packet.actionBarText, player, requirePrefix)?.let { packet.actionBarText = it }
    }
}
