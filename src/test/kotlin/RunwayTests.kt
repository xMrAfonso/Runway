package me.mrafonso.runway

import com.github.retrooper.packetevents.PacketEvents
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.migration.config.OldConfig
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import java.io.File

class RunwayTests : StringSpec({
    lateinit var server: ServerMock
    lateinit var plugin: Runway

    beforeEach {
        server = MockBukkit.mock()

        val sourceFile = File("src/test/resources/oldConfig.yml")
        val sourcePlaceholdersFile = File("src/main/resources/placeholders.yml")
        val targetFolder = server.pluginsFolder.resolve("Runway-2.0.0")
        targetFolder.mkdirs()
        sourceFile.copyTo(targetFolder.resolve("config.yml"), overwrite = true)
        sourcePlaceholdersFile.copyTo(targetFolder.resolve("placeholders.yml"), overwrite = true)

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

    "PacketEvents API is fully initialized after onEnable" {
        PacketEvents.getAPI().isLoaded shouldBe true
        PacketEvents.getAPI().isInitialized shouldBe true
    }

    "config files are created" {
        plugin.dataFolder.resolve("settings.yml").exists() shouldBe true
        plugin.dataFolder.resolve("lang.yml").exists() shouldBe true
    }

    "config.yml migration successful" {
        val newConfig = plugin.configHandler.get<Settings>()
        newConfig.prefix.required shouldBe false
        plugin.dataFolder.resolve("settings.yml").exists() shouldBe true
        plugin.dataFolder.resolve("config.yml").exists() shouldBe false
        plugin.dataFolder.resolve("old-config.yml").exists() shouldBe true
    }

    "placeholders.yml migration successful" {
        val migratedFile = plugin.dataFolder.resolve("placeholders").resolve("migrated.yml")
        migratedFile.exists() shouldBe true
        val migratedText = migratedFile.readText()
        migratedText.contains("server_name") shouldBe true
        migratedText.contains("RunwayMC") shouldBe true
        plugin.dataFolder.resolve("placeholders.yml").exists() shouldBe false
        plugin.dataFolder.resolve("placeholders").resolve("old-placeholders.yml").exists() shouldBe true
    }

    "plugin disables without errors" {
        server.pluginManager.disablePlugin(plugin)
        plugin.isEnabled shouldBe false
    }

    "testing mode is detected correctly" {
        System.getProperty("runway.testmode") shouldBe "true"
    }

    ""
})
