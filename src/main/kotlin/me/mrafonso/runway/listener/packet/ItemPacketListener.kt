package me.mrafonso.runway.listener.packet

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCursorItem
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPlayerInventory
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import org.bukkit.entity.Player


class ItemPacketListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    override fun onPacketPlaySend(e: PacketPlaySendEvent) {
        super.onPacketPlaySend(e)
        if (e.packetType != PacketType.Play.Server.SET_SLOT &&
            e.packetType != PacketType.Play.Server.SET_CURSOR_ITEM &&
            e.packetType != PacketType.Play.Server.SET_PLAYER_INVENTORY) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.items) return

        val player = e.getPlayer<Player>()

        when (e.packetType) {
            PacketType.Play.Server.SET_SLOT -> {
                val packet = WrapperPlayServerSetSlot(e)
                packet.item = handler.processItem(packet.item, player)
            }
            PacketType.Play.Server.SET_CURSOR_ITEM -> {
                val packet = WrapperPlayServerSetCursorItem(e)
                packet.stack = handler.processItem(packet.stack, player)
            }
            else -> {
                val packet = WrapperPlayServerSetPlayerInventory(e)
                packet.stack = handler.processItem(packet.stack, player)
            }
        }

    }
}