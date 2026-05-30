package me.mrafonso.runway.listener.packet

import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.Optional

class EntityTextPacketListener(processHandler: ProcessHandler, configHandler: ConfigHandler) :
    AbstractPacketListener(processHandler, configHandler) {

    override fun handlePacket(e: PacketPlaySendEvent) {
        if (e.packetType != PacketType.Play.Server.ENTITY_METADATA) return

        val settings = configHandler.get<Settings>()
        if (!settings.listeners.entityText.enable) return
        val requirePrefix = settings.requiresPrefix(settings.listeners.entityText)

        val player = e.getPlayer<Player>()
        val packet = WrapperPlayServerEntityMetadata(e)
        packet.entityMetadata = packet.entityMetadata.map { processEntityData(it, player, requirePrefix) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun processEntityData(data: EntityData<*>, player: Player?, requirePrefix: Boolean): EntityData<*> {
        return when (data.type) {
            EntityDataTypes.ADV_COMPONENT -> {
                val componentData = data as EntityData<Component>
                val processed = handler.processComponent(componentData.value, player, requirePrefix) ?: componentData.value
                EntityData(componentData.index, componentData.type, processed)
            }
            EntityDataTypes.OPTIONAL_ADV_COMPONENT -> {
                val componentData = data as EntityData<Optional<Component>>
                val processed = componentData.value
                    .map { original -> handler.processComponent(original, player, requirePrefix) ?: original }
                EntityData(componentData.index, componentData.type, processed)
            }
            else -> data
        }
    }
}
