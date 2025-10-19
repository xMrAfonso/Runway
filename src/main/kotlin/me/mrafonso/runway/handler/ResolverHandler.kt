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
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
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

                createTagForPlaceholder(placeholder)?.let { builder.resolver(TagResolver.resolver(prefix.lowercase(), it)) }
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
    private fun createTagForPlaceholder(placeholder: Placeholder): Tag? {
        return when (placeholder) {
            is TextPlaceholder -> Tag.selfClosingInserting(miniMessage.deserialize(placeholder.value, resolver))
            is NumberPlaceholder -> Tag.selfClosingInserting(miniMessage.deserialize(placeholder.value.toString(), resolver))
            is ConditionalPlaceholder -> evaluateConditionalPlaceholder(placeholder)
            else -> null
        }
    }

    /**
     * Evaluates a [ConditionalPlaceholder] and returns the appropriate [Tag] based on the condition.
     *
     * @param placeholder The [ConditionalPlaceholder] to evaluate.
     * @return [Tag] The resulting [Tag] after evaluating the condition.
     */
    private fun evaluateConditionalPlaceholder(placeholder: ConditionalPlaceholder): Tag {
        val conditionResult = evaluateExpression(placeholder.condition)
        val groupResult = evaluateGroupCondition(placeholder.group)

        return when {
            conditionResult && groupResult -> Tag.selfClosingInserting(miniMessage.deserialize(placeholder.ifTrue, resolver))
            !conditionResult && placeholder.ifElse != null -> Tag.selfClosingInserting(miniMessage.deserialize(placeholder.ifElse, resolver))
            else -> Tag.selfClosingInserting(miniMessage.deserialize("", resolver))
        }
    }

    /**
     * Evaluates a boolean expression using the expression parser.
     *
     * @param expression The expression to evaluate.
     * @return [Boolean] The result of the evaluated expression.
     */
    private fun evaluateExpression(expression: String): Boolean {
        // Add caching logic here if needed
        return expressionParser.compile(expression, ParseWarnCollector(expression)).returnBooleanExpression().evaluate()
    }

    /**
     * Evaluates the condition of a group if it exists.
     *
     * @param groupName The name of the group to evaluate.
     * @return [Boolean] The result of the group's condition evaluation, or true if no condition exists.
     */
    private fun evaluateGroupCondition(groupName: String?): Boolean {
//        val group = groupName?.let { groups[groupName] } ?: return true
//        if (group.condition == null) return true
//
////        val groupCondition = miniMessage.serialize(processPlaceholders(group.condition, player))
////            .replace("\\<", "<").replace("\\>", ">")
//
//        return evaluateExpression(groupCondition)
        return true
    }

    /**
     * Proccesses the input [String] with MiniMessage formatting and custom placeholders.
     */
    fun processPlaceholders(text: String, player: Player?): Component? {
        return try {
            player?.let {
                miniMessage.deserialize(text,player, resolver)
            } ?: miniMessage.deserialize(text, resolver)
        } catch (_: ParsingException) {
            null
        }
    }

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

    fun reloadAll() {
        configs.forEach { (_, config) -> config.reload() }
    }

    fun saveAll() {
        configs.forEach { (_, config) -> config.save() }
    }
}
