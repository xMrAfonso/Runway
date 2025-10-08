package me.mrafonso.runway.handler

import me.mrafonso.runway.config.Settings
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.entity.Player

class ProcessHandler(private val configHandler: ConfigHandler) {
    private val miniMessage = MiniMessage.miniMessage()
    private val gsonSerializer = GsonComponentSerializer.gson()

    private val noItalics = "<italic:false>"

    fun processComponent(input: String?, player: Player?): Component? {
        if (input.isNullOrBlank() || '§' in input) return null

        val config = configHandler.get<Settings>()
        //val requirePrefixMM = config.requirePrefix.minimessage
        //val requirePrefixP = config.requirePrefix.placeholders
        val disableItalics = config.disableItalics
        val miniPlaceholdersHook = config.placeholderHook.miniPlaceholders
        val placeholderAPIHook = config.placeholderHook.placeholderAPI

        val hasMMPrefix = input.startsWith("[mm]")
        //if (requirePrefixMM && !hasMMPrefix) return null

        var text = input.removePrefix("[mm]")
        if (disableItalics) text = noItalics + text


        return null
    }
}