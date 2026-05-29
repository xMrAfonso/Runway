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
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player


class ItemPacketListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    override fun handlePacket(e: PacketPlaySendEvent) {
        if (e.packetType != PacketType.Play.Server.SET_SLOT &&
            e.packetType != PacketType.Play.Server.SET_CURSOR_ITEM &&
            e.packetType != PacketType.Play.Server.SET_PLAYER_INVENTORY) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.items.enable) return
        val requirePrefix = settings.requiresPrefix(settings.listeners.items)

        val player = e.getPlayer<Player>()

        when (e.packetType) {
            PacketType.Play.Server.SET_SLOT -> {
                val packet = WrapperPlayServerSetSlot(e)
                packet.item = handler.processItem(packet.item, player, requirePrefix)
            }
            PacketType.Play.Server.SET_CURSOR_ITEM -> {
                val packet = WrapperPlayServerSetCursorItem(e)
                packet.stack = handler.processItem(packet.stack, player, requirePrefix)
            }
            else -> {
                val packet = WrapperPlayServerSetPlayerInventory(e)
                packet.stack = handler.processItem(packet.stack, player, requirePrefix)
            }
        }

    }
}
