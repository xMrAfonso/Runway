package me.mrafonso.runway.handler

import io.github.miniplaceholders.api.MiniPlaceholders
import me.clip.placeholderapi.libs.kyori.adventure.platform.bukkit.BukkitAudiences
import me.mrafonso.runway.config.Settings
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.entity.Player


class ProcessHandler(
    private val hookHandler: HookHandler,
    private val configHandler: ConfigHandler
) {
    private val MM = MiniMessage.miniMessage()
    private val miniMessage = MiniMessage.miniMessage()
    private val gsonSerializer = GsonComponentSerializer.gson()

    private val noItalics = "<!italic>"

    fun processComponent(input: Component, player: Player?): Component? {
        val settings = configHandler.get<Settings>()
        val requirePrefix = settings.prefix.required
        val prefix = settings.prefix.value

        val disableItalics = settings.disableItalics
        var text = MM.serialize(input)

        if (requirePrefix &&
            !text.startsWith(prefix)
        ) return null

        if (!requirePrefix &&
            text.startsWith("!$prefix")
        ) return null

        if(text.startsWith(prefix)) text = text.drop(prefix.length)
        if (disableItalics) text = "$noItalics$text"

        var resolver: TagResolver = TagResolver.standard()
        if (hookHandler.miniPlaceholders) {
            resolver = MiniPlaceholders.audienceGlobalPlaceholders()
            println("yup mini")
        }

        return try {
            player?.let {
                println("player not null")
                MM.deserialize(text,player, resolver)
            } ?: MM.deserialize(text, resolver)
        } catch (_: ParsingException) {
            null
        }
    }
}