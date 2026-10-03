package me.mrafonso.runway.integration

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.mrafonso.runway.Runway
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.resolver.ResolverHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.OfflinePlayer

class RunwayPlaceholderExpansion(
    private val plugin: Runway,
    private val resolverHandler: ResolverHandler,
    private val configHandler: ConfigHandler
) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "runway"

    override fun getAuthor(): String = "MrAfonso"

    override fun getVersion(): String = plugin.pluginMeta.version

    override fun persist(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val component = resolverHandler.resolveCustomPlaceholder(params, player?.player) ?: return null
        return format(component, configHandler.get<Settings>().placeholderApi.output)
    }

    companion object {
        /**
         * Strict mode closes every tag. The default serializer leaves trailing tags open, so a gradient
         * placeholder would bleed into whatever text the consuming plugin puts after it.
         */
        private val strictMiniMessage = MiniMessage.builder().strict(true).build()
        private val miniMessage = MiniMessage.miniMessage()
        private val plainText = PlainTextComponentSerializer.plainText()

        /** Stands in for the consuming plugin's text so a style-only placeholder serializes its opening tags. */
        private const val SENTINEL = "\uE000"

        private val legacy = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build()

        fun format(component: Component, output: Settings.PlaceholderOutput): String {
            // A placeholder with no text, e.g. `<#5865f2>`, is meant to style what follows it, so its tags stay open.
            if (plainText.serialize(component).isEmpty()) {
                val styled = Component.text(SENTINEL, trailingStyle(component))
                return when (output) {
                    Settings.PlaceholderOutput.MINIMESSAGE -> miniMessage.serialize(styled)
                    Settings.PlaceholderOutput.LEGACY -> legacy.serialize(styled)
                }.removeSuffix(SENTINEL)
            }

            return when (output) {
                Settings.PlaceholderOutput.MINIMESSAGE -> strictMiniMessage.serialize(component)
                Settings.PlaceholderOutput.LEGACY -> legacy.serialize(component)
            }
        }

        /** The effective style at the end of [component], following its last child down. */
        private fun trailingStyle(component: Component, inherited: Style = Style.empty()): Style {
            val style = component.style().merge(inherited, Style.Merge.Strategy.IF_ABSENT_ON_TARGET)
            return component.children().lastOrNull()?.let { trailingStyle(it, style) } ?: style
        }
    }
}
