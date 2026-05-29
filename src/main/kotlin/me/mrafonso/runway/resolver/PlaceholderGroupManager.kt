package me.mrafonso.runway.resolver

import dev.triumphteam.polaris.Config
import me.mrafonso.runway.Runway
import me.mrafonso.runway.config.placeholder.Group
import me.mrafonso.runway.util.load
import java.nio.file.Files
import java.nio.file.Path

class PlaceholderGroupManager(private val plugin: Runway) {

    private val groups: MutableMap<String, Config<Group>> = mutableMapOf()

    /**
     * Reloads all loaded placeholder configurations.
     */
    fun reloadAll() {
        groups.clear()
        loadConfigs()
    }

    /**
     * Saves all loaded placeholder configurations.
     */
    fun saveAll() {
        groups.forEach { (_, config) -> config.save() }
    }

    /**
     * Returns a collection of all loaded groups configurations.
     *
     * @return Collection of [Config] objects representing the loaded placeholder groups.
     */
    fun groups(): Collection<Config<Group>> = groups.values

    /**
     * Loads all placeholder configuration files from the placeholders directory.
     * If the directory does not exist, it creates it and loads the default configuration.
     */
    private fun loadConfigs() {
        val path = Path.of("${plugin.dataFolder}/placeholders")
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            println("Placeholders directory not found. Creating default configuration.")
            Files.createDirectory(path)
            groups["default.yml"] = loadGroup("placeholders/default.yml", true)
        } else {
            println("Loading placeholder configuration files from placeholders directory.")
            val allFiles = Files.walk(path).use { files ->
                files
                    .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".yml") }
                    .map { it.fileName.toString() }
                    .toList()
            }

            allFiles.forEach { fileName ->
                try {
                    groups[fileName] = loadGroup("placeholders/$fileName")
                } catch (e: Exception) {
                    plugin.logger.warning("Failed to load placeholders from $fileName: ${e.message}")
                }
            }
        }
    }

    /**
     * Loads a placeholder configuration file.
     *
     * @param fileName The name of the configuration file to load.
     * @param writeDefault Whether to write the default configuration if the file does not exist.
     * @return [Config] The loaded configuration.
     */
    fun loadGroup(fileName: String, writeDefault: Boolean = false): Config<Group> {
        return load(plugin, fileName, writeDefault) { Group.template() }
            ?: throw IllegalStateException("Failed to load placeholder configuration from $fileName")
    }
}
