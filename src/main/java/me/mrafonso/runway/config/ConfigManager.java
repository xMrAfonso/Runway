package me.mrafonso.runway.config;

import java.nio.file.Path;

public class ConfigManager {

    private Config config;
    private final ConfigFactory factory;

    public ConfigManager(Path dataFolder) {
        this.factory = new ConfigFactory(dataFolder);
    }

    public void reload() {
        config = null;

        config();
    }

    public Config config() {
        if (config == null) {
            config = factory.config();
        }
        return config;
    }
}
