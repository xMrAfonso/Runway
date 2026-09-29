package me.mrafonso.runway

import com.github.retrooper.packetevents.PacketEvents
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.papermc.paper.chat.ChatRenderer
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.integration.HookHandler
import me.mrafonso.runway.integration.RunwayPlaceholderExpansion
import me.mrafonso.runway.listener.ChatListener
import me.mrafonso.runway.processing.ProcessHandler
import me.mrafonso.runway.resolver.ResolverHandler
import me.mrafonso.runway.util.DebugLogger
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

class ProcessingTests : StringSpec({
    lateinit var server: ServerMock
    lateinit var plugin: Runway
    lateinit var resolverHandler: ResolverHandler
    lateinit var processHandler: ProcessHandler
    lateinit var player: Player

    val miniMessage = MiniMessage.miniMessage()
    val plainText = PlainTextComponentSerializer.plainText()

    fun settings(): Settings = plugin.configHandler.get<Settings>()

    beforeEach {
        server = MockBukkit.mock()
        System.setProperty("bstats.relocatecheck", "false")
        System.setProperty("runway.testmode", "true")

        val placeholders = server.pluginsFolder.resolve("Runway-2.0.0").resolve("placeholders")
        placeholders.mkdirs()
        placeholders.resolve("ranks.yml").writeText(
            """
            text-placeholders:
              rank: "<gradient:red:blue>[Admin]"
              selector: "<gradient:#ffff00:#00ffff>SERVER SELECTOR</gradient>"
            """.trimIndent()
        )

        plugin = MockBukkit.load(Runway::class.java)
        resolverHandler = ResolverHandler(plugin, HookHandler().also { it.init(plugin) }, plugin.configHandler)
        resolverHandler.reloadAll()
        processHandler = ProcessHandler(plugin.configHandler, resolverHandler)
        player = server.addPlayer("Steve")
    }

    afterEach {
        PacketEvents.getAPI()?.terminate()
        server.pluginManager.disablePlugin(plugin)
        MockBukkit.unmock()
    }

    fun renderChat(format: ChatRenderer, message: Component = Component.text("hello")): Component {
        val renderer = ChatListener(processHandler, plugin.configHandler).processRenderer(format, settings())
        return renderer.render(player, Component.text("Steve"), message, player)
    }

    // Chat formatting

    "chat format with an unclosed gradient placeholder keeps the message instead of the marker" {
        val rendered = renderChat({ _, name, message, _ ->
            Component.text("<rank> ").append(name).append(Component.text(": ")).append(message)
        })

        plainText.serialize(rendered) shouldBe "[Admin] Steve: hello"
    }

    "chat format with a closed gradient placeholder keeps the message" {
        val rendered = renderChat({ _, name, message, _ ->
            Component.text("<selector> ").append(name).append(Component.text(": ")).append(message)
        })

        plainText.serialize(rendered) shouldBe "SERVER SELECTOR Steve: hello"
    }

    "default translatable chat format keeps the message" {
        plainText.serialize(renderChat(ChatRenderer.defaultRenderer())) shouldBe "<Steve> hello"
    }

    "runway placeholders in the default chat format's display name are parsed" {
        val rendered = ChatListener(processHandler, plugin.configHandler)
            .processRenderer(ChatRenderer.defaultRenderer(), settings())
            .render(player, Component.text("<rank> Steve"), Component.text("hello"), player)

        plainText.serialize(rendered) shouldBe "<[Admin] Steve> hello"
    }

    "chat format never re-parses the player's message" {
        val rendered = renderChat(
            { _, _, message, _ -> Component.text("<rank> ").append(message) },
            Component.text("<selector>")
        )

        plainText.serialize(rendered) shouldBe "[Admin] <selector>"
    }

    "chat output is not forced to non-italic" {
        miniMessage.serialize(renderChat(ChatRenderer.defaultRenderer())) shouldNotContain "!italic"
    }

    // Prefix detection on styled components (team prefixes, tab names, plugin messages)

    "styled text starting with the prefix is processed" {
        val processed = processHandler.processComponent(
            Component.text("$<selector> Steve", NamedTextColor.RED), player, true
        )

        processed shouldNotBe null
        plainText.serialize(processed!!) shouldBe "SERVER SELECTOR Steve"
    }

    "prefix inside a styled child is processed, like a team prefix" {
        val prefix = Component.text().append(Component.text("$<rank>", NamedTextColor.GRAY)).build()

        plainText.serialize(processHandler.processComponent(prefix, player, true)!!) shouldBe "[Admin]"
    }

    "legacy colour codes before the prefix are skipped" {
        val processed = processHandler.processComponent(Component.text("§c$<selector>"), player, true)

        plainText.serialize(processed!!) shouldBe "SERVER SELECTOR"
    }

    "legacy colour codes are translated instead of failing the parse" {
        val processed = processHandler.processComponent(Component.text("$§6Gold §x§f§f§0§0§0§0Hex"), player, true)!!

        miniMessage.serialize(processed) shouldBe "<gold>Gold <#FF0000>Hex"
    }

    "text whose first visible part lacks the prefix is ignored when the prefix is required" {
        val component = Component.text("[", NamedTextColor.GRAY).append(Component.text("$<rank>"))

        processHandler.processComponent(component, player, true) shouldBe null
    }

    "a prefix preceded by ! opts out when the prefix is not required" {
        processHandler.processComponent(Component.text("!$<rank>", NamedTextColor.RED), player, false) shouldBe null
    }

    "text without the prefix is processed when the prefix is not required" {
        plainText.serialize(processHandler.processComponent(Component.text("<rank>"), player, false)!!) shouldBe "[Admin]"
    }

    // PlaceholderAPI output

    "papi minimessage output closes every tag" {
        val component = resolverHandler.resolveCustomPlaceholder("rank", player)!!
        val output = RunwayPlaceholderExpansion.format(component, Settings.PlaceholderOutput.MINIMESSAGE)

        output shouldBe "<gradient:red:blue>[Admin]</gradient>"
        plainText.serialize(miniMessage.deserialize("$output after")) shouldBe "[Admin] after"
        miniMessage.serialize(miniMessage.deserialize("$output after")) shouldContain "</gradient> after"
    }

    "papi legacy output uses hex section codes" {
        val component = resolverHandler.resolveCustomPlaceholder("selector", player)!!
        val output = RunwayPlaceholderExpansion.format(component, Settings.PlaceholderOutput.LEGACY)

        output.startsWith("§x§f§f§f§f§0§0S") shouldBe true
        output shouldNotContain "<"
    }

    "papi returns null for unknown placeholders" {
        resolverHandler.resolveCustomPlaceholder("missing", player) shouldBe null
    }

    // Debug logging

    "debug logger only logs while enabled" {
        val records = mutableListOf<LogRecord>()
        val logger = Logger.getAnonymousLogger().apply {
            useParentHandlers = false
            addHandler(object : Handler() {
                override fun publish(record: LogRecord) { records += record }
                override fun flush() = Unit
                override fun close() = Unit
            })
        }
        var enabled = false
        val debug = DebugLogger(logger) { enabled }

        debug.log { "hidden" }
        enabled = true
        debug.log { "shown" }

        records.map { it.message } shouldBe listOf("[Debug] shown")
    }

    "placeholder loading doesn't print unless debug is enabled" {
        settings().debug shouldBe false
        val output = java.io.ByteArrayOutputStream()
        val original = System.out
        System.setOut(java.io.PrintStream(output))
        try {
            resolverHandler.reloadAll()
            processHandler.processComponent(Component.text("$<rank>"), player, true)
        } finally {
            System.setOut(original)
        }

        output.toString() shouldBe ""
    }
})
