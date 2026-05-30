package me.mrafonso.runway.resolver

import me.mrafonso.runway.Runway
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.integration.HookHandler
import me.mrafonso.runway.integration.TagManager
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class ResolverHandler(val plugin: Runway, val hookHandler: HookHandler, configHandler: ConfigHandler) {

    private val tagManager = TagManager(hookHandler.placeholderAPI)
    private val groupManager = PlaceholderGroupManager(plugin)
    private val miniPlaceholdersResolverCache = MiniPlaceholdersResolverCache(plugin, configHandler)
    private val miniMessage = MiniMessage.miniMessage()
    private val evaluator = PlaceholderEvaluator(miniMessage) { text, player -> processPlaceholders(text, player) }
    private val builder = TagResolverBuilder(evaluator) { text, target -> deserializeWithTarget(text, target) }
    private val sanitizedEvaluator = PlaceholderEvaluator(miniMessage) { text, player ->
        processSanitizedPlaceholders(text, player)
    }
    private val sanitizedBuilder = TagResolverBuilder(sanitizedEvaluator) { text, target ->
        deserializeSanitizedWithTarget(text, target)
    }

    var resolver: TagResolver = tagManager.resolver()
    var sanitizedResolver: TagResolver = TagResolver.empty()
    private var customPlaceholderNames: Set<String> = emptySet()

    fun reloadAll() {
        groupManager.reloadAll()
        loadPlaceholders()
        miniPlaceholdersResolverCache.reload(hookHandler.miniPlaceholders)
    }

    fun stop() {
        miniPlaceholdersResolverCache.stop()
    }

    fun loadPlaceholders() {
        val groups = groupManager.groups().map { it.get() }
        customPlaceholderNames = groups.flatMap { group ->
            group.placeholders.keys.map { key ->
                if (group.prefix != null) "${group.prefix}_$key" else key
            }
        }.map { it.lowercase() }.toSet()
        sanitizedResolver = sanitizedBuilder.buildSanitized(groups)
        resolver = TagResolver.resolver(builder.build(groups), tagManager.resolver())
    }

    fun processCustomPlaceholder(name: String, player: Pointered?): String? {
        val normalizedName = name.lowercase()
        if (normalizedName !in customPlaceholderNames) return null

        val component = processPlaceholders("<$normalizedName>", player) ?: return null
        return miniMessage.serialize(component)
    }

    fun processSanitizedPlaceholders(text: String, player: Pointered?): Component? {
        return try {
            player?.let {
                miniMessage.deserialize(text, player, sanitizedResolver)
            } ?: miniMessage.deserialize(text, sanitizedResolver)
        } catch (_: ParsingException) {
            null
        }
    }

    /**
     * Process the input [String] with MiniMessage formatting and custom placeholders.
     *
     * @param text The input [String] to process.
     * @param player The [Pointered] to use for MiniPlaceholders parsing. Can be null.
     * @return [net.kyori.adventure.text.Component] The processed [net.kyori.adventure.text.Component], or null if parsing fails
     */
    fun processPlaceholders(text: String, player: Pointered?): Component? {
        val currentResolver = withMiniPlaceholders(resolver)
        return try {
            player?.let {
                miniMessage.deserialize(text, player, currentResolver)
            } ?: miniMessage.deserialize(text, currentResolver)
        } catch (_: ParsingException) {
            null
        }
    }

    /**
     * Deserializes a MiniMessage formatted string with an optional target.
     *
     * @param text The MiniMessage formatted string to deserialize.
     * @param target The optional [Pointered] target for context.
     * @return [Component] The deserialized [Component].
     */
    private fun deserializeWithTarget(text: String, target: Pointered?): Component {
        val currentResolver = withMiniPlaceholders(resolver)
        return target?.let { miniMessage.deserialize(text, target, currentResolver) }
            ?: miniMessage.deserialize(text, currentResolver)
    }

    private fun deserializeSanitizedWithTarget(text: String, target: Pointered?): Component {
        return target?.let { miniMessage.deserialize(text, target, sanitizedResolver) }
            ?: miniMessage.deserialize(text, sanitizedResolver)
    }

    fun withMiniPlaceholders(baseResolver: TagResolver): TagResolver {
        if (!hookHandler.miniPlaceholders) return baseResolver
        return TagResolver.resolver(baseResolver, miniPlaceholdersResolverCache.get())
    }
}
