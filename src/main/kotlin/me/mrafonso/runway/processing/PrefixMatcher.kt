package me.mrafonso.runway.processing

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent

/**
 * Detects and strips the Runway prefix on a [Component] tree rather than on its serialized form.
 *
 * Checking the serialized MiniMessage string misses styled text: a red `$<rank>` serializes to
 * `<red>$\<rank>`, which doesn't start with `$`. Team prefixes, tab names and most plugin
 * messages are styled, so the prefix is matched against the first visible text instead.
 */
object PrefixMatcher {
    private const val LEGACY_CHAR = '§'

    /**
     * @return A copy of [component] with [prefix] removed from its first visible text,
     * or null if that text doesn't start with [prefix].
     */
    fun strip(component: Component, prefix: String): Component? {
        if (prefix.isEmpty()) return component
        return (strip(component, prefix, depth = 0) as? Match.Found)?.component
    }

    /**
     * @return Whether the first visible text of [component] starts with [prefix].
     */
    fun startsWith(component: Component, prefix: String): Boolean = strip(component, prefix) != null

    private fun strip(component: Component, prefix: String, depth: Int): Match {
        if (depth > MAX_DEPTH) return Match.Mismatch

        if (component is TextComponent && component.content().isNotEmpty()) {
            val content = component.content()
            // Legacy colour codes inside plain text (e.g. "§c$<rank>") are invisible, so skip past them.
            val codesEnd = legacyCodesEnd(content)
            if (!content.startsWith(prefix, codesEnd)) return Match.Mismatch
            val stripped = content.substring(0, codesEnd) + content.substring(codesEnd + prefix.length)
            return Match.Found(component.content(stripped))
        }

        // Non-text content (translatable, keybind, score...) is visible and can't carry a prefix.
        if (component !is TextComponent) return Match.Mismatch

        val children = component.children()
        children.forEachIndexed { index, child ->
            when (val match = strip(child, prefix, depth + 1)) {
                is Match.Found -> {
                    val newChildren = children.toMutableList().also { it[index] = match.component }
                    return Match.Found(component.children(newChildren))
                }
                Match.Mismatch -> return Match.Mismatch
                Match.Empty -> Unit
            }
        }
        return Match.Empty
    }

    private fun legacyCodesEnd(content: String): Int {
        var index = 0
        while (index + 1 < content.length && content[index] == LEGACY_CHAR) index += 2
        return index
    }

    private sealed interface Match {
        data class Found(val component: Component) : Match
        data object Mismatch : Match
        /** No visible text yet; keep looking in the next sibling. */
        data object Empty : Match
    }

    private const val MAX_DEPTH = 64
}
