package me.mrafonso.runway.processing

import com.github.retrooper.packetevents.protocol.item.ItemStack
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.resolver.ResolverHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

/**
 * Handles processing of [Component] and [ItemStack] with MiniMessage formatting, custom placeholders,
 * and integration with PlaceholderAPI/MiniPlaceholders.
 *
 * @property configHandler The [me.mrafonso.runway.config.ConfigHandler] instance for accessing configuration settings.
 */
class ProcessHandler(
    private val configHandler: ConfigHandler,
    private val resolverHandler: ResolverHandler
) {
    private val miniMessage = MiniMessage.miniMessage()
    private val noItalics = "<!italic>"

    /**
     * Applies MiniMessage formatting, custom placeholders and parses PlaceholderAPI/MiniPlaceholders.
     * Also handles prefix requirements and disabling italics according to settings.
     *
     * @param input The input [Component] to process.
     * @param player The [Player] to use for PlaceholderAPI/MiniPlaceholders parsing. Can be null.
     * @return [Component] The processed [Component], or null if the message should be ignored.
     */
    fun processComponent(input: Component?, player: Player? = null, requirePrefix: Boolean? = null): Component? {
        if (input == null) return null
        return processComponent(miniMessage.serialize(input), player, requirePrefix)
    }

    /**
     * Applies MiniMessage formatting, custom placeholders and parses PlaceholderAPI/MiniPlaceholders.
     * Also handles prefix requirements and disabling italics according to settings.
     *
     * @param input The input [String] to process.
     * @param player The [Player] to use for PlaceholderAPI/MiniPlaceholders parsing. Can be null.
     * @return [Component] The processed [Component], or null if the message should be ignored.
     */
    fun processComponent(input: String, player: Player? = null, requirePrefix: Boolean? = null): Component? {
        val settings = configHandler.get<Settings>()
        val shouldRequirePrefix = requirePrefix ?: settings.prefix.required
        val prefix = settings.prefix.value

        val disableItalics = settings.disableItalics

        // If the prefix is required, ignore messages not starting with `prefix`
        if (shouldRequirePrefix &&
            !input.startsWith(prefix)
        ) return null

        // If the prefix is not required, ignore messages starting with `!prefix`
        if (!shouldRequirePrefix &&
            input.startsWith("!$prefix")
        ) return null

        val splitText = input.split("<sanitized>", limit = 2)

        // When serializing to MiniMessage, '<' is escaped as '\<', so we need to unescape it
        var text = splitText[0].replace("\\<", "<")

        // Remove prefix from text if it exists
        if(text.startsWith(prefix)) text = text.drop(prefix.length)

        val resolver: TagResolver = resolverHandler.withMiniPlaceholders(resolverHandler.resolver)

        val afterText = splitText.getOrNull(1)

        // Apply no italics tag if needed
        if (disableItalics) text = "$noItalics$text"
        val component = deserialize(text, player, resolver) ?: return null
        val sanitizedComponent = afterText?.let {
            deserialize(it, player, resolverHandler.sanitizedResolver) ?: return null
        } ?: Component.empty()

        return component.append(sanitizedComponent)
    }

    /**
     * Deserializes a string using MiniMessage with the provided resolver and optional player context.
     *
     * @param text The text to deserialize.
     * @param player The [Player] to use as context for deserialization. Can be null.
     * @param resolver The [TagResolver] to use for tag resolution.
     * @return [Component] The deserialized component, or null if parsing fails.
     */
    private fun deserialize(text: String, player: Player?, resolver: TagResolver): Component? {
        return try {
            player?.let {
                miniMessage.deserialize(text, player, resolver)
            } ?: miniMessage.deserialize(text, resolver)
        } catch (_: ParsingException) {
            null
        }
    }


    /**
     * Maps `processComponent(Component, Player?)` over a list of [Component]
     *
     * @param input The list of input [Component]s to process.
     * @param player The [Player] to use for PlaceholderAPI/MiniPlaceholders parsing. Can be null.
     * @return [List] of processed [Component], preserving the original line when it should be ignored.
     */
    fun processComponents(input: List<Component>, player: Player?, requirePrefix: Boolean? = null): List<Component> {
        return input.map { original -> processComponent(original, player, requirePrefix) ?: original }
    }

    /**
     * Processes an [ItemStack]'s display name and lore using MiniMessage formatting, custom placeholders and
     * parses PlaceholderAPI/MiniPlaceholders. Also handles prefix requirements and disabling italics
     * according to settings.
     *
     * @param item The [ItemStack] to process.
     * @param player The [Player] to use for PlaceholderAPI/MiniPlaceholders parsing. Can be null.
     * @return [ItemStack] The processed [ItemStack] with updated display name and lore.
     */
    fun processItem(item: ItemStack, player: Player?, requirePrefix: Boolean? = null): ItemStack {
        val bukkitItem = SpigotConversionUtil.toBukkitItemStack(item)
        bukkitItem.itemMeta?.let { meta ->
            meta.displayName()?.let { original ->
                val processed = processComponent(original, player, requirePrefix)
                if (processed != null) {
                    meta.displayName(processed)
                }
            }
            meta.lore()?.let { original ->
                val processed = processComponents(original, player, requirePrefix)
                if (processed.isNotEmpty()) {
                    meta.lore(processed)
                }
            }
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
}
