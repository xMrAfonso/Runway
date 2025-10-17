package me.mrafonso.runway.handler

import dev.triumphteam.polaris.Config
import dev.triumphteam.polaris.loadConfig
import dev.triumphteam.polaris.yaml.Yaml
import me.mrafonso.runway.Runway
import me.mrafonso.runway.config.Settings
import me.mrafonso.runway.config.placeholder.Placeholders
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.walk
import kotlin.sequences.filter
import kotlin.sequences.toList

class ResolverHandler(val plugin: Runway) {

    val configs: MutableMap<String, Config<*>> = mutableMapOf()

    fun loadPlaceholders() {
        val path = Path.of("${plugin.dataFolder}/placeholders")
        if (!Files.exists(path) || !Files.isDirectory(path)) Files.createDirectory(path)
        else {
            val allFiles = Files.walk(path)
                .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".yml") }
                .map { it.fileName.toString() }
                .toList()

            allFiles.forEach { fileName ->
                load<Placeholders>("placeholders/$fileName")?.let { configs[fileName] = it }
            }
        }
    }

    inline fun <reified T : Any> load(fileName: String): Config<T>? {
        val path = Path.of("${plugin.dataFolder}/$fileName")
        return loadConfig<T> {
            file = path
            writeDefaults = false
            format = Yaml {
                indentationSize = 2
                explicitNulls = false
                encodeDefaults = true
            }
        }
    }

    fun reloadAll() {
        configs.forEach { (_, config) -> config.reload() }
    }

    fun saveAll() {
        configs.forEach { (_, config) -> config.save() }
    }
}
