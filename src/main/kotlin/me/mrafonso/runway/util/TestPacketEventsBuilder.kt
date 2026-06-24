package me.mrafonso.runway.util

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import com.github.retrooper.packetevents.injector.ChannelInjector
import com.github.retrooper.packetevents.manager.player.PlayerManager
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager
import com.github.retrooper.packetevents.manager.server.ServerManager
import com.github.retrooper.packetevents.netty.NettyManager
import com.github.retrooper.packetevents.protocol.ProtocolVersion
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.settings.PacketEventsSettings
import io.github.retrooper.packetevents.impl.netty.NettyManagerImpl
import io.github.retrooper.packetevents.impl.netty.manager.protocol.ProtocolManagerAbstract
import io.github.retrooper.packetevents.manager.server.ServerManagerImpl
import org.bukkit.plugin.Plugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Locale

object TestPacketEventsBuilder {
    val LOGGER: Logger = LoggerFactory.getLogger("packetevents")

    private var API_INSTANCE: PacketEventsAPI<Plugin?>? = null

    fun clearBuildCache() {
        API_INSTANCE = null
    }

    fun build(plugin: Plugin): PacketEventsAPI<Plugin?> {
        if (API_INSTANCE == null) {
            API_INSTANCE = buildNoCache(plugin)
        }
        return API_INSTANCE!!
    }

    fun build(plugin: Plugin, settings: PacketEventsSettings): PacketEventsAPI<Plugin?> {
        if (API_INSTANCE == null) {
            API_INSTANCE = buildNoCache(plugin, settings)
        }
        return API_INSTANCE!!
    }

    @JvmOverloads
    fun buildNoCache(
        plugin: Plugin,
        inSettings: PacketEventsSettings = PacketEventsSettings()
    ): PacketEventsAPI<Plugin?> {
        return object : PacketEventsAPI<Plugin?>() {
            private val settings = inSettings
            private val protocolManager: ProtocolManager = object : ProtocolManagerAbstract() {
                override fun getPlatformVersion(): ProtocolVersion {
                    return ProtocolVersion.UNKNOWN
                }
            }
            private val serverManager: ServerManager = ServerManagerImpl()
            private val nettyManager: NettyManager = NettyManagerImpl()

            private var loaded = false
            private var initialized = false
            private var terminated = false

            override fun load() {
                if (!loaded) {
                    //Resolve server version and cache
                    val id = plugin.name.lowercase(Locale.ROOT)
                    PacketEvents.IDENTIFIER = "pe-" + id
                    PacketEvents.ENCODER_NAME = "pe-encoder-" + id
                    PacketEvents.DECODER_NAME = "pe-decoder-" + id
                    PacketEvents.CONNECTION_HANDLER_NAME = "pe-connection-handler-" + id
                    PacketEvents.SERVER_CHANNEL_HANDLER_NAME = "pe-connection-initializer-" + id
                    PacketEvents.TIMEOUT_HANDLER_NAME = "pe-timeout-handler-" + id

                    PacketType.prepare()

                    loaded = true
                }
            }

            override fun isLoaded(): Boolean {
                return loaded
            }

            override fun init() {
                //Load if we haven't loaded already
                load()
                if (!initialized) {
                    initialized = true
                }
            }

            override fun isInitialized(): Boolean {
                return initialized
            }

            override fun terminate() {
                if (initialized) {
                    initialized = false
                    terminated = true
                }
            }

            override fun isTerminated(): Boolean {
                return terminated
            }

            override fun getPlugin(): Plugin {
                return plugin
            }

            override fun getProtocolManager(): ProtocolManager {
                return protocolManager
            }

            override fun getServerManager(): ServerManager {
                return serverManager
            }

            override fun getPlayerManager(): PlayerManager? {
                return null
            }

            override fun getSettings(): PacketEventsSettings {
                return settings
            }

            override fun getNettyManager(): NettyManager {
                return nettyManager
            }

            override fun getInjector(): ChannelInjector? {
                return null
            }
        }
    }
}
