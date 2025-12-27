package me.mrafonso.runway

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.test.base.TestPacketEventsBuilder
import dev.triumphteam.cmd.bukkit.BukkitCommandManager
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.mrafonso.runway.command.RunwayCommand
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Lang
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.*
import me.mrafonso.runway.integration.HookHandler
import me.mrafonso.runway.listeners.SystemChatListener
import me.mrafonso.runway.migration.MigrationHandler
import me.mrafonso.runway.resolver.ResolverHandler
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin


open class Runway : JavaPlugin() {
    private val METRICS_ID = 28365

    override fun onLoad() {
        initPacketEvents()
    }

    override fun onEnable() {
        val hookHandler = HookHandler()
        hookHandler.init(this)

        val configHandler = ConfigHandler(this) {
            register<Settings>("settings.yml") { Settings() }
            register<Lang>("lang.yml") { Lang() }
        }

        val migrationHandler = MigrationHandler(this, configHandler)
        logger.info("Attempting to convert old configurations to new formats...")
        if (migrationHandler.migrate()) {
            logger.info("Found old configuration files, migrated them to new format!")
        } else {
            logger.info("No old configuration files found to migrate.")
        }

        val resolverHandler = ResolverHandler(this, hookHandler)
        resolverHandler.reloadAll()

        val processHandler = ProcessHandler(hookHandler, configHandler, resolverHandler)

        val manager = PacketEvents.getAPI().eventManager
        manager.registerListeners(
            SystemChatListener(processHandler, configHandler)
        )
        PacketEvents.getAPI().init()

        val commandManager = BukkitCommandManager.create(this)
        commandManager.registerCommand(RunwayCommand(configHandler, resolverHandler, processHandler))

        val metrics = Metrics(this, METRICS_ID)

        logger.info("Runway enabled!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Runway disabled!")
    }

    private fun initPacketEvents() {
        PacketEvents.setAPI(
            if (isTestingMode()) TestPacketEventsBuilder.buildNoCache(this)
            else SpigotPacketEventsBuilder.buildNoCache(this)
        )
        PacketEvents.getAPI().settings
            .reEncodeByDefault(true)
            .checkForUpdates(false)
        PacketEvents.getAPI().load()
    }

    private fun isTestingMode(): Boolean {
        return System.getProperty("runway.testmode").equals("true", true)
    }
}