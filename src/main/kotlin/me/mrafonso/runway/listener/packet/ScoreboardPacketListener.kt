package me.mrafonso.runway.listener.packet

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.processing.ProcessHandler
import org.bukkit.entity.Player

class ScoreboardPacketListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    override fun onPacketPlaySend(e: PacketPlaySendEvent) {
        super.onPacketPlaySend(e)
        if (e.packetType != PacketType.Play.Server.SCOREBOARD_OBJECTIVE &&
            e.packetType != PacketType.Play.Server.UPDATE_SCORE &&
            e.packetType != PacketType.Play.Server.DISPLAY_SCOREBOARD
        ) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.scoreboards) return

        val player = e.getPlayer<Player?>()

        when (e.packetType) {
            PacketType.Play.Server.SCOREBOARD_OBJECTIVE -> {
                val packet = WrapperPlayServerScoreboardObjective(e)
                packet.displayName = handler.processComponent(packet.displayName, player) ?: return
            }

            PacketType.Play.Server.DISPLAY_SCOREBOARD -> {
                val packet = WrapperPlayServerDisplayScoreboard(e)
                packet.scoreName = "testing"
            }

            else -> return
        }
    }
}