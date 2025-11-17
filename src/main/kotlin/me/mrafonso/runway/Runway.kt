package me.mrafonso.runway

import com.github.retrooper.packetevents.PacketEvents
import dev.triumphteam.cmd.bukkit.BukkitCommandManager
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.mrafonso.runway.command.RunwayCommand
import me.mrafonso.runway.config.Lang
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.handler.*
import me.mrafonso.runway.listeners.SystemChatListener
import org.bukkit.plugin.java.JavaPlugin


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
            //register<Placeholders>("placeholders.yml") { Placeholders() }
        }

        val resolverHandler = ResolverHandler(this, hookHandler)
        resolverHandler.loadConfigs()
        resolverHandler.loadPlaceholders()

        val migrationHandler = MigrationHandler(this, configHandler)
        logger.info("Attempting to convert old configurations to new formats...")
        if (migrationHandler.migrate()) {
            logger.info("Found old configuration files, migrated them to new format!")
        } else {
            logger.info("No old configuration files found to migrate.")
        }

        val processHandler = ProcessHandler(hookHandler, configHandler, resolverHandler)

        val manager = PacketEvents.getAPI().eventManager
        manager.registerListeners(
            SystemChatListener(processHandler, configHandler)
        )
        PacketEvents.getAPI().init()

        val commandManager = BukkitCommandManager.create(this)
        commandManager.registerCommand(RunwayCommand(configHandler, resolverHandler, processHandler))
        logger.info("Runway enabled!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Runway disabled!")
    }
}