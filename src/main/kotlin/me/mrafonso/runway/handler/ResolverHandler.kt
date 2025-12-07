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
import me.mrafonso.runway.config.placeholder.MatchPlaceholder
import me.mrafonso.runway.config.placeholder.NumberPlaceholder
import me.mrafonso.runway.config.placeholder.Placeholder
import me.mrafonso.runway.config.placeholder.RandomPlaceholder
import me.mrafonso.runway.config.placeholder.SwitchPlaceholder
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

    private val groups: MutableMap<String, Config<Group>> = mutableMapOf()
    var resolver: TagResolver = TagResolver.empty()
    private val miniMessage = MiniMessage.miniMessage()
    private val expressionParser = DefaultExpressionParserEngine.createDefault()

    fun reloadAll() {
        reloadGroupFiles()
        loadPlaceholders()
    }

    fun loadPlaceholders() {
        resolver = buildTagResolver()

        if (hookHandler.miniPlaceholders) {
            resolver = TagResolver.resolver(resolver, MiniPlaceholders.audienceGlobalPlaceholders())
        }
    }

    /**
     * Builds a [TagResolver] from all loaded custom placeholder configurations.
     *
     * @return [TagResolver] The constructed [TagResolver] with all custom placeholders registered.
     */
    private fun buildTagResolver(): TagResolver {
        val builder = TagResolver.builder()

        groups.values.map { it.get() }.forEach { group ->
            println("Loading placeholder group: ${group.prefix}")
            group.placeholders.forEach { (key, placeholder) ->
                println(" - Loading placeholder: $key")
                val prefix = resolvePrefix(key, group)
                println(" - Loading placeholder prefix: $prefix")

                println("-- Creating resolver for placeholder with key: $prefix")
                builder.resolver(TagResolver.resolver(prefix.lowercase()) { _, context ->
                    val target = context.target()
                    println(" - Creating tag for placeholder with target: $target")
                    createTagForPlaceholder(group, placeholder, target)
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
     * Creates a [Tag] for the given [Placeholder].
     *
     * @param group The [Group] the placeholder belongs to.
     * @param placeholder The [Placeholder] to create a [Tag] for.
     * @return [Tag] The created [Tag], or null if the placeholder type
     */
    private fun createTagForPlaceholder(group: Group, placeholder: Placeholder, target: Pointered?): Tag? {
        println("-- Creating tag for placeholder: $placeholder")
        return when (placeholder) {
            is TextPlaceholder -> Tag.selfClosingInserting(deserializeWithTarget(placeholder.value, target))
            is NumberPlaceholder -> Tag.selfClosingInserting(deserializeWithTarget(placeholder.value.toString(), target))
            is ConditionalPlaceholder -> evaluateConditionalPlaceholder(group, placeholder, target)
            is RandomPlaceholder -> Tag.selfClosingInserting(deserializeWithTarget(placeholder.value.random(), target))
            is MatchPlaceholder -> evaluateMatchPlaceholder(placeholder, target)
            is SwitchPlaceholder -> evaluateSwitchPlaceholder(placeholder, target)
        }
    }

    /**
     * Evaluates a [ConditionalPlaceholder] and returns the appropriate [Tag] based on the condition.
     *
     * @param group The [Group] the placeholder belongs to.
     * @param placeholder The [ConditionalPlaceholder] to evaluate.
     * @return [Tag] The resulting [Tag] after evaluating the condition.
     */
    private fun evaluateConditionalPlaceholder(group: Group, placeholder: ConditionalPlaceholder, target: Pointered?): Tag {
        println("- ${placeholder.condition}")
        println("+ ${group.condition}")
        val conditionResult = evaluateExpression(placeholder.condition, target)
        val groupResult = evaluateGroupCondition(group, target)

        val output = when {
            conditionResult && groupResult -> placeholder.ifTrue
            !conditionResult && placeholder.ifElse != null -> placeholder.ifElse
            else -> ""
        }

        return Tag.selfClosingInserting(deserializeWithTarget(output, target))
    }

    /**
     * Evaluates a [MatchPlaceholder] and returns the appropriate [Tag] based on the matching case.
     *
     * @param placeholder The [MatchPlaceholder] to evaluate.
     * @return [Tag] The resulting [Tag] after evaluating the match cases.
     */
    private fun evaluateMatchPlaceholder(placeholder: MatchPlaceholder, target: Pointered?): Tag {
        val input = processPlaceholders(placeholder.input, target)

        val match = placeholder.case.firstOrNull {
            val processedKey = processPlaceholders(it.comparison, target)
            input == processedKey
        }

        val output = match?.output ?: placeholder.default
        return Tag.selfClosingInserting(deserializeWithTarget(output, target))
    }

    /**
     * Evaluates a [SwitchPlaceholder] and returns the appropriate [Tag] based on the evaluated case.
     *
     * @param placeholder The [SwitchPlaceholder] to evaluate.
     * @return [Tag] The resulting [Tag] after evaluating the switch cases.
     */
    private fun evaluateSwitchPlaceholder(placeholder: SwitchPlaceholder, target: Pointered?): Tag {
        val match = placeholder.case.firstOrNull {
            evaluateExpression(placeholder.input + it.comparison, target)
        }

        val output = match?.output ?: placeholder.default
        return Tag.selfClosingInserting(deserializeWithTarget(output, target))
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
     * @param group The [Group] whose condition is to be evaluated.
     * @return [Boolean] The result of the group's condition evaluation, or true if no condition exists.
     */
    private fun evaluateGroupCondition(group: Group, target: Pointered?): Boolean {
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
            println("Placeholders directory not found. Creating default configuration.")
            Files.createDirectory(path)
            load("placeholders/default.yml", true).let { groups["default.yml"] = it }
        } else {
            println("Loading placeholder configuration files from placeholders directory.")
            val allFiles = Files.walk(path)
                .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".yml") }
                .map { it.fileName.toString() }
                .toList()

            allFiles.forEach { fileName ->
                try {
                    load("placeholders/$fileName").let { groups[fileName] = it }
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
     * @param placeholders An optional default [Group] instance to write defaults from.
     * @return [Config] The loaded configuration.
     */
    fun load(fileName: String, writeDefault: Boolean = false): Config<Group> {
        val path = Path.of("${plugin.dataFolder}/$fileName")
        println("Loading placeholders from $fileName")
        return loadConfig<Group> {
            file = path
            writeDefaults = writeDefault
            defaultInstance { Group.template() }
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
    fun reloadGroupFiles() {
        groups.forEach { (_, config) -> config.reload() }
    }

    /**
     * Saves all loaded placeholder configurations.
     */
    fun savePlaceholders() {
        groups.forEach { (_, config) -> config.save() }
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
