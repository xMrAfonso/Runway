package me.mrafonso.runway.processing

import com.github.retrooper.packetevents.protocol.item.ItemStack
import io.github.miniplaceholders.api.MiniPlaceholders
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.mrafonso.runway.config.ConfigHandler
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.integration.HookHandler
import me.mrafonso.runway.resolver.ResolverHandler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player

/**
 * Handles processing of [Component] and [ItemStack] with MiniMessage formatting, custom placeholders,
 * and integration with PlaceholderAPI/MiniPlaceholders.
 *
 * @property hookHandler The [me.mrafonso.runway.integration.HookHandler] instance for checking available hooks.
 * @property configHandler The [me.mrafonso.runway.config.ConfigHandler] instance for accessing configuration settings.
 */
class ProcessHandler(
    private val hookHandler: HookHandler,
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
    fun processComponent(input: Component?, player: Player? = null): Component? {
        if (input == null) return null
        return processComponent(miniMessage.serialize(input), player)
    }

    /**
     * Applies MiniMessage formatting, custom placeholders and parses PlaceholderAPI/MiniPlaceholders.
     * Also handles prefix requirements and disabling italics according to settings.
     *
     * @param input The input [String] to process.
     * @param player The [Player] to use for PlaceholderAPI/MiniPlaceholders parsing. Can be null.
     * @return [Component] The processed [Component], or null if the message should be ignored.
     */
    fun processComponent(input: String, player: Player? = null): Component? {
        val settings = configHandler.get<Settings>()
        val requirePrefix = settings.prefix.required
        val prefix = settings.prefix.value

        val disableItalics = settings.disableItalics

        // If the prefix is required, ignore messages not starting with `prefix`
        if (requirePrefix &&
            !input.startsWith(prefix)
        ) return null

        // If the prefix is not required, ignore messages starting with `!prefix`
        if (!requirePrefix &&
            input.startsWith("!$prefix")
        ) return null

        // When serializing to MiniMessage, '<' is escaped as '\<', so we need to unescape it
        var text = input.replace("\\<", "<")

        // Remove prefix from text if it exists
        if(text.startsWith(prefix)) text = text.drop(prefix.length)

        var resolver: TagResolver = resolverHandler.resolver
        if (hookHandler.miniPlaceholders) {
            resolver = TagResolver.resolver(resolver, MiniPlaceholders.audienceGlobalPlaceholders())
        }

        // Apply no italics tag if needed
        if (disableItalics) text = "$noItalics$text"
        return deserialize(text,player, resolver)
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
     * @return [List] of processed [Component], excluding any that should be ignored.
     */
    fun processComponents(input: List<Component>, player: Player?): List<Component> {
        return input.mapNotNull { processComponent(it, player) }
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
    fun processItem(item: ItemStack, player: Player?): ItemStack {
        val bukkitItem = SpigotConversionUtil.toBukkitItemStack(item)
        bukkitItem.itemMeta?.let { meta ->
            meta.displayName()?.let { meta.displayName(processComponent(it, player)) }
            meta.lore()?.let { meta.lore(processComponents(it, player)) }
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
    fun processItems(items: List<ItemStack>, player: Player?): List<ItemStack> {
        return items.map { processItem(it, player) }
    }
}