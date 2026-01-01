package me.mrafonso.runway.util

import dev.triumphteam.polaris.Config
import dev.triumphteam.polaris.loadConfig
import dev.triumphteam.polaris.yaml.Yaml
import me.mrafonso.runway.Runway
import org.bukkit.plugin.PluginManager
import java.nio.file.Files
import java.nio.file.Path

inline fun <reified T : Any> load(
    plugin: Runway,
    fileName: String,
    writeDefaults: Boolean = true,
    noinline default: (() -> T)? = null
): Config<T>? {
    val path = Path.of("${plugin.dataFolder}/$fileName")

    // If no default and file doesn't exist → behave like the second overload
    if (writeDefaults && default == null && !Files.exists(path)) return null

    println("Loading config from $fileName")
    return loadConfig<T> {
        file = path
        this.writeDefaults = writeDefaults

        defaultInstance { default?.invoke() ?: T::class.java.getDeclaredConstructor().newInstance() }

        format = Yaml {
            indentationSize = 2
            explicitNulls = false
            encodeDefaults = true
        }
    }
}

fun PluginManager.registerEvents(plugin: Runway, vararg listeners: Any) {
    listeners.forEach { this.registerEvents(it as org.bukkit.event.Listener, plugin) }
}
