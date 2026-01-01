package me.mrafonso.runway.listener.packet

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.processing.ProcessHandler
import org.bukkit.entity.Player

class InventoryPacketListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    override fun onPacketPlaySend(e: PacketPlaySendEvent) {
        super.onPacketPlaySend(e)
        if (e.packetType != PacketType.Play.Server.OPEN_WINDOW &&
            e.packetType != PacketType.Play.Server.WINDOW_ITEMS) return

        val settings = configHandler.get<Settings>()
        val player = e.getPlayer<Player?>()

        if (settings.listeners.inventory.title &&
            e.packetType == PacketType.Play.Server.OPEN_WINDOW) {

            val packet = WrapperPlayServerOpenWindow(e)
            packet.title = handler.processComponent(packet.title, player) ?: return

        } else if (settings.listeners.inventory.items &&
            e.packetType == PacketType.Play.Server.WINDOW_ITEMS) {

            val packet = WrapperPlayServerWindowItems(e)
            packet.items = handler.processItems(packet.items, player)
        }
    }
}