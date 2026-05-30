package me.mrafonso.runway.benchmark

import com.github.retrooper.packetevents.PacketEvents
import me.mrafonso.runway.Runway
import me.mrafonso.runway.integration.HookHandler
import me.mrafonso.runway.processing.ProcessHandler
import me.mrafonso.runway.resolver.ResolverHandler
import net.kyori.adventure.text.Component
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@State(Scope.Benchmark)
open class RunwayProcessBenchmark {
    @Param("CHAT_SINGLE", "SYSTEM_COMPONENT", "ITEM_LORE_LINES", "DIALOG_TEXT_BATCH", "INVENTORY_TITLE_AND_LINES")
    lateinit var workload: String

    @Param("PREFIX_HIT", "PREFIX_MISS", "STYLE_ONLY", "CUSTOM_PLACEHOLDERS", "SANITIZED_TAIL", "MIXED_BATCH")
    lateinit var scenario: String

    private lateinit var server: ServerMock
    private lateinit var plugin: Runway
    private lateinit var resolverHandler: ResolverHandler
    private lateinit var processHandler: ProcessHandler
    private lateinit var input: String
    private lateinit var component: Component
    private lateinit var components: List<Component>

    @Setup
    fun setup() {
        System.setProperty("bstats.relocatecheck", "false")
        System.setProperty("runway.testmode", "true")

        server = MockBukkit.mock()
        plugin = MockBukkit.load(Runway::class.java)

        val hookHandler = HookHandler()
        hookHandler.init(plugin)
        resolverHandler = ResolverHandler(plugin, hookHandler, plugin.configHandler)
        resolverHandler.reloadAll()
        processHandler = ProcessHandler(plugin.configHandler, resolverHandler)

        input = inputForScenario()
        component = Component.text(input)
        components = componentBatch()
    }

    @TearDown
    fun tearDown() {
        resolverHandler.stop()
        if (PacketEvents.getAPI() != null) {
            PacketEvents.getAPI().terminate()
        }
        server.pluginManager.disablePlugin(plugin)
        MockBukkit.unmock()
    }

    @Benchmark
    fun processString(): Component? {
        return processHandler.processComponent(input, null, requirePrefix())
    }

    @Benchmark
    fun processComponent(): Component? {
        return processHandler.processComponent(component, null, requirePrefix())
    }

    @Benchmark
    fun processComponentList(): List<Component> {
        return processHandler.processComponents(components, null, requirePrefix())
    }

    private fun inputForScenario(): String {
        return when (scenario) {
            "PREFIX_HIT" -> "$<green>Runway <uppercase>benchmark</uppercase>"
            "PREFIX_MISS" -> "<green>Runway <uppercase>benchmark</uppercase>"
            "STYLE_ONLY" -> "$<gradient:#ffff00:#00ffff>Runway benchmark</gradient> <smallcaps>server</smallcaps>"
            "CUSTOM_PLACEHOLDERS" -> "$<example_text> <example_number> <example_conditional>"
            "SANITIZED_TAIL" -> "$<example_text><sanitized> <example_text> <red>plain red</red>"
            "MIXED_BATCH" -> "$<green>Runway</green> <example_text> <uppercase>benchmark</uppercase>"
            else -> "$<green>Runway benchmark"
        }
    }

    private fun componentBatch(): List<Component> {
        val lines = when (workload) {
            "CHAT_SINGLE" -> listOf(input)
            "SYSTEM_COMPONENT" -> listOf(input, "$<yellow>System notice")
            "ITEM_LORE_LINES" -> List(8) { index -> "$<gray>Lore line $index <example_text>" }
            "DIALOG_TEXT_BATCH" -> List(12) { index -> "$<aqua>Dialog label $index <uppercase>ok</uppercase>" }
            "INVENTORY_TITLE_AND_LINES" -> listOf("$<gold>Inventory") + List(6) { index -> "$<white>Slot $index <example_text>" }
            else -> listOf(input)
        }
        return lines.map { Component.text(it) }
    }

    private fun requirePrefix(): Boolean {
        return scenario == "PREFIX_HIT" || scenario == "PREFIX_MISS"
    }
}
