package me.mrafonso.runway.handler

import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.config.migration.OldConfig

class MigrationHandler(val configHandler: ConfigHandler) {

    fun migrate() {
        val oldConfig = configHandler.load<OldConfig>("config.yml").get()
        val newConfig = configHandler.get<Settings>()

        // Migrate placeholderHook
        newConfig.placeholderHook.placeholderAPI = oldConfig.placeholderHook.placeholderAPI
        newConfig.placeholderHook.miniPlaceholders = oldConfig.placeholderHook.miniPlaceholders

        // Migrate disableItalics
        newConfig.disableItalics = oldConfig.disableItalics

        // Migrate listeners
        newConfig.listeners.systemMessages = oldConfig.listeners.systemMessages

        configHandler.save<Settings>()
    }
}