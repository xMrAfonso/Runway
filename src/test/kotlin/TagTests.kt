package me.mrafonso.runway.integration.tag

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import me.mrafonso.runway.config.placeholder.CaseData
import me.mrafonso.runway.config.placeholder.ConditionalPlaceholder
import me.mrafonso.runway.config.placeholder.Group
import me.mrafonso.runway.config.placeholder.NumberPlaceholder
import me.mrafonso.runway.config.placeholder.RandomPlaceholder
import me.mrafonso.runway.config.placeholder.SwitchPlaceholder
import me.mrafonso.runway.config.placeholder.TextPlaceholder
import me.mrafonso.runway.integration.TagManager
import me.mrafonso.runway.resolver.PlaceholderEvaluator
import me.mrafonso.runway.resolver.TagResolverBuilder
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

class TagTests : StringSpec({
    val miniMessage = MiniMessage.miniMessage()
    val plainText = PlainTextComponentSerializer.plainText()

    fun render(input: String, resolver: TagResolver = TagManager().resolver()): String {
        return plainText.serialize(miniMessage.deserialize(input, resolver))
    }

    fun serialize(input: String, resolver: TagResolver = TagManager().resolver()): String {
        return miniMessage.serialize(miniMessage.deserialize(input, resolver))
    }

    fun customResolver(groups: Collection<Group>): TagResolver {
        lateinit var resolver: TagResolver
        val evaluator = PlaceholderEvaluator(
            miniMessage = miniMessage,
            placeholderProcessor = { text, _ -> miniMessage.deserialize(text, resolver) }
        )
        val builder = TagResolverBuilder(evaluator) { text: String, _: Pointered? ->
            miniMessage.deserialize(text, resolver)
        }

        resolver = TagResolver.resolver(builder.build(groups), TagManager().resolver())
        return resolver
    }

    fun customSanitizedResolver(groups: Collection<Group>): TagResolver {
        lateinit var resolver: TagResolver
        val evaluator = PlaceholderEvaluator(
            miniMessage = miniMessage,
            placeholderProcessor = { text, _ -> miniMessage.deserialize(text, resolver) }
        )
        val builder = TagResolverBuilder(evaluator) { text: String, _: Pointered? ->
            miniMessage.deserialize(text, resolver)
        }

        resolver = builder.buildSanitized(groups)
        return resolver
    }

    fun renderWithSanitized(input: String, resolver: TagResolver, sanitizedResolver: TagResolver = TagResolver.empty()): String {
        val splitText = input.split("<sanitized>", limit = 2)
        val before = miniMessage.deserialize(splitText[0], resolver)
        val after = splitText.getOrNull(1)?.let { miniMessage.deserialize(it, sanitizedResolver) } ?: Component.empty()
        return plainText.serialize(before.append(after))
    }

    "smallcaps tag converts lowercase text" {
        render("<smallcaps>hello world</smallcaps>") shouldBe "\u029C\u1D07\u029F\u029F\u1D0F \u1D21\u1D0F\u0280\u029F\u1D05"
    }

    "smallcaps alias converts text" {
        render("<sc>runway</sc>") shouldBe "\u0280\u1D1C\u0274\u1D21\u1D00\u028F"
    }

    "smallcaps tag preserves unsupported characters" {
        render("<smallcaps>abc 123 !?</smallcaps>") shouldBe "\u1D00\u0299\u1D04 123 !?"
    }

    "smallcaps tag handles mixed case input" {
        render("<smallcaps>RunwayMC</smallcaps>") shouldBe "\u0280\u1D1C\u0274\u1D21\u1D00\u028F\u1D0D\u1D04"
    }

    "smallcaps tag supports nested minimessage formatting" {
        render("<smallcaps><red>Hello</red> <bold>World</bold></smallcaps>") shouldBe "\u029C\u1D07\u029F\u029F\u1D0F \u1D21\u1D0F\u0280\u029F\u1D05"
    }

    "uppercase tag converts text" {
        render("<uppercase>Hello RunwayMC 123</uppercase>") shouldBe "HELLO RUNWAYMC 123"
    }

    "uppercase alias converts text" {
        render("<upper>hello</upper>") shouldBe "HELLO"
    }

    "uppercase tag supports nested minimessage formatting" {
        render("<uppercase><red>Hello</red> <bold>World</bold></uppercase>") shouldBe "HELLO WORLD"
        serialize("<uppercase><red>Hello</red></uppercase>") shouldBe "<red>HELLO"
    }

    "lowercase tag converts text" {
        render("<lowercase>Hello RunwayMC 123</lowercase>") shouldBe "hello runwaymc 123"
    }

    "lowercase alias converts text" {
        render("<lower>HELLO</lower>") shouldBe "hello"
    }

    "lowercase tag supports nested minimessage formatting" {
        render("<lowercase><red>Hello</red> <bold>World</bold></lowercase>") shouldBe "hello world"
        serialize("<lowercase><red>Hello</red></lowercase>") shouldBe "<red>hello"
    }

    "plain tag preserves visible text" {
        render("<plain><red>Hello</red> <bold>World</bold></plain>") shouldBe "Hello World"
    }

    "plain tag strips minimessage styling" {
        serialize("<plain><red>Hello</red> <bold>World</bold></plain>") shouldBe "Hello World"
    }

    "papi tag output preserves minimessage color gradients" {
        val resolver = PAPITag { _, placeholder ->
            placeholder shouldBe "server_selector"
            "<gradient:#ffff00:#00ffff>SERVER SELECTOR</gradient>"
        }.retrieve()

        val serialized = miniMessage.serialize(miniMessage.deserialize("<papi:server_selector>", resolver))

        serialized.contains("<gradient:#FFFF00:#00FFFF>SERVER SELECTOR") shouldBe true
    }

    "papi tag output preserves legacy section colors" {
        val resolver = PAPITag { _, placeholder ->
            placeholder shouldBe "server_name"
            "\u00A7aRunway"
        }.retrieve()

        miniMessage.serialize(miniMessage.deserialize("<papi:server_name>", resolver)) shouldBe "<green>Runway"
    }

    "custom text placeholder tag resolves from a group" {
        val resolver = customResolver(
            listOf(
                Group(
                    placeholders = mapOf(
                        "server" to TextPlaceholder("RunwayMC")
                    )
                )
            )
        )

        render("Welcome to <server>", resolver) shouldBe "Welcome to RunwayMC"
    }

    "custom placeholder tags can use group prefixes" {
        val resolver = customResolver(
            listOf(
                Group(
                    prefix = "global",
                    placeholders = mapOf(
                        "server" to TextPlaceholder("RunwayMC")
                    )
                )
            )
        )

        render("<global_server>", resolver) shouldBe "RunwayMC"
    }

    "text placeholders resolve as custom text placeholder tags" {
        val resolver = customResolver(
            listOf(
                Group(
                    textPlaceholders = mapOf(
                        "text_server" to TextPlaceholder("RunwayMC")
                    )
                )
            )
        )

        render("<text_server>", resolver) shouldBe "RunwayMC"
    }

    "text placeholders can use group prefixes" {
        val resolver = customResolver(
            listOf(
                Group(
                    prefix = "global",
                    textPlaceholders = mapOf(
                        "server" to TextPlaceholder("RunwayMC")
                    )
                )
            )
        )

        render("<global_server>", resolver) shouldBe "RunwayMC"
    }

    "text placeholders respect group condition" {
        val resolver = customResolver(
            listOf(
                Group(
                    condition = "false",
                    textPlaceholders = mapOf(
                        "server" to TextPlaceholder("RunwayMC")
                    )
                )
            )
        )

        render("Before<server>After", resolver) shouldBe "BeforeAfter"
    }

    "text placeholder output preserves minimessage color gradients" {
        val resolver = customResolver(
            listOf(
                Group(
                    textPlaceholders = mapOf(
                        "server_selector" to TextPlaceholder("<gradient:#ffff00:#00ffff>SERVER SELECTOR</gradient>")
                    )
                )
            )
        )

        val serialized = miniMessage.serialize(miniMessage.deserialize("<server_selector>", resolver))

        serialized.contains("<gradient:#FFFF00:#00FFFF>SERVER SELECTOR") shouldBe true
    }

    "text placeholder gradients are not overridden by outer colors" {
        val resolver = customResolver(
            listOf(
                Group(
                    textPlaceholders = mapOf(
                        "server_selector" to TextPlaceholder("<gradient:#ffff00:#00ffff>SERVER SELECTOR</gradient>")
                    )
                )
            )
        )

        val serialized = miniMessage.serialize(miniMessage.deserialize("<yellow><server_selector>", resolver))

        serialized.contains("<gradient:#FFFF00:#00FFFF>SERVER SELECTOR") shouldBe true
        serialized.contains("<yellow>SERVER SELECTOR") shouldBe false
    }

    "custom text placeholder can expand to an open color tag" {
        val resolver = customResolver(
            listOf(
                Group(
                    placeholders = mapOf(
                        "alert_color" to TextPlaceholder("<red>")
                    )
                )
            )
        )

        val serialized = miniMessage.serialize(miniMessage.deserialize("<alert_color>Warning", resolver))

        serialized shouldBe "<red>Warning"
    }

    "custom text placeholder output preserves minimessage color gradients" {
        val resolver = customResolver(
            listOf(
                Group(
                    placeholders = mapOf(
                        "server_selector" to TextPlaceholder("<gradient:#ffff00:#00ffff>SERVER SELECTOR</gradient>")
                    )
                )
            )
        )

        val serialized = miniMessage.serialize(miniMessage.deserialize("<server_selector>", resolver))

        serialized.contains("<gradient:#FFFF00:#00FFFF>SERVER SELECTOR") shouldBe true
    }

    "custom number placeholder tag resolves as text" {
        val resolver = customResolver(
            listOf(
                Group(
                    placeholders = mapOf(
                        "online" to NumberPlaceholder(42.0)
                    )
                )
            )
        )

        render("Players: <online>", resolver) shouldBe "Players: 42.0"
    }

    "custom text placeholder output can contain builtin tags" {
        val resolver = customResolver(
            listOf(
                Group(
                    placeholders = mapOf(
                        "short_server" to TextPlaceholder("<smallcaps>RunwayMC</smallcaps>")
                    )
                )
            )
        )

        render("<short_server>", resolver) shouldBe "\u0280\u1D1C\u0274\u1D21\u1D00\u028F\u1D0D\u1D04"
    }

    "papi tag output can contain runway tags" {
        val resolver = TagResolver.resolver(
            PAPITag { _, text ->
                when (text) {
                    "%runway_nested%" -> "<smallcaps>RunwayMC</smallcaps>"
                    else -> text
                }
            }.retrieve(),
            SmallCapsTag().retrieve()
        )

        render("<papi:runway_nested>", resolver) shouldBe "\u0280\u1D1C\u0274\u1D21\u1D00\u028F\u1D0D\u1D04"
    }

    "papi tag parses nested papi placeholders before runway tags" {
        val resolver = TagResolver.resolver(
            PAPITag { _, text ->
                when (text) {
                    "%outer%" -> "%inner%"
                    "%inner%" -> "<smallcaps>RunwayMC</smallcaps>"
                    else -> text
                }
            }.retrieve(),
            SmallCapsTag().retrieve()
        )

        render("<papi:outer>", resolver) shouldBe "\u0280\u1D1C\u0274\u1D21\u1D00\u028F\u1D0D\u1D04"
    }

    "papi tag is not registered by default" {
        render("<papi:player_name>") shouldBe "<papi:player_name>"
    }

    "empty random placeholder resolves as empty text" {
        val resolver = customResolver(
            listOf(
                Group(
                    placeholders = mapOf(
                        "empty_random" to RandomPlaceholder()
                    )
                )
            )
        )

        render("Before<empty_random>After", resolver) shouldBe "BeforeAfter"
    }

    "conditional placeholder returns true output" {
        val resolver = customResolver(
            listOf(
                Group(
                    placeholders = mapOf(
                        "status" to ConditionalPlaceholder(
                            condition = "true",
                            ifTrue = "Enabled",
                            ifElse = "Disabled"
                        )
                    )
                )
            )
        )

        render("<status>", resolver) shouldBe "Enabled"
    }

    "conditional placeholder returns else output" {
        val resolver = customResolver(
            listOf(
                Group(
                    placeholders = mapOf(
                        "status" to ConditionalPlaceholder(
                            condition = "false",
                            ifTrue = "Enabled",
                            ifElse = "Disabled"
                        )
                    )
                )
            )
        )

        render("<status>", resolver) shouldBe "Disabled"
    }

    "conditional placeholder respects group condition" {
        val resolver = customResolver(
            listOf(
                Group(
                    condition = "false",
                    placeholders = mapOf(
                        "status" to ConditionalPlaceholder(
                            condition = "true",
                            ifTrue = "Enabled",
                            ifElse = "Disabled"
                        )
                    )
                )
            )
        )

        render("Before<status>After", resolver) shouldBe "BeforeAfter"
    }

    "conditional placeholder evaluates numeric expressions" {
        val resolver = customResolver(
            listOf(
                Group(
                    placeholders = mapOf(
                        "status" to ConditionalPlaceholder(
                            condition = "42 > 10",
                            ifTrue = "Large",
                            ifElse = "Small"
                        )
                    )
                )
            )
        )

        render("<status>", resolver) shouldBe "Large"
    }

    "switch placeholder picks first matching expression" {
        val resolver = customResolver(
            listOf(
                Group(
                    placeholders = mapOf(
                        "size" to SwitchPlaceholder(
                            input = "150",
                            case = listOf(
                                CaseData("> 100", "Large"),
                                CaseData("< 100", "Small")
                            ),
                            default = "Balanced"
                        )
                    )
                )
            )
        )

        render("<size>", resolver) shouldBe "Large"
    }

    "switch placeholder uses default when no expression matches" {
        val resolver = customResolver(
            listOf(
                Group(
                    placeholders = mapOf(
                        "size" to SwitchPlaceholder(
                            input = "100",
                            case = listOf(
                                CaseData("> 100", "Large"),
                                CaseData("< 100", "Small")
                            ),
                            default = "Balanced"
                        )
                    )
                )
            )
        )

        render("<size>", resolver) shouldBe "Balanced"
    }

    "conditional placeholder evaluates expanded placeholder expressions" {
        val resolver = customResolver(
            listOf(
                Group(
                    placeholders = mapOf(
                        "answer" to NumberPlaceholder(42.0),
                        "status" to ConditionalPlaceholder(
                            condition = "<answer> > 10",
                            ifTrue = "Large",
                            ifElse = "Small"
                        )
                    )
                )
            )
        )

        render("<status>", resolver) shouldBe "Large"
    }

    "sanitized text parses minimessage but not custom placeholder tags" {
        val resolver = customResolver(
            listOf(
                Group(
                    placeholders = mapOf(
                        "server" to TextPlaceholder("RunwayMC")
                    )
                )
            )
        )

        renderWithSanitized(
            "Before <server><sanitized> After <server> <red>red</red>",
            resolver
        ) shouldBe "Before RunwayMC After <server> red"
    }

    "sanitized text resolves typed placeholders that opt in" {
        val groups = listOf(
            Group(
                placeholders = mapOf(
                    "server" to TextPlaceholder("RunwayMC", sanitized = true),
                    "hidden" to TextPlaceholder("Hidden")
                )
            )
        )

        renderWithSanitized(
            "Before <hidden><sanitized> After <server> <hidden>",
            customResolver(groups),
            customSanitizedResolver(groups)
        ) shouldBe "Before Hidden After RunwayMC <hidden>"
    }
})
