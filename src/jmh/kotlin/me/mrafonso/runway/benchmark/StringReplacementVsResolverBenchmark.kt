package me.mrafonso.runway.benchmark

import net.kyori.adventure.text.Component
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
open class StringReplacementVsResolverBenchmark {
    @Param("SHORT", "MEDIUM", "LONG")
    lateinit var size: String

    @Param("PLAIN", "STYLE_ONLY", "PLACEHOLDERS_4", "PLACEHOLDERS_16", "PLACEHOLDERS_32", "MIXED_HITS_AND_MISSES", "NESTED_PLACEHOLDERS")
    lateinit var scenario: String

    private val miniMessage = MiniMessage.miniMessage()
    private lateinit var input: String
    private lateinit var replacements: LinkedHashMap<String, String>
    private lateinit var regex: Regex
    private lateinit var resolver: TagResolver

    @Setup
    fun setup() {
        val replacementCount = when (scenario) {
            "PLACEHOLDERS_4" -> 4
            "PLACEHOLDERS_32" -> 32
            else -> 16
        }
        replacements = BenchmarkData.replacements(replacementCount)
        input = BenchmarkData.input(size, scenario, replacementCount)
        regex = Regex(replacements.keys.joinToString("|") { Regex.escape(it) })
        resolver = BenchmarkData.quietRunwayTextResolver(replacements)
    }

    @Benchmark
    fun runwayResolver(): Component {
        return miniMessage.deserialize(input, resolver)
    }

    @Benchmark
    fun stringReplaceChainThenDeserialize(): Component {
        return miniMessage.deserialize(BenchmarkData.replaceChain(input, replacements))
    }

    @Benchmark
    fun regexReplaceThenDeserialize(): Component {
        val replaced = regex.replace(input) { match ->
            replacements.getValue(match.value)
        }
        return miniMessage.deserialize(replaced)
    }

    @Benchmark
    fun singlePassReplaceThenDeserialize(): Component {
        return miniMessage.deserialize(BenchmarkData.replaceSinglePass(input, replacements))
    }
}
