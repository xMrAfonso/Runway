package me.mrafonso.runway.listener.packet

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import org.bukkit.entity.Player


class ItemPacketListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    override fun onPacketPlaySend(e: PacketPlaySendEvent) {
        super.onPacketPlaySend(e)
        if (e.packetType != PacketType.Play.Server.SET_SLOT) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.items) return

        val player = e.getPlayer<Player?>()
        val packet = WrapperPlayServerSetSlot(e)

        println("sus")
        packet.item = handler.processItem(packet.item, player)
    }
}