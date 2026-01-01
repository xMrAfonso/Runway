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
) : SimplePacketListenerAbstract(PacketListenerPriority.HIGHEST) {

    protected val mm = MiniMessage.miniMessage()

    override fun onPacketPlaySend(e: PacketPlaySendEvent) {
        if (e.isCancelled) return
    }
}