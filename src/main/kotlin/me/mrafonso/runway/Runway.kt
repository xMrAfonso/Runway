package me.mrafonso.runway

import com.github.retrooper.packetevents.PacketEvents
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.mrafonso.runway.config.Lang
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.config.placeholder.Placeholders
import me.mrafonso.runway.handler.ConfigHandler
import me.mrafonso.runway.handler.HookHandler
import me.mrafonso.runway.handler.MigrationHandler
import me.mrafonso.runway.handler.ProcessHandler
import me.mrafonso.runway.listeners.SystemChatListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.log


class Runway : JavaPlugin() {

    override fun onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this))
        PacketEvents.getAPI().settings
            .reEncodeByDefault(true)
            .checkForUpdates(false)
        PacketEvents.getAPI().load()
    }

    override fun onEnable() {
        val hookHandler = HookHandler()
        hookHandler.init(this)

        val configHandler = ConfigHandler(this) {
            register<Settings>("settings.yml") { Settings() }
            register<Lang>("lang.yml") { Lang() }
            register<Placeholders>("placeholders.yml") { Placeholders() }
        }

        println(configHandler.get<Placeholders>().placeholders)

        val migrationHandler = MigrationHandler(this, configHandler)
        logger.info("Attempting to convert old configurations to new formats...")
        if (migrationHandler.migrate()) {
            logger.info("Found old configuration files, migrated them to new format!")
        } else {
            logger.info("No old configuration files found to migrate.")
        }

        val processHandler = ProcessHandler(hookHandler, configHandler)

        val manager = PacketEvents.getAPI().eventManager
        manager.registerListeners(
            SystemChatListener(processHandler, configHandler)
        )
        PacketEvents.getAPI().init()

        logger.info("Runway enabled!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Runway disabled!")
    }
}