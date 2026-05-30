package me.mrafonso.runway.migration

import me.mrafonso.runway.Runway
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.migration.config.OldConfig
import me.mrafonso.runway.util.load
import org.bukkit.configuration.file.YamlConfiguration
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class MigrationHandler(
    private val plugin: Runway,
    private val configHandler: ConfigHandler,
    private val migrateSettings: Boolean,
    private val migratePlaceholders: Boolean,
) {

    fun migrate(): Boolean {
        val settingsMigrated = if (migrateSettings) migrateSettings() else false
        val placeholdersMigrated = if (migratePlaceholders) migratePlaceholders() else false

        return settingsMigrated || placeholdersMigrated
    }

    private fun migrateSettings(): Boolean {
        val oldConfig = load<OldConfig>(plugin, "config.yml", false)?.get() ?: return false
        val newConfig = configHandler.get<Settings>()

        // Migrate requirePrefix
        newConfig.prefix.required = oldConfig.requirePrefix.minimessage
        newConfig.prefix.value = "[mm]"

        // Migrate disableItalics
        newConfig.disableItalics = oldConfig.disableItalics

        // Migrate listeners
        newConfig.listeners.systemMessages.enable = oldConfig.listeners.systemMessages
        newConfig.listeners.tablist.enable = oldConfig.listeners.tablist
        newConfig.listeners.titles.enable = oldConfig.listeners.titles
        newConfig.listeners.inventory.title.enable = oldConfig.listeners.inventory.title
        newConfig.listeners.inventory.items.enable = oldConfig.listeners.inventory.items
        newConfig.listeners.items.enable = oldConfig.listeners.items

        configHandler.save<Settings>()
        archiveLegacyFile("config.yml", "old-config.yml")

        return true
    }

    private fun migratePlaceholders(): Boolean {
        val oldPlaceholdersPath = plugin.dataFolder.toPath().resolve("placeholders.yml")
        if (!Files.exists(oldPlaceholdersPath)) return false

        val customPlaceholdersSection = YamlConfiguration.loadConfiguration(oldPlaceholdersPath.toFile())
            .getConfigurationSection("custom-placeholders")
            ?: return false
        val oldPlaceholders = customPlaceholdersSection.getKeys(false)
            .associateWith { key -> customPlaceholdersSection.getString(key).orEmpty() }

        val placeholdersPath = plugin.dataFolder.toPath().resolve("placeholders")
        Files.createDirectories(placeholdersPath)

        val migratedFile = placeholdersPath.resolve("migrated.yml").toFile()
        val migrated = YamlConfiguration()
        oldPlaceholders.forEach { (key, value) ->
            migrated.set("text-placeholders.$key", value)
        }
        migrated.save(migratedFile)
        archiveLegacyFile("placeholders.yml", "old-placeholders.yml")

        return true
    }

    private fun archiveLegacyFile(fileName: String, archivedFileName: String) {
        val source = plugin.dataFolder.toPath().resolve(fileName)
        if (!Files.exists(source)) return

        Files.move(
            source,
            plugin.dataFolder.toPath().resolve(archivedFileName),
            StandardCopyOption.REPLACE_EXISTING
        )
    }
}
