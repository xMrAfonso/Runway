package me.mrafonso.runway

import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.handler.ConfigHandler
import org.bukkit.plugin.java.JavaPlugin

class Runway : JavaPlugin() {


    override fun onEnable() {
        val configHandler = ConfigHandler(this) {
            register<Settings>("settings.yml") { Settings() }
        }
        logger.info("Runway enabled!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
        logger.info("Runway disabled!")
    }
}