package me.mrafonso.runway.handler

import ch.andre601.expressionparser.DefaultExpressionParserEngine
import ch.andre601.expressionparser.ParseWarnCollector
import dev.triumphteam.polaris.Config
import dev.triumphteam.polaris.loadConfig
import dev.triumphteam.polaris.yaml.Yaml
import io.github.miniplaceholders.api.MiniPlaceholders
import me.mrafonso.runway.Runway
import me.mrafonso.runway.config.placeholder.ConditionalPlaceholder
import me.mrafonso.runway.config.placeholder.Group
import me.mrafonso.runway.config.placeholder.IndexedPlaceholder
import me.mrafonso.runway.config.placeholder.NumberPlaceholder
import me.mrafonso.runway.config.placeholder.Placeholder
import me.mrafonso.runway.config.placeholder.PlaceholdersTemplate
import me.mrafonso.runway.config.placeholder.TextPlaceholder
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import java.nio.file.Files
import java.nio.file.Path

class ResolverHandler(val plugin: Runway, val hookHandler: HookHandler) {

    private val configs: MutableMap<String, Config<PlaceholdersTemplate>> = mutableMapOf()
    private val groups: MutableMap<String, Group> = mutableMapOf()
    var resolver: TagResolver = TagResolver.empty()
    private val miniMessage = MiniMessage.miniMessage()
    private val expressionParser = DefaultExpressionParserEngine.createDefault()

    fun loadPlaceholders() {
        loadGroups()
        resolver = buildTagResolver()

        if (hookHandler.miniPlaceholders) {
            resolver = TagResolver.resolver(resolver, MiniPlaceholders.audienceGlobalPlaceholders())
        }
    }

    private fun loadGroups() {
        configs.values.map { it.get() }.forEach { config ->
            config.groups.forEach { (key, group) ->
                if (groups.containsKey(key)) {
                    plugin.logger.warning("Group '$key' is defined multiple times, overwriting previous definition")
                }
                groups[key] = group
            }
        }
    }

    /**
     * Builds a [TagResolver] from all loaded custom placeholder configurations.
     *
     * @return [TagResolver] The constructed [TagResolver] with all custom placeholders registered.
     */
    private fun buildTagResolver(): TagResolver {
        val builder = TagResolver.builder()

        configs.values.map { it.get() }.forEach { config ->
            config.placeholders.forEach { (key, placeholder) ->
                val prefix = resolvePrefix(key, placeholder)

                if (placeholder is IndexedPlaceholder) {
                    handleIndexedPlaceholder(builder, prefix, placeholder)
                    return@forEach
                }

                builder.resolver(TagResolver.resolver(prefix.lowercase()) { _, context ->
                    val target = context.target()
                    println(prefix)
                    createTagForPlaceholder(placeholder, target)
                })
            }
        }

        return builder.build()
    }

    /**
     * Resolves the full placeholder key with group prefix if applicable.
     *
     * @param key The base placeholder key.
     * @param placeholder The [Placeholder] object containing group information.
     * @return [String] The resolved placeholder key with group prefix if applicable.
     */
    private fun resolvePrefix(key: String, placeholder: Placeholder): String {
        val group = placeholder.group?.let { groups[it] }

        if (placeholder.group != null && group == null) {
            plugin.logger.warning("Placeholder '$key' references a non-existent group '${placeholder.group}'")
            return key
        }

        return if (group != null) "${group.prefix}_$key" else key
    }

    private fun handleIndexedPlaceholder(builder: TagResolver.Builder, prefix: String, placeholder: Placeholder) {

    }

    /**
     * Creates a [Tag] for the given [Placeholder].
     *
     * @param placeholder The [Placeholder] to create a [Tag] for.
     * @return [Tag] The created [Tag], or null if the placeholder type
     */
    private fun createTagForPlaceholder(placeholder: Placeholder, target: Pointered?): Tag? {
        return when (placeholder) {
            is TextPlaceholder -> {
                target?.let { Tag.selfClosingInserting(miniMessage.deserialize(placeholder.value, target, resolver)) }
                    ?: Tag.selfClosingInserting(miniMessage.deserialize(placeholder.value, resolver))
            }
            is NumberPlaceholder -> {
                target?.let { Tag.selfClosingInserting(miniMessage.deserialize(placeholder.value.toString(), target, resolver)) }
                    ?: Tag.selfClosingInserting(miniMessage.deserialize(placeholder.value.toString(), resolver))
            }
            is ConditionalPlaceholder -> evaluateConditionalPlaceholder(placeholder, target)
            else -> null
        }
    }

    /**
     * Evaluates a [ConditionalPlaceholder] and returns the appropriate [Tag] based on the condition.
     *
     * @param placeholder The [ConditionalPlaceholder] to evaluate.
     * @return [Tag] The resulting [Tag] after evaluating the condition.
     */
    private fun evaluateConditionalPlaceholder(placeholder: ConditionalPlaceholder, target: Pointered?): Tag {
        println("- ${placeholder.condition}")
        println("+ ${placeholder.group}")
        val conditionResult = evaluateExpression(placeholder.condition, target)
        val groupResult = evaluateGroupCondition(placeholder.group, target)

        return when {
            conditionResult && groupResult -> {
                target?.let { Tag.selfClosingInserting(miniMessage.deserialize(placeholder.ifTrue, target, resolver)) }
                    ?: Tag.selfClosingInserting(miniMessage.deserialize(placeholder.ifTrue, resolver))
            }
            !conditionResult && placeholder.ifElse != null -> {
                target?.let { Tag.selfClosingInserting(miniMessage.deserialize(placeholder.ifElse, target, resolver)) }
                    ?: Tag.selfClosingInserting(miniMessage.deserialize(placeholder.ifElse, resolver))
            }
            else -> Tag.selfClosingInserting(miniMessage.deserialize("", resolver))
        }
    }

    /**
     * Evaluates a boolean expression using the expression parser.
     *
     * @param expression The expression to evaluate.
     * @return [Boolean] The result of the evaluated expression.
     */
    private fun evaluateExpression(expression: String, target: Pointered?): Boolean {
        // Add caching logic here if needed
        processPlaceholders(expression, target)?.let {
            val condition = miniMessage.serialize(it)
                .replace("\\<", "<").replace("\\>", ">")
            return expressionParser.compile(condition, ParseWarnCollector(condition)).returnBooleanExpression().evaluate()
        }
        return true
    }

    /**
     * Evaluates the condition of a group if it exists.
     *
     * @param groupName The name of the group to evaluate.
     * @return [Boolean] The result of the group's condition evaluation, or true if no condition exists.
     */
    private fun evaluateGroupCondition(groupName: String?, target: Pointered?): Boolean {
        val group = groupName?.let { groups[groupName] } ?: return true
        if (group.condition == null) return true

        return evaluateExpression(group.condition, target)
    }

    /**
     * Process the input [String] with MiniMessage formatting and custom placeholders.
     *
     * @param text The input [String] to process.
     * @param player The [Pointered] to use for MiniPlaceholders parsing. Can be null.
     * @return [Component] The processed [Component], or null if parsing fails
     */
    fun processPlaceholders(text: String, player: Pointered?): Component? {
        if (hookHandler.miniPlaceholders) {
            resolver = TagResolver.resolver(resolver, MiniPlaceholders.audienceGlobalPlaceholders())
        }
        return try {
            player?.let {
                miniMessage.deserialize(text,player, resolver)
            } ?: miniMessage.deserialize(text, resolver)
        } catch (_: ParsingException) {
            null
        }
    }

    /**
     * Loads all placeholder configuration files from the placeholders directory.
     * If the directory does not exist, it creates it and loads the default configuration.
     */
    fun loadConfigs() {
        val path = Path.of("${plugin.dataFolder}/placeholders")
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            Files.createDirectory(path)
            load("placeholders/default.yml", PlaceholdersTemplate()).let { configs["default.yml"] = it }
        }
        else {
            val allFiles = Files.walk(path)
                .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".yml") }
                .map { it.fileName.toString() }
                .toList()

            allFiles.forEach { fileName ->
                try {
                    load("placeholders/$fileName").let { configs[fileName] = it }
                } catch (e: Exception) {
                    plugin.logger.warning("Failed to load placeholders from $fileName: ${e.message}")
                }
            }
        }
    }

    /**
     * Loads a placeholder configuration file.
     *
     * @param fileName The name of the configuration file to load.
     * @param placeholders An optional default [PlaceholdersTemplate] instance to write defaults from.
     * @return [Config] The loaded configuration.
     */
    fun load(fileName: String, placeholders: PlaceholdersTemplate? = null): Config<PlaceholdersTemplate> {
        val path = Path.of("${plugin.dataFolder}/$fileName")
        return loadConfig<PlaceholdersTemplate> {
            file = path
            writeDefaults = placeholders != null
            defaultInstance { PlaceholdersTemplate() }
            format = Yaml {
                indentationSize = 2
                explicitNulls = false
                encodeDefaults = true
            }
        }
    }

    /**
     * Reloads all loaded placeholder configurations.
     */
    fun reloadAll() {
        configs.forEach { (_, config) -> config.reload() }
    }

    /**
     * Saves all loaded placeholder configurations.
     */
    fun saveAll() {
        configs.forEach { (_, config) -> config.save() }
    }
}
