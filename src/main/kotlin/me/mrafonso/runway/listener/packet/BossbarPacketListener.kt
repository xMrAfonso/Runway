package me.mrafonso.runway.listener.packet

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBossBar
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import org.bukkit.entity.Player

class BossbarPacketListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    override fun handlePacket(e: PacketPlaySendEvent) {
        if (e.packetType != PacketType.Play.Server.BOSS_BAR) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.bossbar.enable) return
        val requirePrefix = settings.requiresPrefix(settings.listeners.bossbar)

        val packet = WrapperPlayServerBossBar(e)
        if (packet.action != WrapperPlayServerBossBar.Action.ADD &&
            packet.action != WrapperPlayServerBossBar.Action.UPDATE_TITLE
        ) return

        val player = e.getPlayer<Player>()
        handler.processComponent(packet.title, player, requirePrefix)?.let { packet.title = it }
    }
}
