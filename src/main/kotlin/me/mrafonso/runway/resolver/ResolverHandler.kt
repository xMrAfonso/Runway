package me.mrafonso.runway.resolver

import ch.andre601.expressionparser.DefaultExpressionParserEngine
import io.github.miniplaceholders.api.MiniPlaceholders
import me.mrafonso.runway.Runway
import me.mrafonso.runway.integration.HookHandler
import me.mrafonso.runway.integration.TagManager
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class ResolverHandler(val plugin: Runway, val hookHandler: HookHandler) {

    private val tagManager = TagManager()
    private val groupManager = PlaceholderGroupManager(plugin)
    private val miniMessage = MiniMessage.miniMessage()
    private val expressionParser = DefaultExpressionParserEngine.createDefault()
    private val evaluator = PlaceholderEvaluator(miniMessage, expressionParser) { text, player -> processPlaceholders(text, player) }
    private val builder = TagResolverBuilder(evaluator) { text, target -> deserializeWithTarget(text, target) }

    var resolver: TagResolver = tagManager.resolver()

    fun reloadAll() {
        groupManager.reloadAll()
        loadPlaceholders()
    }

    fun loadPlaceholders() {
        val groups = groupManager.groups().map { it.get() }
        resolver = TagResolver.resolver(builder.build(groups), tagManager.resolver())

        if (hookHandler.miniPlaceholders) {
            resolver = TagResolver.resolver(resolver, MiniPlaceholders.audienceGlobalPlaceholders())
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
        var currentResolver = resolver
        if (hookHandler.miniPlaceholders) {
            currentResolver = TagResolver.resolver(currentResolver, MiniPlaceholders.audienceGlobalPlaceholders())
        }
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
        return target?.let { miniMessage.deserialize(text, target, resolver) }
            ?: miniMessage.deserialize(text, resolver)
    }
}
