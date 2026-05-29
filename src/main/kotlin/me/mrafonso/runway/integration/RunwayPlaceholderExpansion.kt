package me.mrafonso.runway.integration

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.mrafonso.runway.Runway
import me.mrafonso.runway.resolver.ResolverHandler
import org.bukkit.OfflinePlayer

class RunwayPlaceholderExpansion(
    private val plugin: Runway,
    private val resolverHandler: ResolverHandler
) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "runway"

    override fun getAuthor(): String = "MrAfonso"

    override fun getVersion(): String = plugin.pluginMeta.version

    override fun persist(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        return resolverHandler.processCustomPlaceholder(params, player?.player)
    }
}
