package me.mrafonso.runway.listener.packet

import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class PlayerNamePacketListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler, PacketListenerPriority.MONITOR) {

    private val teamMembers = ConcurrentHashMap<TeamKey, Set<String>>()

    override fun handlePacket(e: PacketPlaySendEvent) {
        if (e.packetType != PacketType.Play.Server.TEAMS &&
            e.packetType != PacketType.Play.Server.PLAYER_INFO_UPDATE &&
            e.packetType != PacketType.Play.Server.PLAYER_INFO
        ) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.playerNames.enable) return
        val requirePrefix = settings.requiresPrefix(settings.listeners.playerNames)
        val viewer = e.getPlayer<Player>()

        when (e.packetType) {
            PacketType.Play.Server.TEAMS -> processTeamPacket(e, viewer, requirePrefix)
            PacketType.Play.Server.PLAYER_INFO_UPDATE -> processPlayerInfoUpdatePacket(e, viewer, requirePrefix)
            else -> processPlayerInfoPacket(e, viewer, requirePrefix)
        }
    }

    private fun processTeamPacket(e: PacketPlaySendEvent, viewer: Player?, requirePrefix: Boolean) {
        val packet = WrapperPlayServerTeams(e)
        updateTeamMembers(packet, viewer)

        if (packet.teamMode != WrapperPlayServerTeams.TeamMode.CREATE &&
            packet.teamMode != WrapperPlayServerTeams.TeamMode.UPDATE
        ) return

        val player = resolveTeamPlayer(packet, viewer)
        val info = packet.teamInfo.orElse(null) ?: return
        info.displayName = processComponent(info.displayName, player, requirePrefix)
        info.prefix = processComponent(info.prefix, player, requirePrefix)
        info.suffix = processComponent(info.suffix, player, requirePrefix)
        packet.setTeamInfo(info)
    }

    private fun processPlayerInfoUpdatePacket(e: PacketPlaySendEvent, viewer: Player?, requirePrefix: Boolean) {
        val packet = WrapperPlayServerPlayerInfoUpdate(e)
        if (!packet.actions.contains(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME)) return

        packet.entries.forEach { entry ->
            val player = getPlayer(entry.profileId) ?: viewer
            entry.displayName = entry.displayName?.let { processComponent(it, player, requirePrefix) }
        }
    }

    private fun processPlayerInfoPacket(e: PacketPlaySendEvent, viewer: Player?, requirePrefix: Boolean) {
        val packet = WrapperPlayServerPlayerInfo(e)
        if (packet.action != WrapperPlayServerPlayerInfo.Action.ADD_PLAYER &&
            packet.action != WrapperPlayServerPlayerInfo.Action.UPDATE_DISPLAY_NAME
        ) return

        packet.playerDataList.forEach { data ->
            val player = data.userProfile?.uuid?.let { getPlayer(it) } ?: viewer
            data.displayName = data.displayName?.let { processComponent(it, player, requirePrefix) }
        }
    }

    private fun processComponent(component: Component, player: Player?, requirePrefix: Boolean): Component {
        return handler.processComponent(component, player, requirePrefix) ?: component
    }

    private fun getPlayer(uuid: UUID): Player? {
        return Bukkit.getPlayer(uuid)
    }

    private fun resolveTeamPlayer(packet: WrapperPlayServerTeams, viewer: Player?): Player? {
        val member = packet.players.singleOrNull()
            ?: viewer?.uniqueId?.let { teamMembers[TeamKey(it, packet.teamName)]?.singleOrNull() }

        return member?.let { Bukkit.getPlayerExact(it) } ?: viewer
    }

    private fun updateTeamMembers(packet: WrapperPlayServerTeams, viewer: Player?) {
        val viewerId = viewer?.uniqueId ?: return
        val key = TeamKey(viewerId, packet.teamName)

        when (packet.teamMode) {
            WrapperPlayServerTeams.TeamMode.CREATE -> teamMembers[key] = packet.players.toSet()
            WrapperPlayServerTeams.TeamMode.ADD_ENTITIES -> {
                teamMembers[key] = teamMembers[key].orEmpty() + packet.players
            }
            WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES -> {
                val remaining = teamMembers[key].orEmpty() - packet.players.toSet()
                if (remaining.isEmpty()) teamMembers.remove(key) else teamMembers[key] = remaining
            }
            WrapperPlayServerTeams.TeamMode.REMOVE -> teamMembers.remove(key)
            WrapperPlayServerTeams.TeamMode.UPDATE -> Unit
        }
    }

    private data class TeamKey(val viewerId: UUID, val teamName: String)
}
