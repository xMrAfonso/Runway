package me.mrafonso.runway

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
open class ComponentBenchmark {

    private lateinit var component: Component
    private lateinit var serialized: String
    private val mini = MiniMessage.miniMessage()

    @Setup(Level.Iteration)
    fun setup() {
        serialized = randomMiniMessageString(256)
        component = mini.deserialize(serialized)
    }

    fun randomMiniMessageString(
        length: Int = 100,
        random: Random = Random.Default
    ): String {
        val colorTags = listOf("red", "green", "blue", "gold", "yellow", "aqua", "white")
        val customTags = listOf("sus1", "sus2", "sus3", "sus4", "sus5", "sus6", "sus7", "sus8", "player")

        val sb = StringBuilder()
        val insertedCustomTags = mutableSetOf<String>()
        val words = List(length / 8) { randomWord(random) }

        for (word in words) {
            when (random.nextInt(10)) {
                0, 1 -> { // Insert a color tag before the word
                    sb.append('<').append(colorTags.random(random)).append('>')
                    sb.append(word)
                }
                2, 3 -> { // Insert a custom tag before the word
                    val tag = customTags.random(random)
                    sb.append('<').append(tag).append('>')
                    sb.append(word)
                    insertedCustomTags.add(tag)
                }
                else -> sb.append(word)
            }
            sb.append(' ')
        }

        while (insertedCustomTags.size < 7) {
            val tag = (customTags - insertedCustomTags).random(random)
            sb.append('<').append(tag).append('>')
            sb.append(randomWord(random)).append(' ')
            insertedCustomTags.add(tag)
        }

        return sb.toString().trim()
    }

    private fun randomWord(random: Random): String {
        val length = random.nextInt(3, 8)
        return (1..length)
            .joinToString("") { ('a'..'z').random(random).toString() }
    }

    /**
     * Serialize → modify → deserialize again
     */
    @Benchmark
    fun modifyBySerializationCycle(): Component {
        val modified = mini.serialize(component)
            .replace("<player>", "Afonso")
            .replace("<sus1>", "Sussy1")
            .replace("<sus2>", "Sussy2")
            .replace("<sus3>", "Sussy3")
            .replace("<sus4>", "Sussy4")
            .replace("<sus5>", "Sussy5")
            .replace("<sus6>", "Sussy6")
            .replace("<sus7>", "Sussy7")
            .replace("<sus8>", "Sussy8")
            .replace("<sus9>", "Sussy9")
            .replace("<sus10>", "Sussy10")
            .replace("<sus11>", "Sussy11")
            .replace("<sus12>", "Sussy12")
        return mini.deserialize(modified)
    }

    /**
     * Use built-in Adventure replaceText()
     */
    @Benchmark
    fun modifyUsingReplaceText(): Component {
        return component.replaceText(
            TextReplacementConfig.builder()
                .matchLiteral("<player>")
                .replacement("Afonso")
                .build()
        ).replaceText(
            TextReplacementConfig.builder()
                .matchLiteral("<sus1>")
                .replacement("Sussy1")
                .build()
        ).replaceText(
            TextReplacementConfig.builder()
                .matchLiteral("<sus2>")
                .replacement("Sussy2")
                .build()
        ).replaceText(
            TextReplacementConfig.builder()
                .matchLiteral("<sus3>")
                .replacement("Sussy3")
                .build()
        ).replaceText(
            TextReplacementConfig.builder()
                .matchLiteral("<sus4>")
                .replacement("Sussy4")
                .build()
        ).replaceText(
            TextReplacementConfig.builder()
                .matchLiteral("<sus5>")
                .replacement("Sussy5")
                .build()
        ).replaceText(
            TextReplacementConfig.builder()
                .matchLiteral("<sus6>")
                .replacement("Sussy6")
                .build()
        ).replaceText(
            TextReplacementConfig.builder()
                .matchLiteral("<sus7>")
                .replacement("Sussy7")
                .build()
        ).replaceText(
            TextReplacementConfig.builder()
                .matchLiteral("<sus8>")
                .replacement("Sussy8")
                .build()
        ).replaceText(
            TextReplacementConfig.builder()
                .matchLiteral("<sus9>")
                .replacement("Sussy9")
                .build()
        ).replaceText(
            TextReplacementConfig.builder()
                .matchLiteral("<sus10>")
                .replacement("Sussy10")
                .build()
        ).replaceText(
            TextReplacementConfig.builder()
                .matchLiteral("<sus11>")
                .replacement("Sussy11")
                .build()
        ).replaceText(
            TextReplacementConfig.builder()
                .matchLiteral("<sus12>")
                .replacement("Sussy12")
                .build()
        )
    }
}