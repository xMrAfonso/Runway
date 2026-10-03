package me.mrafonso.runway.processing

import com.github.retrooper.packetevents.protocol.item.ItemStack
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.resolver.ResolverHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

/**
 * Handles processing of [Component] and [ItemStack] with MiniMessage formatting, custom placeholders,
 * and integration with PlaceholderAPI/MiniPlaceholders.
 *
 * @property configHandler The [ConfigHandler] instance for accessing configuration settings.
 */
class ProcessHandler(
    private val configHandler: ConfigHandler,
    private val resolverHandler: ResolverHandler
) {
    private val miniMessage = MiniMessage.miniMessage()

    /**
     * Applies MiniMessage formatting, custom placeholders and parses PlaceholderAPI/MiniPlaceholders.
     * The prefix is matched against the first visible text, so styled input is still recognised.
     *
     * @param input The input [Component] to process.
     * @param player The [Player] to use for PlaceholderAPI/MiniPlaceholders parsing. Can be null.
     * @return [Component] The processed [Component], or null if the message should be ignored.
     */
    fun processComponent(input: Component?, player: Player? = null, requirePrefix: Boolean? = null): Component? {
        if (input == null) return null
        val settings = configHandler.get<Settings>()
        val prefix = settings.prefix.value

        PrefixMatcher.strip(input, prefix)?.let { stripped ->
            return parse(miniMessage.serialize(stripped), player)
        }

        // If the prefix is required, ignore messages not starting with `prefix`;
        // otherwise ignore messages starting with `!prefix`.
        if (requirePrefix ?: settings.prefix.required) return null
        if (PrefixMatcher.startsWith(input, "!$prefix")) return null

        return parse(miniMessage.serialize(input), player)
    }

    /**
     * Applies MiniMessage formatting, custom placeholders and parses PlaceholderAPI/MiniPlaceholders.
     * Also handles prefix requirements according to settings.
     *
     * @param input The input MiniMessage [String] to process.
     * @param player The [Player] to use for PlaceholderAPI/MiniPlaceholders parsing. Can be null.
     * @param extraResolver Additional tags available only for this call.
     * @return [Component] The processed [Component], or null if the message should be ignored.
     */
    fun processComponent(
        input: String,
        player: Player? = null,
        requirePrefix: Boolean? = null,
        extraResolver: TagResolver = TagResolver.empty()
    ): Component? {
        val settings = configHandler.get<Settings>()
        val prefix = settings.prefix.value

        if (input.startsWith(prefix)) return parse(input.drop(prefix.length), player, extraResolver)
        if (requirePrefix ?: settings.prefix.required) return null
        if (input.startsWith("!$prefix")) return null

        return parse(input, player, extraResolver)
    }

    /**
     * Parses already prefix-stripped MiniMessage text. Anything after a `<sanitized>` marker is parsed
     * with the sanitized resolver only, so player-provided content can't use arbitrary tags.
     *
     * @return The parsed [Component], or null if parsing fails.
     */
    private fun parse(input: String, player: Player?, extraResolver: TagResolver = TagResolver.empty()): Component? {
        val splitText = input.split("<sanitized>", limit = 2)

        // Serializing to MiniMessage escapes '<' as '\<', and once more per nesting level inside tag
        // arguments (e.g. a display name in `<lang:chat.type.text:...>`), so unescape every level.
        val text = LegacyTranslator.toMiniMessage(splitText[0].replace(ESCAPED_TAG_START, "<"))
        val resolver = TagResolver.resolver(resolverHandler.withMiniPlaceholders(resolverHandler.resolver), extraResolver)

        val component = deserialize(text, player, resolver) ?: return null
        val sanitizedComponent = splitText.getOrNull(1)?.let {
            deserialize(it, player, resolverHandler.sanitizedResolver) ?: return null
        } ?: return component

        return component.append(sanitizedComponent)
    }

    /**
     * Deserializes a string using MiniMessage with the provided resolver and optional player context.
     *
     * @return [Component] The deserialized component, or null if parsing fails.
     */
    private fun deserialize(text: String, player: Player?, resolver: TagResolver): Component? {
        return try {
            player?.let {
                miniMessage.deserialize(text, player, resolver)
            } ?: miniMessage.deserialize(text, resolver)
        } catch (e: ParsingException) {
            resolverHandler.debug.log { "Failed to parse '$text': ${e.message}" }
            null
        }
    }

    /**
     * Maps `processComponent(Component, Player?)` over a list of [Component]
     *
     * @return [List] of processed [Component], preserving the original line when it should be ignored.
     */
    fun processComponents(input: List<Component>, player: Player?, requirePrefix: Boolean? = null): List<Component> {
        return input.map { original -> processComponent(original, player, requirePrefix) ?: original }
    }

    /**
     * Processes an [ItemStack]'s display name and lore using MiniMessage formatting, custom placeholders and
     * parses PlaceholderAPI/MiniPlaceholders. Processed lines lose the default item italics if
     * `disable-italics` is enabled.
     *
     * @param item The [ItemStack] to process.
     * @param player The [Player] to use for PlaceholderAPI/MiniPlaceholders parsing. Can be null.
     * @return [ItemStack] The processed [ItemStack] with updated display name and lore.
     */
    fun processItem(item: ItemStack, player: Player?, requirePrefix: Boolean? = null): ItemStack {
        val disableItalics = configHandler.get<Settings>().disableItalics
        fun process(line: Component): Component? = processComponent(line, player, requirePrefix)?.let {
            if (disableItalics) it.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE) else it
        }

        val bukkitItem = SpigotConversionUtil.toBukkitItemStack(item)
        bukkitItem.itemMeta?.let { meta ->
            meta.displayName()?.let { original -> process(original)?.let { meta.displayName(it) } }
            meta.lore()?.let { original -> meta.lore(original.map { line -> process(line) ?: line }) }
            bukkitItem.itemMeta = meta
        }

        return SpigotConversionUtil.fromBukkitItemStack(bukkitItem)
    }

    /**
     * Maps `processItem(ItemStack, Player?)` over a list of [ItemStack]
     *
     * @param items The [List] of [ItemStack]s to process.
     * @param player The [Player] to use for PlaceholderAPI/MiniPlaceholders parsing. Can be null.
     * @return [List] of processed [ItemStack] with updated display names and lores.
     */
    fun processItems(items: List<ItemStack>, player: Player?, requirePrefix: Boolean? = null): List<ItemStack> {
        return items.map { processItem(it, player, requirePrefix) }
    }

    private companion object {
        val ESCAPED_TAG_START = Regex("""\\+<""")
    }
}
