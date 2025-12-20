package me.mrafonso.runway

import com.github.retrooper.packetevents.PacketEvents
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

class RunwayTests : StringSpec({
    lateinit var server: ServerMock
    lateinit var plugin: Runway

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

    "plugin loads successfully" {
        plugin shouldNotBe null
        plugin.isEnabled shouldBe true
    }

    "PacketEvents API is initialized" {
        PacketEvents.getAPI() shouldNotBe null
    }
})
