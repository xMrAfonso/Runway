package me.mrafonso.runway.resolver

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import java.util.function.BiFunction

class PAPITagResolver {
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
    fun papiTag(player: Player): TagResolver {
        return TagResolver.resolver("papi", BiFunction { argumentQueue: ArgumentQueue?, context: Context? ->
            val papiPlaceholder = argumentQueue!!.popOr("papi tag requires an argument").value()

            val parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, "%$papiPlaceholder%")

            val componentPlaceholder: Component = LegacyComponentSerializer.legacySection().deserialize(parsedPlaceholder)
            Tag.selfClosingInserting(componentPlaceholder)
        })
    }
}