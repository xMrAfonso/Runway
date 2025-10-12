package me.mrafonso.runway.handler

import me.mrafonso.runway.Runway
import org.bukkit.Bukkit

data class HookHandler(
    var placeholderAPI: Boolean = false,
    var miniPlaceholders: Boolean = false
) {
    fun init(plugin: Runway) {
        val papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI")
        if (papi != null && papi.isEnabled) {
            placeholderAPI = true
            plugin.logger.info("PlaceholderAPI found, enabled PlaceholderAPI support!")
        }

        val miniP = Bukkit.getPluginManager().getPlugin("MiniPlaceholders")
        if (miniP != null && miniP.isEnabled) {
            miniPlaceholders = true
            plugin.logger.info("MiniPlaceholders found, enabled MiniPlaceholders support!")
        }
    }
}