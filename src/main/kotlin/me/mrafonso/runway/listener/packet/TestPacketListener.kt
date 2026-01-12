package me.mrafonso.runway.listener.packet

import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerListHeaderAndFooter
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import org.bukkit.entity.Player


class TestPacketListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    override fun onPacketPlayReceive(e: PacketPlayReceiveEvent) {
        if (e.packetType != PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) return

    }
}