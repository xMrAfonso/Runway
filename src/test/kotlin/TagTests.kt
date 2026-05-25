package me.mrafonso.runway.integration.tag

import ch.andre601.expressionparser.DefaultExpressionParserEngine
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import me.mrafonso.runway.config.placeholder.Group
import me.mrafonso.runway.config.placeholder.NumberPlaceholder
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

    fun customResolver(groups: Collection<Group>): TagResolver {
        lateinit var resolver: TagResolver
        val evaluator = PlaceholderEvaluator(
            miniMessage = miniMessage,
            expressionParser = DefaultExpressionParserEngine.createDefault(),
            placeholderProcessor = { text, _ -> miniMessage.deserialize(text, resolver) }
        )
        val builder = TagResolverBuilder(evaluator) { text: String, _: Pointered? ->
            miniMessage.deserialize(text, resolver)
        }

        resolver = TagResolver.resolver(builder.build(groups), TagManager().resolver())
        return resolver
    }

    fun renderWithSanitized(input: String, resolver: TagResolver): String {
        val splitText = input.split("<sanitized>", limit = 2)
        val before = miniMessage.deserialize(splitText[0], resolver)
        val after = splitText.getOrNull(1)?.let { miniMessage.deserialize(it) } ?: Component.empty()
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

    "legacy placeholders resolve as custom text placeholder tags" {
        val resolver = customResolver(
            listOf(
                Group(
                    legacyPlaceholders = mapOf(
                        "legacy_server" to TextPlaceholder("RunwayMC")
                    )
                )
            )
        )

        render("<legacy_server>", resolver) shouldBe "RunwayMC"
    }

    "legacy placeholder output preserves minimessage color gradients" {
        val resolver = customResolver(
            listOf(
                Group(
                    legacyPlaceholders = mapOf(
                        "server_selector" to TextPlaceholder("<gradient:#ffff00:#00ffff>SERVER SELECTOR</gradient>")
                    )
                )
            )
        )

        val serialized = miniMessage.serialize(miniMessage.deserialize("<server_selector>", resolver))

        serialized.contains("<gradient:#FFFF00:#00FFFF>SERVER SELECTOR") shouldBe true
    }

    "legacy placeholder gradients are not overridden by outer colors" {
        val resolver = customResolver(
            listOf(
                Group(
                    legacyPlaceholders = mapOf(
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
})
