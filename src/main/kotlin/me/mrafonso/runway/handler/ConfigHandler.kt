package me.mrafonso.runway.handler

import dev.triumphteam.polaris.Config
import dev.triumphteam.polaris.loadConfig
import dev.triumphteam.polaris.yaml.Yaml
import me.mrafonso.runway.Runway
import me.mrafonso.runway.config.Settings
import java.nio.file.Path

class ConfigHandler(val plugin: Runway, init: ConfigHandler.() -> Unit = {}) {

    val configs: MutableMap<Class<*>, Config<*>> = mutableMapOf()

    init {
        init()
    }

    inline fun <reified T : Any> register(fileName: String) {
        val config = loadConfig<T> {
            file = Path.of("${plugin.dataFolder}/$fileName")
            format = Yaml {
                indentationSize = 2
                explicitNulls = false
                encodeDefaults = true
            }
        }
        configs[T::class.java] = config
    }

    fun reload() {
        configs.forEach { (_, config) -> config.reload() }
    }

    fun save() {
        configs.forEach { (_, config) -> config.save() }
    }

    inline fun <reified T : Any> save() {
        configs[T::class.java]?.save()
            ?: throw IllegalStateException("Config for ${T::class.java} not found")
    }

    inline fun <reified T : Any> get(): T {
        return configs[T::class.java]?.get() as? T
            ?: throw IllegalStateException("Config for ${T::class.java} not found")
    }
}
