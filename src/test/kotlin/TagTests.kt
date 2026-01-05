package me.mrafonso.runway.integration.tag

import com.github.retrooper.packetevents.PacketEvents
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import me.clip.placeholderapi.PlaceholderAPI
import me.mrafonso.runway.Runway
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.mockbukkit.mockbukkit.entity.PlayerMock

class TagTests : StringSpec({
    lateinit var server: ServerMock
    lateinit var plugin: Runway
    val plainSerializer = PlainTextComponentSerializer.plainText()

    beforeEach {
        server = MockBukkit.mock()

        System.setProperty("bstats.relocatecheck", "false")
        System.setProperty("runway.testmode", "true")

        plugin = MockBukkit.load(Runway::class.java)
    }

    afterEach {
        if (PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().terminate()
        }
        server.pluginManager.disablePlugin(plugin)
        MockBukkit.unmock()
    }

    class CustomPlayerMock(name: String, server: ServerMock) : PlayerMock(server, name) {
        var lastActionBar: Component? = null

        override fun sendActionBar(message: Component) {
            lastActionBar = message
        }
    }

    "SmallCapsTag converts lowercase text to small caps" {
        val tag = SmallCapsTag()
        val miniMessage = MiniMessage.builder()
            .tags(TagResolver.builder().resolver(tag.retrieve()).build())
            .build()

        val result = miniMessage.deserialize("<smallcaps>hello</smallcaps>")
        val plainText = plainSerializer.serialize(result)

        plainText shouldBe "ʜᴇʟʟᴏ"
    }

    "SmallCapsTag preserves uppercase and special characters" {
        val tag = SmallCapsTag()
        val miniMessage = MiniMessage.builder()
            .tags(TagResolver.builder().resolver(tag.retrieve()).build())
            .build()

        val result = miniMessage.deserialize("<sc>Hello World!</sc>")
        val plainText = plainSerializer.serialize(result)

        plainText shouldBe "ʜᴇʟʟᴏ ᴡᴏʀʟᴅ!"
    }

    "ActionbarTag sends actionbar to player" {
        val player = CustomPlayerMock("TestPlayer", server)
        server.addPlayer(player)
        val tag = ActionbarTag()
        val miniMessage = MiniMessage.builder()
            .tags(TagResolver.builder().resolver(tag.retrieve()).build())
            .build()

        miniMessage.deserialize("<actionbar>Test Message</actionbar>", player)

        player.lastActionBar shouldBe Component.empty().append(Component.text("Test Message"))
    }
})
