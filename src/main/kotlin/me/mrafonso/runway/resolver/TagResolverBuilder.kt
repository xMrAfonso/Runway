package me.mrafonso.runway.resolver

import me.mrafonso.runway.config.placeholder.ConditionalPlaceholder
import me.mrafonso.runway.config.placeholder.Group
import me.mrafonso.runway.config.placeholder.MatchPlaceholder
import me.mrafonso.runway.config.placeholder.NumberPlaceholder
import me.mrafonso.runway.config.placeholder.Placeholder
import me.mrafonso.runway.config.placeholder.RandomPlaceholder
import me.mrafonso.runway.config.placeholder.SwitchPlaceholder
import me.mrafonso.runway.config.placeholder.TextPlaceholder
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

class TagResolverBuilder(
    private val evaluator: PlaceholderEvaluator,
    private val deserialize: (String, Pointered?) -> Component
) {

    /**
     * Builds a [TagResolver] from all loaded custom placeholder configurations.
     *
     * @return [TagResolver] The constructed [TagResolver] with all custom placeholders registered.
     */
    fun build(groups: Collection<Group>): TagResolver {
        val builder = TagResolver.builder()

        groups.forEach { group ->
            println("Loading placeholder group: ${group.prefix}")
            group.placeholders.forEach { (key, placeholder) ->
                println(" - Loading placeholder: $key")
                val prefix = resolvePrefix(key, group)
                println(" - Loading placeholder prefix: $prefix")

                println("-- Creating resolver for placeholder with key: $prefix")
                builder.resolver(TagResolver.resolver(prefix.lowercase()) { _, context ->
                    val target = context.target()
                    println(" - Creating tag for placeholder with target: $target")
                    parseTagForPlaceholder(group, placeholder, target)
                })
            }
        }

        return builder.build()
    }

    /**
     * Resolves the full placeholder key with group prefix if applicable.
     *
     * @param key The base placeholder key.
     * @param group The [Group] object containing group information.
     * @return [String] The resolved placeholder key with group prefix if applicable.
     */
    private fun resolvePrefix(key: String, group: Group): String {
        return if (group.prefix != null) "${group.prefix}_$key" else key
    }

    /**
     * Creates a [net.kyori.adventure.text.minimessage.tag.Tag] for the given [me.mrafonso.runway.config.placeholder.Placeholder].
     *
     * @param group The [Group] the placeholder belongs to.
     * @param placeholder The [me.mrafonso.runway.config.placeholder.Placeholder] to create a [net.kyori.adventure.text.minimessage.tag.Tag] for.
     * @return [net.kyori.adventure.text.minimessage.tag.Tag] The created [net.kyori.adventure.text.minimessage.tag.Tag], or null if the placeholder type
     */
    private fun parseTagForPlaceholder(group: Group, placeholder: Placeholder, target: Pointered?): Tag? {
        println("-- Creating tag for placeholder: $placeholder")
        return when (placeholder) {
            is TextPlaceholder -> Tag.selfClosingInserting(deserialize(placeholder.value, target))
            is NumberPlaceholder -> Tag.selfClosingInserting(deserialize(placeholder.value.toString(), target))
            is ConditionalPlaceholder -> evaluator.evaluateConditional(group, placeholder, target, deserialize)
            is RandomPlaceholder -> Tag.selfClosingInserting(deserialize(placeholder.value.random(), target))
            is MatchPlaceholder -> evaluator.evaluateMatch(placeholder, target, deserialize)
            is SwitchPlaceholder -> evaluator.evaluateSwitch(placeholder, target, deserialize)
        }
    }
}
