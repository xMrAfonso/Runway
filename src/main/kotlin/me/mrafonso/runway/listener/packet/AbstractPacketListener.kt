package me.mrafonso.runway.listener.packet

import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.processing.ProcessHandler
import net.kyori.adventure.text.minimessage.MiniMessage

open class AbstractPacketListener(
    protected val handler: ProcessHandler,
    protected val configHandler: ConfigHandler,
    priority: PacketListenerPriority = PacketListenerPriority.HIGHEST,
) : SimplePacketListenerAbstract(priority) {

    protected val mm = MiniMessage.miniMessage()

    final override fun onPacketPlaySend(e: PacketPlaySendEvent) {
        if (e.isCancelled) return
        handlePacket(e)
    }

    protected open fun handlePacket(e: PacketPlaySendEvent) = Unit
}
