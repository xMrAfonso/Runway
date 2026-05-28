package me.mrafonso.runway.integration.tag

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

class PAPITag(
    private val placeholderParser: (Player?, String) -> String = { player, placeholder ->
        PlaceholderAPI.setPlaceholders(player, "%$placeholder%")
    }
) : AbstractTag() {
    /**
     * A TagResolver that integrates with PlaceholderAPI to replace placeholders in MiniMessage format.
     *
     * Usage in MiniMessage:
     * <papi:placeholder_name>
     *
     * Example:
     * <papi:player> will be replaced with the player's name.
     *
     * @param player The player for whom the placeholders will be resolved.
     * @return A TagResolver that processes PlaceholderAPI tags.
     *
     * Credits to mbaxter for the original code.
     */
    override fun retrieve(): TagResolver {
        return TagResolver.resolver(setOf("papi", "placeholderapi")) { argumentQueue, context ->
            val papiPlaceholder = argumentQueue!!.popOr("papi tag requires an argument").value()
            val player = context.target() as? Player

            val parsedPlaceholder = placeholderParser(player, papiPlaceholder)

            Tag.preProcessParsed(parsedPlaceholder)
        }
    }
}
