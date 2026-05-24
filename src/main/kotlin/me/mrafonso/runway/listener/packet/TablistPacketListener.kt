package me.mrafonso.runway.listener.packet

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import org.bukkit.entity.Player


class TablistPacketListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    override fun onPacketPlaySend(e: PacketPlaySendEvent) {
        super.onPacketPlaySend(e)
        if (e.packetType != PacketType.Play.Server.PLAYER_LIST_HEADER_AND_FOOTER) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.tablist) return

        val player = e.getPlayer<Player>()
        val packet = WrapperPlayServerPlayerListHeaderAndFooter(e)

        handler.processComponent(packet.footer, player)?.let { packet.footer = it }
        handler.processComponent(packet.header, player)?.let { packet.header = it }
    }
}