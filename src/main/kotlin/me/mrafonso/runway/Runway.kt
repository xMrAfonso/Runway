package me.mrafonso.runway

import com.github.retrooper.packetevents.PacketEvents
import me.mrafonso.runway.util.TestPacketEventsBuilder
import dev.triumphteam.cmd.bukkit.BukkitCommandManager
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import me.mrafonso.runway.command.RunwayCommand
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Lang
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.*
import me.mrafonso.runway.integration.HookHandler
import me.mrafonso.runway.listener.ChatListener
import me.mrafonso.runway.listener.packet.InventoryPacketListener
import me.mrafonso.runway.listener.packet.ItemPacketListener
import me.mrafonso.runway.listener.packet.SystemChatPacketListener
import me.mrafonso.runway.listener.packet.TablistPacketListener
import me.mrafonso.runway.listener.packet.TitlePacketListener
import me.mrafonso.runway.listener.packet.TestPacketListener
import me.mrafonso.runway.migration.MigrationHandler
import me.mrafonso.runway.resolver.ResolverHandler
import me.mrafonso.runway.util.registerEvents
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

open class Runway : JavaPlugin() {
    private val METRICS_ID = 28365
    lateinit var configHandler: ConfigHandler

    /**
     * Called when the plugin is first loaded by the server.
     * This method pre-initializes PacketEvents to prepare it for usage.
     */
    override fun onLoad() {
        preInitPacketEvents()
    }

    /**
     * Called when the plugin is enabled by the server.
     * This method initializes various components of the plugin,
     * including hook handlers, configuration handlers, migration,
     * packet listeners, command managers, and metrics.
     */
    override fun onEnable() {
        val hookHandler = initHookHandler()
        configHandler = initConfigHandler()
        val handlers = initResolverHandler(hookHandler, configHandler)

        initMigration(configHandler)
        initListeners(handlers.second, configHandler)
        initPacketListeners(configHandler, handlers)
        initPacketEvents()
        initCommandManager(configHandler, handlers)
        initMetrics()

        logger.info("Runway enabled!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Runway disabled!")
    }

    /**
     * Pre-initializes PacketEvents to prepare it for usage.
     * This method sets up the PacketEvents API with appropriate settings
     * and loads it without caching, allowing for a fresh instance.
     */
    @Suppress("UnstableApiUsage")
    private fun preInitPacketEvents() {
        PacketEvents.setAPI(
            if (isTestingMode()) TestPacketEventsBuilder.buildNoCache(this)
            else SpigotPacketEventsBuilder.buildNoCache(this)
        )
        PacketEvents.getAPI().settings
            .reEncodeByDefault(true)
            .checkForUpdates(false)
        PacketEvents.getAPI().load()
    }

    /**
     * This method finalizes the PacketEvents setup, making it ready for use
     * within the plugin.
     */
    private fun initPacketEvents() = PacketEvents.getAPI().init()

    private fun initListeners(processHandler: ProcessHandler, configHandler: ConfigHandler) {
        val manager = server.pluginManager
        manager.registerEvents(this,
            ChatListener(processHandler, configHandler)
        )
    }

    /**
     * Initializes packet listeners for handling specified events.
     *
     * @param configHandler The configuration handler for accessing settings.
     * @param handlers A pair containing the resolver and process handlers.
     */
    private fun initPacketListeners(configHandler: ConfigHandler, handlers: Pair<ResolverHandler, ProcessHandler>) {
        val manager = PacketEvents.getAPI().eventManager
        manager.registerListeners(
            SystemChatPacketListener(handlers.second, configHandler),
            TablistPacketListener(handlers.second, configHandler),
            InventoryPacketListener(handlers.second, configHandler),
            ItemPacketListener(handlers.second, configHandler),
            TitlePacketListener(handlers.second, configHandler),
            TestPacketListener(handlers.second, configHandler)
        )
    }

    /**
     * Initializes bStats metrics for the plugin.
     */
    private fun initMetrics() {
        val metrics = Metrics(this, METRICS_ID)
    }

    /**
     * Initializes the configuration handler and registers configuration files.
     *
     * @return The initialized ConfigHandler instance.
     */
    private fun initConfigHandler(): ConfigHandler {
        return ConfigHandler(this) {
            register<Settings>("settings.yml") { Settings() }
            register<Lang>("lang.yml") { Lang() }
        }
    }

    /**
     * Initializes the hook handler for managing integrations with other plugins.
     *
     * @return The initialized HookHandler instance.
     */
    private fun initHookHandler(): HookHandler {
        val hookHandler = HookHandler()
        hookHandler.init(this)
        return hookHandler
    }

    /**
     * Initializes the migration handler to convert old configurations to new formats.
     *
     * @param configHandler The configuration handler for accessing settings.
     */
    private fun initMigration(configHandler: ConfigHandler) {
        val migrationHandler = MigrationHandler(this, configHandler)
        logger.info("Attempting to convert old configurations to new formats...")
        if (migrationHandler.migrate()) {
            logger.info("Found old configuration files, migrated them to new format!")
        } else {
            logger.info("No old configuration files found to migrate.")
        }
    }

    /**
     * Initializes the resolver and process handlers.
     *
     * @param hookHandler The hook handler for managing integrations.
     * @param configHandler The configuration handler for accessing settings.
     * @return A pair containing the initialized ResolverHandler and ProcessHandler instances.
     */
    private fun initResolverHandler(hookHandler: HookHandler, configHandler: ConfigHandler): Pair<ResolverHandler, ProcessHandler> {
        val resolverHandler = ResolverHandler(this, hookHandler)
        resolverHandler.reloadAll()

        val processHandler = ProcessHandler(hookHandler, configHandler, resolverHandler)
        return Pair(resolverHandler, processHandler)
    }

    /**
     * Initializes the command manager and registers commands.
     *
     * @param configHandler The configuration handler for accessing settings.
     * @param handlers A pair containing the resolver and process handlers.
     */
    private fun initCommandManager(configHandler: ConfigHandler, handlers: Pair<ResolverHandler, ProcessHandler>) {
        val commandManager = BukkitCommandManager.create(this)
        commandManager.registerCommand(RunwayCommand(configHandler, handlers.first, handlers.second))
    }

    /**
     * Checks if the plugin is running in testing mode.
     *
     * @return True if in testing mode, false otherwise.
     */
    private fun isTestingMode(): Boolean {
        return System.getProperty("runway.testmode").equals("true", true)
    }
}