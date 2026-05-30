package me.mrafonso.runway.benchmark

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
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
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 2, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@State(Scope.Benchmark)
open class ComponentReplacementBenchmark {
    @Param("SHORT", "MEDIUM", "LONG")
    lateinit var size: String

    @Param("4", "16", "32")
    var placeholderCount: Int = 16

    private val miniMessage = MiniMessage.miniMessage()
    private lateinit var unresolvedInput: String
    private lateinit var unresolvedComponent: Component
    private lateinit var replacements: LinkedHashMap<String, String>
    private lateinit var replacementConfigs: List<TextReplacementConfig>
    private lateinit var resolver: TagResolver

    @Setup
    fun setup() {
        replacements = BenchmarkData.replacements(placeholderCount)
        unresolvedInput = BenchmarkData.input(size, "CUSTOM_PLACEHOLDERS", placeholderCount)
        unresolvedComponent = Component.text(unresolvedInput)
        replacementConfigs = BenchmarkData.replacementConfigs(replacements)
        resolver = BenchmarkData.quietRunwayTextResolver(replacements)
    }

    @Benchmark
    fun serializeReplaceDeserialize(): Component {
        val serialized = miniMessage.serialize(unresolvedComponent).replace("\\<", "<")
        return miniMessage.deserialize(BenchmarkData.replaceChain(serialized, replacements))
    }

    @Benchmark
    fun adventureReplaceText(): Component {
        var output = unresolvedComponent
        replacementConfigs.forEach { config ->
            output = output.replaceText(config)
        }
        return output
    }

    @Benchmark
    fun runwayResolverFromSerialized(): Component {
        val serialized = miniMessage.serialize(unresolvedComponent).replace("\\<", "<")
        return miniMessage.deserialize(serialized, resolver)
    }
}
