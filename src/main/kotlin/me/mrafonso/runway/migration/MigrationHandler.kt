package me.mrafonso.runway.migration

import me.mrafonso.runway.Runway
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.config.placeholder.Group
import me.mrafonso.runway.config.placeholder.TextPlaceholder
import me.mrafonso.runway.migration.config.OldConfig
import me.mrafonso.runway.migration.config.OldPlaceholders
import me.mrafonso.runway.util.load
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.nio.file.Files
import java.nio.file.Path

class MigrationHandler(private val plugin: Runway, private val configHandler: ConfigHandler) {

    fun migrate(): Boolean {
        if (plugin.dataFolder.resolve("config.yml").exists().not()) return false

        val oldConfig = load<OldConfig>(plugin, "config.yml", false)?.get() ?: return false
        val newConfig = configHandler.get<Settings>()

        // Migrate requirePrefix
        newConfig.prefix.required = oldConfig.requirePrefix.minimessage
        newConfig.prefix.value = "[mm]"

        // Migrate disableItalics
        newConfig.disableItalics = oldConfig.disableItalics

        // Migrate listeners
        newConfig.listeners.systemMessages = oldConfig.listeners.systemMessages
        newConfig.listeners.tablist = oldConfig.listeners.tablist
        newConfig.listeners.titles = oldConfig.listeners.titles
        newConfig.listeners.scoreboards = oldConfig.listeners.scoreboards
        newConfig.listeners.inventory.title = oldConfig.listeners.inventory.title
        newConfig.listeners.inventory.items = oldConfig.listeners.inventory.items
        newConfig.listeners.items = oldConfig.listeners.items

        configHandler.save<Settings>()

        migratePlaceholders()
        return true
    }

    private fun migratePlaceholders() {
        val oldPlaceholdersPath = Path.of("${plugin.dataFolder}/placeholders.yml")

        if (Files.exists(oldPlaceholdersPath)) {
            val oldPlaceholders = load<OldPlaceholders>(plugin, "placeholders.yml", false)?.get() ?: return

            println("-" + oldPlaceholders.customPlaceholders.map { "${it.key}: ${it.value}" })
            load<Group>(plugin, "placeholders/migrated.yml", true) {
                Group(
                    placeholders = oldPlaceholders.customPlaceholders.map { it.key to TextPlaceholder(it.value) }.toMap()
                )
            }
        }
    }
}