package me.mrafonso.runway.resolver

import io.github.miniplaceholders.api.MiniPlaceholders
import me.mrafonso.runway.Runway
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.scheduler.BukkitTask

class MiniPlaceholdersResolverCache(
    private val plugin: Runway,
    private val configHandler: ConfigHandler
) {

    @Volatile
    private var resolver: TagResolver = TagResolver.empty()

    private var task: BukkitTask? = null

    fun get(): TagResolver {
        if (refreshSeconds() <= 0) refresh()
        return resolver
    }

    fun reload(enabled: Boolean) {
        stop()

        if (!enabled) {
            resolver = TagResolver.empty()
            return
        }

        refresh()

        val refreshSeconds = refreshSeconds()
        if (refreshSeconds <= 0) return

        val refreshTicks = refreshSeconds * 20L
        val refreshTask: Runnable = { refresh() }
        task = plugin.server.scheduler.runTaskTimerAsynchronously(plugin, refreshTask, refreshTicks, refreshTicks)
    }

    fun stop() {
        task?.cancel()
        task = null
    }

    private fun refresh() {
        resolver = MiniPlaceholders.audienceGlobalPlaceholders()
    }

    private fun refreshSeconds(): Long = configHandler.get<Settings>()
        .miniPlaceholders
        .refreshRate
}
