package me.mrafonso.runway.config

import dev.triumphteam.polaris.Config
import dev.triumphteam.polaris.loadConfig
import dev.triumphteam.polaris.yaml.Yaml
import me.mrafonso.runway.Runway
import me.mrafonso.runway.util.load
import java.nio.file.Files
import java.nio.file.Path

class ConfigHandler(val plugin: Runway, init: ConfigHandler.() -> Unit = {}) {

    val configs: MutableMap<Class<*>, Config<*>> = mutableMapOf()

    init {
        init()
    }

    inline fun <reified T : Any> register(fileName: String, noinline default: () -> T) {
        load<T>(plugin, fileName, true, { default() })?.let { configs[T::class.java] = it }
    }

    fun reloadAll() {
        configs.forEach { (_, config) -> config.reload() }
    }

    fun saveAll() {
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