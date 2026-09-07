package me.mrafonso.runway.integration.tag

import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player

class PAPITag(
    private val placeholderParser: (Player?, String) -> String = { player, placeholder ->
        PlaceholderAPI.setPlaceholders(player, "%$placeholder%")
    }
) : AbstractTag() {
    private val miniMessage = MiniMessage.miniMessage()
    private val legacySection = LegacyComponentSerializer.legacySection()

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

            val parsedPlaceholder = resolvePlaceholder(player, papiPlaceholder)

            Tag.preProcessParsed(parsedPlaceholder.toMiniMessage())
        }
    }

    /**
     * Resolve both the original bare-name callback contract and callbacks that
     * expect a complete PlaceholderAPI token. The latter is also needed when a
     * placeholder expands to another PlaceholderAPI placeholder.
     */
    private fun resolvePlaceholder(player: Player?, placeholder: String): String {
        val parsed = placeholderParser(player, placeholder)
        if (parsed != placeholder) return parsed

        var nested = placeholderParser(player, "%$placeholder%")
        repeat(MAX_NESTED_PLACEHOLDER_DEPTH) {
            if (!PAPI_PLACEHOLDER.matches(nested)) return nested
            val next = placeholderParser(player, nested)
            if (next == nested) return nested
            nested = next
        }
        return nested
    }

    private fun String.toMiniMessage(): String {
        if (!contains('\u00A7')) return this
        return miniMessage.serialize(legacySection.deserialize(this))
    }

    private companion object {
        private const val MAX_NESTED_PLACEHOLDER_DEPTH = 16
        private val PAPI_PLACEHOLDER = Regex("%[^%]+%")
    }
}
