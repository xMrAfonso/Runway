package me.mrafonso.runway.handler

import me.mrafonso.runway.Runway
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.config.migration.OldConfig
import java.nio.file.Files
import java.nio.file.Path

class MigrationHandler(private val plugin: Runway, private val configHandler: ConfigHandler) {

    fun migrate(): Boolean {

        val oldConfig = configHandler.load<OldConfig>("config.yml")?.get() ?: return false
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
        return true
    }
}