package me.mrafonso.runway.config;

import de.leonhard.storage.Config;
import de.leonhard.storage.internal.settings.ReloadSettings;
import me.mrafonso.runway.Runway;

import java.io.InputStream;

public class ConfigManager {

    private final Runway plugin;
    private final Config config;
    private final Config lang;
    private final Config placeholders;

    public ConfigManager(Runway plugin) {
        this.plugin = plugin;

        this.config = init("config.yml");
        this.lang = init("lang.yml");
        this.placeholders = init("placeholders.yml");
    }

    public void reload() {
        config.forceReload();
        lang.forceReload();
        placeholders.forceReload();
    }

    public Config config() {
        return config;
    }

    public Config lang() {
        return lang;
    }

    public Config placeholders() {
        return placeholders;
    }
    
    private Config init(String name) {
        InputStream def = plugin.resourceAsStream(name);
        Config config = new Config(name, "plugins/Runway/", def);
        config.setReloadSettings(ReloadSettings.MANUALLY);
        return config;
    }
}
