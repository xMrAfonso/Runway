package me.mrafonso.runway.listener.packet

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleSubtitle
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetTitleText
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import org.bukkit.entity.Player


class TitlePacketListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    override fun onPacketPlaySend(e: PacketPlaySendEvent) {
        super.onPacketPlaySend(e)
        if (e.packetType != PacketType.Play.Server.SET_TITLE_TEXT && e.packetType != PacketType.Play.Server.SET_TITLE_SUBTITLE) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.titles) return

        val player = e.getPlayer<Player>()
        if (e.packetType == PacketType.Play.Server.SET_TITLE_TEXT) {
            val packet = WrapperPlayServerSetTitleText(e)
            handler.processComponent(packet.title, player)?.let { packet.title = it }
        } else {
            val packet = WrapperPlayServerSetTitleSubtitle(e)
            handler.processComponent(packet.subtitle, player)?.let { packet.subtitle = it }
        }
    }
}