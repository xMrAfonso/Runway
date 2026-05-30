package me.mrafonso.runway.benchmark

import me.mrafonso.runway.integration.TagManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import kotlin.random.Random

object BenchmarkData {
    private val colors = listOf("red", "green", "blue", "gold", "yellow", "aqua", "white")
    private val words = listOf(
        "runway", "server", "player", "message", "dialog", "inventory", "title", "reward",
        "season", "lobby", "quest", "rank", "packet", "component", "placeholder", "format"
    )

    fun replacements(count: Int): LinkedHashMap<String, String> {
        val replacements = linkedMapOf("<player>" to "Afonso")
        for (index in 1 until count) {
            replacements["<sus$index>"] = "Sussy$index"
        }
        return replacements
    }

    fun input(size: String, scenario: String, replacementCount: Int = 16, prefix: String = ""): String {
        val targetLength = when (size) {
            "SHORT" -> 80
            "MEDIUM" -> 320
            "LONG" -> 1_280
            else -> 320
        }
        val replacements = replacements(replacementCount)
        val keys = replacements.keys.toList()
        val random = Random("${size}_$scenario".hashCode())
        val builder = StringBuilder(prefix)

        while (builder.length < targetLength) {
            when (scenario) {
                "PLAIN" -> builder.append(word(random))
                "STYLE_ONLY" -> builder.append("<").append(colors.random(random)).append(">")
                    .append(word(random)).append("</").append(colors.random(random)).append(">")
                "PLACEHOLDERS_4", "PLACEHOLDERS_16", "PLACEHOLDERS_32", "CUSTOM_PLACEHOLDERS" -> {
                    builder.append(keys.random(random)).append(" ").append(word(random))
                }
                "MIXED_HITS_AND_MISSES" -> {
                    if (random.nextBoolean()) builder.append(keys.random(random)) else builder.append("<missing_")
                        .append(random.nextInt(32)).append(">")
                    builder.append(" ").append(word(random))
                }
                "NESTED_PLACEHOLDERS" -> builder.append("<yellow>").append(keys.random(random)).append("</yellow> ")
                    .append("<uppercase>").append(word(random)).append("</uppercase>")
                "SANITIZED_TAIL" -> builder.append("<green>").append(keys.random(random)).append("<sanitized> ")
                    .append(keys.random(random)).append(" <red>").append(word(random)).append("</red>")
                else -> builder.append(word(random))
            }
            builder.append(' ')
        }

        return builder.toString().trim()
    }

    fun quietRunwayTextResolver(replacements: Map<String, String>): TagResolver {
        val builder = TagResolver.builder()
        replacements.forEach { (token, replacement) ->
            val name = token.removePrefix("<").removeSuffix(">").lowercase()
            builder.resolver(TagResolver.resolver(name) { _, _ -> Tag.preProcessParsed(replacement) })
        }
        return TagResolver.resolver(builder.build(), TagManager().resolver())
    }

    fun replacementConfigs(replacements: Map<String, String>): List<TextReplacementConfig> {
        return replacements.map { (token, replacement) ->
            TextReplacementConfig.builder()
                .matchLiteral(token)
                .replacement(replacement)
                .build()
        }
    }

    fun replaceChain(input: String, replacements: Map<String, String>): String {
        var output = input
        replacements.forEach { (token, replacement) ->
            output = output.replace(token, replacement)
        }
        return output
    }

    fun replaceSinglePass(input: String, replacements: Map<String, String>): String {
        val output = StringBuilder(input.length)
        var index = 0

        while (index < input.length) {
            val start = input.indexOf('<', index)
            if (start == -1) {
                output.append(input, index, input.length)
                break
            }

            output.append(input, index, start)
            val end = input.indexOf('>', start + 1)
            if (end == -1) {
                output.append(input, start, input.length)
                break
            }

            val token = input.substring(start, end + 1)
            val replacement = replacements[token]
            if (replacement == null) output.append(token) else output.append(replacement)
            index = end + 1
        }

        return output.toString()
    }

    fun component(miniMessage: MiniMessage, input: String): Component = miniMessage.deserialize(input)

    private fun word(random: Random): String = words.random(random)
}
