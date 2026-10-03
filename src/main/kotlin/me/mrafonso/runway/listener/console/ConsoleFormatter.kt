package me.mrafonso.runway.listener.console

import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.processing.ProcessHandler
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer

/**
 * Parses Runway text inside console log lines, e.g. `[Plugin] $<rank> started`.
 *
 * Console output mixes plugin text with ordinary logs, so a line is only touched when the prefix
 * starts a word (line start, after whitespace or after colour codes) and a MiniMessage tag follows it.
 * That keeps things like `paid $5` intact. The result uses ANSI escapes: Paper's console shows them as
 * colours and strips them from `latest.log` (it no longer translates section-sign codes).
 */
class ConsoleFormatter(
    private val processHandler: ProcessHandler,
    private val configHandler: ConfigHandler
) {
    /**
     * @return The rewritten message, or null if nothing in it was Runway text.
     */
    fun format(message: String): String? {
        val settings = configHandler.get<Settings>()
        if (!settings.listeners.console.enable) return null

        val prefix = settings.prefix.value
        if (prefix.isEmpty() || !message.contains(prefix)) return null

        var changed = false
        val lines = message.split('\n').map { line ->
            formatLine(line, prefix)?.also { changed = true } ?: line
        }
        return if (changed) lines.joinToString("\n") else null
    }

    private fun formatLine(line: String, prefix: String): String? {
        var index = line.indexOf(prefix)
        while (index >= 0) {
            if (isWordStart(line, index) && line.indexOf('<', index + prefix.length) >= 0) {
                val parsed = processHandler.processComponent(line.substring(index), null, true) ?: return null
                return line.substring(0, index) + ansi.serialize(parsed)
            }
            index = line.indexOf(prefix, index + 1)
        }
        return null
    }

    private fun isWordStart(line: String, index: Int): Boolean {
        val before = line.substring(0, index)
        val codes = TRAILING_CODES.find(before)?.range?.first ?: before.length
        return codes == 0 || before[codes - 1].isWhitespace()
    }

    private companion object {
        /** ANSI escapes or section-sign codes directly before the prefix, e.g. from a coloured message. */
        val TRAILING_CODES = Regex("(?:\u001B\\[[0-9;]*m|§.)+$")

        val ansi: ANSIComponentSerializer = ANSIComponentSerializer.ansi()
    }
}
